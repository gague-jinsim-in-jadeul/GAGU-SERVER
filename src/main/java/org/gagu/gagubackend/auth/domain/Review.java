package org.gagu.gagubackend.auth.domain;

import jakarta.persistence.*;
import lombok.*;
import org.gagu.gagubackend.global.domain.BaseTimeEntity;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
@Entity
@Table
public class Review extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String writer; // 작성자 닉네임

    @Column(nullable = false)
    private String workshopName; // 공방 이름

    @Column(nullable = false, length = 900)
    private String description;

    @Column(nullable = true)
    private String reviewPhoto1; // 첫 번째 사진 URL

    @Column(nullable = true)
    private String reviewPhoto2; // 두 번째 사진 URL

    @Column(nullable = true)
    private String reviewPhoto3; // 세 번째 사진 URL

    @Column(nullable = false, precision = 2, scale = 1) // 최대 2자리 1.5, 12...
    private BigDecimal stars; // 별점
}
