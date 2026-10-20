package com.web.back.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description="StudentDataDto 요청·응답 데이터. 사용 필드는 API 설명을 확인하세요.")
public class StudentDataDto {
    @Schema(description="데이터 ID", example="1")
    private Long id;
    @Schema(description="목록의 표시 순번", example="1")
    private String number;
    @Schema(description="이름", example="홍길동")
    private String name;
    @Schema(description="제출한 소스 코드")
    private String code;
    @Schema(description="채점 점수", example="100")
    private int score;
}
