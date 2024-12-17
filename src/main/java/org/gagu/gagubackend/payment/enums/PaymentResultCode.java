package org.gagu.gagubackend.payment.enums;

import lombok.Getter;
import org.springframework.http.ResponseEntity;

@Getter
public enum PaymentResultCode {
    OK(200, "거래 내역이 정상 확인됐습니다."),
    NOT_FOUND(400,"거래 내역을 확인 할 수 없습니다.");

    private final int code;
    private final String msg;
    PaymentResultCode(int code, String msg){
        this.code = code;
        this.msg = msg;
    }
    public ResponseEntity<String> toResponseEntity(){
        return ResponseEntity.status(this.code).body(this.msg);
    }

}
