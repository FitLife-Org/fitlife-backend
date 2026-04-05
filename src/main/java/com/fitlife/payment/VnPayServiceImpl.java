package com.fitlife.payment;

import com.fitlife.core.config.VNPayConfig;
import com.fitlife.subscription.Subscription;
import com.fitlife.subscription.SubscriptionRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service("vnPayService")
@RequiredArgsConstructor
public class VnPayServiceImpl implements PaymentService {

    private final VNPayConfig vnPayConfig;
    private final PaymentRepository paymentRepository;
    private final SubscriptionRepository subscriptionRepository;

    @Transactional
    public String createPaymentUrl(Long subscriptionId, HttpServletRequest request) {
        Subscription subscription = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy Subscription ID: " + subscriptionId));

        // Block it if the package has already been paid
        if ("ACTIVE".equals(subscription.getStatus())) {
            throw new RuntimeException("Gói tập này đã được thanh toán và đang hoạt động!");
        }

        // Create Payment record with PENDING status
        Payment payment = Payment.builder()
                .subscription(subscription)
                .amount(subscription.getGymPackage().getPrice())
                .paymentMethod("VNPAY")
                .status("PENDING")
                .build();
        payment = paymentRepository.save(payment);

        // Config VNPay parameter
        Map<String, String> vnp_Params = new HashMap<>();
        vnp_Params.put("vnp_Version", vnPayConfig.getVnp_Version());
        vnp_Params.put("vnp_Command", vnPayConfig.getVnp_Command());
        vnp_Params.put("vnp_TmnCode", vnPayConfig.getVnpTmnCode());
        vnp_Params.put("vnp_Amount", String.valueOf(payment.getAmount().multiply(new BigDecimal("100")).longValue()));
        vnp_Params.put("vnp_CurrCode", "VND");
        vnp_Params.put("vnp_TxnRef", String.valueOf(payment.getId()));
        vnp_Params.put("vnp_OrderInfo", "Thanh toan Sub ID: " + subscriptionId);
        vnp_Params.put("vnp_OrderType", vnPayConfig.getVnp_OrderType());
        vnp_Params.put("vnp_Locale", "vn");
        vnp_Params.put("vnp_ReturnUrl", vnPayConfig.getVnpReturnUrl());
        vnp_Params.put("vnp_IpAddr", request.getRemoteAddr());

        java.text.SimpleDateFormat formatter = new java.text.SimpleDateFormat("yyyyMMddHHmmss");
        vnp_Params.put("vnp_CreateDate", formatter.format(new Date()));

        List<String> fieldNames = new ArrayList<>(vnp_Params.keySet());
        Collections.sort(fieldNames);
        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();

        try {
            for (int i = 0; i < fieldNames.size(); i++) {
                String fieldName = fieldNames.get(i);
                String fieldValue = vnp_Params.get(fieldName);
                if (fieldValue != null && !fieldValue.isEmpty()) {
                    hashData.append(fieldName).append('=').append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
                    query.append(URLEncoder.encode(fieldName, StandardCharsets.US_ASCII.toString())).append('=').append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
                    if (i < fieldNames.size() - 1) {
                        query.append('&');
                        hashData.append('&');
                    }
                }
            }
            String vnp_SecureHash = vnPayConfig.hmacSHA512(vnPayConfig.getSecretKey(), hashData.toString());
            return vnPayConfig.getVnpPayUrl() + "?" + query.toString() + "&vnp_SecureHash=" + vnp_SecureHash;

        } catch (Exception e) {
            throw new RuntimeException("Lỗi xây dựng URL thanh toán: " + e.getMessage());
        }
    }

    @Transactional
    public String processPaymentReturn(HttpServletRequest request) {
        try {
            String vnp_ResponseCode = request.getParameter("vnp_ResponseCode");
            String txnRef = request.getParameter("vnp_TxnRef");

            Payment payment = paymentRepository.findById(Long.parseLong(txnRef))
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy giao dịch"));

            // Avoid VNPay call two
            if ("COMPLETED".equals(payment.getStatus())) {
                return "ALREADY_PAID";
            }

            payment.setVnpTransactionNo(request.getParameter("vnp_TransactionNo"));
            payment.setVnpResponseCode(vnp_ResponseCode);
            payment.setVnpOrderInfo(request.getParameter("vnp_OrderInfo"));

            if ("00".equals(vnp_ResponseCode)) {
                payment.setStatus("COMPLETED");
                payment.setPaymentDate(LocalDateTime.now());

                // Active Subscription after money into bag
                Subscription sub = payment.getSubscription();
                sub.setStatus("ACTIVE");
                sub.setStartDate(LocalDate.now());

                if (sub.getGymPackage() != null) {
                    int duration = sub.getGymPackage().getDurationMonths();
                    sub.setEndDate(LocalDate.now().plusMonths(duration));
                }

                subscriptionRepository.save(sub);
                paymentRepository.save(payment);
                return "SUCCESS";
            } else {
                payment.setStatus("FAILED");
                paymentRepository.save(payment);
                return "FAILED";
            }
        } catch (Exception e) {
            System.err.println("Lỗi xử lý kết quả VNPay: " + e.getMessage());
            return "ERROR";
        }
    }
}