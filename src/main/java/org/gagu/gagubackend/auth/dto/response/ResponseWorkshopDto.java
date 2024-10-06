package org.gagu.gagubackend.auth.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.math.BigInteger;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class ResponseWorkshopDto {
    private String workshopName;
    private String description;
    private String address;
    private String thumbnail;
    private BigDecimal starAverage;
    private BigInteger count;
    private Long id;
}
