package com.web.back.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@Schema(description="ProblemDataDto 요청·응답 데이터. 사용 필드는 API 설명을 확인하세요.")
public class ProblemDataDto {
    ProblemDto problem;
    List<SubmitDto> submits = new ArrayList<>();
}
