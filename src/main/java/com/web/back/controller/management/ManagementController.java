package com.web.back.controller.management;

import com.web.back.dto.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.Operation;
import com.web.back.service.ManagementService;
import com.web.back.utils.ErrorResponseManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name="관리")
@RestController
public class ManagementController {

    @Autowired
    private ErrorResponseManager errorResponseManager;

    @Autowired
    private ManagementService managementService;

    //관리자 입장 - 회원 전체 조회
    @Operation(summary="회원 목록 조회")
    @ApiResponse(responseCode="200", description="성공", content=@Content(mediaType="application/json", schema=@Schema(implementation=SearchResultManageDto.class)))
    @ApiResponse(responseCode="400", description="요청 처리 실패", content=@Content(mediaType="application/json", schema=@Schema(ref="#/components/schemas/ApiError")))
    @ApiResponse(responseCode="401", description="JWT 인증 필요", content=@Content)
    @GetMapping("/manage")
    public ResponseEntity<?> readManageMemberList(@io.swagger.v3.oas.annotations.Parameter(description="페이지 번호 (1부터 시작)", example="1") @RequestParam(value="page") int page) throws Exception {
        try {
            SearchResultManageDto members = managementService.getManageMemberList(page);
            return new ResponseEntity<>(members, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(errorResponseManager.getErrorResponse(e), HttpStatus.BAD_REQUEST);
        }
    }
}
