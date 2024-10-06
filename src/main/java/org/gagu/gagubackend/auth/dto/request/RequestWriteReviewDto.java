package org.gagu.gagubackend.auth.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class RequestWriteReviewDto {
    private String workshopName;
    private String description;
    private String reviewPhoto1;
    private String reviewPhoto2;
    private String reviewPhoto3;

    @NotNull(message = "별점은 필수입니다.")
    private BigDecimal stars;
}
