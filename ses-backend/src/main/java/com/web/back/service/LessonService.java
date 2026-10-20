package com.web.back.service;

import com.web.back.dto.*;

import java.util.List;

public interface LessonService {
    List<LessonDto> getLessonList() throws Exception;
    List<LessonDto> getMyLessonList() throws Exception;
    LessonDto getLessonDetail(Long lessonId) throws Exception;
    LessonDto addLesson(LessonDto lessonDto) throws Exception;
    LessonDto editLesson(Long lessonId, LessonDto lessonDto) throws Exception;
    void deleteLesson(Long lessonId) throws Exception;
    /*List<LessonDto> searchLesson(String keyword) throws Exception;*/
    SearchResultLessonDto searchLesson(String keyword, int page) throws Exception;

    StudentDto registerForLesson(Long lessonId, StudentDto studentDto) throws Exception;
    StudentDto acceptForLesson(Long studentId) throws Exception;
    void refuseForLesson(Long studentId) throws Exception;
    List<StudentDataDto> getMyStudentList(Long lessonId) throws Exception;
    List<StudentDataDto> getRegisterList(Long lessonId) throws Exception;
    List<StudentDto> getMyRegisterList() throws Exception;
    void deleteMyStudent(Long studentId) throws Exception;
}
