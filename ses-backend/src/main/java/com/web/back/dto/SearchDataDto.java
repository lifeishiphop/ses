package com.web.back.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description="SearchDataDto 요청·응답 데이터. 사용 필드는 API 설명을 확인하세요.")
public class SearchDataDto {
    @Schema(description="검색어", example="반복문")
    String keyword;
    List<Object> data;


}
