package org.gagu.gagubackend.user.domain;

import jakarta.persistence.*;
import lombok.*;
import org.gagu.gagubackend.chat.domain.ChatRoomMember;
import org.gagu.gagubackend.global.domain.BaseTimeEntity;
import org.hibernate.annotations.ColumnDefault;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.Serial;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table
public class User extends BaseTimeEntity implements UserDetails {
    @Serial
    private static final long serialVersionUID = 937195358763181265L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String nickName;

    @Column(nullable = false)
    private String password;

    @Column
    private String phoneNumber;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String profileUrl;

    @Column(nullable = false)
    private String loginType;

    @Column(nullable = false)
    @ColumnDefault(value = "true")
    private Boolean useAble = true;

    @Column(nullable = true)
    private LocalDateTime deletionDate;

    @Column(nullable = true)
    private String address;

    @Column(nullable = true)
    private String profileMessage;

    @Column(nullable = true) // 임시 true
    private String FCMToken;

    @ElementCollection(fetch = FetchType.EAGER)
    private List<String> roles = new ArrayList<>();

    @OneToMany(mappedBy = "member")
    private List<ChatRoomMember> chatRoom;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return this.roles.stream().map(role -> new SimpleGrantedAuthority(role)).collect(Collectors.toList());
    }

    @Override
    public String getUsername() {
        return this.nickName;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
