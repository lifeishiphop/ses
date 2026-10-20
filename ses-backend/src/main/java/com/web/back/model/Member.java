package com.web.back.model;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Transient;

import com.fasterxml.jackson.annotation.JsonIgnore;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Entity
@EntityListeners(AuditingEntityListener.class)
// Serializable 상속
public class Member implements UserDetails, Serializable {

    @Serial
    private static final long serialVersionUID = 6655342832430428387L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 8)
    private String number;

    @Column(nullable = false, unique = true, length = 20)
    private String email;

    @JsonIgnore
    @Column(nullable = false, columnDefinition = "TEXT")
    private String password;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String name;

    @JsonIgnore
    @Column(nullable = false, columnDefinition = "MEDIUMINT", length = 1)
    private Integer identity;

//    @JsonIgnore
//    @CreatedDate
//    @Column(nullable = false, updatable = false)
//    private LocalDateTime createdDate;
    

    // 권한 목록
    @Transient
    private Collection<? extends GrantedAuthority> authorities;

    // 권한 목록 return
    @JsonIgnore
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.emptyList();
    }

    // 아이디 return
    @JsonIgnore
    @Override
    public String getUsername() {
//        return this.id.toString();
        return number;
    }

    // 비밀번호 return
    @JsonIgnore
    @Override
    public String getPassword() {
        return this.password;
    }

    // 계정 만료 여부 고정
    @JsonIgnore
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    // 계정 잠김 여부 고정
    @JsonIgnore
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    // 비밀번호 만료 여부 고정
    @JsonIgnore
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    // 사용자 활성화 여부
    @JsonIgnore
    @Override
    public boolean isEnabled() {
        return true;
    }
}
