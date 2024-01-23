# (Back-end) 일진 전자입찰 시스템
## 사전요건
* git 설치
* java : zulu 8 64bit

## 패키지 구조
- core 패키지: 프레임워크 표준이며 핵심 기능  
- ijeas 패키지: 일진 다이아 전자전표 비즈니스 패키지  

### core package
1. aop: aspect (로깅)
2. config: java class 기반 설정
3. database: 멀티 데이터 소스 (미구현)
4. deploy: 운영 배포 자동화 (미구현)
5. file: 파일 업로드 관련 (사용하지 않음 -> ijeas.sm.evid)
6. mail: 메일 서비스
7. push: 모바일 푸시 알림 서비스
8. scheduled: 스케쥴 서비스 (컴포넌트화 필요)
9. security: 로그인 관련 서비스 (권한, 사용자, 인증 토큰 등 스프링 시큐리티)
10. util: key/value 콤보박스 서비스, Error 메시지

## 주요 설정
1. application.properties
2. 스프링 설정 파일 -> core.config.*

## 의존성
1. pom.xml

## api 작성 예시
- 전체 사원 조회: GET /api/v1/user  
- 특정 사원 조회: GET /api/v1/user/id/1  
- 새로운 사원 생성: POST /api/v1/user/370372  
- 사원 정보 변경: PUT /api/v1/user/370372  
- 사원 정보 삭제: DELETE /api/v1/user/370372  

## 예외처리
- 관련 패키지에 명확한 Exception 클래스 생성  
- 컨트롤러 클래스 상에 ExceptionHandler 클래스 연결  

## fasoo 관련 (미사용)
상세내용은 .properties 파일 내용 확인
1. 유관 DLL 파일을 윈도우 서버의 경우 windows32 디렉토리에 복사 붙여넣기 해줘야 함.
