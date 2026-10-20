# S.E.S Backend

S.E.S의 백엔드 API와 Java·C++ 코드 채점기를 담당하는 패키지입니다. 계정, 코딩 문제, 강의실, 과제, 게시판 기능과 데이터 저장을 제공합니다.

서비스 소개와 모노레포 구성은 [루트 README](../README.md)를 참고하세요.

## 실행

모노레포 루트에서 백엔드 디렉터리로 이동해 실행합니다.

```sh
cd ses-backend
./gradlew bootRun
```

JDK 17 또는 21이 필요합니다. 코드 채점에는 Docker의 Linux 컨테이너 실행 환경과 채점 이미지가 추가로 필요합니다. 상세 설정과 이미지 빌드, 테스트 명령은 [개발·실행 가이드](docs/development.md)를 참고하세요.

## 관련 패키지와 문서

- [프런트엔드](../ses-frontend/README.md): 디렉터리와 API 참조를 준비한 상태이며 이후 구현 예정
- [개발·실행 가이드](docs/development.md): 환경 설정, API 사용, 테스트, Docker 채점기와 현재 제한 사항
- [Swagger UI](http://localhost:8080/swagger-ui.html): 서버 실행 후 API 명세 확인 및 요청 실행

Git과 원격 저장소는 상위 `ses/`에서 관리합니다. Gradle, Docker, 애플리케이션 설정은 이 백엔드 패키지에 속합니다.
