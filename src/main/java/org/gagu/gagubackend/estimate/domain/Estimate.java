package org.gagu.gagubackend.estimate.domain;

import jakarta.persistence.*;
import lombok.*;
import org.gagu.gagubackend.auth.domain.User;

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
    private String furnitureName; // 가구 이름

    @Column(nullable = false)
    private String furniture2DUrl; // 2D URL

    @Column(nullable = false)
    private String furniture3DUrl; // 3D OBJ URL

    @Column(nullable = false)
    private String createdTime; // 생성 시간

    @Column(nullable = true)
    private String price;

    @Column(nullable = true)
    private String makerName; // 공방 이름

    @Column(nullable = true)
    private String description;

    @ManyToOne
    @JoinColumn
    private User nickName; // 유저 닉네임
}
