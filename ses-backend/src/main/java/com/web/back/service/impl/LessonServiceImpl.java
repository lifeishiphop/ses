package com.web.back.service.impl;

import com.web.back.dto.*;
import com.web.back.model.Lesson;
import com.web.back.model.Member;
import com.web.back.model.Problem;
import com.web.back.model.Student;
import com.web.back.repository.AssignmentRepository;
import com.web.back.repository.LessonRepository;
import com.web.back.repository.MemberRepository;
import com.web.back.repository.StudentRepository;
import com.web.back.service.AccountService;
import com.web.back.service.LessonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;


import java.util.ArrayList;
import java.util.List;

@Service
public class LessonServiceImpl implements LessonService {

    @Autowired
    private LessonRepository lessonRepository;
    @Autowired
    private AssignmentRepository assignmentRepository;
    @Autowired
    private StudentRepository studentRepository;
    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private AccountService accountService;

    // 강의실 목록 조회
    @Override
    public List<LessonDto> getLessonList() throws Exception {
        List<Lesson> lessons = lessonRepository.findAll();
        List<LessonDto> lessonDtos = new ArrayList<>();
        lessons.forEach(s -> lessonDtos.add(LessonDto.setFormat(s)));

        return lessonDtos;
    }

    // 내 강의실 목록 조회
    @Override
    public List<LessonDto> getMyLessonList() throws Exception {
        List<Lesson> lessons;
        Member member = accountService.getAuthenticatedMember();
        Integer type = member.getIdentity();
        if (type == 1) {
            // 학생이라면
            // Student에서 accept 된 거만 뽑아서 출력하기
            lessons = studentRepository.findAllMyLessonS(member.getId());
        }

        else if (type == 2){
            // 강사(교수)라면
            // lesson에서 강사 ID 검색해서 목록 출력
            lessons = lessonRepository.findAllByMemberId(member.getId());
        }
        else {
            throw new Exception("관리자의 요청입니다.");
        }

        List<LessonDto> lessonDtos = new ArrayList<>();
        lessons.forEach(s -> lessonDtos.add(LessonDto.setFormat(s, lessons.indexOf(s) + 1)));

        return lessonDtos;
    }

    // 강의실 상세 조회
    @Override
    public LessonDto getLessonDetail(Long lessonId) throws Exception {
        Lesson lesson = lessonRepository.findById(lessonId).orElseThrow(() -> {
            return new IllegalArgumentException("해당 강의실을 찾을 수 없습니다.");
        });

        LessonDto lessonDto = LessonDto.setFormat(lesson);
        return lessonDto;
    }

    // 강의실 등록
    @Override
    public LessonDto addLesson(LessonDto lessonDto) throws Exception {
        Member member = accountService.getAuthenticatedMember();

        Lesson lesson = new Lesson();
        lesson.setName(lessonDto.getName());
        lesson.setMember(member);
        lesson.setSemester(lessonDto.getSemester());
        lesson.setYear(lessonDto.getYear());

        lessonRepository.save(lesson);

        return LessonDto.setFormat(lesson);
    }

    // 강의실 수정
    @Override
    public LessonDto editLesson(Long lessonId, LessonDto lessonDto) throws Exception {
        Lesson lesson = lessonRepository.findById(lessonId).orElseThrow(() -> {
            return new IllegalArgumentException("해당 강의실을 찾을 수 없습니다.");
        });
        lesson.setName(lessonDto.getName());
//        lesson.setMember(memberRepository.findById(lessonDto.getOwner_id()).orElseThrow(() -> {
//            return new IllegalArgumentException("해당 회원을 찾을 수 없습니다.");
//        }));
        lesson.setSemester(lessonDto.getSemester());
        lesson.setYear(lessonDto.getYear());

        lessonRepository.save(lesson);

        return LessonDto.setFormat(lesson);
    }

    // 강의실 삭제
    @Override
    public void deleteLesson(Long lessonId) throws Exception {
        Lesson lesson = lessonRepository.findById(lessonId).orElseThrow(() -> {
            return new IllegalArgumentException("해당 강의실을 찾을 수 없습니다.");
        });
        lessonRepository.delete(lesson);
    }

    // 강의실 검색
    @Override
    public SearchResultLessonDto searchLesson(String keyword, int page) throws Exception {
        int pageSize = 5;
        int pageNum = page - 1;
        Pageable pageable = PageRequest.of(pageNum, pageSize);

        Page<Lesson> lessonPage = lessonRepository.findAllSearch(keyword,pageable);
//        Page<Lesson> lessonPage =  lessonRepository.findAllSearch(keyword,pageable);
        List<Lesson> lessons = lessonPage.getContent();
        int totalPage = lessonPage.getTotalPages();
        List<LessonDto> lessonDtos = new ArrayList<>();


        lessons.forEach(s -> lessonDtos.add(LessonDto.setFormat(s, lessons.indexOf(s)+1*(pageNum * pageSize)+1)));

        return new SearchResultLessonDto( lessonDtos, totalPage);
    }
    /*@Override
    public List<LessonDto> searchLesson(String keyword) throws Exception {
        List<Lesson> lessons = lessonRepository.findAllSearch(keyword);
        List<LessonDto> lessonDtos = new ArrayList<>();
        lessons.forEach(s -> lessonDtos.add(LessonDto.setFormat(s, lessons.indexOf(s) + 1)));

        return lessonDtos;
    }*/

    // 수강 신청 (강의실 신청)
    @Override
    public StudentDto registerForLesson(Long lessonId, StudentDto studentDto) throws Exception {
        Student student = new Student();
        Lesson lesson = lessonRepository.findById(lessonId).orElseThrow(() -> {
            return new IllegalArgumentException("해당 강의실을 찾을 수 없습니다.");
        });

        Member member = accountService.getAuthenticatedMember();
        if(studentRepository.findAlreadyExist(lesson.getId(), member.getId()).isEmpty()) {
            student.setLesson(lesson);

            student.setMember(member);
//        student.setAccept(studentDto.getAccept());
            student.setAccept(0);
            studentRepository.save(student);
        }
        else {
            throw new IllegalArgumentException("이미 수강신청 하였습니다.");
        }

        return StudentDto.setFormat(student);
    }

    // 수강 승인
    @Override
    public StudentDto acceptForLesson(Long studentId) throws Exception {
        Student student = studentRepository.findById(studentId).orElseThrow(() -> {
            return new IllegalArgumentException("해당 신청 정보를 찾을 수 없습니다.");
        });
        student.setAccept(1);
        studentRepository.save(student);

        return StudentDto.setFormat(student);
    }

    // 수강 신청 거절
    @Override
    public void refuseForLesson(Long studentId) throws Exception {
        Student student = studentRepository.findById(studentId).orElseThrow(() -> {
            return new IllegalArgumentException("해당 신청 정보를 찾을 수 없습니다.");
        });
        studentRepository.delete(student);
    }

    // 수강생 정보 목록 조회
    @Override
    public List<StudentDataDto> getMyStudentList(Long lessonId) throws Exception {
        List<Student> students = studentRepository.findMyStudent(lessonId);
        List<StudentDataDto> result = new ArrayList<>();

        students.forEach(s -> {
            StudentDataDto resultData = new StudentDataDto();
            Member member = memberRepository.findByNumber(s.getMember().getNumber()).orElseThrow();
            resultData.setId(s.getId());
            resultData.setName(member.getName());
            resultData.setNumber(member.getNumber());
            result.add(resultData);
            // 몇 개 중에 몇 개 과제
        });

        return result;
    }

    // 수강 신청자 목록 조회
    @Override
    public List<StudentDataDto> getRegisterList(Long lessonId) throws Exception {
        List<Student> students = studentRepository.findMyLessonRegister(lessonId);
        List<StudentDataDto> result = new ArrayList<>();

        students.forEach(s -> {
            StudentDataDto resultData = new StudentDataDto();
            Member member = memberRepository.findByNumber(s.getMember().getNumber()).orElseThrow();
            resultData.setId(s.getId());
            resultData.setName(member.getName());
            resultData.setNumber(member.getNumber());
            result.add(resultData);
        });

        return result;
    }

    // 내 수강 신청 목록 조회
    @Override
    public List<StudentDto> getMyRegisterList() throws Exception {
        Member member = accountService.getAuthenticatedMember();

        List<Student> students = studentRepository.findMyRegister(member.getId());
        List<StudentDto> studentDtos = new ArrayList<>();
        students.forEach(s -> studentDtos.add(StudentDto.setFormat(s)));

        return studentDtos;
    }

    // 수강생 삭제
    @Override
    public void deleteMyStudent(Long studentId) throws Exception {
        Student student = studentRepository.findById(studentId).orElseThrow(() -> {
            return new IllegalArgumentException("해당 회원을 찾을 수 없습니다.");
        });;
        studentRepository.delete(student);
    }
}
