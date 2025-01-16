package org.gagu.gagubackend.auth.dto.response;

import lombok.*;
import org.gagu.gagubackend.auth.domain.StarReview;

import java.math.BigDecimal;
import java.math.BigInteger;

@Getter
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

    public ResponseWorkshopDto(StarReview starReview){
        this.workshopName = starReview.getWorkshopName();
        this.description = starReview.getWorkshop().getProfileMessage();
        this.address = starReview.getWorkshop().getAddress();
        this.thumbnail = starReview.getWorkshop().getProfileUrl();
        this.starAverage = starReview.getStarsAverage();
        this.count = starReview.getCount();
        this.id = starReview.getWorkshop().getId();
    }
}
