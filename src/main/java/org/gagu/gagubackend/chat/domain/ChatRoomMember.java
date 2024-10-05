package org.gagu.gagubackend.chat.domain;

import jakarta.persistence.*;
import lombok.*;
import org.gagu.gagubackend.global.domain.BaseTimeEntity;
import org.gagu.gagubackend.auth.domain.User;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table
public class ChatRoomMember extends BaseTimeEntity {
    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne
    @JoinColumn
    private ChatRoom roomId;

    @ManyToOne
    @JoinColumn
    private User member;
}
