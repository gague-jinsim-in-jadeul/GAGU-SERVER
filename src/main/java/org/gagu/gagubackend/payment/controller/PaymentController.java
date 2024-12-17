package org.gagu.gagubackend.payment.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.gagu.gagubackend.payment.service.PaymentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Payment", description = "결제 관련 API 입니다.")
@RestController
@RequestMapping("/payment")
@RequiredArgsConstructor
@Slf4j
public class PaymentController {
    private final PaymentService paymentService;
    @Operation(summary = "사용자 결제 후 결제 완료 처리", description = "PG 사에 결제 완료 여부를 확인합니다.")
    @PostMapping("/complete")
    public ResponseEntity<?> checkPayment(@RequestBody String paymentId){
        return paymentService.checkByPaymentId(paymentId);
    }

}
