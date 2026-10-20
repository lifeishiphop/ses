package com.web.back.controller.assignment;

import com.web.back.dto.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.Operation;


import com.web.back.service.AssignmentService;
import com.web.back.utils.ErrorResponseManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name="과제")
@RestController
public class AssignmentController {

    @Autowired
    private AssignmentService assignmentService;

    @Autowired
    private ErrorResponseManager errorResponseManager;


    // 과제 목록 조회
    @Operation(summary="과제 목록 조회")
    @ApiResponse(responseCode="200", description="성공", content=@Content(mediaType="application/json", array=@ArraySchema(schema=@Schema(implementation=AssignmentDto.class))))
    @ApiResponse(responseCode="400", description="요청 처리 실패", content=@Content(mediaType="application/json", schema=@Schema(ref="#/components/schemas/ApiError")))
    @ApiResponse(responseCode="401", description="JWT 인증 필요", content=@Content)
    @GetMapping("/lesson/{lessonIdx}/assignment")
    public ResponseEntity<?> getAssignmentList(@PathVariable("lessonIdx") Long lessonIdx) throws Exception {
        try {
            List<AssignmentDto> assignments = assignmentService.getAssignmentList(lessonIdx);
            return new ResponseEntity<>(assignments, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(errorResponseManager.getErrorResponse(e), HttpStatus.BAD_REQUEST);
        }
    }

    // 과제 상세 조회
    @Operation(summary="과제 상세 조회")
    @ApiResponse(responseCode="200", description="성공", content=@Content(mediaType="application/json", schema=@Schema(implementation=AssignmentDto.class)))
    @ApiResponse(responseCode="400", description="요청 처리 실패", content=@Content(mediaType="application/json", schema=@Schema(ref="#/components/schemas/ApiError")))
    @ApiResponse(responseCode="401", description="JWT 인증 필요", content=@Content)
    @GetMapping("/assignment/{assignmentIdx}")
    public ResponseEntity<?> getAssignmentDetail(@PathVariable("assignmentIdx") Long assignmentIdx) throws Exception {
        try {
            AssignmentDto assignment = assignmentService.getAssignmentDetail(assignmentIdx);
            return new ResponseEntity<>(assignment, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(errorResponseManager.getErrorResponse(e), HttpStatus.BAD_REQUEST);
        }
    }

    // 과제 등록
    @Operation(summary="과제 등록")
    @ApiResponse(responseCode="200", description="성공", content=@Content(mediaType="application/json", schema=@Schema(implementation=AssignmentDto.class)))
    @ApiResponse(responseCode="400", description="요청 처리 실패", content=@Content(mediaType="application/json", schema=@Schema(ref="#/components/schemas/ApiError")))
    @ApiResponse(responseCode="401", description="JWT 인증 필요", content=@Content)
    @PostMapping("/assignment/add")
    public ResponseEntity<?> addAssignment(@RequestBody AssignmentDto assignmentDto) throws Exception {
        try {
            AssignmentDto assignment = assignmentService.addAssignment(assignmentDto);
            return new ResponseEntity<>(assignment, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(errorResponseManager.getErrorResponse(e), HttpStatus.BAD_REQUEST);
        }
    }

    // 과제 수정
    @Operation(summary="과제 수정")
    @ApiResponse(responseCode="200", description="성공", content=@Content(mediaType="application/json", schema=@Schema(implementation=AssignmentDto.class)))
    @ApiResponse(responseCode="400", description="요청 처리 실패", content=@Content(mediaType="application/json", schema=@Schema(ref="#/components/schemas/ApiError")))
    @ApiResponse(responseCode="401", description="JWT 인증 필요", content=@Content)
    @PutMapping("/assignment/edit/{assignmentIdx}")
    public ResponseEntity<?> editAssignment(@PathVariable("assignmentIdx") Long assignmentIdx, @RequestBody AssignmentDto assignmentDto) throws Exception {
        try {
            AssignmentDto assignment = assignmentService.editAssignment(assignmentIdx, assignmentDto);
            return new ResponseEntity<>(assignment, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(errorResponseManager.getErrorResponse(e), HttpStatus.BAD_REQUEST);
        }
    }

    // 과제 삭제
    @Operation(summary="과제 삭제")
    @ApiResponse(responseCode="200", description="성공", content=@Content)
    @ApiResponse(responseCode="400", description="요청 처리 실패", content=@Content(mediaType="application/json", schema=@Schema(ref="#/components/schemas/ApiError")))
    @ApiResponse(responseCode="401", description="JWT 인증 필요", content=@Content)
    @DeleteMapping("/assignment/delete/{assignmentIdx}")
    public ResponseEntity<?> deleteAssignment(@PathVariable("assignmentIdx") Long assignmentIdx) throws Exception {
        try {
            assignmentService.deleteAssignment(assignmentIdx);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(errorResponseManager.getErrorResponse(e), HttpStatus.BAD_REQUEST);
        }
    }

    // 과제 코드 실행
    @Operation(summary="과제 코드 실행", description="Docker 채점 이미지가 필요합니다. language(1=Java, 2=C++)와 submit_contents를 입력합니다. 모든 테스트 통과 시 100점, 실패 시 0점, 테스트가 없으면 -1점입니다. assignment_id는 경로에서 설정하며 본문에 넣으면 경로와 같아야 합니다. 실행만으로 제출 내역을 저장하지 않습니다.")
    @ApiResponse(responseCode="200", description="성공", content=@Content(mediaType="application/json", schema=@Schema(implementation=SubmitDto.class)))
    @ApiResponse(responseCode="400", description="요청 처리 실패", content=@Content(mediaType="application/json", schema=@Schema(ref="#/components/schemas/ApiError")))
    @ApiResponse(responseCode="401", description="JWT 인증 필요", content=@Content)
    @PostMapping("/assignment/run/{assignmentIdx}")
    public ResponseEntity<?> runAssignmentCode(@PathVariable("assignmentIdx") Long assignmentIdx, @RequestBody SubmitDto submitDto) throws Exception {
        try {
            SubmitDto submit = assignmentService.runAssignmentCode(assignmentIdx, submitDto);
            return new ResponseEntity<>(submit, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(errorResponseManager.getErrorResponse(e), HttpStatus.BAD_REQUEST);
        }
    }

    // 과제 코드 제출
    @Operation(summary="과제 코드 제출", description="마감일을 확인한 뒤 Docker 채점 결과를 저장합니다. language와 submit_contents가 필요하고 assignment_id는 본문에서 생략할 수 있습니다. 기존 계약대로 result는 제출되었습니다.를 반환하며 실제 점수는 score에 담습니다. Docker 장애 시 점수를 저장하지 않습니다.")
    @ApiResponse(responseCode="200", description="성공", content=@Content(mediaType="application/json", schema=@Schema(implementation=SubmitDto.class)))
    @ApiResponse(responseCode="400", description="요청 처리 실패", content=@Content(mediaType="application/json", schema=@Schema(ref="#/components/schemas/ApiError")))
    @ApiResponse(responseCode="401", description="JWT 인증 필요", content=@Content)
    @PostMapping("/assignment/submit/{assignmentIdx}")
    public ResponseEntity<?> submitAssignmentCode(@PathVariable("assignmentIdx") Long assignmentIdx, @RequestBody SubmitDto submitDto) throws Exception {
        try {
            SubmitDto submit = assignmentService.submitAssignmentCode(assignmentIdx, submitDto);
            return new ResponseEntity<>(submit, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(errorResponseManager.getErrorResponse(e), HttpStatus.BAD_REQUEST);
        }
    }


    // 멤버별 과제 채점 결과 조회
    @Operation(summary="멤버별 과제 채점 결과 조회")
    @ApiResponse(responseCode="200", description="성공", content=@Content(mediaType="application/json", array=@ArraySchema(schema=@Schema(implementation=StudentDataDto.class))))
    @ApiResponse(responseCode="400", description="요청 처리 실패", content=@Content(mediaType="application/json", schema=@Schema(ref="#/components/schemas/ApiError")))
    @ApiResponse(responseCode="401", description="JWT 인증 필요", content=@Content)
    @GetMapping("/lesson/{lessonIdx}/assignment/score/{memberIdx}")
    public ResponseEntity<?> getMyAssignmentScoreList(@PathVariable("lessonIdx") Long lessonIdx, @PathVariable("memberIdx") Long memberIdx) throws Exception {
        try {
            List<StudentDataDto> datas = assignmentService.getMyAssignmentScoreList(lessonIdx, memberIdx);
            return new ResponseEntity<>(datas, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(errorResponseManager.getErrorResponse(e), HttpStatus.BAD_REQUEST);
        }
    }

    // 과제별 채점 결과 조회
    @Operation(summary="과제별 채점 결과 조회")
    @ApiResponse(responseCode="200", description="성공", content=@Content(mediaType="application/json", array=@ArraySchema(schema=@Schema(implementation=StudentDataDto.class))))
    @ApiResponse(responseCode="400", description="요청 처리 실패", content=@Content(mediaType="application/json", schema=@Schema(ref="#/components/schemas/ApiError")))
    @ApiResponse(responseCode="401", description="JWT 인증 필요", content=@Content)
    @GetMapping("/assignment/{assignmentIdx}/score")
    public ResponseEntity<?> getAssignmentScoreList(@PathVariable("assignmentIdx") Long assignmentIdx) throws Exception {
        try {
            List<StudentDataDto> datas = assignmentService.getAssignmentScoreList(assignmentIdx);
            return new ResponseEntity<>(datas, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(errorResponseManager.getErrorResponse(e), HttpStatus.BAD_REQUEST);
        }
    }

    // 과제 채점 결과 상세 조회
    @Operation(summary="과제 채점 결과 상세 조회")
    @ApiResponse(responseCode="200", description="성공", content=@Content(mediaType="application/json", schema=@Schema(implementation=SubmitDto.class)))
    @ApiResponse(responseCode="400", description="요청 처리 실패", content=@Content(mediaType="application/json", schema=@Schema(ref="#/components/schemas/ApiError")))
    @ApiResponse(responseCode="401", description="JWT 인증 필요", content=@Content)
    @GetMapping("/assignment/score/{submitIdx}")
    public ResponseEntity<?> getAssignmentScoreDetail(@PathVariable("submitIdx") Long submitIdx) throws Exception {
        try {
            SubmitDto Submit = assignmentService.getAssignmentScoreDetail(submitIdx);
            return new ResponseEntity<>(Submit, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(errorResponseManager.getErrorResponse(e), HttpStatus.BAD_REQUEST);
        }
    }


    // 과제 채점 결과 수정
    @Operation(summary="과제 채점 결과 수정")
    @ApiResponse(responseCode="200", description="성공", content=@Content(mediaType="application/json", schema=@Schema(implementation=SubmitDto.class)))
    @ApiResponse(responseCode="400", description="요청 처리 실패", content=@Content(mediaType="application/json", schema=@Schema(ref="#/components/schemas/ApiError")))
    @ApiResponse(responseCode="401", description="JWT 인증 필요", content=@Content)
    @PutMapping("/assignment/score/edit/{submitIdx}")
    public ResponseEntity<?> editAssignmentScoreDetail(@PathVariable("submitIdx") Long submitIdx, @RequestBody SubmitDto submitDto) throws Exception {
        // 이거도 클라이언트에서 점수 매핑해서 DTO를 보낼 건지 아니면 백에서 찾아서 점수만 수정할 건지
        try {
            SubmitDto Submit = assignmentService.editAssignmentScoreDetail(submitIdx, submitDto);
            return new ResponseEntity<>(Submit, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(errorResponseManager.getErrorResponse(e), HttpStatus.BAD_REQUEST);
        }
    }

    // 과제 테스트케이스 조회
    @Operation(summary="과제 테스트케이스 조회")
    @ApiResponse(responseCode="200", description="성공", content=@Content(mediaType="application/json", array=@ArraySchema(schema=@Schema(implementation=TestcaseDto.class))))
    @ApiResponse(responseCode="400", description="요청 처리 실패", content=@Content(mediaType="application/json", schema=@Schema(ref="#/components/schemas/ApiError")))
    @ApiResponse(responseCode="401", description="JWT 인증 필요", content=@Content)
    @GetMapping("/assignment/{assignmentIdx}/testcase")
    public ResponseEntity<?> getProblemTestcase(@PathVariable("assignmentIdx") Long assignmentIdx) throws Exception {
        try {
            List<TestcaseDto> testcases = assignmentService.getAssignmentTestcaseList(assignmentIdx);
            return new ResponseEntity<>(testcases, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(errorResponseManager.getErrorResponse(e), HttpStatus.BAD_REQUEST);
        }
    }

    // 과제 테스트케이스 등록
    @Operation(summary="과제 테스트케이스 등록", description="본문의 assignment_id에도 경로의 ID를 넣어야 합니다. 현재 서비스는 본문의 ID를 사용합니다.")
    @ApiResponse(responseCode="200", description="성공", content=@Content(mediaType="application/json", schema=@Schema(implementation=TestcaseDto.class)))
    @ApiResponse(responseCode="400", description="요청 처리 실패", content=@Content(mediaType="application/json", schema=@Schema(ref="#/components/schemas/ApiError")))
    @ApiResponse(responseCode="401", description="JWT 인증 필요", content=@Content)
    @PostMapping("/assignment/{assignmentIdx}/testcase/add")
    public ResponseEntity<?> addProblemTestcase(@PathVariable("assignmentIdx") Long assignmentIdx, @RequestBody TestcaseDto testcaseDto) throws Exception {
        try {
            return new ResponseEntity<>(assignmentService.addAssignmentTestcase(testcaseDto), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(errorResponseManager.getErrorResponse(e), HttpStatus.BAD_REQUEST);
        }
    }

    // 과제 테스트케이스 수정
    @Operation(summary="과제 테스트케이스 수정")
    @ApiResponse(responseCode="200", description="성공", content=@Content(mediaType="application/json", schema=@Schema(implementation=TestcaseDto.class)))
    @ApiResponse(responseCode="400", description="요청 처리 실패", content=@Content(mediaType="application/json", schema=@Schema(ref="#/components/schemas/ApiError")))
    @ApiResponse(responseCode="401", description="JWT 인증 필요", content=@Content)
    @PutMapping("/assignment/{assignmentIdx}/testcase/edit/{testcaseIdx}")
    public ResponseEntity<?> editProblemTestcase(@PathVariable("assignmentIdx") Long assignmentIdx, @PathVariable("testcaseIdx") Long testcaseIdx, @RequestBody TestcaseDto testcaseDto) throws Exception {
        try {
            TestcaseDto testcase = assignmentService.editAssignmentTestcase(testcaseIdx, testcaseDto);
            return new ResponseEntity<>(testcase, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(errorResponseManager.getErrorResponse(e), HttpStatus.BAD_REQUEST);
        }
    }

    // 과제 테스트케이스 삭제
    @Operation(summary="과제 테스트케이스 삭제")
    @ApiResponse(responseCode="200", description="성공", content=@Content)
    @ApiResponse(responseCode="400", description="요청 처리 실패", content=@Content(mediaType="application/json", schema=@Schema(ref="#/components/schemas/ApiError")))
    @ApiResponse(responseCode="401", description="JWT 인증 필요", content=@Content)
    @DeleteMapping("/assignment/{assignmentIdx}/testcase/delete/{testcaseIdx}")
    public ResponseEntity<?> deleteProblemTestcase(@PathVariable("assignmentIdx") Long assignmentIdx, @PathVariable("testcaseIdx") Long testcaseIdx) throws Exception {
        try {
            assignmentService.deleteAssignmentTestcase(testcaseIdx);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(errorResponseManager.getErrorResponse(e), HttpStatus.BAD_REQUEST);
        }
    }

}
