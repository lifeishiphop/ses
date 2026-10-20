package com.web.back.repository;

import java.util.Optional;

import com.web.back.model.Member;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {
    boolean existsByEmail(String email); // 이메일 중복 검사

    boolean existsByNumber(String number); // 학번 중복 검사

    Optional<Member> findByEmailAndPassword(String email, String password); // 로그인

    Optional<Member> findByNumber(String number);
    Optional<Member> findByEmail(String username);
}
