package com.web.back.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor

@Schema(description="SearchResultBoardDto 요청·응답 데이터. 사용 필드는 API 설명을 확인하세요.")
public class SearchResultBoardDto {

    private List<PostDto> postDtos;
    @Schema(description="전체 페이지 수. 요청 page는 1부터 시작", example="1")
    private int totalPage;

    public SearchResultBoardDto(List<PostDto> postDtos, int totalPage) {
        this.postDtos = postDtos;
        this.totalPage = totalPage;
    }

}