import React from 'react';
import axios from 'axios';
import {jwtDecode} from 'jwt-decode';
import { GoogleOAuthProvider, GoogleLogin } from '@react-oauth/google';

const GoogleAuth = () => {
    const clientId = '1026499139600-liicdsroc313usf21qm6us5h3et6jf0q.apps.googleusercontent.com';
    const handleLoginSuccess = async (credentialResponse) => {
    const credential = credentialResponse.credential;
    console.log(credential);

    // 토큰 디코딩 (선택사항)
    const decoded = jwtDecode(credential);
    console.log('구글 사용자 정보:', decoded);

    try {
      const response = await axios.post(
        '/api/auth/google',
        { token: credential },
        { withCredentials: true } // 세션 쿠키 공유
      );

      alert('로그인 성공: ' + response.data);
    } catch (err) {
      console.error('로그인 실패', err.response?.data || err.message);
      alert('로그인 실패');
    }
  };

    return (
        <GoogleOAuthProvider clientId={clientId}>
            <GoogleLogin
            onSuccess={handleLoginSuccess}
            onError={() => console.log('Google 로그인 실패')}
            />
        </GoogleOAuthProvider>
    );
};

export default GoogleAuth;