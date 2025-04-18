import React, { useState } from 'react';
import { Form, Input, Button, Divider, Typography, Checkbox } from 'antd';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { faGoogle } from '@fortawesome/free-brands-svg-icons';
import { faUser } from '@fortawesome/free-solid-svg-icons';
import AuthService from "../../../Services/AuthService.js";
import { useNavigate } from 'react-router-dom';
import '../../../Styles/Login.css';

const { Title } = Typography;

/*
这里只是Login 页面实现， 所有的 API 逻辑都在 Services/AuthService.js
 */
function Login() {
    const [form] = Form.useForm();
    const [loading, setLoading] = useState(false);
    const [googleLoading, setGoogleLoading] = useState(false);
    const navigate = useNavigate();

    const handleSignin = async (values) => {
        const { email, password } = values;
        setLoading(true);

        try {
            await AuthService.signin(email, password);
            navigate('/upload'); // 跳转到upload 页面
        } catch (error) {
            console.error("SignIn failed: ", error);
            // 处理不同的错误情况
            if (error.response && error.response.status === 401) {
                form.setFields([
                    {
                        name: "password",
                        errors: ['Invalid email or password.']
                    }
                ]);
            } else {
                form.setFields([{
                    name: "email",
                    errors: ['Login failed. Please try again later.']
                }]);
            }
        } finally {
            setLoading(false);
        }
    };

    // 处理 Google 登录
    const handleGoogleLogin = async () => {
        setGoogleLoading(true);
        try {
            await AuthService.googleLogin();
            navigate('/upload');
        } catch (error) {
            console.error("Google login failed : ", error);
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