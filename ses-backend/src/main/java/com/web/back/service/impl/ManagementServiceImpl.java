package com.web.back.service.impl;

import com.web.back.dto.MemberDto;
import com.web.back.dto.ProblemDto;
import com.web.back.dto.SearchResultManageDto;
import com.web.back.model.Member;
import com.web.back.model.Problem;
import com.web.back.repository.MemberRepository;
import com.web.back.service.AccountService;
import com.web.back.service.ManagementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;


import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ManagementServiceImpl implements ManagementService {
    @Autowired
    private AccountService accountService;
    @Autowired
    private MemberRepository memberRepository;



    //관리자 입장 - 회원 전체 조회
    @Override
    public SearchResultManageDto getManageMemberList(int page) throws Exception {
        Member member = accountService.getAuthenticatedMember();
        Integer type = member.getIdentity();

        if (type != 0) {
            throw new IllegalAccessException("관리자가 아닙니다.");
        }
//        List<Member> members = memberRepository.findAll();
//
//        List<MemberDto> memberDtos = new ArrayList<>();
//        members.forEach(s -> memberDtos.add(MemberDto.setFormat(s)));
//
//        return memberDtos;
        int pageSize = 5;
        int pageNum = page - 1;
        Pageable pageable = PageRequest.of(pageNum, pageSize);

        Page<Member> memberPage = memberRepository.findAll(pageable);
        List<Member> members = memberPage.getContent();
        int totalPage = memberPage.getTotalPages();
        List<MemberDto> memberDtos = new ArrayList<>();
        members.forEach(s -> memberDtos.add(MemberDto.setFormat(s)));

        return new SearchResultManageDto(memberDtos,totalPage);
    }

}
