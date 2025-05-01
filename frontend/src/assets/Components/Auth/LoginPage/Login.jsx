import React, { useState ,useEffect } from 'react';
import { Form, Input, Button, Divider, Typography, Checkbox,Alert,message} from 'antd';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { faGoogle } from '@fortawesome/free-brands-svg-icons';
import { faUser } from '@fortawesome/free-solid-svg-icons';
import { useNavigate,useLocation  } from 'react-router-dom';
import '../../../Styles/Login.css';
import {useAuth} from "../../../../contexts/AuthContext.jsx";



const { Title } = Typography;

/*
Login page implementation using AuthContext for authentication managment
 */
function Login() {
    const [form] = Form.useForm();
    const [loading, setLoading] = useState(false);
    const [googleLoading, setGoogleLoading] = useState(false);
    const navigate = useNavigate();
    const [loginError, setLoginError] = useState(null);
    const location = useLocation(); // 使用useLocation获取导航状态

    // Use the auth context instead of direct AuthService calls
    const { login, googleLogin, refresherUserData } = useAuth();

    // 检查导航状态中是否有注册成功的消息
    useEffect(() => {
        if (location.state?.registrationSuccess) {
            message.success('Registration successful! Please log in.');
            // 清除状态，防止刷新页面时再次显示消息
            navigate(location.pathname, { replace: true, state: {} });
        }
    }, [location, navigate]);

    const handleSignin = async (values) => {
        const { email, password} = values;
        setLoading(true);

        try {
            const response = await login(email,password);
            console.log("Login response:", response);

            // Refresh user data to ensure all components have the latest user info
            await refresherUserData();

            navigate('/upload');
        } catch (error) {
            console.error("SignIn failed: ", error);
            if (error.response && error.response.status === 401) {
                setLoginError('Invalid email or password.');
            } else {
                setLoginError('Login failed. Please try again later.');
            }
        } finally {
            setLoading(false);
        }
    };

    // 处理 Google 登录
    const handleGoogleLogin = async () => {
        setGoogleLoading(true);
        try {
            await googleLogin();
            // Refresh user data to ensure all components have the latest user info
            await refresherUserData();
            navigate('/upload');
        } catch (error) {
            console.error("Google login failed : ", error);
            setLoginError('Google login failed. Please try again later.');
        } finally {
            setGoogleLoading(false);
        }
    };

    return (
        <div className="signin-container">
            <div className="signin-card">
                <div className="user-icon-container">
                    <FontAwesomeIcon icon={faUser} className="user-icon" />
                </div>
                <Title level={2} className={"sign-in-title"}>Sign in</Title>

                {loginError && (
                    <Alert
                        message="Login Error"
                        description={loginError}
                        type="error"
                        showIcon
                        style={{ marginBottom: '20px' }}
                        closable
                        onClose={() => setLoginError(null)}
                    />
                )}

                <Form
                    form={form}
                    name="sign-in-form"
                    onFinish={handleSignin}
                    layout="vertical">

                    {/* Email 栏 */}
                    <Form.Item
                        name="email"
                        label="Email Address"
                        rules={[
                            { required: true, message: "Please enter your email! " },
                            { type: 'email', message: 'Please enter a valid email address!' }
                        ]}
                    >
                        <Input
                            placeholder="Email Address"
                            size="large"

                        />
                    </Form.Item>

                    {/* Password 栏 */}
                    <Form.Item
                        name="password"
                        label="Password"
                        rules={[
                            { required: true, message: "Please enter your password! " }
                        ]}
                    >
                        <Input.Password
                            placeholder="Password"
                            size="large"
                        />
                    </Form.Item>

                    {/* Remember me and Forgot password */}
                    <div className="signin-options">
                        <Form.Item name="remember" valuePropName="checked" noStyle>
                            <Checkbox>Remember me</Checkbox>
                        </Form.Item>
                        <a className="forgot-password" href="/forgot-password">
                            Forgot password?
                        </a>
                    </div>

                    {/* Button Sign in */}
                    <Form.Item>
                        <Button
                            type="primary"
                            htmlType='submit'
                            className="sign-in-button"
                            block
                            loading={loading}
                            size={"large"}>
                            Sign in
                        </Button>
                    </Form.Item>
                    <div>
                        Don't have an account? <a onClick={() => navigate('/signup')}>Sign up</a>
                    </div>

                    <Divider plain>Or sign in with</Divider>
                    <Button
                        icon={<FontAwesomeIcon icon={faGoogle} />}
                        onClick={handleGoogleLogin}
                        loading={googleLoading}
                        size="large"
                        className="google-button"
                        block
                    >
                        Continue with Google
                    </Button>
                </Form>
            </div>
        </div>
    );
}

export default Login;