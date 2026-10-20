package com.web.back.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import com.web.back.model.Assignment;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@Schema(description="AssignmentDto 요청·응답 데이터. 사용 필드는 API 설명을 확인하세요.")
public class AssignmentDto {
    @Schema(description="데이터 ID", example="1")
    private Long id;
    @Schema(description="목록의 표시 순번", example="1")
    private int number;
    @Schema(description="제목", example="반복문 연습")
    private String title;
    @Schema(description="문제 또는 과제 설명", example="두 정수의 합을 출력하세요.")
    private String description;
    @Schema(description="과제 시작 일시 (등록 시 서버에서 설정)", example="2026-10-21T09:00:00")
    private LocalDateTime start_time;
    @Schema(description="과제 마감 일시", example="2026-12-31T23:59:59")
    private LocalDateTime end_time;
    @Schema(description="지원 언어: 0=Java와 C++, 1=Java, 2=C++", example="1")
    private Integer language;
    @Schema(description="강의실 ID", example="1")
    private Long lessonId;

    @Schema(description="제출 학생 수", example="0")
    private Integer submitStudents;
    @Schema(description="전체 수강 학생 수", example="1")
    private Integer totalStudents;

    @Schema(description="문제 풀이 상태 또는 과제 제출 상태", example="-")
    private String solve;

    @Schema(description="소스 코드", example="public class Main { public static void main(String[] args) { System.out.println(3); } }")
    private String submit_contents;
    @Schema(description="최근 제출 코드의 언어: 1=Java, 2=C++")
    private Integer submit_language;
    @Schema(description="최근 제출의 채점 점수")
    private Integer score;

    public AssignmentDto() {

    }

    public AssignmentDto(Long id, String title, String description, LocalDateTime start_time, LocalDateTime end_time, Integer language, Long lessonId) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.start_time = start_time;
        this.end_time = end_time;
        this.language = language;
        this.lessonId = lessonId;
    }

    public static AssignmentDto setFormat(Assignment assignment) {
        AssignmentDto assignmentDto = new AssignmentDto(
                assignment.getId(),
                assignment.getTitle(),
                assignment.getDescription(),
                assignment.getStart_time(),
                assignment.getEnd_time(),
                assignment.getLanguage(),
                assignment.getLesson().getId()
        );
        return assignmentDto;
    }

    public static AssignmentDto setFormat(Assignment assignment, int number) {
        AssignmentDto assignmentDto = new AssignmentDto(
                assignment.getId(),
                assignment.getTitle(),
                assignment.getDescription(),
                assignment.getStart_time(),
                assignment.getEnd_time(),
                assignment.getLanguage(),
                assignment.getLesson().getId()
        );
        assignmentDto.setNumber(number);
        return assignmentDto;
    }

    public static AssignmentDto setFormat(Assignment assignment, int number, String solve) {
        AssignmentDto assignmentDto = new AssignmentDto(
                assignment.getId(),
                assignment.getTitle(),
                assignment.getDescription(),
                assignment.getStart_time(),
                assignment.getEnd_time(),
                assignment.getLanguage(),
                assignment.getLesson().getId()
        );
        assignmentDto.setNumber(number);
        assignmentDto.setSolve(solve);
        return assignmentDto;
    }

    public static AssignmentDto setFormat(Assignment assignment, int number, int submit, int total) {
        AssignmentDto assignmentDto = new AssignmentDto(
                assignment.getId(),
                assignment.getTitle(),
                assignment.getDescription(),
                assignment.getStart_time(),
                assignment.getEnd_time(),
                assignment.getLanguage(),
                assignment.getLesson().getId()
        );
        assignmentDto.setNumber(number);
        assignmentDto.setSubmitStudents(submit);
        assignmentDto.setTotalStudents(total);
        return assignmentDto;
    }
}
