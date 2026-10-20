package com.web.back.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import com.web.back.model.Member;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description="MemberDto 요청·응답 데이터. 사용 필드는 API 설명을 확인하세요.")
public class MemberDto {
    @Schema(description="데이터 ID", example="1")
    private Long id;
    @Schema(description="학번 (로그인의 id로 사용)", example="20260001")
    private String number;
    @Schema(description="비밀번호", example="SesTest123!")
    private String password;
    @Schema(description="새 비밀번호", example="NewSes123!")
    private String newPassword;
    @Schema(description="비밀번호 확인", example="SesTest123!")
    private String passwordCheck;
    @Schema(description="새 비밀번호 확인", example="NewSes123!")
    private String newPasswordCheck;
    @Schema(description="이메일 (회원가입은 kumoh.ac.kr 도메인)", example="s1@kumoh.ac.kr")
    private String email;
    @Schema(description="회원 DTO의 이메일 인증번호 필드. 현재 가입 로직에서 검증하지 않음", example="aB12cD34")
    private String emailAuthenticationNumber;
    @Schema(description="이름", example="홍길동")
    private String name;
    @Schema(description="기존 역할 구분: 0=관리자, 1=학생, 2=강사", example="1")
    private Integer identity;

    /*public MemberDto setFormat() {
        return new MemberDto();
    }*/

    public MemberDto( String number,  String email, String name, Integer identity) {
        this.number = number;
        this.email = email;
        this.name = name;
        this.identity = identity;
    }

    public static MemberDto setFormat(Member member){
        MemberDto memberDto = new MemberDto(
                member.getNumber(),
                member.getEmail(),
                member.getName(),
                member.getIdentity()
        );
        memberDto.setId(member.getId());

        return memberDto;
    }




}
