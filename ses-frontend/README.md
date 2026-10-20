# S.E.S Frontend

S.E.S의 웹 프런트엔드를 개발할 패키지입니다. 현재는 디렉터리와 백엔드 문서 참조만 준비했으며, 화면·프레임워크·의존성·실행 스크립트는 이후 구현합니다.

서비스 소개와 전체 저장소 구성은 [루트 README](../README.md)를 참고하세요.

## 디렉터리

```text
ses-frontend/
├── README.md
├── public/    # 정적 자산
└── src/       # 프런트엔드 소스
```

`.gitkeep`은 빈 디렉터리를 Git에 보존하기 위한 파일입니다. 프런트엔드 구현을 시작할 때 선택한 프레임워크에 맞춰 소스 하위 구조와 패키지 설정을 추가합니다. 현재 `package.json`이나 잠금 파일은 없습니다.

## 백엔드 참조

- 백엔드 패키지: [ses-backend](../ses-backend/README.md)
- API 사용·인증·실행 안내: [백엔드 개발 가이드](../ses-backend/docs/development.md)
- 로컬 API 기본 주소: `http://localhost:8080`
- API 명세: 백엔드 실행 후 [Swagger UI](http://localhost:8080/swagger-ui.html) 또는 [OpenAPI JSON](http://localhost:8080/v3/api-docs)

API 계약은 백엔드에서 생성하는 OpenAPI 문서를 기준으로 확인합니다. 로그인 응답의 JWT를 `Authorization: Bearer <token>` 헤더로 전달합니다. API 클라이언트와 타입 생성, 개발 서버 프록시는 프런트엔드 구현 단계에서 구성합니다.

이 디렉터리는 `ses` 모노레포의 일부입니다. 별도의 Git 초기화나 원격 저장소 연결은 필요하지 않습니다.
