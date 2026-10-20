package com.web.back.service.impl;

import java.io.UnsupportedEncodingException;
import java.util.Optional;
import java.util.Random;

import com.web.back.dto.EmailDto;
import com.web.back.dto.MemberDto;
import com.web.back.model.Member;
import com.web.back.repository.MemberRepository;
import com.web.back.service.AccountService;
import com.web.back.utils.AccountValidation;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AccountServiceImpl implements AccountService {
    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AccountValidation accountValidation;

    @Value("${spring.mail.username}")
    private String senderEmail;

    @Autowired
    private JavaMailSender emailSender;

    private static String verificationCode;

    // 회원 가입
    @Override
    public MemberDto signUp(MemberDto memberDto) throws Exception {
        final String password = memberDto.getPassword();
        final String email = memberDto.getEmail();
//        final Long id = memberDto.getId();
        final String number = memberDto.getNumber();

        accountValidation.validateSignUp(memberDto);

        if (memberRepository.existsByEmail(email)) {
            throw new Exception("가입된 이메일이 존재합니다.");
        }
        if (memberRepository.existsByNumber(number)) {
            throw new Exception("가입된 학번이 존재합니다");
        }

        Member member = new Member();
        member.setEmail(email);
//        member.setId(id); // id = AI
        String encodedPassword = passwordEncoder.encode(password);
        // {bcrypt} 제거 후 저장
        // member.setPassword(encodedPassword.replaceFirst("^\\{bcrypt\\}", ""));
        member.setPassword(encodedPassword);
        member.setNumber(number);
        member.setName(memberDto.getName());

        // 회원가입 역할 구분
        if (memberDto.getIdentity().equals(0)) {
            member.setIdentity(0);
        }
        else if (memberDto.getIdentity().equals(2)) {
            member.setIdentity(2);
        }
        else {
            // 교수 or 관리자가 아니면 전부 일반 회원
            member.setIdentity(1);
        }
        memberRepository.save(member);
        return MemberDto.setFormat(member);
    }

    // 비밀번호 변경
    @Override
    public MemberDto changePassword(MemberDto memberDto) throws Exception {
        final String password = memberDto.getPassword();
        final String newPassword = memberDto.getNewPassword();
        accountValidation.validateChangePassword(memberDto);
        Member authenticatedMember = getAuthenticatedMember();
        // 비밀번호 확인
        if (!passwordEncoder.matches(password, authenticatedMember.getPassword())) {
            throw new Exception("비밀번호가 일치하지 않습니다.");
        }

        String encodedPassword = passwordEncoder.encode(newPassword);
        // {bcrypt} 제거 후 저장
        //authenticatedMember.setPassword(encodedPassword.replaceFirst("^\\{bcrypt\\}", ""));
        authenticatedMember.setPassword(encodedPassword);
        memberRepository.save(authenticatedMember);
        return MemberDto.setFormat(authenticatedMember);
    }

    // 비밀번호 찾기
    @Override
    public MemberDto findPassword(MemberDto memberDto) throws Exception {
        final String email = memberDto.getEmail();
        final String newPassword = memberDto.getNewPassword();
        accountValidation.validateFindPassword(memberDto);
        Member authenticatedMember = memberRepository.findByEmail(email).orElseThrow();
        String encodedPassword = passwordEncoder.encode(newPassword);
        authenticatedMember.setPassword(encodedPassword);
        memberRepository.save(authenticatedMember);
        return MemberDto.setFormat(authenticatedMember);
    }

    // 계정 삭제
    @Override
    public void deleteAccount() throws Exception {
        Member authenticatedMember = getAuthenticatedMember();
        memberRepository.delete(authenticatedMember);
        if (memberRepository.findById(authenticatedMember.getId()).isPresent()) {
            throw new Exception("계정을 삭제하는 중에 오류가 발생했습니다.");
        }
    }

    @Override
    public MemberDto getMyMemberInfo() throws Exception {
        Member authenticatedMember = getAuthenticatedMember();

        // 풀이한 문제 리스트, 작성 글, 댓글 정보, 강의실 정보 등등...
        return MemberDto.setFormat(authenticatedMember);
    }

    // 학번을 이용해 Member 정보 탐색
    @Override
    public UserDetails loadUserByUsername(String id) throws UsernameNotFoundException {
        Optional<Member> userOptional = memberRepository.findByNumber(id);
        if (!userOptional.isPresent()) {
            throw new UsernameNotFoundException(String.format("The user info or password is not correct: '%s'.", id));
        }
        return userOptional.get();
    }

    // 토큰에서 회원 정보 추출
    @Override
    public Member getAuthenticatedMember() throws Exception {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            throw new UsernameNotFoundException("회원 정보를 찾을 수 없습니다.");
        }
        Object principal = authentication.getPrincipal();
        if (principal == null) {
            throw new Exception("인증되지 않았습니다.");
        }
        Member authenticatedMember = (Member)principal;
        return authenticatedMember;
    }

    // 이메일 인증 번호 전송
    @Override
    public EmailDto sendEmailVerificationCode(EmailDto emailDto) throws Exception {
        accountValidation.validateEmail(emailDto);
        verificationCode = createEmailAuthenticationNumber();
        MimeMessage message = createEmailForm(emailDto.getEmail());
        emailSender.send(message);
        emailDto.setVerificationCode(verificationCode);
        return emailDto;
    }

    // 이메일 인증 번호 확인
    @Override
    public EmailDto validateEmailVerificationCode(EmailDto emailDto) throws Exception {
        if (verificationCode.equals(emailDto.getEnteredVerificationCode())) {
            emailDto.setValidate(true);
        }
        else {
            throw new Exception("인증 번호가 일치하지 않습니다.");
        }
        return emailDto;
    }

    // 이메일 인증 번호 생성
    public static String createEmailAuthenticationNumber() {
        StringBuffer key = new StringBuffer();
        Random rnd = new Random();

        for (int i = 0; i < 8; i++) { // 인증코드 8자리
            int index = rnd.nextInt(3);
            switch (index) {
                case 0:
                    key.append((char) ((int) (rnd.nextInt(26)) + 97));
                    break;
                case 1:
                    key.append((char) ((int) (rnd.nextInt(26)) + 65));
                    break;
                case 2:
                    key.append((rnd.nextInt(10)));
                    break;
            }
        }

        return key.toString();
    }

    // 인증 이메일 형식 지정
    public MimeMessage createEmailForm(String email) throws MessagingException, UnsupportedEncodingException {
        String toEmail = email;
        String title = "SES 회원가입 인증번호입니다.";

        MimeMessage message = emailSender.createMimeMessage();
        try {
            message.addRecipients(MimeMessage.RecipientType.TO, email);
            message.setSubject(title);
            message.setFrom(senderEmail);
            String body = "<div style=\"font-family: 'Apple SD Gothic Neo', 'sans-serif' !important; width: 500px; height: 500px; border-top: 4px solid #003458; margin: 100px auto; padding: 30px 0; box-sizing: border-box;\">\n" +
                    "<h1 style=\"margin: 0; padding: 0 5px; font-size: 28px; font-weight: 400;\">" +
                    "<span style=\"font-size: 15px; margin: 0 0 10px 3px;\">SES</span><br />" +
                    "이메일 <span style=\"color: #003458;\">인증 번호</span> 안내입니다." +
                    "</h1>" +
                    "<p style=\"font-size: 16px; line-height: 26px; margin-top: 50px; padding: 0 5px;\">" +
                    "안녕하세요.<br />" +
                    "요청하신 인증번호입니다.<br /><br />" +
                    "<b style=\"color: #003458; font-size: 22px;\">" + verificationCode + "</b><br /><br />" +
                    "화면으로 돌아가 인증을 완료해주세요.<br />" +
                    "감사합니다." +
                    "</p>" +
                    "<div style=\"border-top: 1px solid #DDD; padding: 5px;\">" +
                    "<p style=\"font-size: 13px; line-height: 21px; color: #555;\">" +
                    "만약 인증이 수차례 정상적으로 진행되지 않는다면 아래 이메일로 연락 바랍니다.<br />" +
                    "kit23ses@gmail.com" +
                    "</p>" +
                    "</div>" +
                    "</div>";
            message.setText(body, "UTF-8", "html");
        }  catch (MessagingException e) {
            e.printStackTrace();
        }
        return message;
    }

}
