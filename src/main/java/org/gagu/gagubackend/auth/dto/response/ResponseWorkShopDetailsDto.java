package org.gagu.gagubackend.auth.dto.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class ResponseWorkShopDetailsDto {
    private String workshopName;
    private String address;
    private String description;
}
