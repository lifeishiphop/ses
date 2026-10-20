package com.web.back.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import com.web.back.model.Problem;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@Schema(description="ProblemDto 요청·응답 데이터. 사용 필드는 API 설명을 확인하세요.")
public class ProblemDto {
    @Schema(description="데이터 ID", example="1")
    private Long id;
    @Schema(description="목록의 표시 순번", example="1")
    private int number;
    @Schema(description="제목", example="반복문 연습")
    private String title;
    @Schema(description="문제 또는 과제 설명", example="두 정수의 합을 출력하세요.")
    private String description;
    @Schema(description="소스 코드", example="public class Main { public static void main(String[] args) { System.out.println(3); } }")
    private String submit_contents;
    @Schema(description="지원 언어: 0=Java와 C++, 1=Java, 2=C++", example="1")
    private Integer language;
    @Schema(description="등록 일시", example="2026-10-21T09:00:00")
    private LocalDateTime upload_time;
    @Schema(description="출제자 ID", example="1")
    private Long uploader_id;
    @Schema(description="출제자 이름", example="홍길동")
    private String uploader;
    @Schema(description="문제 풀이 상태 또는 과제 제출 상태", example="-")
    String solve;

    public ProblemDto() {

    }

    public ProblemDto(Long id, String title, String description, Integer language, LocalDateTime upload_time, Long uploader_id, String uploader) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.language = language;
        this.upload_time = upload_time;
        this.uploader_id = uploader_id;
        this.uploader = uploader;
    }



    public static ProblemDto setFormat(Problem problem) {
        ProblemDto problemDto = new ProblemDto(
                problem.getId(),
                problem.getTitle(),
                problem.getDescription(),
                problem.getLanguage(),
                problem.getUpload_time(),
                problem.getMember().getId(),
                problem.getMember().getName()
        );

        return problemDto;
    }

    public static ProblemDto setFormat(Problem problem, int number) {
        ProblemDto problemDto = setFormat(problem);
        problemDto.setNumber(number);
        return problemDto;
    }
    public static ProblemDto setFormat(Problem problem, int number, String solve) {
        ProblemDto problemDto = setFormat(problem, number);
        problemDto.setSolve(solve);
        return problemDto;
    }
    public static ProblemDto setFormat(Problem problem, String submit_contents) {
        ProblemDto problemDto = setFormat(problem);
        problemDto.setSubmit_contents(submit_contents);
        return problemDto;
    }
}
