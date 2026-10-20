package com.web.back.service;

import com.web.back.dto.CommentDto;
import com.web.back.dto.PostDto;
import com.web.back.dto.SearchDataDto;
import com.web.back.dto.SearchResultBoardDto;

import java.util.List;

public interface BoardService {

    List<PostDto> getPostList(Long boardId) throws Exception;

    PostDto getPostDetail(Long postId) throws Exception;

    PostDto registerPost(Long boardId, PostDto postDto) throws Exception;

    PostDto editPost(Long postId, PostDto postDto) throws Exception;

    void deletePost(Long postId) throws Exception;

    List<CommentDto> getCommentList(Long postId) throws Exception;

    CommentDto registerComment(Long postId, CommentDto commentDto) throws Exception;

    CommentDto editComment(Long commentId, CommentDto commentDto) throws Exception;

    void deleteComment(Long commentId)throws Exception;

    SearchResultBoardDto searchPost(Long boardId, String keyword, int page) throws Exception;

//    List<PostDto> searchPost(Long boardId, String keyword) throws Exception;

}
