package com.web.back.utils;

import com.web.back.dto.EmailDto;
import com.web.back.dto.MemberDto;
import org.springframework.stereotype.Component;
import org.apache.commons.lang3.StringUtils;

@Component
public class AccountValidation {
    private final String emailRegex = "^[0-9a-zA-Z]([-_.]?[0-9a-zA-Z])*@kumoh\\.ac\\.kr$";//허용문자
    private final String passwordRegex = "^.*(?=^.{8,30}$)(?=.*\\d)(?=.*[a-zA-Z])(?=.*[!@#$%^&+=]).*$"; //특수문자 / 문자 / 숫자 포함 형태의 8~30자리 이내의 암호 정규식
    private final String nameRegex = "^[a-zA-Z가-힣|\\s]{2,15}$";

    public void validateSignUp(MemberDto memberDto) throws Exception {
        final String password = memberDto.getPassword();
        final String passwordCheck = memberDto.getPasswordCheck();
        final String email = memberDto.getEmail();
        final String number = memberDto.getNumber();
        final String name = memberDto.getName();
        if(StringUtils.isBlank(password)||
        StringUtils.isBlank(passwordCheck)||
        StringUtils.isBlank(email) || 
        StringUtils.isBlank(name) ||
        StringUtils.isBlank(number)){
            throw new Exception("빈칸 없이 입력해주세요.");
        }
        if(!email.matches(emailRegex)){
            throw new Exception("사용할 수 없는 이메일입니다.");
        }
        if(!password.matches(passwordRegex)){
            throw new Exception("사용할 수 없는 비밀번호입니다.");
        }
        // 비밀번호 비밀번호 확인 일치 체크
        if (!password.equals(passwordCheck)) {
            throw new Exception("비밀번호가 일치하지 않습니다.");
        }
    }

    public void validateChangePassword(MemberDto memberDto) throws Exception {
        final String password = memberDto.getPassword();
        final String newPassword = memberDto.getNewPassword();
        final String newPasswordCheck = memberDto.getNewPasswordCheck();
        if(StringUtils.isBlank(password)||
        StringUtils.isBlank(newPassword)||
        StringUtils.isBlank(newPasswordCheck)){
            throw new Exception("빈칸 없이 입력해주세요.");
        }
        if(!newPassword.matches(passwordRegex)){
            throw new Exception("사용할 수 없는 비밀번호입니다.");
        }
        if(!newPassword.equals(newPasswordCheck)){
            throw new Exception("새 비밀번호가 일치하지 않습니다.");
        }
    }

    public void validateFindPassword(MemberDto memberDto) throws Exception {
        final String newPassword = memberDto.getNewPassword();
        final String newPasswordCheck = memberDto.getNewPasswordCheck();
        if(StringUtils.isBlank(newPassword)||
                StringUtils.isBlank(newPasswordCheck)){
            throw new Exception("빈칸 없이 입력해주세요.");
        }
        if(!newPassword.matches(passwordRegex)){
            throw new Exception("사용할 수 없는 비밀번호입니다.");
        }
        if(!newPassword.equals(newPasswordCheck)){
            throw new Exception("새 비밀번호가 일치하지 않습니다.");
        }
    }

    public void validateEmail(EmailDto emailDto) throws Exception {
        final String email = emailDto.getEmail();
        if (StringUtils.isBlank(email)){
            throw new Exception("빈칸 없이 입력해주세요.");
        }
        if(!email.matches(emailRegex)){
            throw new Exception("사용할 수 없는 이메일입니다.");
        }
    }
}

