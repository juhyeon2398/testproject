package org.test.controller;

import java.util.Collections;
import java.util.Map;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken.Payload;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;

import lombok.extern.log4j.Log4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.servlet.http.HttpSession;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

@RestController
@Log4j
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class LoginTestController {
	
	// 구글 로그인
	@PostMapping("/google")
	@ResponseBody
	public ResponseEntity<?> googleLogin(@RequestBody Map<String, String> body) {
		String GOOGLE_CLIENT_ID = "1026499139600-liicdsroc313usf21qm6us5h3et6jf0q.apps.googleusercontent.com";
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
                while ((line = br.readLine()) != null) result.append(line);

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
}
