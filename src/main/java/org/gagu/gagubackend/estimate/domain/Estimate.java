package org.gagu.gagubackend.estimate.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class Estimate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String nickName; // 유저 닉네임
    @Column(nullable = false)
    private String furnitureName; // 가구 이름
    @Column(nullable = false)
    private String furniture2DUrl; // 2D URL
    @Column(nullable = false)
    private String furniture3DObj; // 3D OBJ URL
    @Column(nullable = false)
    private String furniture3DMtl; // 3D MTL URL
    @Column(nullable = false)
    private String furniture3DTexture1; // 3D Texture1 URL
    @Column(nullable = false)
    private String furniture3DTexture2; // 3D Texture2 URL
    @Column(nullable = false)
    private String createdTime; // 생성 시간
    @Column(nullable = true)
    private String makerName; // 공방 이름
    @Column(nullable = true)
    private String price;
    @Column(nullable = true)
    private String description;
}
