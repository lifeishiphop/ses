package com.web.back.repository;

import com.web.back.model.Lesson;
import com.web.back.model.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {
    List<Student> findAllByMemberId(Long memberId);

    Student findByMemberId(Long memberId);
    @Query(value = "SELECT s FROM Student s WHERE s.lesson.id = :lessonId AND s.accept = 1")
    List<Student> findMyStudent(Long lessonId); // 내 강의실의 수강생 목록
    @Query(value = "SELECT s FROM Student s WHERE s.lesson.id = :lessonId AND s.accept = 0")
    List<Student> findMyLessonRegister(Long lessonId); // 내 강의실의 수강 신청자 목록
    @Query(value = "SELECT s FROM Student s WHERE s.lesson.id = :lessonId AND s.member.id = :memberId")
    List<Student> findAlreadyExist(Long lessonId, Long memberId); // 이미 수강신청 했는지 확인용
    @Query(value = "SELECT s FROM Student s WHERE s.accept = 0 AND s.member.id = :memberId")
    List<Student> findMyRegister(Long memberId); // 내 수강신청 목록
    @Query(value = "SELECT s.lesson FROM Student s WHERE s.accept = 1 AND s.member.id = :stdId")
    List<Lesson> findAllMyLessonS(Long stdId); // 학생 기준 내 강의실 목록
}
