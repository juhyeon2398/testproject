import React, { useEffect } from 'react';

import KakaoLoginButton from './KakaoLoginButton';
import GoogleLoginButton from './GoogleLoginButton';
import NaverLoginButton from './NaverLoginButton';
import axios from 'axios';


const Login = () => {
  useEffect(() => {
    const checkLogin = async () => {
      try {
        const response = await axios.get('/api/auth/session', {
          withCredentials: true
        });

        if (response.data.loggedIn) {
          alert(`네이버 로그인 성공! ${response.data.user.name || '사용자'}님 환영합니다`);
        }
      } catch (error) {
        console.error('세션 확인 실패:', error);
      }
    };

    checkLogin();
  }, []);
  return (
    <>
      <GoogleLoginButton />
      <KakaoLoginButton />
      <NaverLoginButton />
    </>
  );
};

export default Login;