package com.web.back.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description="EmailDto 요청·응답 데이터. 사용 필드는 API 설명을 확인하세요.")
public class EmailDto {
    @Schema(description="이메일 (회원가입은 kumoh.ac.kr 도메인)", example="s1@kumoh.ac.kr")
    String email;
    @Schema(description="서버가 발급한 인증번호 (현재 구현에서 응답에 포함)", example="aB12cD34")
    String verificationCode;
    @Schema(description="사용자가 입력한 인증번호", example="aB12cD34")
    String enteredVerificationCode;
    @Schema(description="인증번호 일치 여부", example="true")
    Boolean validate;
}
