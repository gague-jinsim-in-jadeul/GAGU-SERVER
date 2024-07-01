package org.gagu.gagubackend.chat.domain;

import jakarta.persistence.*;
import lombok.*;
import org.gagu.gagubackend.global.domain.BaseTimeEntity;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table
public class ChatContents extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDateTime sendTime;

    @Column(nullable = false)
    private String sender;

    @Column(nullable = false)
    private String message;

    @ManyToOne
    @JoinColumn
    private ChatRoom chatRoom;
}
