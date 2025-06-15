# 네이버 소셜 로그인

## 1. 프론트엔드 (React)

### 1) 패키지 설치

```bash
npm install react-naver-login
```

### 2) 사용 예시

```jsx
import NaverLogin from 'react-naver-login';

function LoginPage() {
  return (
    <NaverLogin
      clientId="네이버 클라이언트 ID"
      callbackUrl="http://localhost:8080/naver/callback"
      render={(props) => <button onClick={props.onClick}>네이버 로그인</button>}
    />
  );
}
```

---

## 2. 백엔드 (Spring Legacy)

### 1) 네이버 로그인 콜백 엔드포인트

```java
@RequestMapping("/naver/callback")
public String naverCallback(@RequestParam String code, @RequestParam String state, HttpSession session) {
    // 1. code를 이용해 access_token 요청
    String accessToken = naverService.getAccessToken(code, state);

    // 2. access_token으로 사용자 정보 요청
    NaverUser user = naverService.getUserProfile(accessToken);

    // 3. 세션 또는 JWT 발급 후 처리
    session.setAttribute("user", user);

    // 프론트 리디렉션
    return "redirect:http://localhost:3000";
}
```

### 2) 네이버 서비스

```java
public class NaverService {

    public String getAccessToken(String code, String state) {
        String url = "https://nid.naver.com/oauth2.0/token";
        // URLConnection, RestTemplate, OkHttp 등 사용 가능
        // POST 파라미터: grant_type, client_id, client_secret, code, state

        // 응답에서 access_token 파싱 후 반환
    }

    public NaverUser getUserProfile(String accessToken) {
        String url = "https://openapi.naver.com/v1/nid/me";
        // Authorization: Bearer access_token
        // JSON 응답 파싱 후 NaverUser 객체로 매핑
    }
}
```