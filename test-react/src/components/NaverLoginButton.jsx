// NaverLoginButton.jsx
import React from 'react';
import axios from 'axios';

const NaverLoginButton = () => {
  const handleLogin = async () => {
    try {
      const response = await axios.get('/api/auth/naver/login', {
        withCredentials: true
      });

      if (response.data && response.data.redirectUrl) {
        window.location.href = response.data.redirectUrl;
      }
      if (response.data.loggedIn) {
        alert(`네이버 로그인 성공! ${response.data.user.name || '사용자'}님 환영합니다`);
      }
    } catch (error) {
      console.error('네이버 로그인 실패:', error);
    }
  };

  return <button onClick={handleLogin}>네이버로 로그인</button>;
};

export default NaverLoginButton;
