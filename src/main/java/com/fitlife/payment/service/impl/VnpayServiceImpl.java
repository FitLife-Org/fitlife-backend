package com.fitlife.payment.service.impl;

import com.fitlife.common.exception.AppException;
import com.fitlife.common.exception.ErrorCode;
import com.fitlife.invoice.entity.Invoice;
import com.fitlife.invoice.enums.InvoiceStatus;
import com.fitlife.invoice.repository.InvoiceRepository;
import com.fitlife.member.entity.Member;
import com.fitlife.member.service.CurrentMemberService;
import com.fitlife.payment.config.VnpayProperties;
import com.fitlife.payment.dto.request.VnpayCreateUrlRequest;
import com.fitlife.payment.dto.response.VnpayCreateUrlResponse;
import com.fitlife.payment.entity.Payment;
import com.fitlife.payment.enums.PaymentMethod;
import com.fitlife.payment.enums.PaymentStatus;
import com.fitlife.payment.repository.PaymentRepository;
import com.fitlife.payment.service.VnpayService;
import com.fitlife.payment.util.VnpayUtils;
import com.fitlife.subscription.entity.Subscription;
import com.fitlife.subscription.enums.SubscriptionStatus;
import com.fitlife.subscription.repository.SubscriptionRepository;
import com.fitlife.subscription.service.SubscriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
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
    private final SubscriptionService subscriptionService;
    private final CurrentMemberService currentMemberService;

    /**
     * VNPay sử dụng thời gian Việt Nam cho vnp_CreateDate / vnp_ExpireDate.
     *
     * Không dùng LocalDateTime.now() mặc định của server vì Render có thể chạy UTC.
     */
    private static final ZoneId VIETNAM_ZONE =
            ZoneId.of("Asia/Ho_Chi_Minh");

    private static final DateTimeFormatter VNP_DATE_FORMAT =
            DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private static final int VNPAY_PAYMENT_TIMEOUT_MINUTES = 15;

    @Override
    @Transactional
    public VnpayCreateUrlResponse createPaymentUrl(
            VnpayCreateUrlRequest request,
            String ipAddress
    ) {
        validateVnpayConfig();

        if (request == null) {
            throw new AppException(
                    ErrorCode.INVALID_REQUEST,
                    "Payment request is required"
            );
        }

        Invoice invoice;
        Subscription subscription = null;

        if (request.getSubscriptionId() != null) {

            subscription = subscriptionRepository
                    .findById(request.getSubscriptionId())
                    .orElseThrow(() ->
                            new AppException(
                                    ErrorCode.SUBSCRIPTION_NOT_FOUND,
                                    "Subscription not found"
                            )
                    );

            if (subscription.getStatus() == SubscriptionStatus.ACTIVE) {
                throw new AppException(
                        ErrorCode.INVALID_REQUEST,
                        "Subscription has already been paid"
                );
            }

            invoice = invoiceRepository
                    .findBySubscriptionId(subscription.getId())
                    .orElseThrow(() ->
                            new AppException(
                                    ErrorCode.INVOICE_NOT_FOUND,
                                    "Invoice not found"
                            )
                    );

        } else if (request.getInvoiceId() != null) {

            invoice = invoiceRepository
                    .findById(request.getInvoiceId())
                    .orElseThrow(() ->
                            new AppException(
                                    ErrorCode.INVOICE_NOT_FOUND,
                                    "Invoice not found"
                            )
                    );

            subscription = invoice.getSubscription();

        } else {
            throw new AppException(
                    ErrorCode.INVALID_REQUEST,
                    "Either subscriptionId or invoiceId must be provided"
            );
        }

        Member currentMember =
                currentMemberService.getCurrentMember();

        validateOwnership(
                invoice,
                subscription,
                currentMember
        );

        validateInvoiceForPayment(invoice);

        cancelPreviousPendingVnpayPayment(invoice);

        Payment payment = createPendingPayment(
                invoice,
                subscription
        );

        String txnRef =
                "FITLIFE_"
                        + payment.getId()
                        + "_"
                        + System.currentTimeMillis();

        payment.setVnpTxnRef(txnRef);

        payment.setVnpOrderInfo(
                "Thanh toan hoa don FitLife "
                        + invoice.getInvoiceCode()
        );

        payment = paymentRepository.save(payment);

        String paymentUrl = buildPaymentUrl(
                invoice,
                payment,
                txnRef,
                normalizeIpAddress(ipAddress),
                request.getBankCode()
        );

        return VnpayCreateUrlResponse.builder()
                .paymentId(payment.getId())
                .paymentCode(payment.getPaymentCode())
                .paymentUrl(paymentUrl)
                .amount(payment.getAmount())
                .build();
    }

    private void validateOwnership(
            Invoice invoice,
            Subscription subscription,
            Member currentMember
    ) {
        if (invoice.getMember() == null
                || !invoice.getMember()
                .getId()
                .equals(currentMember.getId())) {

            throw new AppException(
                    ErrorCode.INVOICE_NOT_OWNED_BY_MEMBER
            );
        }

        if (subscription != null
                && (
                subscription.getMember() == null
                        || !subscription.getMember()
                        .getId()
                        .equals(currentMember.getId())
        )) {

            throw new AppException(
                    ErrorCode.PAYMENT_NOT_OWNED_BY_MEMBER
            );
        }
    }

    private void validateInvoiceForPayment(
            Invoice invoice
    ) {
        if (invoice.getStatus() == InvoiceStatus.PAID) {
            throw new AppException(
                    ErrorCode.INVALID_REQUEST,
                    "Invoice is already paid"
            );
        }

        if (invoice.getStatus() == InvoiceStatus.CANCELLED) {
            throw new AppException(
                    ErrorCode.INVALID_REQUEST,
                    "Invoice is cancelled"
            );
        }

        if (invoice.getFinalAmount() == null
                || invoice.getFinalAmount()
                .compareTo(BigDecimal.ZERO) <= 0) {

            throw new AppException(
                    ErrorCode.INVALID_REQUEST,
                    "Invoice amount must be greater than zero"
            );
        }
    }

    private void cancelPreviousPendingVnpayPayment(
            Invoice invoice
    ) {
        paymentRepository
                .findFirstByInvoiceIdAndPaymentStatusOrderByCreatedAtDesc(
                        invoice.getId(),
                        PaymentStatus.PENDING
                )
                .filter(existing ->
                        existing.getPaymentMethod()
                                == PaymentMethod.VNPAY
                )
                .ifPresent(existing -> {

                    existing.setPaymentStatus(
                            PaymentStatus.CANCELLED
                    );

                    existing.setCancelledAt(
                            LocalDateTime.now(VIETNAM_ZONE)
                    );

                    existing.setFailedReason(
                            "Replaced by a new VNPay payment URL"
                    );

                    paymentRepository.save(existing);
                });
    }

    private Payment createPendingPayment(
            Invoice invoice,
            Subscription subscription
    ) {
        Payment payment = Payment.builder()
                .paymentCode(generatePaymentCode())
                .invoice(invoice)
                .member(invoice.getMember())
                .subscription(subscription)
                .amount(invoice.getFinalAmount())
                .paymentMethod(PaymentMethod.VNPAY)
                .paymentStatus(PaymentStatus.PENDING)
                .note("Thanh toán online qua VNPay")
                .build();

        return paymentRepository.save(payment);
    }

    private String buildPaymentUrl(
            Invoice invoice,
            Payment payment,
            String txnRef,
            String ipAddress,
            String bankCode
    ) {

        /*
         * QUAN TRỌNG:
         *
         * Render có thể chạy UTC.
         * VNPay cần thời gian theo Asia/Ho_Chi_Minh.
         */
        LocalDateTime now =
                LocalDateTime.now(VIETNAM_ZONE);

        LocalDateTime expireTime =
                now.plusMinutes(
                        VNPAY_PAYMENT_TIMEOUT_MINUTES
                );

        Map<String, String> vnpParams =
                new LinkedHashMap<>();

        vnpParams.put(
                "vnp_Version",
                vnpayProperties.getVersion()
        );

        vnpParams.put(
                "vnp_Command",
                vnpayProperties.getCommand()
        );

        vnpParams.put(
                "vnp_TmnCode",
                vnpayProperties.getTmnCode()
        );

        vnpParams.put(
                "vnp_Amount",
                invoice.getFinalAmount()
                        .multiply(
                                BigDecimal.valueOf(100)
                        )
                        .toBigInteger()
                        .toString()
        );

        vnpParams.put(
                "vnp_CurrCode",
                "VND"
        );

        if (bankCode != null
                && !bankCode.isBlank()) {

            vnpParams.put(
                    "vnp_BankCode",
                    bankCode.trim()
            );
        }

        vnpParams.put(
                "vnp_TxnRef",
                txnRef
        );

        vnpParams.put(
                "vnp_OrderInfo",
                payment.getVnpOrderInfo()
        );

        vnpParams.put(
                "vnp_OrderType",
                vnpayProperties.getOrderType()
        );

        vnpParams.put(
                "vnp_Locale",
                vnpayProperties.getLocale()
        );

        vnpParams.put(
                "vnp_ReturnUrl",
                vnpayProperties.getReturnUrl()
        );

        vnpParams.put(
                "vnp_IpAddr",
                ipAddress
        );

        vnpParams.put(
                "vnp_CreateDate",
                now.format(VNP_DATE_FORMAT)
        );

        vnpParams.put(
                "vnp_ExpireDate",
                expireTime.format(VNP_DATE_FORMAT)
        );

        String hashData =
                VnpayUtils.buildHashData(vnpParams);

        String secureHash =
                VnpayUtils.hmacSHA512(
                        vnpayProperties.getHashSecret(),
                        hashData
                );

        return vnpayProperties.getPayUrl()
                + "?"
                + VnpayUtils.buildQuery(vnpParams)
                + "&vnp_SecureHash="
                + URLEncoder.encode(
                secureHash,
                StandardCharsets.US_ASCII
        );
    }

    private void validateVnpayConfig() {

        if (isBlank(vnpayProperties.getPayUrl())) {
            throw invalidConfig("VNPAY_PAY_URL");
        }

        if (isBlank(vnpayProperties.getReturnUrl())) {
            throw invalidConfig("VNPAY_RETURN_URL");
        }

        if (isBlank(vnpayProperties.getTmnCode())) {
            throw invalidConfig("VNPAY_TMN_CODE");
        }

        if (isBlank(vnpayProperties.getHashSecret())) {
            throw invalidConfig("VNPAY_HASH_SECRET");
        }

        if (isBlank(vnpayProperties.getVersion())) {
            throw invalidConfig("VNPAY_VERSION");
        }

        if (isBlank(vnpayProperties.getCommand())) {
            throw invalidConfig("VNPAY_COMMAND");
        }

        if (isBlank(vnpayProperties.getOrderType())) {
            throw invalidConfig("VNPAY_ORDER_TYPE");
        }

        if (isBlank(vnpayProperties.getLocale())) {
            throw invalidConfig("VNPAY_LOCALE");
        }

        if (isBlank(
                vnpayProperties.getFrontendResultUrl()
        )) {
            throw invalidConfig(
                    "VNPAY_FRONTEND_RESULT_URL"
            );
        }
    }

    private AppException invalidConfig(
            String configName
    ) {
        return new AppException(
                ErrorCode.INVALID_REQUEST,
                configName + " is not configured"
        );
    }

    private String generatePaymentCode() {
        return "PAY-VNPAY-"
                + System.currentTimeMillis()
                + "-"
                + ThreadLocalRandom.current()
                .nextInt(1000, 9999);
    }

    private String normalizeIpAddress(
            String ipAddress
    ) {
        if (ipAddress == null
                || ipAddress.isBlank()) {

            return "127.0.0.1";
        }

        String normalized =
                ipAddress.trim();

        /*
         * X-Forwarded-For có thể:
         * clientIp, proxy1, proxy2
         *
         * VNPay chỉ cần IP đầu tiên.
         */
        if (normalized.contains(",")) {
            normalized =
                    normalized.split(",")[0].trim();
        }

        /*
         * IPv6 localhost.
         */
        if ("0:0:0:0:0:0:0:1".equals(normalized)
                || "::1".equals(normalized)) {

            return "127.0.0.1";
        }

        return normalized;
    }

    private boolean isBlank(
            String value
    ) {
        return value == null
                || value.isBlank();
    }

    @Override
    @Transactional
    public String handleReturn(
            Map<String, String> params
    ) {
        boolean validHash =
                verifySecureHash(params);

        String txnRef =
                params.get("vnp_TxnRef");

        String responseCode =
                params.get("vnp_ResponseCode");

        String transactionStatus =
                params.get("vnp_TransactionStatus");

        if (!validHash) {
            return buildFrontendRedirect(
                    "FAILED",
                    "INVALID_SIGNATURE",
                    null
            );
        }

        Payment payment =
                paymentRepository
                        .findByVnpTxnRef(txnRef)
                        .orElse(null);

        if (payment == null) {
            return buildFrontendRedirect(
                    "FAILED",
                    "PAYMENT_NOT_FOUND",
                    null
            );
        }

        if (!isValidReturnedAmount(
                payment,
                params.get("vnp_Amount")
        )) {

            markPaymentFailed(
                    payment,
                    params,
                    "VNPay returned an invalid amount"
            );

            return buildFrontendRedirect(
                    "FAILED",
                    "INVALID_AMOUNT",
                    payment.getId()
            );
        }

        if ("00".equals(responseCode)
                && "00".equals(transactionStatus)) {

            markPaymentSuccess(
                    payment,
                    params
            );

            return buildFrontendRedirect(
                    "SUCCESS",
                    "PAYMENT_SUCCESS",
                    payment.getId()
            );
        }

        markPaymentFailed(
                payment,
                params
        );

        return buildFrontendRedirect(
                "FAILED",
                responseCode,
                payment.getId()
        );
    }

    private boolean verifySecureHash(
            Map<String, String> params
    ) {
        String receivedHash =
                params.get("vnp_SecureHash");

        if (receivedHash == null
                || receivedHash.isBlank()) {
            return false;
        }

        Map<String, String> filteredParams =
                new LinkedHashMap<>(params);

        filteredParams.remove(
                "vnp_SecureHash"
        );

        filteredParams.remove(
                "vnp_SecureHashType"
        );

        String hashData =
                VnpayUtils.buildHashData(
                        filteredParams
                );

        String calculatedHash =
                VnpayUtils.hmacSHA512(
                        vnpayProperties.getHashSecret(),
                        hashData
                );

        return calculatedHash.equalsIgnoreCase(
                receivedHash
        );
    }

    private void markPaymentSuccess(
            Payment payment,
            Map<String, String> params
    ) {
        /*
         * Idempotent:
         * return URL và IPN có thể cùng gọi vào.
         */
        if (payment.getPaymentStatus()
                == PaymentStatus.SUCCESS) {
            return;
        }

        payment.setPaymentStatus(
                PaymentStatus.SUCCESS
        );

        payment.setTransactionNo(
                params.get("vnp_TransactionNo")
        );

        payment.setVnpTransactionNo(
                params.get("vnp_TransactionNo")
        );

        payment.setVnpBankCode(
                params.get("vnp_BankCode")
        );

        payment.setVnpCardType(
                params.get("vnp_CardType")
        );

        payment.setVnpResponseCode(
                params.get("vnp_ResponseCode")
        );

        payment.setVnpTransactionStatus(
                params.get(
                        "vnp_TransactionStatus"
                )
        );

        payment.setVnpPayDate(
                params.get("vnp_PayDate")
        );

        payment.setGatewayMessage(
                "VNPay payment success"
        );

        payment.setPaidAt(
                LocalDateTime.now(VIETNAM_ZONE)
        );

        Invoice invoice =
                payment.getInvoice();

        invoice.setStatus(
                InvoiceStatus.PAID
        );

        invoice.setPaidAt(
                LocalDateTime.now(VIETNAM_ZONE)
        );

        Subscription subscription =
                invoice.getSubscription();

        if (subscription != null) {

            subscriptionService
                    .activateSubscriptionAfterPayment(
                            subscription.getId()
                    );
        }

        paymentRepository.save(payment);
        invoiceRepository.save(invoice);
    }

    private void markPaymentFailed(
            Payment payment,
            Map<String, String> params
    ) {
        markPaymentFailed(
                payment,
                params,
                "VNPay failed with code: "
                        + params.get(
                        "vnp_ResponseCode"
                )
        );
    }

    private void markPaymentFailed(
            Payment payment,
            Map<String, String> params,
            String reason
    ) {
        if (payment.getPaymentStatus()
                == PaymentStatus.SUCCESS) {

            return;
        }

        payment.setPaymentStatus(
                PaymentStatus.FAILED
        );

        payment.setFailedReason(reason);

        payment.setVnpResponseCode(
                params.get("vnp_ResponseCode")
        );

        payment.setVnpTransactionStatus(
                params.get(
                        "vnp_TransactionStatus"
                )
        );

        payment.setGatewayMessage(reason);

        paymentRepository.save(payment);
    }

    private boolean isValidReturnedAmount(
            Payment payment,
            String vnpAmountValue
    ) {
        if (payment == null
                || payment.getAmount() == null
                || isBlank(vnpAmountValue)) {

            return false;
        }

        try {
            BigDecimal returnedAmount =
                    new BigDecimal(vnpAmountValue)
                            .divide(
                                    BigDecimal.valueOf(100)
                            );

            return payment
                    .getAmount()
                    .compareTo(returnedAmount)
                    == 0;

        } catch (NumberFormatException exception) {
            return false;
        }
    }

    private String buildFrontendRedirect(
            String status,
            String code,
            Long paymentId
    ) {
        StringBuilder redirectUrl =
                new StringBuilder(
                        vnpayProperties
                                .getFrontendResultUrl()
                );

        redirectUrl
                .append("?status=")
                .append(
                        encodeRedirectValue(status)
                );

        redirectUrl
                .append("&code=")
                .append(
                        encodeRedirectValue(code)
                );

        if (paymentId != null) {
            redirectUrl
                    .append("&paymentId=")
                    .append(paymentId);
        }

        return redirectUrl.toString();
    }

    private String encodeRedirectValue(
            String value
    ) {
        return URLEncoder.encode(
                value == null ? "" : value,
                StandardCharsets.UTF_8
        );
    }

    @Override
    @Transactional
    public Map<String, String> handleIpn(
            Map<String, String> params
    ) {
        boolean validHash =
                verifySecureHash(params);

        if (!validHash) {
            return Map.of(
                    "RspCode",
                    "97",
                    "Message",
                    "Invalid signature"
            );
        }

        String txnRef =
                params.get("vnp_TxnRef");

        Payment payment =
                paymentRepository
                        .findByVnpTxnRef(txnRef)
                        .orElse(null);

        if (payment == null) {
            return Map.of(
                    "RspCode",
                    "01",
                    "Message",
                    "Order not found"
            );
        }

        /*
         * VNPay có thể gửi IPN nhiều lần.
         * Nếu đã SUCCESS, trả confirm success để idempotent.
         */
        if (payment.getPaymentStatus()
                == PaymentStatus.SUCCESS) {

            return Map.of(
                    "RspCode",
                    "00",
                    "Message",
                    "Confirm Success"
            );
        }

        if (payment.getPaymentStatus()
                != PaymentStatus.PENDING) {

            return Map.of(
                    "RspCode",
                    "02",
                    "Message",
                    "Order already confirmed"
            );
        }

        String vnpAmountValue =
                params.get("vnp_Amount");

        if (!isValidReturnedAmount(
                payment,
                vnpAmountValue
        )) {

            return Map.of(
                    "RspCode",
                    "04",
                    "Message",
                    "Invalid Amount"
            );
        }

        String responseCode =
                params.get("vnp_ResponseCode");

        String transactionStatus =
                params.get(
                        "vnp_TransactionStatus"
                );

        if ("00".equals(responseCode)
                && "00".equals(transactionStatus)) {

            markPaymentSuccess(
                    payment,
                    params
            );

        } else {

            markPaymentFailed(
                    payment,
                    params
            );
        }

        return Map.of(
                "RspCode",
                "00",
                "Message",
                "Confirm Success"
        );
    }
}