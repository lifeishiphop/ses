package com.web.back.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description="LoginDto 요청·응답 데이터. 사용 필드는 API 설명을 확인하세요.")
public class LoginDto {
    @Schema(description="학번 (회원가입의 number)", example="20260001")
    private String id;
    @Schema(description="비밀번호", example="SesTest123!")
    private String password;
}
