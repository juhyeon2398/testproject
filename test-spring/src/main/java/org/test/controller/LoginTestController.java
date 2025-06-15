package org.test.controller;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken.Payload;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;

import lombok.extern.log4j.Log4j;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;



@RestController
@Log4j
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class LoginTestController {

	@GetMapping("/session")
	public Map<String, Object> checkSession(HttpSession session) {
	    Map<String, Object> result = new HashMap<>();
	    Object user = session.getAttribute("user");

	    if (user != null) {
	        result.put("loggedIn", true);
	        result.put("user", user); // 필요 시 닉네임이나 이메일만 넘겨도 됨
	    } else {
	        result.put("loggedIn", false);
	    }

	    return result;
	}
	
	
	@Value("${google.oauth.client.id}")
	private String Google_Client_Id;

	// 구글 로그인
	@PostMapping("/google")
	@ResponseBody
	public ResponseEntity<?> googleLogin(@RequestBody Map<String, String> body) {
		String GOOGLE_CLIENT_ID = Google_Client_Id;
		String token = body.get("token");

		try {
			// 토큰 검증 및 사용자 정보 요청
			GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(),
					new JacksonFactory()).setAudience(Collections.singletonList(GOOGLE_CLIENT_ID)).build();

			GoogleIdToken idToken = verifier.verify(token);
			if (idToken != null) {
				Payload payload = idToken.getPayload();

				String email = payload.getEmail();
				String name = (String) payload.get("name");

				// DB 조회 및 회원가입/로그인 처리
				// 세션 또는 JWT 발급

				return ResponseEntity.ok().body(Map.of("email", email, "name", name));
			} else {
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token");
			}

		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " + e.getMessage());
		}
	}

//	==========================================================================================================================================================================

	// 카카오 로그인
	@PostMapping(value = "/kakao", produces = "text/plain;charset=UTF-8")
	@ResponseBody
	public ResponseEntity<String> kakaoLogin(@RequestBody Map<String, String> body, HttpSession session) {
		String accessToken = body.get("accessToken");

		if (accessToken == null) {
			return ResponseEntity.badRequest().body("accessToken 누락됨");
		}

		try {
			// 1. 사용자 정보 조회
			URL url = new URL("https://kapi.kakao.com/v2/user/me");
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			conn.setRequestProperty("Authorization", "Bearer " + accessToken);

			int responseCode = conn.getResponseCode();
			if (responseCode == 200) {
				BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
				StringBuilder result = new StringBuilder();
				String line;
				while ((line = br.readLine()) != null)
					result.append(line);

				// 2. JSON 파싱
				ObjectMapper objectMapper = new ObjectMapper();
				JsonNode userJson = objectMapper.readTree(result.toString());

				String kakaoId = userJson.get("id").asText();
				String nickname = userJson.get("properties").get("nickname").asText();
				String email = userJson.path("kakao_account").path("email").asText();

				// 3. 세션 저장 (또는 DB 연동)
				session.setAttribute("userEmail", email);
				session.setAttribute("userNickname", nickname);
				session.setAttribute("kakaoId", kakaoId);

				return ResponseEntity.ok("카카오 로그인 성공");
			} else {
				return ResponseEntity.status(responseCode).body("카카오 사용자 정보 조회 실패");
			}
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.status(500).body("서버 오류: " + e.getMessage());
		}
	}

//	==========================================================================================================================================================================

	@Value("${naver.oauth.client.id}")
	private String NAVER_CLIENT_ID;

	@Value("${naver.oauth.client.secret}")
	private String NAVER_CLIENT_SECRET;

	@Value("${naver.oauth.callback.uri}")
	private String NAVER_CALLBACK;

	@GetMapping("/naver/login")
    public Map<String, String> redirectToNaver() throws Exception {
        String state = UUID.randomUUID().toString();
        String url = "https://nid.naver.com/oauth2.0/authorize?response_type=code"
                + "&client_id=" + NAVER_CLIENT_ID
                + "&redirect_uri=" + URLEncoder.encode(NAVER_CALLBACK, StandardCharsets.UTF_8)
                + "&state=" + state;

        Map<String, String> response = new HashMap<>();
        response.put("redirectUrl", url);
        return response;
    }

    @GetMapping("/naver")
    public void handleCallback(@RequestParam String code, @RequestParam String state,
                               HttpSession session, HttpServletResponse response) throws Exception {
        // 1. access token 요청
        RestTemplate restTemplate = new RestTemplate();
        String tokenUrl = "https://nid.naver.com/oauth2.0/token";

        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(tokenUrl)
                .queryParam("grant_type", "authorization_code")
                .queryParam("client_id", NAVER_CLIENT_ID)
                .queryParam("client_secret", NAVER_CLIENT_SECRET)
                .queryParam("code", code)
                .queryParam("state", state);

        ResponseEntity<Map> tokenResponse = restTemplate.getForEntity(builder.toUriString(), Map.class);
        String accessToken = (String) tokenResponse.getBody().get("access_token");

        // 2. 사용자 정보 요청
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<Map> userInfoResponse = restTemplate.exchange(
                "https://openapi.naver.com/v1/nid/me", HttpMethod.GET, entity, Map.class
        );

        Map<String, Object> user = (Map<String, Object>) userInfoResponse.getBody().get("response");

        // 3. 세션 저장
        session.setAttribute("user", user);

        // 4. 프론트엔드로 리다이렉트
        response.sendRedirect("http://localhost:3000");
    }

}
