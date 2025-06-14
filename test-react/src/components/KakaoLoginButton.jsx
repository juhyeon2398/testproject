import { useEffect } from 'react';
import axios from 'axios';

const KakaoLoginButton = () => {
  useEffect(() => {
    // Kakao SDK 로드
    const script = document.createElement('script');
    script.src = 'https://developers.kakao.com/sdk/js/kakao.js';
    script.async = true;
    script.onload = () => {
      if (window.Kakao && !window.Kakao.isInitialized()) {
        window.Kakao.init('56a1079174ea8026777a9d8b11807bce');
        console.log('✅ Kakao SDK 초기화 완료');
      }
    };
    document.body.appendChild(script);
  }, []);

  const loginWithKakao = () => {
    if (!window.Kakao) {
      alert('Kakao SDK가 아직 로드되지 않았습니다.');
      return;
    }

    window.Kakao.Auth.login({
      scope: 'profile_nickname,account_email',
      success: function (authObj) {
        const accessToken = authObj.access_token;
        console.log('🔐 access_token:', accessToken);

        // ✅ axios로 백엔드에 access token 전송
        axios.post('/api/auth/kakao', { accessToken })
          .then((res) => {
            console.log('✅ 백엔드 응답:', res.data);
            // 로그인 처리 또는 세션 저장 등
          })
          .catch((err) => {
            console.error('❌ 백엔드 오류:', err);
          });
      },
      fail: function (err) {
        console.error('카카오 로그인 실패', err);
      },
    });
  };

  return (
    <button onClick={loginWithKakao}>
      카카오 로그인
    </button>
  );
};

export default KakaoLoginButton;
