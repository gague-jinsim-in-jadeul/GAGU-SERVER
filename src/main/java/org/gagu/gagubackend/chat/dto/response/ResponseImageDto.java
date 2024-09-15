package org.gagu.gagubackend.chat.dto.response;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class ResponseImageDto {
    private String image;
    private LocalDateTime dateTime;
}
