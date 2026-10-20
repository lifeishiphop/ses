package com.web.back.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import com.web.back.model.Post;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@Schema(description="PostDto 요청·응답 데이터. 사용 필드는 API 설명을 확인하세요.")
public class PostDto {
    @Schema(description="데이터 ID", example="1")
    private Long id;
    @Schema(description="목록의 표시 순번", example="1")
    private int number;
    @Schema(description="제목", example="반복문 연습")
    private String title;
    @Schema(description="본문", example="내용을 입력하세요.")
    private String content;
    @Schema(description="등록 일시", example="2026-10-21T09:00:00")
    private LocalDateTime upload_time;
    @Schema(description="조회수", example="0")
    private Integer views;
    @Schema(description="작성자 ID", example="1")
    private Long writer_id;
    @Schema(description="작성자 이름", example="홍길동")
    private String writer;

    public PostDto() {

    }

    public PostDto(Long id, String title, String content, LocalDateTime upload_time, Integer views, Long writer_id, String writer) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.upload_time = upload_time;
        this.views = views;
        this.writer_id = writer_id;
        this.writer = writer;
    }

    public static PostDto setFormat(Post post) {
        PostDto postDto = new PostDto(
                post.getId(),
                post.getTitle(),
                post.getContent(),
                post.getUpload_time(),
                post.getViews(),
                post.getWriter().getId(),
                post.getWriter().getName()
        );
        return postDto;
    }

    public static PostDto setFormat(Post post, int number) {
        PostDto postDto = new PostDto(
                post.getId(),
                post.getTitle(),
                post.getContent(),
                post.getUpload_time(),
                post.getViews(),
                post.getWriter().getId(),
                post.getWriter().getName()
        );
        postDto.setNumber(number);
        return postDto;
    }
}
