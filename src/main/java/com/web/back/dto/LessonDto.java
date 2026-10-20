package com.web.back.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import com.web.back.model.Lesson;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@Schema(description="LessonDto 요청·응답 데이터. 사용 필드는 API 설명을 확인하세요.")
public class LessonDto {
    @Schema(description="데이터 ID", example="1")
    private Long id;

    @Schema(description="목록의 표시 순번", example="1")
    private int number;

    @Schema(description="강의실 이름", example="소프트웨어공학")
    private String name;

    @Schema(description="개설 연도", example="2026")
    private String year;

    @Schema(description="학기", example="1")
    private String semester;

    @Schema(description="강의 담당자 이름", example="홍길동")
    private String owner;

    @Schema(description="강의 담당 회원 ID", example="1")
    private Long owner_id;

    public LessonDto() {

    }

    public LessonDto(Long id, String name, String year, String semester, String owner, Long owner_id) {
        this.id = id;
        this.name = name;
        this.year = year;
        this.semester = semester;
        this.owner = owner;
        this.owner_id = owner_id;
    }

    public static LessonDto setFormat(Lesson lesson) {
        LessonDto lessonDto = new LessonDto(
                lesson.getId(),
                lesson.getName(),
                lesson.getYear(),
                lesson.getSemester(),
                lesson.getMember().getName(),
                lesson.getMember().getId()
        );

        return lessonDto;
    }

    public static LessonDto setFormat(Lesson lesson, int number) {
        LessonDto lessonDto = new LessonDto(
                lesson.getId(),
                lesson.getName(),
                lesson.getYear(),
                lesson.getSemester(),
                lesson.getMember().getName(),
                lesson.getMember().getId()
        );

        lessonDto.setNumber(number);

        return lessonDto;
    }
}
