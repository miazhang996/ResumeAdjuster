/*
负责处理所有与用户认证相关的API 通信：
1.用户register
2. User login
3. user log out
4. 获取当前用户信息
5. 认证状态管理
6. google Oauth 认证

该服务用axios 拦截器 自动为需要认证的请求添加JWT 令牌
并统一处理未授权错误 401 ，实现自动log out

Token 存储在浏览器的localstorage 中， 便于持久化session 状态
Use stateless design pattern , 服务器不需要维护session信息。
 */

import axios from 'axios';
import { getAuth, signInWithPopup, GoogleAuthProvider, signOut } from "firebase/auth";
import {app} from'../Config/firebase.js';

const API_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080';

const isBrowser = typeof window !== 'undefined' && window !== null;


const getToken = () => {
    if (isBrowser && window.localStorage) {
        try {
            return window.localStorage.getItem('authToken');
        } catch (e) {
            console.error("Error accessing localStorage:", e);
            return null;
        }
    }
    return null;
};

const setToken = (token) => {
    if (isBrowser && window.localStorage) {
        try {
            window.localStorage.setItem('authToken', token);
        } catch (e) {
            console.error("Error storing token in localStorage:", e);
        }
    }
};

const removeToken = () => {
    if (isBrowser && window.localStorage) {
        try {
            window.localStorage.removeItem('authToken');
        } catch (e) {
            console.error("Error removing token from localStorage:", e);
        }
    }
};








// 创建一个axios instance ， 可以设置默认配置
const apiClient = axios.create({
    baseURL: API_URL,
    headers: {
        'Content-type': 'application/json'
    }
});

// 请求拦截器， 添加认证token 的header
apiClient.interceptors.request.use(
    (config) => {
        const token = getToken();
        if (token) {
            config.headers['Authorization'] = `Bearer ${token}`;
        }
        return config;
    },
    (error) => {
        return Promise.reject(error);
    }
);

//响应拦截器，处理常见错误
apiClient.interceptors.response.use(
    (response) => {
        return response;
    },
    (error) => {
        //处理401 错误， 清除本地token 并重定向到登录页面
        if (error.response && error.response.status === 401) {
            removeToken();
            if (isBrowser) {
                window.location.href = '/login';
            }
        }
        return Promise.reject(error);
    }
);
// 获取 firebase 认证实例
const auth = isBrowser ? getAuth(app) : null;
// 认证相关API 服务
const AuthService = {

    // 注册新用户
    signup: async (firstName, lastName, email, password) => {
        try {
            const response = await apiClient.post('/api/auth/signup', {
                firstName,
                lastName,
                email,
                password
            });
            // 如果后端返回JWT token, store it at localstorage
            if (response.data.token) {
                setToken(response.data.token);
            }
            return response.data;
        } catch (error) {
            throw error;
        }
    },

    // 用户登录
    signin: async (email, password) => {
        try {
            const response = await apiClient.post('/api/auth/login', {
                email,
                password
            });
            if (response.data.token) {
                setToken(response.data.token);
            }
            return response.data;
        } catch (error) {
            throw error;
        }
    },

    // google login
    googleLogin: async () => {
        if (!isBrowser) {
            throw new Error("Google login can only be performed in browser environment");
        }
        try {
            // 确保auth不为null
            if (!auth) {
                throw new Error("Firebase Auth is not initialized");
            }
            //1.使用firebase 进行google 认证
            const provider = new GoogleAuthProvider();
            const result = await signInWithPopup(auth, provider);

            //2. 获取Firebase idToken
            const idToken = await result.user.getIdToken();

            //3. 将Firebase Token 发送到backend , 获取自己的JWT
            const response = await apiClient.post('/api/auth/google', idToken, {
                headers: {
                    'Content-Type': 'text/plain'  // 发送原始idToken
                }
            });

            //存储自己的后端生成的JWT
            if (response.data.token) {
                localStorage.setItem('authToken', response.data.token);
            }
            return response.data;
        } catch (error) {
            console.error('Google Login error', error);
            throw error;
        }
    },

    // 用户 log out
    logout: async () => {
        try {
            // 获取当前token
            const token = getToken();

            //1.调用Firebase 登出 （如果用户是通过firebase 登录的）
            // 只在浏览器环境中执行Firebase登出
            if (isBrowser && auth) {
                try {
                    await signOut(auth);
                } catch (signOutError) {
                    console.warn("Firebase sign out failed:", signOutError);
                }
            }
            // 通知后端用户已登出
            if (token) {
                try {
                    await apiClient.post('/api/auth/logout', null, {
                        headers: {
                            'Authorization': `Bearer ${token}`
                        }
                    });
                } catch (err) {
                    // 即使后端登出失败，我们也继续客户端的登出流程
                    console.warn("Backend logout failed, continuing with client logout");
                }
            }

            //3. 清除本地token
            removeToken();
            return true;
        } catch (error) {
            console.error("Log out error: ", error);
            //即使发生错误，也要确保令牌被清除
            removeToken();
            throw error;
        }
    },

    // 获取当前用户信息
    getCurrentUser: async () => {
        try {
            // 可以添加一个获取当前用户信息的API
            // 这个可能需要在后端实现
            const response = await apiClient.get('/api/auth/user');
            console.log("User data from API:", response.data);
            return response.data;
        } catch (error) {
            throw error;
        }
    },

    // 检查用户是否已经验证
    isAuthenticated: () => {
        return !!getToken();
    },

    // 验证token是否有效
    validateToken: async (token) => {
        try {
            const response = await apiClient.get(`/api/auth/validate?token=${token}`);
            return response.data;
        } catch (error) {
            return false;
        }
    }
};

export default AuthService;