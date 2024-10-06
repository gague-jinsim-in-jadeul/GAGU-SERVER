package org.gagu.gagubackend.chat.dto.response;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class ResponseReviewDto {
    private String writer;
    private String date;
    private String description;
    private BigDecimal stars;
    private String img1;
    private String img2;
    private String img3;
}
