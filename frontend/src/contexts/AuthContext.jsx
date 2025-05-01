/*
AuthContext.js

提供应用程序范围的认证状态管理和用户身份验证功能
使用react context API 实现全局状态共享，避免组件之间的props 传递

 */

/*
关于 stateless User authentication 的理解：
1. 普通 email , password authentication 流程：
用户注册时，密码存储在数据库中，服务器返回注册成功响应，但此时不进行身份验证
身份验证发生在用户登录时
由于使用的是无状态（serverless）架构，用户信息不会存储在服务器session中
当用户登录成功后，服务器会返回token
Axios拦截器会将这个token与Authorization header一起放入后续请求的header中
服务器端验证token的有效性，验证成功后返回true给前端

2. Google Oauth 认证流程
用户进行Google 登录
Google 认证成功后会提供一个authToken
前端将这个authToken 传递给后端，服务器验证google token 后，生成自己的JWT token
返回给前端 （和 前面医用由 axios interceptor执行 ）

 */




import React, {createContext, useState, useContext, useEffect} from 'react';
import AuthService from "../assets/Services/AuthService.js";


//1 创建认证上下文， 初始化为undefined， 确保useAuth hook 可以检测是否在provider 内部使用

const AuthContext =createContext();
/**
 * 认证提供者组件
 * 包装子组件并提供认证状态和方法
 *
 * @param {Object} props - 组件属性
 * @param {ReactNode} props.children - 子组件
 * @returns {JSX.Element} 带有认证上下文的Provider组件
 */
export const AuthProvider=({children})=>{
    const [currentUser,setCurrentUser]= useState(null); // 当前用户状态
    const[loading,setLoading]=useState(true); // 加载状态 true 表示正在执行异步，false 表示操作完成或初始状态
    const [error,setError]=useState(null);// 错误状态，null 表示无错误，string 表示有错误


    /*
    组件挂载时自动检查用户登录状态
    如果localstorage 中有有效token, 则获取用户信息
     */
    useEffect(()=>{
        // 异步加载用户数据， 通过AuthService 获取当前登录用户信息 (如果认证成功)
        const loadUserData=async()=>{
            try{
                if(AuthService.isAuthenticated()){
                    const userData= await AuthService.getCurrentUser();
                    setCurrentUser(userData);
                }
            }catch(err){
                console.error('Failed to load user:',err);
                setError("Failed to load user data...")
            }finally {
                setLoading(false);
            }
        };
        // 执行加载用户数据的函数
        loadUserData();
    },[]); // 空依赖数组，确保仅在组件挂载时执行一次










    /**
     * Handles regular email/password login
     *
     * @param {string} email - User's email address
     * @param {string} password - User's password
     * @returns {Promise<Object>} Login response including user info and token
     * @throws {Error} Throws error on login failure
     */

    const login= async(email,password)=>{
        try{
            setLoading(true);
            setError(null);
            //call AuthService to perform login
            const response=await AuthService.signin(email,password);
            //login successful, update user state
            setCurrentUser(response.user);
            return response;


        }catch(err){
            console.error('Login failed:', err);
            setError(err.message || 'Login failed');
            throw err;
        }finally{
            setLoading(false); // Login operation completed, end loading state
        }
    };


    /**
     * Handles Google OAuth login
     * Uses Firebase for Google authentication, then exchanges for JWT
     *
     * @returns {Promise<Object>} Login response including user info and token
     * @throws {Error} Throws error on login failure
     */
    const googleLogin=async()=>{
        try{
            setLoading(true);
            setError(null);

            // call Authservice GoogleLogin
            const response=await  AuthService.googleLogin();
            setCurrentUser(response.user);
            return response;
        }catch(err){
            // Log and set error state
            console.error('Google login failed:', err);
            setError(err.message || 'Google login failed');
            throw err; // Rethrow error for caller handling
        }finally {
            // Login operation completed, end loading state
            setLoading(false);
        }
    };


    // handle user logout , clears frontend stored token and user state
    const logout = async ()=>{
        try{
            setLoading(true);

            // call Authservice's logout
            await AuthService.logout();
            // Clear current user state
            setCurrentUser(null);
        }catch(err){
            console.error('Logout failed:', err);
            setError(err.message || 'Logout failed');
        }finally{
            setLoading(false);
        }
    };


    /**
     * Refreshes user data
     * Solves user state inconsistency issues, manually fetches latest user info
     * Especially useful after Google login when user info may not update immediately
     *
     * @returns {Promise<Object>} Latest user data
     * @throws {Error} Throws error on refresh failure
     */
    const refresherUserData=async()=>{
        try{
            setLoading(true);

            //get latest user information
            const userData=await AuthService.getCurrentUser();
            //update user state
            setCurrentUser(userData);
            return userData;
        }catch(err){
            console.error('Failed to refresh user data:', err);
            setError(err.message || 'Failed to refresh user data');
            throw err; // Rethrow error for caller handling
        }finally{setLoading(false);}
    };


    /**
     * Register a new user
     *
     * @param {string} firstName - User's first name
     * @param {string} lastName - User's last name
     * @param {string} email - User's email address
     * @param {string} password - User's password
     * @returns {Promise<Object>} Registration response
     * @throws {Error} Throws error on registration failure
     */
    const signup = async (firstName, lastName, email, password) => {
        try {
            setLoading(true);
            setError(null);

            // Call AuthService to perform registration
            const response = await AuthService.signup(firstName, lastName, email, password);
            return response;
        } catch (err) {
            console.error('Registration failed:', err);
            setError(err.message || 'Registration failed');
            throw err;
        } finally {
            setLoading(false);
        }
    };




// value provided to context consumers
    // contains state and operation methods

    const value ={
        currentUser,
        loading,
        error,
        login,
        googleLogin,
        logout,
        refresherUserData,
        signup,
        setCurrentUser
    };

// return provider component ,passing value object and wrapping children
    return (
        <AuthContext.Provider value={value}>
            {children}
        </AuthContext.Provider>
    );

};

/**
 * Custom Hook to simplify Context usage
 * Provides type safety and error boundary checking
 *
 * @returns {Object} Object containing authentication state and methods
 * @throws {Error} Throws error if used outside Provider
 */

export const useAuth=()=>{
    const context=useContext(AuthContext);
    // validate context exists
    if(context==undefined){
        throw new Error('useAuth must be used inside AuthProvider');
    }
    return context;
};



export default AuthContext;