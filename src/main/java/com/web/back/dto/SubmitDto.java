package com.web.back.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import com.web.back.model.Submit;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@Schema(description="SubmitDto 요청·응답 데이터. 사용 필드는 API 설명을 확인하세요.")
public class SubmitDto {
    @Schema(description="데이터 ID", example="1")
    private Long id;
    @Schema(description="실행 언어: 1=Java, 2=C++", example="1")
    private Integer language;
    @Schema(description="소스 코드", example="public class Main { public static void main(String[] args) { System.out.println(3); } }")
    private String submit_contents;
    @Schema(description="제출 일시", example="2026-10-21T09:00:00")
    private LocalDateTime submit_time;
    @Schema(description="모든 테스트 통과 100점, 실패 0점, 테스트 없음 -1점", example="100")
    private Integer score;
    @Schema(description="문제 ID. 실행·제출 시 경로에서 설정하며 본문은 생략 가능", example="1")
    private Long problem_id;
    @Schema(description="과제 ID. 실행·제출 시 경로에서 설정하며 본문은 생략 가능", example="1")
    private Long assignment_id;
    @Schema(description="회원 ID", example="1")
    private Long member_id;
    @Schema(description="실행 또는 채점 결과", example="정답입니다.")
    private String result;

    public SubmitDto() {

    }

    public SubmitDto(Long id, Integer language, String submit_contents, LocalDateTime submit_time, Integer score, Long member_id, String result) {
        this.id = id;
        this.language = language;
        this.submit_contents = submit_contents;
        this.submit_time = submit_time;
        this.score = score;
        this.member_id = member_id;
        this.result = result;
    }

    public static SubmitDto setFormat(Submit submit) {
        SubmitDto submitDto = new SubmitDto(
                submit.getId(),
                submit.getLanguage(),
                submit.getSubmit_contents(),
                submit.getSubmit_time(),
                submit.getScore(),
                submit.getMember().getId(),
                submit.getResult()
        );
        if (submit.getAssignment() == null) {
            submitDto.setProblem_id(submit.getProblem().getId());
        }
        else {
            submitDto.setAssignment_id(submit.getAssignment().getId());
        }
        return submitDto;
    }
}
