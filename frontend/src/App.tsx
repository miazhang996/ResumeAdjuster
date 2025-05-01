// src/App.tsx
import React from 'react';
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
// 导入AuthProvider
// @ts-ignore
import { AuthProvider, useAuth } from "./contexts/AuthContext.jsx";

// 创建一个包含受保护路由的组件
// @ts-ignore
function ProtectedRoute({ children }) {
    const { currentUser, loading } = useAuth();

    // 如果正在加载，显示加载指示器
    if (loading) {
        return <div>Loading...</div>;
    }

    // 如果未认证，重定向到登录页面
    if (!currentUser) {
        return <Navigate to="/login" replace />;
    }

    // 已认证，显示子组件
    return children;
}

function AppRoutes() {
    return (
        <Routes>
            {/* 公共路由 - 无需认证可访问 */}
            <Route path="/signup" element={<Signup />} />
            <Route path="/login" element={<Login />} />

            {/* 需要认证的路由 */}
            <Route path="/upload" element={
                <ProtectedRoute>
                    <>
                        <Nav />
                        <UploadedPage1 />
                    </>
                </ProtectedRoute>
            } />
            <Route path="/display" element={
                <ProtectedRoute>
                    <>
                        <Nav />
                        <ResumeDisplay />
                    </>
                </ProtectedRoute>
            } />

            {/* 默认路由 - 重定向到注册页面 */}
            <Route path="/" element={<Navigate to="/signup" replace />} />
        </Routes>
    );
}

function App() {
    return (
        <Router>
            <AuthProvider>
                <AppRoutes />
            </AuthProvider>
        </Router>
    );
}

export default App;