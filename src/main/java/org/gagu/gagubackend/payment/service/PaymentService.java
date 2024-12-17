package org.gagu.gagubackend.payment.service;

import org.springframework.http.ResponseEntity;

public interface PaymentService {
    /**
     * @Author HandMK
     * @param paymentId
     * @return API response
     */
    ResponseEntity<?> checkByPaymentId(String paymentId);
}
