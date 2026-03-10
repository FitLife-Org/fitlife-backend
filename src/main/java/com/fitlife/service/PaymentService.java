package com.fitlife.service;

import com.fitlife.config.VNPayConfig;
import com.fitlife.entity.Payment;
import com.fitlife.entity.Subscription;
import com.fitlife.repository.PaymentRepository;
import com.fitlife.repository.SubscriptionRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate; // Thêm import này
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final VNPayConfig vnPayConfig;
    private final PaymentRepository paymentRepository;
    private final SubscriptionRepository subscriptionRepository;

    @Transactional
    public String createPaymentUrl(Long subscriptionId, HttpServletRequest request) {
        System.out.println("===> Bắt đầu tạo URL cho Sub ID: " + subscriptionId);

        Subscription subscription = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy Subscription ID: " + subscriptionId));

        if (subscription.getGymPackage() == null) {
            throw new RuntimeException("Subscription này chưa được gắn Gói tập!");
        }

        // Tạo Payment tạm thời (PENDING)
        Payment payment = Payment.builder()
                .subscription(subscription)
                .amount(subscription.getGymPackage().getPrice())
                .paymentMethod("VNPAY")
                .status("PENDING")
                .build();
        payment = paymentRepository.save(payment);

        // Cấu hình tham số VNPay
        Map<String, String> vnp_Params = new HashMap<>();
        vnp_Params.put("vnp_Version", vnPayConfig.getVnp_Version());
        vnp_Params.put("vnp_Command", vnPayConfig.getVnp_Command());
        vnp_Params.put("vnp_TmnCode", vnPayConfig.getVnpTmnCode());
        vnp_Params.put("vnp_Amount", String.valueOf((long)(payment.getAmount() * 100L)));
        vnp_Params.put("vnp_CurrCode", "VND");
        vnp_Params.put("vnp_TxnRef", String.valueOf(payment.getId()));
        vnp_Params.put("vnp_OrderInfo", "Thanh toan Sub ID: " + subscriptionId);
        vnp_Params.put("vnp_OrderType", vnPayConfig.getVnp_OrderType());
        vnp_Params.put("vnp_Locale", "vn");
        vnp_Params.put("vnp_ReturnUrl", vnPayConfig.getVnpReturnUrl());
        vnp_Params.put("vnp_IpAddr", request.getRemoteAddr());

        java.text.SimpleDateFormat formatter = new java.text.SimpleDateFormat("yyyyMMddHHmmss");
        vnp_Params.put("vnp_CreateDate", formatter.format(new Date()));

        // Sắp xếp tham số
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

            payment.setVnpTransactionNo(request.getParameter("vnp_TransactionNo"));
            payment.setVnpResponseCode(vnp_ResponseCode);
            payment.setVnpOrderInfo(request.getParameter("vnp_OrderInfo"));

            if ("00".equals(vnp_ResponseCode)) {
                payment.setStatus("COMPLETED");
                payment.setPaymentDate(LocalDateTime.now());

                // Cập nhật Subscription
                Subscription sub = payment.getSubscription();
                sub.setStatus("ACTIVE");

                // FIX LỖI ĐỎ: Ép từ LocalDateTime về LocalDate
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