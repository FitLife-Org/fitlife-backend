package com.fitlife.payment.service.impl;

import com.fitlife.common.exception.AppException;
import com.fitlife.common.exception.ErrorCode;
import com.fitlife.subscription.entity.Subscription;
import com.fitlife.subscription.enums.SubscriptionStatus;
import com.fitlife.subscription.repository.SubscriptionRepository;
import com.fitlife.invoice.entity.Invoice;
import com.fitlife.invoice.enums.InvoiceStatus;
import com.fitlife.invoice.repository.InvoiceRepository;
import com.fitlife.payment.config.VnpayProperties;
import com.fitlife.payment.dto.request.VnpayCreateUrlRequest;
import com.fitlife.payment.dto.response.VnpayCreateUrlResponse;
import com.fitlife.payment.entity.Payment;
import com.fitlife.payment.enums.PaymentMethod;
import com.fitlife.payment.enums.PaymentStatus;
import com.fitlife.payment.repository.PaymentRepository;
import com.fitlife.payment.service.VnpayService;
import com.fitlife.payment.util.VnpayUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class VnpayServiceImpl implements VnpayService {

    private final VnpayProperties vnpayProperties;
    private final InvoiceRepository invoiceRepository;
    private final PaymentRepository paymentRepository;
    private final SubscriptionRepository subscriptionRepository;

    private static final DateTimeFormatter VNP_DATE_FORMAT =
            DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    @Override
    @Transactional
    public VnpayCreateUrlResponse createPaymentUrl(
            VnpayCreateUrlRequest request,
            String ipAddress
    ) {
        validateVnpayConfig();

        Invoice invoice = invoiceRepository.findById(request.getInvoiceId())
                .orElseThrow(() -> new AppException(ErrorCode.INVOICE_NOT_FOUND));

        if (invoice.getStatus() == InvoiceStatus.PAID) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }

        if (invoice.getStatus() == InvoiceStatus.CANCELLED) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }

        Payment payment = Payment.builder()
                .paymentCode(generatePaymentCode())
                .invoice(invoice)
                .member(invoice.getMember())
                .subscription(invoice.getSubscription())
                .amount(invoice.getFinalAmount())
                .paymentMethod(PaymentMethod.VNPAY)
                .paymentStatus(PaymentStatus.PENDING)
                .note("Thanh toán online qua VNPay")
                .build();

        payment = paymentRepository.save(payment);

        String txnRef = "FITLIFE_" + payment.getId() + "_" + System.currentTimeMillis();

        payment.setVnpTxnRef(txnRef);
        payment.setVnpOrderInfo("Thanh toan hoa don FitLife " + invoice.getInvoiceCode());

        payment = paymentRepository.save(payment);

        String paymentUrl = buildPaymentUrl(invoice, payment, txnRef, ipAddress);

        return VnpayCreateUrlResponse.builder()
                .paymentId(payment.getId())
                .paymentCode(payment.getPaymentCode())
                .paymentUrl(paymentUrl)
                .amount(payment.getAmount())
                .build();
    }

    private String buildPaymentUrl(
            Invoice invoice,
            Payment payment,
            String txnRef,
            String ipAddress
    ) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expireTime = now.plusMinutes(15);

        Map<String, String> vnpParams = new LinkedHashMap<>();

        vnpParams.put("vnp_Version", vnpayProperties.getVersion());
        vnpParams.put("vnp_Command", vnpayProperties.getCommand());
        vnpParams.put("vnp_TmnCode", vnpayProperties.getTmnCode());

        vnpParams.put(
                "vnp_Amount",
                invoice.getFinalAmount()
                        .multiply(BigDecimal.valueOf(100))
                        .toBigInteger()
                        .toString()
        );

        vnpParams.put("vnp_CurrCode", "VND");
        vnpParams.put("vnp_TxnRef", txnRef);
        vnpParams.put("vnp_OrderInfo", payment.getVnpOrderInfo());
        vnpParams.put("vnp_OrderType", vnpayProperties.getOrderType());
        vnpParams.put("vnp_Locale", vnpayProperties.getLocale());
        vnpParams.put("vnp_ReturnUrl", vnpayProperties.getReturnUrl());
        vnpParams.put("vnp_IpAddr", ipAddress);
        vnpParams.put("vnp_CreateDate", now.format(VNP_DATE_FORMAT));
        vnpParams.put("vnp_ExpireDate", expireTime.format(VNP_DATE_FORMAT));

        String hashData = VnpayUtils.buildHashData(vnpParams);

        String secureHash = VnpayUtils.hmacSHA512(
                vnpayProperties.getHashSecret(),
                hashData
        );

        return vnpayProperties.getPayUrl()
                + "?"
                + VnpayUtils.buildQuery(vnpParams)
                + "&vnp_SecureHash="
                + URLEncoder.encode(secureHash, StandardCharsets.US_ASCII);
    }

    private void validateVnpayConfig() {
        if (isBlank(vnpayProperties.getPayUrl())
                || isBlank(vnpayProperties.getReturnUrl())
                || isBlank(vnpayProperties.getTmnCode())
                || isBlank(vnpayProperties.getHashSecret())
                || isBlank(vnpayProperties.getVersion())
                || isBlank(vnpayProperties.getCommand())
                || isBlank(vnpayProperties.getOrderType())
                || isBlank(vnpayProperties.getLocale())) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }
    }

    private String generatePaymentCode() {
        return "PAY-VNPAY-"
                + System.currentTimeMillis()
                + "-"
                + ThreadLocalRandom.current().nextInt(1000, 9999);
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    @Override
    @Transactional
    public String handleReturn(Map<String, String> params) {
        boolean validHash = verifySecureHash(params);

        String txnRef = params.get("vnp_TxnRef");
        String responseCode = params.get("vnp_ResponseCode");
        String transactionStatus = params.get("vnp_TransactionStatus");

        if (!validHash) {
            return buildFrontendRedirect("FAILED", "INVALID_SIGNATURE", null);
        }

        Payment payment = paymentRepository.findByVnpTxnRef(txnRef)
                .orElse(null);

        if (payment == null) {
            return buildFrontendRedirect("FAILED", "PAYMENT_NOT_FOUND", null);
        }

        if ("00".equals(responseCode) && "00".equals(transactionStatus)) {
            markPaymentSuccess(payment, params);
            return buildFrontendRedirect("SUCCESS", "PAYMENT_SUCCESS", payment.getId());
        }

        markPaymentFailed(payment, params);
        return buildFrontendRedirect("FAILED", responseCode, payment.getId());
    }

    private boolean verifySecureHash(Map<String, String> params) {
        String receivedHash = params.get("vnp_SecureHash");

        if (receivedHash == null || receivedHash.isBlank()) {
            return false;
        }

        Map<String, String> filteredParams = new LinkedHashMap<>(params);
        filteredParams.remove("vnp_SecureHash");
        filteredParams.remove("vnp_SecureHashType");

        String hashData = VnpayUtils.buildHashData(filteredParams);

        String calculatedHash = VnpayUtils.hmacSHA512(
                vnpayProperties.getHashSecret(),
                hashData
        );

        return calculatedHash.equalsIgnoreCase(receivedHash);
    }

    private void markPaymentSuccess(Payment payment, Map<String, String> params) {
        if (payment.getPaymentStatus() == PaymentStatus.SUCCESS) {
            return;
        }

        payment.setPaymentStatus(PaymentStatus.SUCCESS);
        payment.setTransactionNo(params.get("vnp_TransactionNo"));
        payment.setVnpTransactionNo(params.get("vnp_TransactionNo"));
        payment.setVnpBankCode(params.get("vnp_BankCode"));
        payment.setVnpCardType(params.get("vnp_CardType"));
        payment.setVnpResponseCode(params.get("vnp_ResponseCode"));
        payment.setVnpTransactionStatus(params.get("vnp_TransactionStatus"));
        payment.setVnpPayDate(params.get("vnp_PayDate"));
        payment.setGatewayMessage("VNPay payment success");
        payment.setPaidAt(LocalDateTime.now());

        Invoice invoice = payment.getInvoice();
        invoice.setStatus(InvoiceStatus.PAID);
        invoice.setPaidAt(LocalDateTime.now());

        Subscription subscription = invoice.getSubscription();

        if (subscription != null) {
            activateSubscription(subscription);
            subscriptionRepository.save(subscription);
        }

        paymentRepository.save(payment);
        invoiceRepository.save(invoice);
    }

    private void markPaymentFailed(Payment payment, Map<String, String> params) {
        if (payment.getPaymentStatus() == PaymentStatus.SUCCESS) {
            return;
        }

        payment.setPaymentStatus(PaymentStatus.FAILED);
        payment.setFailedReason("VNPay failed with code: " + params.get("vnp_ResponseCode"));
        payment.setVnpResponseCode(params.get("vnp_ResponseCode"));
        payment.setVnpTransactionStatus(params.get("vnp_TransactionStatus"));
        payment.setGatewayMessage("VNPay payment failed");

        paymentRepository.save(payment);
    }

    private void activateSubscription(Subscription subscription) {
        if (subscription.getStatus() == SubscriptionStatus.ACTIVE) {
            return;
        }

        LocalDate startDate = LocalDate.now();

        subscription.setStatus(SubscriptionStatus.ACTIVE);
        subscription.setStartDate(startDate);

        if (subscription.getPackageDuration() != null) {
            subscription.setEndDate(
                    startDate.plusMonths(subscription.getPackageDuration().getMonths())
            );
        }
    }

    private String buildFrontendRedirect(String status, String code, Long paymentId) {
        StringBuilder redirectUrl = new StringBuilder(vnpayProperties.getFrontendResultUrl());

        redirectUrl.append("?status=").append(status);
        redirectUrl.append("&code=").append(code);

        if (paymentId != null) {
            redirectUrl.append("&paymentId=").append(paymentId);
        }

        return redirectUrl.toString();
    }

    @Override
    @Transactional
    public Map<String, String> handleIpn(Map<String, String> params) {
        boolean validHash = verifySecureHash(params);

        if (!validHash) {
            return Map.of(
                    "RspCode", "97",
                    "Message", "Invalid signature"
            );
        }

        String txnRef = params.get("vnp_TxnRef");

        Payment payment = paymentRepository.findByVnpTxnRef(txnRef)
                .orElse(null);

        if (payment == null) {
            return Map.of(
                    "RspCode", "01",
                    "Message", "Order not found"
            );
        }

        String responseCode = params.get("vnp_ResponseCode");
        String transactionStatus = params.get("vnp_TransactionStatus");

        if ("00".equals(responseCode) && "00".equals(transactionStatus)) {
            markPaymentSuccess(payment, params);
        } else {
            markPaymentFailed(payment, params);
        }

        return Map.of(
                "RspCode", "00",
                "Message", "Confirm Success"
        );
    }
}