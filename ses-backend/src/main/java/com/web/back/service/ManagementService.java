package com.web.back.service;

import com.web.back.dto.MemberDto;
import com.web.back.dto.SearchResultManageDto;

import java.util.List;

public interface ManagementService {

//    List<MemberDto> getManageMemberList(int page) throws Exception;
    SearchResultManageDto getManageMemberList(int page) throws Exception;
}
