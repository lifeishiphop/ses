package com.web.back.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

// 게시글 + 댓글 리스트 (게시글 상세 조회 용도)
@Data
@Schema(description="PostDataDto 요청·응답 데이터. 사용 필드는 API 설명을 확인하세요.")
public class PostDataDto{
    PostDto post;
    List<CommentDto> comments;

    public PostDataDto(PostDto newPost, List<CommentDto> newComments) {
        post = newPost;
        comments = newComments;
    }
}