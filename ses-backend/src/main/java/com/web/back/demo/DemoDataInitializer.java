package com.web.back.demo;

import com.web.back.model.*;
import jakarta.persistence.EntityManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Component
@Profile("local & demo")
public class DemoDataInitializer implements ApplicationRunner {
    private static final Logger log = LoggerFactory.getLogger(DemoDataInitializer.class);
    private static final String BOARD = "[DEMO v1] 학습 게시판";
    private static final String PASSWORD = "SesDemo123!";
    private final EntityManager em;
    private final PasswordEncoder encoder;

    public DemoDataInitializer(EntityManager em, PasswordEncoder encoder) {
        this.em = em;
        this.encoder = encoder;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        List<Board> existing = em.createQuery("select b from Board b where b.category = :category", Board.class)
                .setParameter("category", BOARD).getResultList();
        if (!existing.isEmpty()) {
            log.info("[SES_DEMO] Existing demo data preserved. VITE_BOARD_IDS={}", existing.get(0).getId());
            return;
        }
        // Persist the entire fixture in one transaction; the board also marks successful initialization.
        Member student = member("20990001", "demo1@kumoh.ac.kr", "김서연(데모)", 1);
        Member peer = member("20990002", "demo2@kumoh.ac.kr", "박지민(데모)", 1);
        Member teacher = member("20999901", "demot@kumoh.ac.kr", "이정훈(데모)", 2);
        member("20999999", "demoa@kumoh.ac.kr", "운영자(데모)", 0);
        LocalDate today = LocalDate.now();
        Lesson structures = lesson("자료구조 · 데모", teacher, today);
        Lesson java = lesson("객체지향 프로그래밍 · 데모", teacher, today);
        Lesson algorithms = lesson("알고리즘 · 데모", teacher, today);
        Lesson software = lesson("소프트웨어공학 · 데모", teacher, today);
        for (Lesson lesson : List.of(structures, java, algorithms)) enrollment(student, lesson, 1);
        enrollment(student, software, 0);
        enrollment(peer, structures, 1);
        enrollment(peer, java, 0);

        Problem sum = problem("두 정수의 합", "두 정수 A와 B를 입력받아 합을 출력하세요.", "두 정수 A와 B (-100 ≤ A, B ≤ 100)", "A와 B의 합", 0, teacher);
        testcase(sum, null, "1 2", "3");
        testcase(sum, null, "-5 8", "3");
        testcase(sum, null, "0 0", "0");
        Problem maximum = problem("세 정수 중 최댓값", "세 정수 중 가장 큰 값을 출력하세요.", "공백으로 구분된 세 정수", "가장 큰 정수 하나", 0, teacher);
        testcase(maximum, null, "1 7 3", "7");
        testcase(maximum, null, "-4 -1 -9", "-1");
        Problem reverse = problem("문자열 뒤집기", "공백이 없는 문자열을 거꾸로 출력하세요.", "영문 소문자 문자열 (길이 1~100)", "뒤집은 문자열", 1, teacher);
        testcase(reverse, null, "hello", "olleh");
        testcase(reverse, null, "ses", "ses");
        Problem loop = problem("1부터 N까지의 합", "1부터 N까지 모든 정수의 합을 출력하세요.", "정수 N (1 ≤ N ≤ 1000)", "1부터 N까지의 합", 2, teacher);
        testcase(loop, null, "5", "15");
        testcase(loop, null, "100", "5050");
        Problem even = problem("짝수와 홀수", "주어진 정수가 짝수이면 EVEN, 홀수이면 ODD를 출력하세요.", "정수 N (0 ≤ N ≤ 10000)", "EVEN 또는 ODD", 0, teacher);
        testcase(even, null, "4", "EVEN");
        testcase(even, null, "7", "ODD");
        Problem count = problem("배열에서 양수 세기", "배열에 포함된 양수의 개수를 출력하세요. 0은 양수가 아닙니다.", "첫 줄에 N, 다음 줄에 N개의 정수", "양수의 개수", 0, teacher);
        testcase(count, null, "5\n1 -2 0 3 4", "3");
        testcase(count, null, "3\n-1 0 -2", "0");
        submission(student, sum, null, 1, JAVA_SUM, 100);
        submission(student, maximum, null, 2, "// 데모용 오답 제출\n#include <iostream>\nint main() { std::cout << 0; }", 0);

        Assignment list = assignment("연결 리스트의 원소 합계 · 데모", structures, today.plusDays(1), 0,
                "N개의 원소를 읽어 합계를 출력하세요. 연결 리스트에 저장한 뒤 순회하는 방법으로 구현해 보세요.", "첫 줄에 N (1~100), 다음 줄에 N개의 정수", "모든 원소의 합");
        testcase(null, list, "3\n1 2 3", "6");
        testcase(null, list, "4\n-1 2 -3 4", "2");
        Assignment classwork = assignment("클래스와 메서드: 사각형 넓이 · 데모", java, today.plusDays(3), 1,
                "너비와 높이를 저장하는 클래스를 만들고 넓이를 출력하세요.", "너비와 높이 (각 1~100)", "사각형의 넓이");
        testcase(null, classwork, "3 4", "12");
        testcase(null, classwork, "5 5", "25");
        Assignment search = assignment("이진 탐색: 값 찾기 · 데모", algorithms, today.plusDays(7), 0,
                "오름차순 배열에서 목표 값의 위치를 찾으세요. 위치는 0부터 시작하며 없으면 -1을 출력합니다.", "N과 목표 값, 다음 줄에 N개의 정렬된 정수", "목표 값의 위치 또는 -1");
        testcase(null, search, "5 7\n1 3 5 7 9", "3");
        testcase(null, search, "3 4\n1 2 3", "-1");
        Assignment done = assignment("입출력 연습: 두 수의 합 · 데모", java, today.plusDays(5), 0,
                "두 정수를 읽고 합을 출력하세요. 데모 학생에게는 제출 완료 상태가 준비되어 있습니다.", "두 정수", "두 정수의 합");
        testcase(null, done, "1 2", "3");
        submission(student, null, done, 1, JAVA_SUM, 100);
        submission(peer, null, list, 2, "// 화면 확인용 데모 제출\n#include <iostream>\nint main() { int n, x, sum = 0; std::cin >> n; while(n--) { std::cin >> x; sum += x; } std::cout << sum; }", 100);
        Assignment expired = assignment("복습: 짝수 판별 · 데모", structures, today.minusDays(1), 0,
                "데모용 마감 과제입니다. 제출 버튼의 마감 상태를 확인할 수 있습니다.", "정수 N", "EVEN 또는 ODD");
        testcase(null, expired, "2", "EVEN");

        Board board = new Board();
        board.setCategory(BOARD);
        em.persist(board);
        Post welcome = post(board, teacher, "[데모] S.E.S 학습 공간에 오신 것을 환영합니다",
                "이 게시판은 화면과 기능을 둘러보기 위한 테스트 데이터입니다.\n\n학생 계정에서는 문제 풀이와 과제 제출, 수강 신청을 확인해 보세요.\n강사 계정에서는 과제 만들기, 테스트케이스 관리, 수강 승인과 점수 수정을 확인할 수 있습니다.\n\n미리 저장된 제출 점수는 화면 확인용 예시입니다. 새 코드를 실행하거나 제출하려면 Docker 채점 환경이 필요합니다.");
        comment(welcome, student, "학습 홈에서 과제 마감일을 한 번에 확인할 수 있어 편리하네요!");
        Post question = post(board, student, "[데모] 연결 리스트에서 마지막 노드를 처리하는 방법이 궁금해요",
                "연결 리스트를 순회할 때 마지막 원소를 놓치는 경우가 있습니다.\n반복문의 종료 조건을 어떻게 잡으면 좋을까요?\n\n직접 작성한 글과 댓글의 수정·삭제 버튼도 확인해 보세요.");
        comment(question, teacher, "현재 노드가 null인지 확인하고, 원소를 처리한 뒤 다음 노드로 이동해 보세요.");
        comment(question, peer, "원소가 하나인 경우도 테스트해 보면 도움이 됩니다.");
        post(board, peer, "[데모] Java 입력 처리: Scanner 사용 팁", "정수는 nextInt(), 공백이 없는 문자열은 next()로 읽을 수 있습니다.\n처음에는 입력값이 예상대로 읽히는지 작은 예제부터 확인해 보세요.");
        em.flush();
        log.info("[SES_DEMO] Demo data created. VITE_BOARD_IDS={}", board.getId());
    }

    private Member member(String number, String email, String name, int identity) {
        long conflicts = em.createQuery("select count(m) from Member m where m.number = :number or m.email = :email", Long.class)
                .setParameter("number", number).setParameter("email", email).getSingleResult();
        if (conflicts > 0) throw new IllegalStateException("Demo account conflicts with existing data: " + number + ". Existing accounts were not changed.");
        Member member = new Member();
        member.setNumber(number); member.setEmail(email); member.setName(name);
        member.setIdentity(identity); member.setPassword(encoder.encode(PASSWORD));
        em.persist(member);
        return member;
    }

    private Lesson lesson(String name, Member teacher, LocalDate today) {
        Lesson lesson = new Lesson();
        lesson.setName(name); lesson.setMember(teacher);
        lesson.setYear(String.valueOf(today.getYear())); lesson.setSemester(today.getMonthValue() < 7 ? "1" : "2");
        em.persist(lesson);
        return lesson;
    }

    private void enrollment(Member member, Lesson lesson, int accept) {
        Student student = new Student();
        student.setMember(member); student.setLesson(lesson); student.setAccept(accept);
        em.persist(student);
    }

    private Problem problem(String title, String description, String input, String output, int language, Member teacher) {
        Problem problem = new Problem();
        problem.setTitle("[데모] " + title); problem.setDescription(description(description, input, output));
        problem.setLanguage(language); problem.setMember(teacher);
        em.persist(problem);
        return problem;
    }

    private Assignment assignment(String title, Lesson lesson, LocalDate due, int language, String description, String input, String output) {
        Assignment assignment = new Assignment();
        assignment.setTitle(title); assignment.setLesson(lesson); assignment.setLanguage(language);
        assignment.setDescription(description(description, input, output));
        assignment.setEnd_time(due.atTime(LocalTime.of(23, 59)));
        em.persist(assignment);
        return assignment;
    }

    private String description(String description, String input, String output) {
        return description + "\n\n입력\n" + input + "\n\n출력\n" + output + "\n\n[데모 데이터] 화면과 사용자 흐름 확인을 위한 예제입니다.";
    }

    private void testcase(Problem problem, Assignment assignment, String input, String output) {
        Testcase testcase = new Testcase();
        testcase.setProblem(problem); testcase.setAssignment(assignment);
        testcase.setInput(input); testcase.setOutput(output);
        em.persist(testcase);
    }

    private void submission(Member member, Problem problem, Assignment assignment, int language, String code, int score) {
        Submit submit = new Submit();
        submit.setMember(member); submit.setProblem(problem); submit.setAssignment(assignment);
        submit.setLanguage(language); submit.setSubmit_contents(code); submit.setScore(score);
        submit.setResult(assignment != null ? "제출되었습니다." : score == 100 ? "정답입니다." : "오답입니다.");
        em.persist(submit);
    }

    private Post post(Board board, Member writer, String title, String content) {
        Post post = new Post();
        post.setBoard(board); post.setWriter(writer); post.setTitle(title); post.setContent(content);
        post.setUpload_time(LocalDateTime.now()); post.setViews(0);
        em.persist(post);
        return post;
    }

    private void comment(Post post, Member writer, String content) {
        Comment comment = new Comment();
        comment.setPost(post); comment.setWriter(writer); comment.setContent(content);
        em.persist(comment);
    }

    private static final String JAVA_SUM = "// 화면 확인용 데모 제출\nimport java.util.Scanner;\npublic class Main {\n    public static void main(String[] args) {\n        Scanner sc = new Scanner(System.in);\n        System.out.println(sc.nextInt() + sc.nextInt());\n    }\n}";
}
