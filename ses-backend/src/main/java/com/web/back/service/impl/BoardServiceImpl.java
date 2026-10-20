package com.web.back.service.impl;

import com.web.back.dto.CommentDto;
import com.web.back.dto.PostDto;
import com.web.back.dto.SearchResultBoardDto;
import com.web.back.model.*;
import com.web.back.repository.BoardRepository;
import com.web.back.repository.CommentRepository;
import com.web.back.repository.MemberRepository;
import com.web.back.repository.PostRepository;
import com.web.back.service.AccountService;
import com.web.back.service.BoardService;
import com.web.back.utils.AccountValidation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class BoardServiceImpl implements BoardService {
    @Autowired
    private PostRepository postRepository;

    @Autowired
    private BoardRepository boardRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private AccountValidation boardValidation;

    @Autowired
    private AccountService accountService;

    // 게시글 목록 조회
    @Override
    public List<PostDto> getPostList(Long boardId) throws Exception {
        List<Post> posts = postRepository.findAllByBoardId(boardId);
        List<PostDto> postDtos = new ArrayList<>();
        posts.forEach(s -> postDtos.add(PostDto.setFormat(s, posts.indexOf(s) + 1)));

        return postDtos;

    }

    // 게시글 상세 정보 조회
    @Override
    public PostDto getPostDetail(Long postId) throws Exception {
        Post post = postRepository.findById(postId).orElseThrow(() -> {
            return new IllegalArgumentException("해당 게시글을 찾을 수 없습니다.");
        });

        // 조회수 증가
        post.setViews(post.getViews() + 1);
        postRepository.save(post);

        return PostDto.setFormat(post);
    }

    // 게시글 등록
    @Override
    public PostDto registerPost(Long boardId, PostDto postDto) throws Exception {
        Board board = boardRepository.findById(boardId).orElseThrow(() -> {
            return new IllegalArgumentException("해당 게시판을 찾을 수 없습니다.");
        });

        Post post = new Post();
        post.setBoard(board);
        post.setTitle(postDto.getTitle());
        post.setContent(postDto.getContent());
        post.setUpload_time(LocalDateTime.now());
        post.setViews(0);
        Member writer = accountService.getAuthenticatedMember();
        post.setWriter(writer);

        postRepository.save(post);

        return PostDto.setFormat(post);
    }

    // 게시글 수정
    @Override
    public PostDto editPost(Long postId, PostDto postDto) throws Exception {
        Member authenticatedMember = accountService.getAuthenticatedMember();
        Long memberId = authenticatedMember.getId();

        Post post = postRepository.findById(postId).orElseThrow(() -> {
            return new IllegalArgumentException("해당 게시글을 찾을 수 없습니다.");
        });

        //작성자와 동일한지 검증
        if (!memberId.equals(post.getWriter().getId())) {
            throw new IllegalAccessException("작성자가 아닙니다.");
        }

        post.setTitle(postDto.getTitle());
        post.setContent(postDto.getContent());
        postRepository.save(post);

        return PostDto.setFormat(post);
    }

    // 게시글 삭제
    @Override
    public void deletePost(Long postId) throws Exception {
        Member authenticatedMember = accountService.getAuthenticatedMember();
        Long memberId = authenticatedMember.getId();

        Post post = postRepository.findById(postId).orElseThrow(() -> {
            return new IllegalArgumentException("해당 게시글을 찾을 수 없습니다.");
        });

        //작성자와 동일한지 검증
        if (!memberId.equals(post.getWriter().getId())) {
            throw new IllegalAccessException("작성자가 아닙니다.");
        }

        postRepository.deleteById(postId);
    }

    // 댓글 목록 조회
    @Override
    public List<CommentDto> getCommentList(Long postId) throws Exception {
        List<Comment> comments = commentRepository.findAllByPostId(postId);
        List<CommentDto> commentDtos = new ArrayList<>();
        comments.forEach(s -> commentDtos.add(CommentDto.setFormat(s)));

        return commentDtos;
    }

    // 댓글 작성
    @Override
    public CommentDto registerComment(Long postId, CommentDto commentDto) throws Exception {
        Comment comment = new Comment();
        // 게시글 조회
        Post post = postRepository.findById(postId).orElseThrow(() -> {
            return new IllegalArgumentException("해당 게시글을 찾을 수 없습니다.");
        });
        comment.setPost(post);
        comment.setContent(commentDto.getContent());
        comment.setUpload_time(LocalDateTime.now());

        Member writer = accountService.getAuthenticatedMember();
        comment.setWriter(writer);
        commentRepository.save(comment);

        return CommentDto.setFormat(comment);
    }

    // 댓글 수정
    @Override
    public CommentDto editComment(Long commentId, CommentDto commentDto) throws Exception {
        Member authenticatedMember = accountService.getAuthenticatedMember();
        Long memberId = authenticatedMember.getId();

        Comment comment = commentRepository.findById(commentId).orElseThrow(() -> {
            return new IllegalArgumentException("해당 댓글을 찾을 수 없습니다.");
        });

        // 작성자와 접속자가 동일한지 검증
        if (!memberId.equals(comment.getWriter().getId())) {
            throw new IllegalAccessException("작성자가 아닙니다.");
        }

        comment.setContent(commentDto.getContent());
        commentRepository.save(comment);
        return CommentDto.setFormat(comment);
    }

    // 댓글 삭제
    @Override
    public void deleteComment(Long commentId)throws Exception {
        Member authenticatedMember = accountService.getAuthenticatedMember();
        Long memberId = authenticatedMember.getId();

        Comment comment = commentRepository.findById(commentId).orElseThrow(() -> {
            return new IllegalArgumentException("해당 댓글을 찾을 수 없습니다.");
        });
        // 작성자와 접속자가 동일한지 검증
        if (!memberId.equals(comment.getWriter().getId())) {
            throw new IllegalAccessException("작성자가 아닙니다.");
        }
        commentRepository.deleteById(commentId);
    }

    // 게시글 검색
    @Override
    public SearchResultBoardDto searchPost(Long boardId, String keyword,int page) throws Exception {
        int pageSize = 5;
        int pageNum = page - 1;
        Pageable pageable = PageRequest.of(pageNum, pageSize);

        // 게시판별 게시글 검색
        Page<Post> postPage = postRepository.searchCategoryPost(boardId, keyword,pageable);
        List<Post> posts = postPage.getContent();
        int totalPage = postPage.getTotalPages();

        List<PostDto> postDtos = new ArrayList<>();
        posts.forEach(s -> postDtos.add(PostDto.setFormat(s, posts.indexOf(s)+1*(pageNum * pageSize)+1)));

        return new SearchResultBoardDto(postDtos, totalPage);
    }
}
