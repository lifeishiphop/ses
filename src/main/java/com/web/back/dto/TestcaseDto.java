package com.web.back.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import com.web.back.model.Submit;
import com.web.back.model.Testcase;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@Schema(description="TestcaseDto 요청·응답 데이터. 사용 필드는 API 설명을 확인하세요.")
public class TestcaseDto {

    @Schema(description="데이터 ID", example="1")
    private Long id;

    @Schema(description="테스트 입력", example="1 2")
    private String input;

    @Schema(description="기대 출력", example="3")
    private String output;

    @Schema(description="문제 ID. 테스트케이스 등록·문제 제출 시 경로 ID와 함께 입력", example="1")
    private Long problem_id;

    @Schema(description="과제 ID. 테스트케이스 등록·과제 제출 시 경로 ID와 함께 입력", example="1")
    private Long assignment_id;

    public TestcaseDto() {

    }

    public TestcaseDto(Long id, String input, String output) {
        this.id = id;
        this.input = input;
        this.output = output;
    }

    public static TestcaseDto setFormat(Testcase testcase) {
        TestcaseDto testcaseDto = new TestcaseDto(
                testcase.getId(),
                testcase.getInput(),
                testcase.getOutput()
        );
        if (testcase.getAssignment() == null) {
            testcaseDto.setProblem_id(testcase.getProblem().getId());
        }
        else {
            testcaseDto.setAssignment_id(testcase.getAssignment().getId());
        }
        return testcaseDto;
    }
}
