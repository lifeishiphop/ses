package com.web.back.controller.problem;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.Operation;
import com.web.back.dto.*;
import com.web.back.service.ProblemService;
import com.web.back.utils.ErrorResponseManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name="문제")
@RestController
public class ProblemController {

    @Autowired
    private ErrorResponseManager errorResponseManager;

    @Autowired
    private ProblemService problemService;


    // 문제 목록 조회
    @Operation(summary="문제 목록 조회")
    @ApiResponse(responseCode="200", description="성공", content=@Content(mediaType="application/json", array=@ArraySchema(schema=@Schema(implementation=ProblemDto.class))))
    @ApiResponse(responseCode="400", description="요청 처리 실패", content=@Content(mediaType="application/json", schema=@Schema(ref="#/components/schemas/ApiError")))
    @ApiResponse(responseCode="401", description="JWT 인증 필요", content=@Content)
    @GetMapping("/problem")
    public ResponseEntity<?> readProblemList() throws Exception {
        try {
            List<ProblemDto> problems = problemService.getProblemList();
            return new ResponseEntity<>(problems, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(errorResponseManager.getErrorResponse(e), HttpStatus.BAD_REQUEST);
        }
    }

    // 출제 문제 목록 조회
    @Operation(summary="출제 문제 목록 조회")
    @ApiResponse(responseCode="200", description="성공", content=@Content(mediaType="application/json", array=@ArraySchema(schema=@Schema(implementation=ProblemDto.class))))
    @ApiResponse(responseCode="400", description="요청 처리 실패", content=@Content(mediaType="application/json", schema=@Schema(ref="#/components/schemas/ApiError")))
    @ApiResponse(responseCode="401", description="JWT 인증 필요", content=@Content)
    @GetMapping("/myProblem")
    public ResponseEntity<?> readMyProblemList() throws Exception {
        try {
            List<ProblemDto> problems = problemService.getMyProblemList();
            return new ResponseEntity<>(problems, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(errorResponseManager.getErrorResponse(e), HttpStatus.BAD_REQUEST);
        }
    }

    // 문제 상세 조회
    @Operation(summary="문제 상세 조회")
    @ApiResponse(responseCode="200", description="성공", content=@Content(mediaType="application/json", schema=@Schema(implementation=ProblemDataDto.class)))
    @ApiResponse(responseCode="400", description="요청 처리 실패", content=@Content(mediaType="application/json", schema=@Schema(ref="#/components/schemas/ApiError")))
    @ApiResponse(responseCode="401", description="JWT 인증 필요", content=@Content)
    @GetMapping("/problemDetail/{problemIdx}")
    public ResponseEntity<?> openPostDetail(@PathVariable("problemIdx") Long problemIdx) throws Exception {
        try {
            ProblemDataDto problem = problemService.getProblemDetail(problemIdx);
            return new ResponseEntity<>(problem, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(errorResponseManager.getErrorResponse(e), HttpStatus.BAD_REQUEST);
        }
    }

    // 문제 등록
    @Operation(summary="문제 등록")
    @ApiResponse(responseCode="200", description="성공", content=@Content(mediaType="application/json", schema=@Schema(implementation=ProblemDto.class)))
    @ApiResponse(responseCode="400", description="요청 처리 실패", content=@Content(mediaType="application/json", schema=@Schema(ref="#/components/schemas/ApiError")))
    @ApiResponse(responseCode="401", description="JWT 인증 필요", content=@Content)
    @PostMapping("/problem/write")
    public ResponseEntity<?> addProblem(@RequestBody ProblemDto problemDto) throws Exception {
        try {
            return new ResponseEntity<>(problemService.registerProblem(problemDto), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(errorResponseManager.getErrorResponse(e), HttpStatus.BAD_REQUEST);
        }
    }

    // 문제 수정
    @Operation(summary="문제 수정")
    @ApiResponse(responseCode="200", description="성공", content=@Content(mediaType="application/json", schema=@Schema(implementation=ProblemDto.class)))
    @ApiResponse(responseCode="400", description="요청 처리 실패", content=@Content(mediaType="application/json", schema=@Schema(ref="#/components/schemas/ApiError")))
    @ApiResponse(responseCode="401", description="JWT 인증 필요", content=@Content)
    @PutMapping("/problem/edit/{problemIdx}")
    public ResponseEntity<?> editProblem(@PathVariable("problemIdx") Long problemIdx, @RequestBody ProblemDto problemDto) throws Exception {
        try {
            ProblemDto problem = problemService.editProblem(problemIdx, problemDto);
            return new ResponseEntity<>(problem, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(errorResponseManager.getErrorResponse(e), HttpStatus.BAD_REQUEST);
        }
    }

    // 문제 삭제
    @Operation(summary="문제 삭제")
    @ApiResponse(responseCode="200", description="성공", content=@Content)
    @ApiResponse(responseCode="400", description="요청 처리 실패", content=@Content(mediaType="application/json", schema=@Schema(ref="#/components/schemas/ApiError")))
    @ApiResponse(responseCode="401", description="JWT 인증 필요", content=@Content)
    @DeleteMapping("/problem/delete/{problemIdx}")
    public ResponseEntity<?> deleteProblem(@PathVariable("problemIdx") Long problemIdx) throws Exception {
        try {
            problemService.deleteProblem(problemIdx);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(errorResponseManager.getErrorResponse(e), HttpStatus.BAD_REQUEST);
        }
    }



    // 문제 코드 실행
    @Operation(summary="문제 코드 실행", description="Docker 채점 이미지가 필요합니다. language(1=Java, 2=C++)와 submit_contents를 입력합니다. 모든 테스트 통과 시 100점, 실패 시 0점, 테스트가 없으면 -1점입니다. problem_id는 경로에서 설정하며 본문에 넣으면 경로와 같아야 합니다. 실행만으로 제출 내역을 저장하지 않습니다.")
    @ApiResponse(responseCode="200", description="성공", content=@Content(mediaType="application/json", schema=@Schema(implementation=SubmitDto.class)))
    @ApiResponse(responseCode="400", description="요청 처리 실패", content=@Content(mediaType="application/json", schema=@Schema(ref="#/components/schemas/ApiError")))
    @ApiResponse(responseCode="401", description="JWT 인증 필요", content=@Content)
    @PostMapping("/problem/run/{problemIdx}")
    public ResponseEntity<?> runProblem(@PathVariable("problemIdx") Long problemIdx, @RequestBody SubmitDto submitDto) {
        try {

            SubmitDto result = problemService.runProblemCode(problemIdx, submitDto);
            return new ResponseEntity<>(result, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(errorResponseManager.getErrorResponse(e), HttpStatus.BAD_REQUEST);
        }
    }

    // 문제 풀이 제출
    @Operation(summary="문제 풀이 제출", description="Docker 채점 후 회원·문제·언어별 최신 제출을 저장합니다. language와 submit_contents가 필요하고 problem_id는 본문에서 생략할 수 있습니다. Docker 장애 시 오류를 반환하며 점수를 저장하지 않습니다.")
    @ApiResponse(responseCode="200", description="성공", content=@Content(mediaType="application/json", schema=@Schema(implementation=SubmitDto.class)))
    @ApiResponse(responseCode="400", description="요청 처리 실패", content=@Content(mediaType="application/json", schema=@Schema(ref="#/components/schemas/ApiError")))
    @ApiResponse(responseCode="401", description="JWT 인증 필요", content=@Content)
    @PostMapping("/problem/submit/{problemIdx}")
    public ResponseEntity<?> submitProblem(@PathVariable("problemIdx") Long problemIdx, @RequestBody SubmitDto submitDto) throws Exception {
        try {
            // 클라이언트에서 보낼 때 DTO에 문제 정보 같이 넣어서 보낼지, 아니면 문제 ID 넣고 여기서 DTO에 넣어줄지
            return new ResponseEntity<>(problemService.submitProblemCode(problemIdx, submitDto), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(errorResponseManager.getErrorResponse(e), HttpStatus.BAD_REQUEST);
        }
    }

    @Operation(summary="문제 검색")
    @ApiResponse(responseCode="200", description="성공", content=@Content(mediaType="application/json", schema=@Schema(implementation=SearchResultDto.class)))
    @ApiResponse(responseCode="400", description="요청 처리 실패", content=@Content(mediaType="application/json", schema=@Schema(ref="#/components/schemas/ApiError")))
    @ApiResponse(responseCode="401", description="JWT 인증 필요", content=@Content)
    @GetMapping("/problem/search")
    public ResponseEntity<?> searchProblem(@RequestParam(value="keyword") String keyword,
                                           @io.swagger.v3.oas.annotations.Parameter(description="페이지 번호 (1부터 시작)", example="1") @RequestParam(value="page") int page) throws Exception {
        try {
            SearchResultDto problems = problemService.searchProblem(keyword, page);
            return new ResponseEntity<>(problems, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(errorResponseManager.getErrorResponse(e), HttpStatus.BAD_REQUEST);
        }
    }

    // 문제 테스트케이스 조회
    @Operation(summary="문제 테스트케이스 조회")
    @ApiResponse(responseCode="200", description="성공", content=@Content(mediaType="application/json", array=@ArraySchema(schema=@Schema(implementation=TestcaseDto.class))))
    @ApiResponse(responseCode="400", description="요청 처리 실패", content=@Content(mediaType="application/json", schema=@Schema(ref="#/components/schemas/ApiError")))
    @ApiResponse(responseCode="401", description="JWT 인증 필요", content=@Content)
    @GetMapping("/problem/{problemIdx}/testcase")
    public ResponseEntity<?> getProblemTestcase(@PathVariable("problemIdx") Long problemIdx) throws Exception {
        try {
            List<TestcaseDto> testcases = problemService.getProblemTestcaseList(problemIdx);
            return new ResponseEntity<>(testcases, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(errorResponseManager.getErrorResponse(e), HttpStatus.BAD_REQUEST);
        }
    }

    // 문제 테스트케이스 등록
    @Operation(summary="문제 테스트케이스 등록", description="본문의 problem_id에도 경로의 ID를 넣어야 합니다. 현재 서비스는 본문의 ID를 사용합니다.")
    @ApiResponse(responseCode="200", description="성공", content=@Content(mediaType="application/json", schema=@Schema(implementation=TestcaseDto.class)))
    @ApiResponse(responseCode="400", description="요청 처리 실패", content=@Content(mediaType="application/json", schema=@Schema(ref="#/components/schemas/ApiError")))
    @ApiResponse(responseCode="401", description="JWT 인증 필요", content=@Content)
    @PostMapping("/problem/{problemIdx}/testcase/add")
    public ResponseEntity<?> addProblemTestcase(@PathVariable("problemIdx") Long problemIdx, @RequestBody TestcaseDto testcaseDto) throws Exception {
        try {
            // Dto에 problem id 담아서 프론트에서 백으로 보낼지 아니면 아이디랑 DTO 따로 보내고 받아서 DTO에 담아주고 실행할지 결정해야 할듯
            return new ResponseEntity<>(problemService.addProblemTestcase(testcaseDto), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(errorResponseManager.getErrorResponse(e), HttpStatus.BAD_REQUEST);
        }
    }

    // 문제 테스트케이스 수정
    @Operation(summary="문제 테스트케이스 수정")
    @ApiResponse(responseCode="200", description="성공", content=@Content(mediaType="application/json", schema=@Schema(implementation=TestcaseDto.class)))
    @ApiResponse(responseCode="400", description="요청 처리 실패", content=@Content(mediaType="application/json", schema=@Schema(ref="#/components/schemas/ApiError")))
    @ApiResponse(responseCode="401", description="JWT 인증 필요", content=@Content)
    @PutMapping("/problem/{problemIdx}/testcase/edit/{testcaseIdx}")
    public ResponseEntity<?> editProblemTestcase(@PathVariable("problemIdx") Long problemIdx, @PathVariable("testcaseIdx") Long testcaseIdx, @RequestBody TestcaseDto testcaseDto) throws Exception {
        try {
            TestcaseDto testcase = problemService.editProblemTestcase(testcaseIdx, testcaseDto);
            return new ResponseEntity<>(testcase, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(errorResponseManager.getErrorResponse(e), HttpStatus.BAD_REQUEST);
        }
    }

    // 문제 테스트케이스 삭제
    @Operation(summary="문제 테스트케이스 삭제")
    @ApiResponse(responseCode="200", description="성공", content=@Content)
    @ApiResponse(responseCode="400", description="요청 처리 실패", content=@Content(mediaType="application/json", schema=@Schema(ref="#/components/schemas/ApiError")))
    @ApiResponse(responseCode="401", description="JWT 인증 필요", content=@Content)
    @DeleteMapping("/problem/{problemIdx}/testcase/delete/{testcaseIdx}")
    public ResponseEntity<?> deleteProblemTestcase(@PathVariable("problemIdx") Long problemIdx, @PathVariable("testcaseIdx") Long testcaseIdx) throws Exception {
        try {
            problemService.deleteProblemTestcase(testcaseIdx);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(errorResponseManager.getErrorResponse(e), HttpStatus.BAD_REQUEST);
        }
    }

}
