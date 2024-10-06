package org.gagu.gagubackend.auth.domain;

import jakarta.persistence.*;
import lombok.*;
import org.gagu.gagubackend.global.domain.BaseTimeEntity;

import java.math.BigDecimal;
import java.math.BigInteger;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
@Entity
@Table
public class StarReview extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String workshopName;

    @Column(nullable = false, precision = 2, scale = 1) // 최대 2자리 1.5, 12...
    private BigDecimal starsAverage;

    @Column(nullable = false)
    private BigDecimal sum;

    @Column(nullable = false)
    private BigInteger count;

    @OneToOne
    @JoinColumn
    private User workshop; // 공방 일대일 대응
}
