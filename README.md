# koreanit-server-spring-example

Koreanit 예제용 Spring Boot 백엔드 API 서버입니다.

- GitHub: https://github.com/lili0101ed/koreanit-server-spring-example
- Stack: Spring Boot 3.5, Java 17, MySQL, Redis(Session), Spring Security

## 실행 환경

- Java 17+
- MySQL
- Redis

## 로컬 실행

```bash
cd ~/projects/koreanit-server/spring
./gradlew bootRun
```

기본 프로필은 `dev`입니다 (`application.yaml` 기준).

## 설정 파일

- `src/main/resources/application.yaml`
- `src/main/resources/application-dev.yml`
- `src/main/resources/application-prod.yml`

주요 설정(dev):

- DB: `jdbc:mysql://localhost:3306/koreanit_service`
- Redis: `127.0.0.1:6379`
- Session store: Redis

## API 요약

### 인증 (`/api`)

- `POST /api/login` : 로그인 (세션 생성)
- `POST /api/logout` : 로그아웃 (세션 제거)
- `GET /api/me` : 현재 로그인 사용자 조회

### 사용자 (`/api/users`)

- `POST /api/users` : 회원가입
- `GET /api/users/{id}` : 사용자 단건 조회
- `GET /api/users?limit=1000` : 사용자 목록 조회
- `PUT /api/users/{id}/nickname` : 닉네임 변경
- `PUT /api/users/{id}/password` : 비밀번호 변경
- `PUT /api/users/{id}/email` : 이메일 변경
- `DELETE /api/users/{id}` : 사용자 삭제

### 게시글 (`/api/posts`)

- `POST /api/posts` : 게시글 생성
- `GET /api/posts?page=1&limit=20` : 게시글 목록
- `GET /api/posts/{id}` : 게시글 상세
- `PUT /api/posts/{id}` : 게시글 수정
- `DELETE /api/posts/{id}` : 게시글 삭제

### 댓글 (`/api`)

- `POST /api/posts/{postId}/comments` : 댓글 작성
- `GET /api/posts/{postId}/comments?before=&limit=20` : 댓글 목록
- `DELETE /api/comments/{id}` : 댓글 삭제

## 응답 형식

공통 응답 래퍼 `ApiResponse`를 사용합니다.

- 성공: `ApiResponse.ok(data)`
- 실패: `GlobalExceptionHandler`에서 공통 에러 포맷으로 반환

## 참고

현재 GitHub 원격 저장소의 README는 최소 내용만 포함되어 있어,
이 문서는 저장소 정보 + 실제 코드 구조를 기반으로 정리했습니다.
원하시면 GitHub 페이지(위키/프로젝트/노션 등) 원문 링크 기준으로 문구를 1:1 맞춰서 다시 수정해드릴게요.
