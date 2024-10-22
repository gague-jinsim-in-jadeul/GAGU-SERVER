package org.gagu.gagubackend.chat.domain;

import jakarta.persistence.*;
import lombok.*;
import org.gagu.gagubackend.auth.domain.User;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table
public class ChatContents {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String sendTime;

    @Column(nullable = false)
    private String message;

    @Column(nullable = false)
    private Long chatRoomId;

    @ManyToOne
    @JoinColumn
    private User sender;
}
