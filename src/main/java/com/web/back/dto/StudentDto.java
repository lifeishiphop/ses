package com.web.back.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import com.web.back.model.Lesson;
import com.web.back.model.Member;
import com.web.back.model.Student;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description="StudentDto 요청·응답 데이터. 사용 필드는 API 설명을 확인하세요.")
public class StudentDto {
    @Schema(description="데이터 ID", example="1")
    private Long id;
    @Schema(description="회원 ID", example="1")
    private Long member_id;
    @Schema(description="강의실 ID", example="1")
    private Long lesson_id;
    @Schema(description="수강 상태: 0=신청, 1=승인", example="0")
    private Integer accept;

    public static StudentDto setFormat(Student student) {
        StudentDto studentDto = new StudentDto(
                student.getId(),
                student.getMember().getId(),
                student.getLesson().getId(),
                student.getAccept()
        );
        return studentDto;
    }
}
