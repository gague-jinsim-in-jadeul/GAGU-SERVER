package org.gagu.gagubackend.chat.domain;

import jakarta.persistence.*;
import lombok.*;
import org.gagu.gagubackend.auth.domain.User;

import java.time.LocalDateTime;
import java.util.List;

@Getter
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

    @Column(nullable = false)
    private String MessageType;

    @Column(nullable = true)
    private Long estimateId;

    @ManyToOne
    @JoinColumn
    private User sender;

    public ChatContents(String sendTime, String message, Long chatRoomId, String messageType, User sender){
        this.sendTime = sendTime;
        this.message = message;
        this.chatRoomId = chatRoomId;
        this.MessageType = messageType;
        this.sender = sender;
    }
    public ChatContents(String sendTime, String message, Long chatRoomId, String messageType, Long estimateId, User sender){
        this.sendTime = sendTime;
        this.message = message;
        this.chatRoomId = chatRoomId;
        this.MessageType = messageType;
        this.estimateId = estimateId;
        this.sender = sender;
    }
}
