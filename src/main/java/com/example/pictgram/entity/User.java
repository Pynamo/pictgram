package com.example.pictgram.entity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.Data;

/**
 * UserDetails: 認証処理で必要となる資格情報(ユーザー名とパスワード)とユーザーの状態を提供するためのインタフェース
 * @author user
 *
 */

@Entity
@Table(name = "users")
@Data
public class User extends AbstractEntity implements UserDetails, UserInf {
    private static final long serialVersionUID = 1L;

    public enum Authority {
        ROLE_USER, ROLE_ADMIN
    };

    public User() {
        super();
    }

    public User(String email, String name, String password, Authority authority) {
        this.username = email;
        this.name = name;
        this.password = password;
        this.authority = authority;
    }

    @Id
    @SequenceGenerator(name = "users_id_seq")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Authority authority;

    
    /**
     * ユーザーに与えられている権限リストを返却する。
     * このメソッドは認可処理で使用される。
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority(authority.toString()));
        return authorities;
    }

    /**
     * アカウントの有効期限の状態を判定する
     * true:有効期限内
     */
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    /**
     * アカウントのロック状態を判定する
     * true:ロックされていない状態
     */
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    /**
     * 資格情報の有効期限の状態を判定する
     * true:有効期限内
     */
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    /**
     * 有効なユーザーかを判定する
     * true:有効なユーザー
     */
    @Override
    public boolean isEnabled() {
        return true;
    }
}