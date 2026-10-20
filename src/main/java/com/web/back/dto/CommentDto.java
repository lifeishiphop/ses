package com.web.back.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import com.web.back.model.Comment;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description="CommentDto 요청·응답 데이터. 사용 필드는 API 설명을 확인하세요.")
public class CommentDto {
    @Schema(description="데이터 ID", example="1")
    private Long id;
    @Schema(description="본문", example="내용을 입력하세요.")
    private String content;
    @Schema(description="등록 일시", example="2026-10-21T09:00:00")
    private LocalDateTime upload_time;
    @Schema(description="작성자 이름", example="홍길동")
    private String writer;

    public static CommentDto setFormat(Comment comment) {
        CommentDto commentDto = new CommentDto(
                comment.getId(),
                comment.getContent(),
                comment.getUpload_time(),
                comment.getWriter().getName()
        );
        return commentDto;
    }
}
