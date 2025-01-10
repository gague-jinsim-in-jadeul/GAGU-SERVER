package org.gagu.gagubackend.auth.dto.response;

import lombok.*;
import org.gagu.gagubackend.auth.domain.User;

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

    public ResponseWorkShopDetailsDto(User user){
        this.workshopName = user.getNickName();
        this.description = user.getProfileMessage();
        this.address = user.getAddress();
    }
}
