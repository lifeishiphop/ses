# 개발·실행 가이드

S.E.S 서버의 실행, 환경 설정, API 사용, 테스트와 채점기 운영을 안내합니다. 서비스 소개와 전체 패키지 구성은 [루트 README](../../README.md), 백엔드 패키지 안내는 [Backend README](../README.md)를 참고하세요.

## 빠른 실행

JDK **17 또는 21**이 필요합니다. Gradle은 별도로 설치하지 않아도 됩니다. 첫 실행에서는 Gradle Wrapper와 의존성을 다운로드하므로 인터넷 연결이 필요합니다.

모노레포 루트(`ses/`)에서 백엔드 디렉터리로 이동해 실행합니다. 이후 이 문서의 실행·빌드·테스트 명령은 모두 백엔드 디렉터리(`ses-backend/`)를 기준으로 합니다.

```sh
cd ses-backend
./gradlew bootRun
```

Windows에서는 `gradlew.bat bootRun`을 사용합니다. `java -version`이 17 또는 21인지 확인하고, 다른 버전이면 `JAVA_HOME`을 해당 JDK로 지정하세요.

- Swagger UI: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
- OpenAPI JSON: [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)
- OpenAPI YAML: [http://localhost:8080/v3/api-docs.yaml](http://localhost:8080/v3/api-docs.yaml)

기본 `local` 프로필은 MySQL 호환 모드의 H2 파일 DB를 사용합니다. 별도 DB·SMTP 서버 없이 서버를 시작하고 회원가입·로그인 및 일반 API를 확인할 수 있습니다. 이메일 발송과 코드 채점은 아래의 별도 환경이 필요합니다.

서버는 기본적으로 `127.0.0.1:8080`에서 실행합니다. H2 데이터는 실행 디렉터리의 `data/`에 보관되며 재시작 후에도 유지됩니다. 실행 중인 서버를 종료한 뒤 `data/`를 삭제하면 로컬 데이터가 초기화됩니다. 기본 계정이나 게시판 데이터는 자동으로 생성하지 않습니다. `/`에는 별도 웹 화면이 없습니다.

## API 사용

1. Swagger의 **계정 → POST /signup**에서 회원가입합니다.
2. **POST /login**의 `id`에 회원가입의 `number`(학번)를 넣습니다.
3. 응답의 `token` 값을 Swagger 상단 **Authorize**에 입력합니다. `Bearer ` 접두어는 입력하지 않습니다.
4. 문제·강의실·과제·게시판 API를 호출합니다. 검색의 `page`는 **1부터** 시작합니다.

회원가입 예시:

```sh
curl -X POST http://localhost:8080/signup \
  -H 'Content-Type: application/json' \
  -d '{"number":"20260001","password":"SesTest123!","passwordCheck":"SesTest123!","email":"s1@kumoh.ac.kr","name":"홍길동","identity":1}'
```

로그인 예시:

```sh
curl -X POST http://localhost:8080/login \
  -H 'Content-Type: application/json' \
  -d '{"id":"20260001","password":"SesTest123!"}'
```

인증 API 호출 예시 (`로그인_응답의_token`을 실제 값으로 교체):

```sh
curl http://localhost:8080/problem \
  -H 'Authorization: Bearer 로그인_응답의_token'
```

회원가입 이메일은 `kumoh.ac.kr` 도메인이어야 합니다. 비밀번호는 영문·숫자·특수문자를 포함한 8~30자이고 `passwordCheck`와 같아야 합니다. `identity`의 기존 구분은 `0=관리자`, `1=학생`, `2=강사`입니다.

기존 URL·DTO 필드명·성공 응답 코드(`200`)를 유지합니다. 서비스 처리 오류는 보통 `400`과 `{"error - ":"오류 메시지"}`를 반환하고, 로그인 실패 및 JWT 인증 실패는 `401`입니다. 요청 바인딩 오류나 인증 오류는 Spring의 기본 오류 응답을 사용할 수 있습니다. 각 API의 요청·응답 스키마는 Swagger를 참고하세요.

## 빌드와 테스트

```sh
./gradlew test
./gradlew clean build
java -jar build/libs/ses-backend-0.0.1-SNAPSHOT.jar
```

JAR도 백엔드 디렉터리(`ses-backend/`)에서 실행하면 개발 실행과 같은 `data/`를 사용합니다. `test` 프로필은 메모리 H2 DB를 사용하며 로컬 파일 DB나 외부 DB를 변경하지 않습니다.

- `ApiIntegrationTest`: 실제 Security 필터·컨트롤러·서비스·JPA를 연결해 회원가입/로그인, 중복·비밀번호 검증, JWT 누락·변조·만료·탈퇴 회원, 비밀번호 변경, 문제 CRUD·검색·테스트케이스, 강의실·과제, 게시글·댓글 및 작성자 검증을 확인합니다.
- `OpenApiIntegrationTest`: 모든 컨트롤러 경로의 문서 포함 여부, 요청·성공·오류 응답 스키마, 공개 API와 JWT 표시, Swagger UI 리소스와 JSON/YAML 접근을 확인합니다.
- `HttpErrorIntegrationTest`: 실제 HTTP 서버에서 문서 비활성화 시 `404`, 미인증 요청의 `401` 상태와 오류 응답을 확인합니다.
- `GradingApiIntegrationTest`: 문제·과제 실행/제출 연결, 경로 ID 검증, 마감일, 결과 저장 및 Docker 장애 시 저장 방지를 확인합니다.
- `JudgeServiceImplTest`, `DockerCodeRunnerTest`: 점수 판정, 실패 결과, 빈 테스트, 허용 언어 및 입력 크기 제한을 확인합니다.
- `DockerJudgeIntegrationTest`: 실제 Java/C++ 컴파일·실행과 시간·출력·파일·권한·네트워크 격리 및 동시 제출을 확인합니다.

일반 `test`는 Docker 없이 실행할 수 있습니다. 실제 컴파일러·격리·시간 제한·출력 제한을 검증하는 테스트는 채점 이미지를 준비한 뒤 별도로 실행합니다.

```sh
./gradlew dockerTest
# 일반 테스트, JAR 빌드, 실제 Docker 채점까지 한 번에 검증
./gradlew clean build dockerTest
```

SMTP 전송은 테스트에 포함하지 않습니다. Gradle HTML 결과는 `build/reports/tests/test/index.html`, `build/reports/tests/dockerTest/index.html`에서 확인할 수 있습니다.

## Docker 채점기

Docker Engine의 **Linux 컨테이너** 실행 환경이 필요합니다. macOS·Windows에서는 Docker Desktop의 Linux 컨테이너 모드를, Linux에서는 Docker Engine을 사용할 수 있습니다. 채점 이미지는 저장소에 포함된 Dockerfile로 한 번 빌드합니다. 빌드 시 인터넷 연결이 필요하며 채점 요청 중에는 이미지를 자동으로 내려받지 않습니다.

```sh
docker build -t ses-judge:local docker/judge
./gradlew bootRun
```

Windows PowerShell에서도 같은 `docker build` 명령을 사용하고 서버는 `.\gradlew.bat bootRun`으로 시작합니다. 호스트에는 C++ 컴파일러를 설치하지 않아도 됩니다. 소스와 테스트 입력을 JSON 표준 입력으로 전달하므로 호스트 폴더 공유, 드라이브 문자, 고정 절대 경로에 의존하지 않습니다. Docker CLI 경로에 공백이 있으면 `JUDGE_DOCKER_COMMAND`에 실행 파일의 전체 경로를 환경 변수로 지정하세요.

API의 `language`는 `1=Java`, `2=C++`입니다. Java는 package 선언 없는 `Main` 클래스의 단일 `Main.java`, C++는 C++17 단일 소스 파일을 지원합니다. 문제·과제의 허용 언어가 `0`이면 두 언어 모두 사용할 수 있습니다.

`POST /problem/run/{problemIdx}` 또는 `/assignment/run/{assignmentIdx}`의 요청 예시:

```json
{
  "language": 1,
  "submit_contents": "public class Main { public static void main(String[] args) { System.out.println(3); } }"
}
```

먼저 문제·과제에 입력과 기대 출력이 있는 테스트케이스를 등록하세요. 실행 API는 채점만 하고, `/problem/submit/{problemIdx}`와 `/assignment/submit/{assignmentIdx}`는 결과를 저장합니다. 회원 ID는 로그인한 사용자에서 가져오고 문제·과제 ID는 경로에서 설정합니다. 과제 제출 응답의 `result`는 기존대로 `제출되었습니다.`이며 실제 점수는 `score`에 있습니다.

- 모든 테스트 통과: `100`, `정답입니다.`
- 출력 불일치: `0`, `오답입니다.`
- 컴파일·실행 오류, 시간·출력·메모리 제한: `0`과 해당 실패 메시지
- 테스트케이스 없음: `-1`, 테스트할 수 없는 문제/과제라는 메시지
- Docker 미실행·이미지 누락·채점기 응답 오류·동시 채점 초과: 오류 응답을 반환하며 제출 결과를 저장하지 않음

출력 비교는 기존처럼 앞뒤 공백을 제거하고 CRLF 줄바꿈을 LF로 통일합니다. 표준 오류 출력은 정답 비교에서 제외합니다. 소스와 각 테스트 입력은 UTF-8 64 KiB, 테스트케이스는 최대 20개, 각 테스트의 표준 출력·오류 합계는 64 KiB로 제한합니다.

제출마다 새 컨테이너에서 한 번 컴파일하고 테스트마다 별도 비특권 UID와 작업 폴더로 실행합니다. 컨테이너는 네트워크 없이 CPU 1개, 기본 메모리 512 MiB, PID 128개로 제한합니다. 루트 파일시스템은 읽기 전용이고 작업 공간은 64 MiB, 임시 공간은 32 MiB입니다. 감독 프로세스만 사용자 전환·파일 소유권 변경·자식 종료에 필요한 권한을 가지며 제출 코드에는 해당 권한을 주지 않습니다. 실행 종료·제한 초과·요청 중단 시 자식 프로세스와 컨테이너를 정리합니다.

Java 실행 힙은 128 MiB, 메타스페이스는 96 MiB로 제한합니다. JVM 내부의 메모리 부족은 실행 오류로, 컨테이너 전체가 메모리 제한으로 종료되면 메모리 제한 메시지로 나타날 수 있습니다. 전체 작업에도 컴파일 제한 + 테스트 수 × 실행 제한 + 시작 여유 15초의 제한을 적용합니다.

| 환경 변수 | 기본값 | 용도 |
| --- | --- | --- |
| `JUDGE_DOCKER_COMMAND` | `docker` | Docker CLI 실행 파일 |
| `JUDGE_IMAGE` | `ses-judge:local` | 로컬 채점 이미지 |
| `JUDGE_MEMORY` | `512m` | 컨테이너 메모리 및 스왑 합계 제한 |
| `JUDGE_COMPILE_TIMEOUT` | `10s` | 컴파일 제한 |
| `JUDGE_RUN_TIMEOUT` | `10s` | 테스트 하나의 실행 제한 |
| `JUDGE_MAX_CONCURRENT` | `2` | 동시에 실행할 제출 수. 초과 요청은 즉시 거절 |

서버 자체를 컨테이너에 배포한다면 Docker CLI와 전용 Docker 엔진 접근을 별도로 구성해야 합니다. 현재 실행 예시는 서버를 호스트에서 실행하는 구성입니다. 이 채점기는 Docker의 격리 기능을 사용하며, 공개 서비스에서 적대적인 코드를 받는 경우에는 별도 채점 호스트와 gVisor/마이크로 VM 같은 추가 실행 격리를 검토하세요.

## 설정

| 프로필 | DB | 스키마 처리 | JWT 키 |
| --- | --- | --- | --- |
| `local` (기본) | H2 파일 `data/ses` | `update` | 개발 전용 기본 키, `JWT_SECRET`으로 교체 가능 |
| `test` | H2 메모리 | `create-drop` | 테스트 전용 키 |
| `mysql` | `DB_URL`의 MySQL | 기본 `validate` | `JWT_SECRET` 필수 |

| 환경 변수 | 기본값 / 용도 |
| --- | --- |
| `SERVER_PORT` | `8080` |
| `SERVER_ADDRESS` | `127.0.0.1`; 컨테이너에서는 필요에 따라 `0.0.0.0` |
| `JWT_SECRET` | Base64로 인코딩한 64바이트 이상의 HS512 서명 키 |
| `JWT_EXPIRATION_SECONDS` | `3600` |
| `API_DOCS_ENABLED` | `true`; `false`이면 Swagger UI와 OpenAPI 비활성화 |
| `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` | `mysql` 프로필의 DB 연결 정보 |
| `DDL_AUTO` | `mysql` 프로필에서 `validate`; 새 개발 DB 초기화 시 `update` 사용 가능 |
| `MAIL_HOST`, `MAIL_PORT` | `localhost`, `1025` |
| `MAIL_USERNAME`, `MAIL_PASSWORD` | `ses@localhost`, 빈 문자열. username은 발신 이메일에도 사용 |
| `MAIL_SMTP_AUTH`, `MAIL_STARTTLS` | 둘 다 `false`; 사용하는 SMTP 서버에 맞게 설정 |

MySQL을 사용하려면 먼저 빈 `ses` 데이터베이스와 접근 가능한 DB 계정을 준비합니다. `.env.example`을 복사해 DB 연결 정보를 수정하고 JWT 키를 설정합니다. Spring Boot는 `.env`를 자동으로 읽지 않으므로 셸에서 명시적으로 불러옵니다.

```sh
cp .env.example .env
# .env의 DB_URL, DB_USERNAME, DB_PASSWORD 등을 수정
set -a
source .env
set +a
export JWT_SECRET="$(openssl rand -base64 64 | tr -d '\n')"
# 새 개발 DB를 처음 초기화할 때만 사용
export DDL_AUTO=update
./gradlew bootRun
```

이 예시는 macOS/Linux 셸 기준입니다. Windows에서는 같은 환경 변수를 PowerShell 등에서 설정하세요. 실제 환경에서는 JWT 키를 재사용 가능한 비밀 설정으로 보관하고 기존 DB에 맞는 스키마를 준비한 뒤 `DDL_AUTO=validate`를 사용하세요. MySQL 기본 URL은 `jdbc:mysql://localhost:3306/ses?serverTimezone=Asia/Seoul&characterEncoding=UTF-8`입니다.

## API 문서 유지 방법

현재 문서는 **springdoc-openapi + Swagger UI**로 컨트롤러와 DTO에서 생성합니다. 기존 [스프레드시트 API 목록](https://docs.google.com/spreadsheets/d/e/2PACX-1vSnev6YZPfECpCVEy1elDhtEpV2ZLJuiibHMtVGvdQM4uXVnA-9odb6VnvfeiTDe2QyN2rAx3ADtm0Y/pubhtml?gid=0&single=true)은 과거 참고 자료이며, 실행 중인 코드가 현재 문서의 기준입니다.

- 컨트롤러의 `@Tag`, `@Operation`에 분류와 설명을 작성합니다.
- `ResponseEntity<?>`를 사용하는 기존 구조이므로 `@ApiResponse`에 실제 응답 DTO를 명시합니다. 목록은 `@ArraySchema`, 빈 응답은 `@Content`를 사용합니다.
- DTO 필드는 `@Schema`에 의미와 예시를 작성합니다.
- JWT는 기본 보안 요구 사항입니다. 인증 없이 호출하는 API에는 `@SecurityRequirements`를 지정하고 실제 Security 설정과 일치시킵니다.
- 변경 후 `./gradlew test`로 경로와 문서의 일치를 확인합니다. 문서 테스트는 요청·응답 값 전체의 정확성을 보장하지 않으므로 바뀐 동작의 통합 테스트도 추가하세요.

OpenAPI 명세를 파일로 보관하려면 실행 중인 서버에서 내려받습니다.

```sh
curl --fail http://localhost:8080/v3/api-docs.yaml -o openapi.yaml
```

기술 버전은 Java 17, Spring Boot 3.5.16, Gradle 8.14.6, springdoc 2.8.17로 고정했습니다. Spring Boot 3 계열에 맞는 [springdoc 2.x 호환 안내](https://springdoc.org/v2/)를 기준으로 구성했습니다.

## 기존 구현의 제한

현재 구현에는 다음 제한이 있습니다.

- **회원·인증 정책**: 가입 요청으로 관리자/강사 역할을 지정할 수 있고, 비밀번호 찾기에서 이메일 인증 결과를 확인하지 않습니다. 이메일 인증번호는 서버 메모리에 한 개만 저장하며 응답에도 포함합니다. 외부 공개 서비스로 사용하기 전에 해당 정책을 보완해야 합니다. 기본 로컬 서버는 이 때문에 루프백 주소에 바인딩합니다.
- **로그아웃·내 정보**: `/logoutAccount`는 JWT를 폐기하지 않고, `GET /my`는 요청 본문만 받고 빈 `200`을 반환하는 미구현 상태입니다.
- **이메일·초기 데이터**: 이메일 전송에는 별도 SMTP 설정이 필요합니다. 게시글 작성에는 기존 `Board` 데이터가 필요하며 현재 게시판 생성 API는 없습니다.
- **경로와 본문의 ID**: 문제/과제 테스트케이스 등록은 본문의 `problem_id`/`assignment_id`를 사용합니다. 실행·제출은 경로 ID를 사용하므로 본문의 해당 ID는 생략할 수 있습니다. 본문에도 넣으면 경로와 같아야 합니다.

## 소스 구조

```text
src/main/java/com/web/back/
  config/       # JPA, MVC, 암호화, OpenAPI 설정
  controller/   # 계정, 문제, 강의실, 과제, 게시판, 관리 API
  dto/          # 요청·응답 및 Swagger 필드 설명
  model/        # JPA 엔티티
  repository/   # Spring Data JPA
  security/     # JWT 인증
  service/      # 비즈니스 로직
  utils/        # 검증, 오류 응답, Docker 채점 실행
src/main/resources/  # 공통·local·mysql 설정
src/test/            # 단위·API·Docker 통합 테스트와 test 설정
docker/judge/        # Java 17·C++17 채점 이미지와 감독 프로세스
```
