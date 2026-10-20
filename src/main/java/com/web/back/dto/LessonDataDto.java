package com.web.back.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

// 강의실 정보 + 해당 강의 과제 목록 정보
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description="LessonDataDto 요청·응답 데이터. 사용 필드는 API 설명을 확인하세요.")
public class LessonDataDto {
    private LessonDto lesson;
    private List<AssignmentDto> assignment;
}
