package com.web.back.controller.account;

import com.web.back.dto.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.Operation;
import java.util.Objects;

import com.web.back.model.Member;
import com.web.back.security.JwtTokenProvider;
import com.web.back.utils.ErrorResponseManager;
import jakarta.servlet.http.HttpServletResponse;

import com.web.back.service.AccountService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@Tag(name="계정")
@RestController
public class AccountController {
    @Value("${jwt.http.request.header}")
    private String tokenHeader;
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private JwtTokenProvider jwtTokenProvider;
    @Autowired
    private AccountService accountService;
    @Autowired
    private ErrorResponseManager errorResponseManager;

    // 로그인
    @Operation(summary="로그인", description="id에는 학번(number)을 입력합니다. 응답의 token을 Swagger Authorize에 입력합니다.")
    @SecurityRequirements
    @ApiResponse(responseCode="200", description="성공", content=@Content(mediaType="application/json", schema=@Schema(implementation=TokenDto.class)))
    @ApiResponse(responseCode="401", description="요청 처리 실패", content=@Content(mediaType="application/json", schema=@Schema(ref="#/components/schemas/ApiError")))
    @RequestMapping(value = "${jwt.get.token.url}", method = RequestMethod.POST)
    public ResponseEntity<?> login(@RequestBody LoginDto authenticationRequest, HttpServletResponse response) throws Exception {
        try {
            authenticate(authenticationRequest.getId(), authenticationRequest.getPassword());
            final UserDetails userDetails = accountService.loadUserByUsername(authenticationRequest.getId());
            final String token = jwtTokenProvider.generateToken(userDetails);
            TokenDto tokenDto = new TokenDto();
            tokenDto.setToken(token);
            tokenDto.setMemberDto(MemberDto.setFormat((Member) userDetails));
            return new ResponseEntity<>(tokenDto, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(errorResponseManager.getErrorResponse(e), HttpStatus.UNAUTHORIZED);
        }
    }

    // 로그아웃
    @Operation(summary="로그아웃", description="현재는 200 응답만 반환하며 이미 발급된 JWT를 폐기하지 않습니다.")
    @SecurityRequirements
    @ApiResponse(responseCode="200", description="성공", content=@Content)
    @PostMapping("/logoutAccount")
    public ResponseEntity<?> logout(HttpServletResponse response) throws Exception {
        // 토큰 만료 시켜야 하나?
        return new ResponseEntity<>(HttpStatus.OK);
    }

    // 회원가입
    @Operation(summary="회원가입", description="number(학번), password, passwordCheck, email, name, identity를 입력합니다. 이메일 도메인은 kumoh.ac.kr이며 비밀번호는 영문·숫자·특수문자를 포함한 8~30자입니다. identity는 기존 구현상 0=관리자, 1=학생, 2=강사이며 현재 가입 요청 값을 사용합니다.")
    @SecurityRequirements
    @ApiResponse(responseCode="200", description="성공", content=@Content(mediaType="application/json", schema=@Schema(implementation=MemberDto.class)))
    @ApiResponse(responseCode="400", description="요청 처리 실패", content=@Content(mediaType="application/json", schema=@Schema(ref="#/components/schemas/ApiError")))
    @PostMapping("/signup")
    public ResponseEntity<?> signUp(@RequestBody MemberDto memberDto) throws Exception {
        try {
            MemberDto signUpMemberDto = accountService.signUp(memberDto);
            return new ResponseEntity<>(signUpMemberDto, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(errorResponseManager.getErrorResponse(e), HttpStatus.BAD_REQUEST);
        }
    }

    // 이메일 인증번호 전송 요청
    @Operation(summary="이메일 인증번호 전송 요청", description="SMTP 서버 설정이 필요합니다. 현재 구현은 인증번호를 응답에도 포함하고 서버 메모리에 한 개만 보관합니다.")
    @SecurityRequirements
    @ApiResponse(responseCode="200", description="성공", content=@Content(mediaType="application/json", schema=@Schema(implementation=EmailDto.class)))
    @ApiResponse(responseCode="400", description="요청 처리 실패", content=@Content(mediaType="application/json", schema=@Schema(ref="#/components/schemas/ApiError")))
    @PostMapping("/mail")
    public ResponseEntity<?> mailSend(@RequestBody EmailDto emailDto){
        try {
            EmailDto signUpMemberDto = accountService.sendEmailVerificationCode(emailDto);
            return new ResponseEntity<>(signUpMemberDto, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(errorResponseManager.getErrorResponse(e), HttpStatus.BAD_REQUEST);
        }
    }

    // 이메일 인증번호 확인 요청
    @Operation(summary="이메일 인증번호 확인 요청", description="현재 구현은 마지막으로 발급한 인증번호 한 개와 enteredVerificationCode를 비교합니다.")
    @SecurityRequirements
    @ApiResponse(responseCode="200", description="성공", content=@Content(mediaType="application/json", schema=@Schema(implementation=EmailDto.class)))
    @ApiResponse(responseCode="400", description="요청 처리 실패", content=@Content(mediaType="application/json", schema=@Schema(ref="#/components/schemas/ApiError")))
    @PostMapping("/mailVerify")
    public ResponseEntity<?> mailVerify(@RequestBody EmailDto emailDto){
        try {
            EmailDto signUpMemberDto = accountService.validateEmailVerificationCode(emailDto);
            return new ResponseEntity<>(signUpMemberDto, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(errorResponseManager.getErrorResponse(e), HttpStatus.BAD_REQUEST);
        }
    }

    // 비밀번호 변경
    @Operation(summary="비밀번호 변경", description="현재 password와 newPassword, newPasswordCheck를 입력합니다.")
    @ApiResponse(responseCode="200", description="성공", content=@Content(mediaType="application/json", schema=@Schema(implementation=MemberDto.class)))
    @ApiResponse(responseCode="400", description="요청 처리 실패", content=@Content(mediaType="application/json", schema=@Schema(ref="#/components/schemas/ApiError")))
    @ApiResponse(responseCode="401", description="JWT 인증 필요", content=@Content)
    @PutMapping("/changePassword")
    public ResponseEntity<?> changePassword(@RequestBody MemberDto memberDto) throws Exception {
        try {
            MemberDto newMemberDto = accountService.changePassword(memberDto);
            return new ResponseEntity<>(newMemberDto, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(errorResponseManager.getErrorResponse(e), HttpStatus.BAD_REQUEST);
        }
    }

    // 비밀번호 찾기
    @Operation(summary="비밀번호 찾기", description="email, newPassword, newPasswordCheck를 입력합니다. 현재 구현에는 이메일 인증 결과를 검사하는 로직이 없어 로컬 확인 용도로만 사용하세요.")
    @SecurityRequirements
    @ApiResponse(responseCode="200", description="성공", content=@Content(mediaType="application/json", schema=@Schema(implementation=MemberDto.class)))
    @ApiResponse(responseCode="400", description="요청 처리 실패", content=@Content(mediaType="application/json", schema=@Schema(ref="#/components/schemas/ApiError")))
    @PutMapping("/findPassword")
    public ResponseEntity<?> findPassword(@RequestBody MemberDto memberDto) throws Exception {
        try {
            MemberDto newMemberDto = accountService.findPassword(memberDto);
            return new ResponseEntity<>(newMemberDto, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(errorResponseManager.getErrorResponse(e), HttpStatus.BAD_REQUEST);
        }
    }

    // 내 정보 조회
    @Operation(summary="내 정보 조회 (미구현)", description="현재 구현은 MemberDto 요청 본문을 받고 빈 200 응답을 반환합니다. 회원 정보 조회는 아직 구현되지 않았습니다. GET 본문은 Swagger UI에서 전송할 수 없습니다.")
    @ApiResponse(responseCode="200", description="성공", content=@Content)
    @ApiResponse(responseCode="400", description="요청 처리 실패", content=@Content(mediaType="application/json", schema=@Schema(ref="#/components/schemas/ApiError")))
    @ApiResponse(responseCode="401", description="JWT 인증 필요", content=@Content)
    @GetMapping("/my")
    public ResponseEntity<?> myPage(@RequestBody MemberDto memberDto) throws Exception {
        try {
            // 받아오도록
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(errorResponseManager.getErrorResponse(e), HttpStatus.BAD_REQUEST);
        }
    }

    // 키 인증 과정
    private void authenticate(String id, String password) {
        Objects.requireNonNull(id);
        Objects.requireNonNull(password);
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(id, password));
        } catch (BadCredentialsException e) {
            throw new BadCredentialsException("ID OR PW IS NOT CORRECT.", e);
        } catch (DisabledException e) {
            throw new DisabledException("DISABLED MEMBER", e);
        }
    }

    //    // 회원 탈퇴
//    @PutMapping("/deleteAccount")
//    public ResponseEntity<?> deleteAccount(HttpServletResponse response) throws Exception {
//        try {
//            accountService.deleteAccount();
//            return new ResponseEntity<>(HttpStatus.OK);
//        } catch (Exception e) {
//            return new ResponseEntity<>(errorResponseManager.makeErrorResponse(e), HttpStatus.BAD_REQUEST);
//        }
//    }


}