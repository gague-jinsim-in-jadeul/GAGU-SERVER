package org.gagu.gagubackend.payment.service.Impl;

import lombok.extern.slf4j.Slf4j;
import org.gagu.gagubackend.global.domain.enums.ResultCode;
import org.gagu.gagubackend.payment.enums.PaymentResultCode;
import org.gagu.gagubackend.payment.service.PaymentService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@Slf4j
public class PaymentServiceImpl implements PaymentService {
    @Value("${portone.api.key}")
    private String PORTONE_API_KEY;
    @Value("${portone.api.url}")
    private String PORTONE_API_URL;
    @Override
    public ResponseEntity<?> checkByPaymentId(String paymentId) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        StringBuilder urlBuilder = new StringBuilder();

        headers.add("Authorization", "PortOne "+PORTONE_API_KEY);
        urlBuilder.append(PORTONE_API_URL).append(paymentId);
        log.info("[checkByPaymentId] api 호출 : {}", urlBuilder.toString());
        HttpEntity<String> entity = new HttpEntity<>(headers);
        try{
            ResponseEntity<String> response = restTemplate.exchange(
                    urlBuilder.toString(),
                    HttpMethod.GET,
                    entity,
                    String.class);

            if(response.getStatusCode().is4xxClientError()){
                log.error("[checkByPaymentId] 거래 내역 확인 불가");
                return PaymentResultCode.NOT_FOUND.toResponseEntity();
            }else if(response.getStatusCode().is2xxSuccessful()){
                return ResultCode.OK.toResponseEntity();
            }
        }catch (Exception e){
            log.error("[checkByPaymentId] error is occured during check payment");
            e.printStackTrace();
        }
        return PaymentResultCode.NOT_FOUND.toResponseEntity();
    }
}
