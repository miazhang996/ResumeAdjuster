// src/App.tsx
import React, {useEffect, useState} from 'react';
import {BrowserRouter as Router, Routes, Route, Navigate} from 'react-router-dom';

// @ts-ignore
import ResumeDisplay from "./assets/Components/ResumeDisplay.jsx";
// @ts-ignore
import UploadedPage1 from "./assets/Components/UploadedPage1.jsx";
// @ts-ignore
import Login from "./assets/Components/Auth/LoginPage/Login.jsx";
// @ts-ignore
import Signup from "./assets/Components/Auth/SignupPage/Signup.jsx";
// @ts-ignore
import Nav from "./assets/Components/Nav.jsx";
// @ts-ignore
import AuthService from "./assets/Services/AuthService.js";

function App() {

    const [currentUser, setCurrentUser]=useState(null);
    const[loading, setLoading]=useState(false);
    //
    // useEffect(() => {
    //     // 延迟执行认证检查，确保页面和相关库完全加载
    //     const timer = setTimeout(() => {
    //         const initializeAuth = async () => {
    //             try {
    //                 setLoading(true);
    //
    //                 // 确保在浏览器环境中执行，且所有必要的API都可用
    //                 if (typeof window !== 'undefined' &&
    //                     window.localStorage &&
    //                     typeof AuthService !== 'undefined') {
    //
    //                     // 检查是否有token
    //                     if (AuthService.isAuthenticated()) {
    //                         try {
    //                             // 获取当前用户信息
    //                             const userData = await AuthService.getCurrentUser();
    //                             console.log("User data fetched:", userData);
    //                             setCurrentUser(userData);
    //                         } catch (userError) {
    //                             console.error("Failed to fetch user data:", userError);
    //                             // 清除token但不要重定向(避免循环)
    //                             if (window.localStorage) {
    //                                 window.localStorage.removeItem('authToken');
    //                             }
    //                             setCurrentUser(null);
    //                         }
    //                     } else {
    //                         console.log("No auth token found, user not authenticated");
    //                     }
    //                 }
    //             } catch (error) {
    //                 console.error("Auth initialization error:", error);
    //                 setCurrentUser(null);
    //             } finally {
    //                 setLoading(false);
    //                 console.log("Auth initialization completed");
    //             }
    //         };
    //
    //         initializeAuth();
    //     }, 500); // 延迟500毫秒，确保页面完全加载
    //
    //     // 清除定时器，避免内存泄漏
    //     return () => clearTimeout(timer);
    // }, []);



    return (
        <Router>
            <Routes>
                <Route path="/signup" element={<Signup/>} />
                <Route path="/login" element={<Login/>} />


                {/* 需要导航栏的页面 */}
                <Route path="/upload" element={
                    <>
                        <Nav currentUser={currentUser} />
                        <UploadedPage1 />
                    </>
                } />
                <Route path="/display" element={
                    <>
                        <Nav currentUser={currentUser} />
                        <ResumeDisplay />
                    </>
                } />


                <Route path="/" element={<Navigate to="/signup" replace />} />  {/* 重定向根路径到注册页面 */}
            </Routes>
        </Router>
    );
}

export default App;

