package com.web.back.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description="TokenDto 요청·응답 데이터. 사용 필드는 API 설명을 확인하세요.")
public class TokenDto {
    @Schema(description="JWT. Swagger Authorize에 Bearer 접두어 없이 입력")
    private String token;
    private MemberDto memberDto;
}