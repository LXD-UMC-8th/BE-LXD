# BE-LXD
> 프로젝트 한줄 소개

---

## 개발 멤버
| 슝슝/박소윤 | 만두/김민지 | 미르/추정은 | 밤하늘/임준현 | 서머/신서현 |
|:------:|:------:|:------:|:------:|:------:|
| <img width="100" alt="슝슝/박소윤" src="https://avatars.githubusercontent.com/happine2s" />  | <img width="100" alt="만두/김민지" src="https://avatars.githubusercontent.com/minzix" /> | <img width="100" alt="미르/추정은" src="https://avatars.githubusercontent.com/forlyby" />  | <img width="100" alt="밤하늘/임준현" src="https://avatars.githubusercontent.com/eclipse021" /> | <img width="100" alt="서머/신서현" src="https://avatars.githubusercontent.com/ss99x2002" />  |
| Leader/BE | BE | BE | BE | BE |
| [@happine2s](https://github.com/happine2s) | [@minzix](https://github.com/minzix) | [@forlyby](https://github.com/forlyby) | [@eclipse021](https://github.com/eclipse021) | [@ss99x2002](https://github.com/ss99x2002) |

<br>


## 소개

> 프로젝트 소개 내용

<br>

## 🛠️ 개발 환경

| |  |
| --- | --- |
| 통합 개발 환경 |  |
| Spring 버전 | |
| 데이터베이스 |  |
| 배포 |  |
| CI/CD 툴 |  |
| ERD 다이어그램 툴 | |
| Java version |  |
| 패키지 구조 | |
| API 테스트 |  |
| 외부 연동 | |


<br>

## ⚙️ 시스템 아키텍처

<br>

## 📝 API Docs
> API 명세서 링크

## 💾 ERD

<br>


## 주요 기능
<table>
  <tr>
    <td>
      사진 
    </td>
    <td>
      사진 
    </td>
   
  </tr>
</table>

<br>

## 📜브랜치 컨벤션

### 1. 브랜치 이름 지정

```java
// ex. feat/#1-emailLogin
feat/#{이슈번호}-{기능내용}
```

- 브랜치 이름은 영어 소문자로 작성하기

### 2. develop 브랜치 활용

- 개발하는 과정에서 사용
- 코드 검증이 끝난 후 main으로 푸시하여 배포 서버에 반영시키기
- feat 브랜치에서 PR 작성 시 main이 아닌 develop으로 날리도록 주의

## 🏷️ 커밋 메시지 컨벤션

### 1. 커밋 유형 지정
- 커밋 유형은 첫글자 영어 대문자로 작성하기
    | 커밋 유형 | 의미 |
    | --- | --- |
    | `Feat` | 새로운 기능 추가 |
    | `Fix` | 버그 수정 |
    | `Docs` | 문서 수정 |
    | `Style` | 코드 formatting, 세미콜론 누락, 코드 자체의 변경이 없는 경우 |
    | `Refactor` | 코드 리팩토링 |
    | `Test` | 테스트 코드, 리팩토링 테스트 코드 추가 |
    | `Chore` | 패키지 매니저 수정, 그 외 기타 수정 ex) .gitignore |
    | `Design` | CSS 등 사용자 UI 디자인 변경 |
    | `Comment` | 필요한 주석 추가 및 변경 |
    | `Rename` | 파일 또는 폴더 명을 수정하거나 옮기는 작업만인 경우 |
    | `Remove` | 파일을 삭제하는 작업만 수행한 경우 |
    | `!BREAKING CHANGE` | 커다란 API 변경의 경우 |
    | `!HOTFIX` | 급하게 치명적인 버그를 고쳐야 하는 경우 |

### 2. 제목과 본문을 빈행으로 분리

- 커밋 유형 이후 제목과 본문은 한글로 작성하여 내용이 잘 전달될 수 있도록 할 것
- 본문에는 변경한 내용과 이유 설명 (어떻게보다는 무엇 & 왜를 설명)

### 3. 제목 첫 글자는 대문자로, 끝에는 `.` 금지

### 4. 제목은 영문 기준 50자 이내로 할 것

### 5. 자신의 코드가 직관적으로 바로 파악할 수 있다고 생각하지 말자