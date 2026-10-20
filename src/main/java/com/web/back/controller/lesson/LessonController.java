package com.web.back.controller.lesson;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.Operation;
import com.web.back.dto.*;
import com.web.back.service.LessonService;
import com.web.back.utils.ErrorResponseManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name="강의실")
@RestController
public class LessonController {
    @Autowired
    private ErrorResponseManager errorResponseManager;

    @Autowired
    private LessonService lessonService;

    // 강의실 목록 조회
    @Operation(summary="강의실 목록 조회")
    @ApiResponse(responseCode="200", description="성공", content=@Content(mediaType="application/json", array=@ArraySchema(schema=@Schema(implementation=LessonDto.class))))
    @ApiResponse(responseCode="400", description="요청 처리 실패", content=@Content(mediaType="application/json", schema=@Schema(ref="#/components/schemas/ApiError")))
    @ApiResponse(responseCode="401", description="JWT 인증 필요", content=@Content)
    @GetMapping("/lesson")
    public ResponseEntity<?> getLessonList() throws Exception {
        try {
            List<LessonDto> lessons = lessonService.getLessonList();
            return new ResponseEntity<>(lessons, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(errorResponseManager.getErrorResponse(e), HttpStatus.BAD_REQUEST);
        }
    }

    // 내 강의실 목록 조회 (강사랑 학생 따로)
    @Operation(summary="내 강의실 목록 조회 (학생)")
    @ApiResponse(responseCode="200", description="성공", content=@Content(mediaType="application/json", array=@ArraySchema(schema=@Schema(implementation=LessonDto.class))))
    @ApiResponse(responseCode="400", description="요청 처리 실패", content=@Content(mediaType="application/json", schema=@Schema(ref="#/components/schemas/ApiError")))
    @ApiResponse(responseCode="401", description="JWT 인증 필요", content=@Content)
    @GetMapping("/lesson/s")
    public ResponseEntity<?> getMyLessonListS() throws Exception {
        try {
            List<LessonDto> lessons = lessonService.getMyLessonList();
            return new ResponseEntity<>(lessons, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(errorResponseManager.getErrorResponse(e), HttpStatus.BAD_REQUEST);
        }
    }

    @Operation(summary="내 강의실 목록 조회 (강사)")
    @ApiResponse(responseCode="200", description="성공", content=@Content(mediaType="application/json", array=@ArraySchema(schema=@Schema(implementation=LessonDto.class))))
    @ApiResponse(responseCode="400", description="요청 처리 실패", content=@Content(mediaType="application/json", schema=@Schema(ref="#/components/schemas/ApiError")))
    @ApiResponse(responseCode="401", description="JWT 인증 필요", content=@Content)
    @GetMapping("/lesson/t")
    public ResponseEntity<?> getMyLessonListT() throws Exception {
        try {
            List<LessonDto> lessons = lessonService.getMyLessonList();
            return new ResponseEntity<>(lessons, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(errorResponseManager.getErrorResponse(e), HttpStatus.BAD_REQUEST);
        }
    }

    // 강의실 상세 조회
    @Operation(summary="강의실 상세 조회")
    @ApiResponse(responseCode="200", description="성공", content=@Content(mediaType="application/json", schema=@Schema(implementation=LessonDto.class)))
    @ApiResponse(responseCode="400", description="요청 처리 실패", content=@Content(mediaType="application/json", schema=@Schema(ref="#/components/schemas/ApiError")))
    @ApiResponse(responseCode="401", description="JWT 인증 필요", content=@Content)
    @GetMapping("/lesson/{lessonIdx}")
    public ResponseEntity<?> getLessonDetail(@PathVariable("lessonIdx") Long lessonIdx) throws Exception {
        try {
            LessonDto lesson = lessonService.getLessonDetail(lessonIdx);
            return new ResponseEntity<>(lesson, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(errorResponseManager.getErrorResponse(e), HttpStatus.BAD_REQUEST);
        }
    }

    // 강의실 등록
    @Operation(summary="강의실 등록")
    @ApiResponse(responseCode="200", description="성공", content=@Content(mediaType="application/json", schema=@Schema(implementation=LessonDto.class)))
    @ApiResponse(responseCode="400", description="요청 처리 실패", content=@Content(mediaType="application/json", schema=@Schema(ref="#/components/schemas/ApiError")))
    @ApiResponse(responseCode="401", description="JWT 인증 필요", content=@Content)
    @PostMapping("/lesson/add")
    public ResponseEntity<?> addLesson(@RequestBody LessonDto lessonDto) throws Exception {
        try {
            LessonDto lesson = lessonService.addLesson(lessonDto);
            return new ResponseEntity<>(lesson, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(errorResponseManager.getErrorResponse(e), HttpStatus.BAD_REQUEST);
        }
    }

    // 강의실 수정
    @Operation(summary="강의실 수정")
    @ApiResponse(responseCode="200", description="성공", content=@Content(mediaType="application/json", schema=@Schema(implementation=LessonDto.class)))
    @ApiResponse(responseCode="400", description="요청 처리 실패", content=@Content(mediaType="application/json", schema=@Schema(ref="#/components/schemas/ApiError")))
    @ApiResponse(responseCode="401", description="JWT 인증 필요", content=@Content)
    @PutMapping("/lesson/edit/{lessonIdx}")
    public ResponseEntity<?> editLesson(@PathVariable("lessonIdx") Long lessonIdx, @RequestBody LessonDto lessonDto) throws Exception {
        try {
            LessonDto lesson = lessonService.editLesson(lessonIdx, lessonDto);
            return new ResponseEntity<>(lesson, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(errorResponseManager.getErrorResponse(e), HttpStatus.BAD_REQUEST);
        }
    }

    // 강의실 삭제
    @Operation(summary="강의실 삭제")
    @ApiResponse(responseCode="200", description="성공", content=@Content)
    @ApiResponse(responseCode="400", description="요청 처리 실패", content=@Content(mediaType="application/json", schema=@Schema(ref="#/components/schemas/ApiError")))
    @ApiResponse(responseCode="401", description="JWT 인증 필요", content=@Content)
    @DeleteMapping("/lesson/delete/{lessonIdx}")
    public ResponseEntity<?> deleteLesson(@PathVariable("lessonIdx") Long lessonIdx) throws Exception {
        try {
            lessonService.deleteLesson(lessonIdx);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(errorResponseManager.getErrorResponse(e), HttpStatus.BAD_REQUEST);
        }
    }

    // 강의실 검색
    @Operation(summary="강의실 검색")
    @ApiResponse(responseCode="200", description="성공", content=@Content(mediaType="application/json", schema=@Schema(implementation=SearchResultLessonDto.class)))
    @ApiResponse(responseCode="400", description="요청 처리 실패", content=@Content(mediaType="application/json", schema=@Schema(ref="#/components/schemas/ApiError")))
    @ApiResponse(responseCode="401", description="JWT 인증 필요", content=@Content)
    @GetMapping("/lesson/search")
    public ResponseEntity<?> searchProblem(@RequestParam(value="keyword") String keyword,
                                           @io.swagger.v3.oas.annotations.Parameter(description="페이지 번호 (1부터 시작)", example="1") @RequestParam(value="page") int page) throws Exception {
        try {
            SearchResultLessonDto lessons = lessonService.searchLesson(keyword,page);
            return new ResponseEntity<>(lessons, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(errorResponseManager.getErrorResponse(e), HttpStatus.BAD_REQUEST);
        }
    }

    // 수강 신청 (강의실 신청)
    @Operation(summary="수강 신청 (강의실 신청)")
    @ApiResponse(responseCode="200", description="성공", content=@Content(mediaType="application/json", schema=@Schema(implementation=StudentDto.class)))
    @ApiResponse(responseCode="400", description="요청 처리 실패", content=@Content(mediaType="application/json", schema=@Schema(ref="#/components/schemas/ApiError")))
    @ApiResponse(responseCode="401", description="JWT 인증 필요", content=@Content)
    @PostMapping("/lesson/{lessonIdx}/register")
    public ResponseEntity<?> registerForLesson(@PathVariable("lessonIdx") Long lessonIdx, @RequestBody StudentDto studentDto) throws Exception {
        try {
            StudentDto student = lessonService.registerForLesson(lessonIdx, studentDto);
            return new ResponseEntity<>(student, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(errorResponseManager.getErrorResponse(e), HttpStatus.BAD_REQUEST);
        }
    }

    // 수강 승인
    @Operation(summary="수강 승인")
    @ApiResponse(responseCode="200", description="성공", content=@Content(mediaType="application/json", schema=@Schema(implementation=StudentDto.class)))
    @ApiResponse(responseCode="400", description="요청 처리 실패", content=@Content(mediaType="application/json", schema=@Schema(ref="#/components/schemas/ApiError")))
    @ApiResponse(responseCode="401", description="JWT 인증 필요", content=@Content)
    @PutMapping("/lesson/accept/{studentId}")
    public ResponseEntity<?> acceptForLesson(@PathVariable("studentId") Long studentId) throws Exception {
        try {
            StudentDto student = lessonService.acceptForLesson(studentId);
            return new ResponseEntity<>(student, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(errorResponseManager.getErrorResponse(e), HttpStatus.BAD_REQUEST);
        }
    }

    // 수강 신청 거절
    @Operation(summary="수강 신청 거절")
    @ApiResponse(responseCode="200", description="성공", content=@Content)
    @ApiResponse(responseCode="400", description="요청 처리 실패", content=@Content(mediaType="application/json", schema=@Schema(ref="#/components/schemas/ApiError")))
    @ApiResponse(responseCode="401", description="JWT 인증 필요", content=@Content)
    @DeleteMapping("/lesson/refuse/{studentId}")
    public ResponseEntity<?> refuseForLesson(@PathVariable("studentId") Long studentId) throws Exception {
        try {
            lessonService.refuseForLesson(studentId);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(errorResponseManager.getErrorResponse(e), HttpStatus.BAD_REQUEST);
        }
    }

    // 수강생 정보 목록 조회
    @Operation(summary="수강생 정보 목록 조회")
    @ApiResponse(responseCode="200", description="성공", content=@Content(mediaType="application/json", array=@ArraySchema(schema=@Schema(implementation=StudentDataDto.class))))
    @ApiResponse(responseCode="400", description="요청 처리 실패", content=@Content(mediaType="application/json", schema=@Schema(ref="#/components/schemas/ApiError")))
    @ApiResponse(responseCode="401", description="JWT 인증 필요", content=@Content)
    @GetMapping("/lesson/{lessonId}/student")
    public ResponseEntity<?> getMyStudentList(@PathVariable("lessonId") Long lessonId) throws Exception {
        try {
            List<StudentDataDto> datas = lessonService.getMyStudentList(lessonId);
            return new ResponseEntity<>(datas, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(errorResponseManager.getErrorResponse(e), HttpStatus.BAD_REQUEST);
        }
    }

    // 수강 신청자 목록 조회
    @Operation(summary="수강 신청자 목록 조회")
    @ApiResponse(responseCode="200", description="성공", content=@Content(mediaType="application/json", array=@ArraySchema(schema=@Schema(implementation=StudentDataDto.class))))
    @ApiResponse(responseCode="400", description="요청 처리 실패", content=@Content(mediaType="application/json", schema=@Schema(ref="#/components/schemas/ApiError")))
    @ApiResponse(responseCode="401", description="JWT 인증 필요", content=@Content)
    @GetMapping("/lesson/{lessonId}/register")
    public ResponseEntity<?> getRegisterList(@PathVariable("lessonId") Long lessonId) throws Exception {
        try {
            List<StudentDataDto> datas = lessonService.getRegisterList(lessonId);
            return new ResponseEntity<>(datas, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(errorResponseManager.getErrorResponse(e), HttpStatus.BAD_REQUEST);
        }
    }

    // 내 수강 신청 목록 조회
    @Operation(summary="내 수강 신청 목록 조회")
    @ApiResponse(responseCode="200", description="성공", content=@Content(mediaType="application/json", array=@ArraySchema(schema=@Schema(implementation=StudentDto.class))))
    @ApiResponse(responseCode="400", description="요청 처리 실패", content=@Content(mediaType="application/json", schema=@Schema(ref="#/components/schemas/ApiError")))
    @ApiResponse(responseCode="401", description="JWT 인증 필요", content=@Content)
    @GetMapping("/myRegister")
    public ResponseEntity<?> getMyRegisterList() throws Exception {
        try {
            // 일단 임시 멤버
            List<StudentDto> lessons = lessonService.getMyRegisterList();
            return new ResponseEntity<>(lessons, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(errorResponseManager.getErrorResponse(e), HttpStatus.BAD_REQUEST);
        }
    }

    // 수강생 삭제
    @Operation(summary="수강생 삭제")
    @ApiResponse(responseCode="200", description="성공", content=@Content)
    @ApiResponse(responseCode="400", description="요청 처리 실패", content=@Content(mediaType="application/json", schema=@Schema(ref="#/components/schemas/ApiError")))
    @ApiResponse(responseCode="401", description="JWT 인증 필요", content=@Content)
    @DeleteMapping("/lesson/{lessonIdx}/student/delete/{studentId}")
    public ResponseEntity<?> deleteMyStudent(@PathVariable("lessonIdx") Long lessonIdx, @PathVariable("studentId") Long studentId) throws Exception {
        try {
            lessonService.deleteMyStudent(studentId);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(errorResponseManager.getErrorResponse(e), HttpStatus.BAD_REQUEST);
        }
    }

}
