import { useEffect } from 'react';
import axios from 'axios';
import styled from 'styled-components';

const KakaoLoginButton = () => {

  const KakaoButtonWrapper = styled.div`
      width: 100%;
      display: flex;
      justify-content: center;
    `;

  const KakaoButton = styled.button`
      background-color: #fee500;
      border: none;
      border-radius: 8px;
      width: 90%;
      max-width: 400px;
      height: 48px;
      display: flex;
      align-items: center;
      justify-content: center;
      cursor: pointer;
      transition: background 0.2s;
      font-size: 16px;
      font-weight: bold;
      color: #3c1e1e;
      gap: 12px;

      &:hover {
        background-color: #ffe14a;
      }
    `;

  const KakaoSvg = () => (
    <svg width="28" height="28" viewBox="0 0 24 24" fill="none">
      <rect width="24" height="24" rx="6" fill="#fee500" />
      <path d="M12 6C7.58 6 4 8.94 4 12.22c0 2.13 1.81 3.97 4.5 4.72-.18.62-.65 2.18-.75 2.53 0 0-.01.03 0 .04.07.09.19.08.26.06.11-.02 1.61-1.07 2.26-1.51.5.07 1.02.11 1.56.11 4.42 0 8-2.94 8-6.22S16.42 6 12 6z" fill="#3c1e1e" />
    </svg>
  );

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
            alert('✅ 백엔드 응답:', res.data);
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
    <KakaoButtonWrapper>
      <KakaoButton onClick={loginWithKakao}>
        <KakaoSvg />
        카카오 로그인
      </KakaoButton>
    </KakaoButtonWrapper>
  );
};

export default KakaoLoginButton;
