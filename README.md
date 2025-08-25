#  Language Xchange Diary
LXD는 외국어 학습자가 서로의 일기를 공유하고 교정해주며 자연스럽게 언어를 배우는 교류형 다이어리 플랫폼입니다.

---
## 📖 프로젝트 소개
외국어 학습자는 글쓰기 연습의 필요성을 느끼지만 *꾸준히 실천할 기회와 적절한 피드백* 을 얻기 어렵습니다.

사용자는 자신의 일기를 작성하고 **원어민 혹은 다른 학습자에게 교정**을 받을 수 있으며,
단순히 글을 고치는 데서 그치지 않고 댓글·좋아요·친구 기능을 통해 지속적인 학습 네트워크를 만들어갑니다.
이 과정을 통해 학습자는 **자연스러운 글쓰기 습관을 형성하고, 실질적인 피드백과 동기부여**를 얻을 수 있습니다.

## 🎬 기능 미리보기

| 구글 소셜 로그인 | 이메일 인증 회원가입  | 친구 |
| :----------------------------------------------------------------------------------: | :----------------------------------------------------------------------------------: | :----------------------------------------------------------------------------------: | 
| <img width="478" alt="image" src="https://github.com/user-attachments/assets/8c377564-cf05-40b9-8a6a-e8f85f6b9a16"> | <img width="478" alt="image" src="https://github.com/user-attachments/assets/c2e60a98-179c-4e52-94fe-ec9f95318898"> | <img width="478" alt="image" src="https://github.com/user-attachments/assets/51a99970-d8c0-4a44-adf2-4a565a472dfc"> |

| 일기 작성하기 | 일기 댓글, 좋아요 |
| :----------------------------------------------------------------------------------: | :----------------------------------------------------------------------------------: |
| <img width="478" alt="image" src="https://github.com/user-attachments/assets/8200596f-c920-4bc1-bace-b8c4d0150409"> | <img width="478" alt="image" src="https://github.com/user-attachments/assets/de433838-aad3-4d0a-9e82-a1ff387c8087"> |

| 교정 작성하기 | 교정 댓글, 좋아요, 메모 |
| :----------------------------------------------------------------------------------: | :----------------------------------------------------------------------------------: |
| <img width="478" alt="image" src="https://github.com/user-attachments/assets/e8b9f7ef-ea20-48fd-96b1-98e8165de597"> | <img width="478" alt="image" src="https://github.com/user-attachments/assets/1807449c-c544-41ee-8d67-177e8c9bbf3f"> |

| 피드 | 알림 | 다국어 지원(한국어/영어) |
| :----------------------------------------------------------------------------------: | :----------------------------------------------------------------------------------: | :----------------------------------------------------------------------------------: |
| <img width="478" alt="image" src="https://github.com/user-attachments/assets/7729bdd0-6c56-458a-b452-832da0144d89"> | <img width="478" alt="image" src="https://github.com/user-attachments/assets/2a9f8b51-ebb1-4d61-9417-bcc53bec2a5e"> | <img width="478" alt="image" src="https://github.com/user-attachments/assets/d6d34d25-b12b-49fc-9ffa-046c8bd86301"> |

## 🚀 주요 기능
- **일기 작성**
  - 외국어로 자유롭게 일기를 작성하고 공개 범위를 설정
  - 꾸준한 기록을 통해 학습 습관 형성
- **교정 기능**
  * 다른 사용자의 일기를 교정하거나 내 일기를 교정받음
  * 원어민/학습자 간 상호 피드백 제공

- **커뮤니케이션**
  * 댓글과 좋아요를 통해 교류 강화
  * 친구 요청/수락 기능으로 지속적인 네트워크 형성

- **실시간 알림**
  * 교정, 댓글, 좋아요, 친구 요청 시 즉시 알림 제공

- **회원 관리**
  - 구글 소셜 회원가입/로그인
  - 이메일 인증 회원가입
 
---   
## 🏗️ 시스템 아키텍처
<img width="900" alt="Group 1000000904 (1)" src="https://github.com/user-attachments/assets/d207df42-954b-451f-a643-9f8af1c1c59f" />
<br>

## ⚙️ 기술 스택
<img src="https://img.shields.io/badge/Spring%20Boot-6DB33F.svg?style=for-the-badge&logo=springboot&logoColor=white"> <img src="https://img.shields.io/badge/JPA-59666C.svg?style=for-the-badge&logo=hibernate&logoColor=white"> <img src="https://img.shields.io/badge/QueryDSL-4479A1.svg?style=for-the-badge&logo=codefactor&logoColor=white">
<br>
<img src="https://img.shields.io/badge/MySQL-4479A1.svg?style=for-the-badge&logo=mysql&logoColor=white"> <img src="https://img.shields.io/badge/Redis-DC382D.svg?style=for-the-badge&logo=redis&logoColor=white">
<br>
<img src="https://img.shields.io/badge/AWS-232F3E.svg?style=for-the-badge&logo=amazonaws&logoColor=white"> <img src="https://img.shields.io/badge/Docker-2496ED.svg?style=for-the-badge&logo=docker&logoColor=white"> <img src="https://img.shields.io/badge/GitHub%20Actions-2671E5.svg?style=for-the-badge&logo=githubactions&logoColor=white">
<br>

## 🗄️ ERD
<img alt="LXD-ERD" src="https://github.com/user-attachments/assets/32090814-bab6-47e1-abb2-a3ee7cb66d84" />
<br>

## 📂 프로젝트 구조
```bash
BE-LXD/
 ├── .github/  
 │   └── workflows/  
 │        └── deploy.yml             # 배포 자동화 설정 (CI/CD)
 │
 ├── src/
 │   └── main/
 │       ├── java/
 │       │   ├── name.fraser.neil.plaintext/
 │       │   │    └── diff_match_patch       # 텍스트 비교/교정(diff) 라이브러리
 │       │   │
 │       │   └── org.lxdproject.lxd/
 │       │        ├── apiPayload             # 공통 응답 포맷, 예외 처리
 │       │        ├── auth                   # 인증 관련 (로그인/회원가입)
 │       │        ├── authz                  # 인가 정책 및 권한
 │       │        ├── common                 # 공통 유틸, 상수, 베이스 클래스
 │       │        ├── config                 # 스프링 및 인프라 설정
 │       │        ├── correction             # 교정 도메인
 │       │        ├── correctioncomment      # 교정 댓글 도메인
 │       │        ├── correctionlike         # 교정 좋아요 도메인
 │       │        ├── diary                  # 일기 도메인
 │       │        ├── diarycomment           # 일기 댓글 도메인
 │       │        ├── diarycommentlike       # 일기 댓글 좋아요 도메인
 │       │        ├── diarylike              # 일기 좋아요 도메인
 │       │        ├── friend                 # 친구 도메인
 │       │        ├── infra                  # 외부 연동, AWS/Redis 서비스
 │       │        ├── member                 # 회원 도메인
 │       │        ├── notification           # 알림 도메인 (SSE, Redis Pub/Sub)
 │       │        ├── validation             # 커스텀 Validator
 │       │        └── LxdApplication         # Spring Boot 실행 클래스
 │       │
 │       └── resources/
 │           ├── templates/
 │           │    ├── email.html             # 이메일 인증 템플릿
 │           │    └── password.html          # 비밀번호 재설정 템플릿
 │           │
 │           ├── application.yml             # 공통 설정
 │           ├── application-local.yml       # 로컬 환경 변수
 │           └── application-prod.yml        # 운영 환경 변수
 │
 ├── docker-compose.yml
 └── Dockerfile
```

## 📑 API 명세서
🔗 [Notion 문서](https://rigorous-tourmaline-47c.notion.site/API-22b91de6073780588b2ee92b881492e8?source=copy_link)  
🔗 [Swagger UI](https://umc-lxd.site/swagger-ui/index.html#/)

---
## 🏷️ Git 사용 전략

### 1. 작업 유형 지정
- 첫글자를 영어 대문자로 작성하기
```bash
✨Feat : 새로운 기능 구현
♻️Refactor : 코드 리팩토링
🐛Fix : 버그, 오류 해결
🎨Style : 코드 formatting, 세미콜론 누락, 코드 자체의 변경이 없는 경우
📝Docs : 문서 수정
✅Test : 테스트 코드, 리팩토링 테스트 코드 추가
📦Chore : 코드 수정, 내부 파일 수정
🚑!️HOTFIX : 급하게 치명적인 버그를 고쳐야 하는 경우
💥!BREAKING CHANGE : 커다란 API 변경의 경우
```

### 2. 브랜치 이름 지정

```java
// ex. feat/#1-emailLogin
{커밋유형}/#{이슈번호}-{기능내용}
```

## 👥 개발 멤버
| 슝슝/박소윤 | 만두/김민지 | 미르/추정은 | 밤하늘/임준현 | 서머/신서현 |
|:------:|:------:|:------:|:------:|:------:|
| <img width="100" alt="슝슝/박소윤" src="https://avatars.githubusercontent.com/happine2s" />  | <img width="100" alt="만두/김민지" src="https://avatars.githubusercontent.com/minzix" /> | <img width="100" alt="미르/추정은" src="https://avatars.githubusercontent.com/forlyby" />  | <img width="100" alt="밤하늘/임준현" src="https://avatars.githubusercontent.com/eclipse021" /> | <img width="100" alt="서머/신서현" src="https://avatars.githubusercontent.com/ss99x2002" />  |
| BE (Leader) | BE | BE | BE | BE |
| [@happine2s](https://github.com/happine2s) | [@minzix](https://github.com/minzix) | [@forlyby](https://github.com/forlyby) | [@eclipse021](https://github.com/eclipse021) | [@ss99x2002](https://github.com/ss99x2002) |
