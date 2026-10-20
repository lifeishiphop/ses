package com.web.back.service;

import com.web.back.dto.EmailDto;
import com.web.back.dto.MemberDto;
import com.web.back.model.Member;

import org.springframework.security.core.userdetails.UserDetailsService;

public interface AccountService extends UserDetailsService{
    MemberDto signUp(MemberDto memberDto) throws Exception;

    MemberDto changePassword(MemberDto memberDto) throws Exception;

    MemberDto findPassword(MemberDto memberDto) throws Exception;

    void deleteAccount() throws Exception;

    Member getAuthenticatedMember() throws Exception;

    EmailDto sendEmailVerificationCode(EmailDto emailDto) throws Exception;

    EmailDto validateEmailVerificationCode(EmailDto emailDto) throws Exception;

    MemberDto getMyMemberInfo() throws Exception;
}