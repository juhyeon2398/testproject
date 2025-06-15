import { useEffect } from "react";
import axios from "axios";
import { useNavigate, useLocation } from "react-router-dom";

const NaverCallback = () => {
    const location = useLocation();
    const navigate = useNavigate();

    useEffect(() => {
        const query = new URLSearchParams(location.search);
        const code = query.get("code");
        const state = query.get("state");

        if (!code || !state) {
            alert("로그인 실패: code 또는 state 누락");
            return;
        }

        // 백엔드로 code, state 전달
        axios.post("/api/auth/naver", {
            code,
            state,
        }, {
            withCredentials: true,
        })
            .then((res) => {
                alert(`네이버 로그인 성공: ${res.data.email}`);
                console.log("사용자 정보:", res.data);
                navigate("/"); // 홈으로 리다이렉트
            })
            .catch((err) => {
                alert("네이버 로그인 실패");
                console.error(err);
            });
    }, [location, navigate]);

    return <div>네이버 로그인 처리 중...</div>;
};

export default NaverCallback;