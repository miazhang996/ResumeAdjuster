import React,{useState} from 'react';
import {Form,Input,Button,Divider,Typography,Checkbox} from 'antd';
import {FontAwesomeIcon} from '@fortawesome/react-fontawesome';
import { faGoogle } from '@fortawesome/free-brands-svg-icons';
import AuthService from "../../../Services/AuthService.js";
import { useNavigate } from 'react-router-dom';
import '../../../Styles/Signup.css';

const {Title} = Typography;


/*
这里只是Sign up 页面实现， 所有的 API 逻辑都在 Services/AuthService.js
 */
function Signup(){
    const [form]=Form.useForm();
    const[loading,setLoading]=useState(false);
    const[googleLoading,setGoogleLoading]=useState(false);
    const navigate=useNavigate();

    const handleSignup = async (values)=>{
        const {firstName,lastName,email,password} = values;
        setLoading(true);

        try{
            await AuthService.signup(firstName,lastName,email,password);
            navigate('/login') // 跳转到login 页面
        }catch(error){
            console.error("SignUp failed: ",error);
            //处理不同的错误情况
            if(error.response && error.response.status===409){
                form.setFields([
                    {
                        name:"email",
                        errors: ['This email is already registered.']
                    }
                ]);

            }else{
                form.setFields([{
                    name:"email",
                    errors:['Registration failed. Please try again later.']
                }]);

            }


        }finally{
            setLoading(false);

        }

    };

    // 处理 Google 登录
   const handleGoogleLogin=async ()=>{
       setGoogleLoading(true);
       try{
           await AuthService.googleLogin();
           navigate('/upload');// 跳转到upload 页面

       }catch (error){
           console.error("Google login failed : ",error );

       }finally{
           setGoogleLoading(false);
       }

   };

    return (
        <div className="signup-container">
            <div className="signup-card">
            <Title level={2} className={"sign-up-title"}>Sign up for an account</Title>
                <Form
                    form={form}
                    name="sign-up-form"
                    onFinish={handleSignup}
                    layout="vertical">
                    <div className="name-row">
                        <Form.Item
                            name="firstName"
                            label="First Name"
                            className="name-item"
                            rules={[
                                {required: true, message: "Please enter your first name! "}
                            ]}
                        >
                            <Input
                                placeholder="First Name"
                                size="large"

                            />
                        </Form.Item>
                        <Form.Item
                            name="lastName"
                            label="Last Name"
                            className="name-item"
                            rules={[
                                {required: true, message: "Please enter your last name! "}
                            ]}
                        >
                            <Input
                                placeholder="Last Name"
                                size="large"

                            />
                        </Form.Item>

                    </div>

                    {/* Email  栏*/}

                    <Form.Item
                        name="email"
                        label="Email Address"
                        rules={[
                            {required: true, message: "Please enter your email! "},
                            {type: 'email', message: 'Please enter a valid email address !'}
                        ]}
                    >
                        <Input
                            placeholder="Email Address"
                            size="large"

                        />
                    </Form.Item>


                    {/* Password 栏*/}

                    <Form.Item
                        name="password"
                        label="Password"
                        rules={[
                            {required: true, message: "Please enter your password! "},
                            {min: 8, message: 'Password must be at least 8 characters!'}
                        ]}
                    >
                        <Input.Password
                            placeholder="Password"
                            size="large"


                        />
                    </Form.Item>

                    {/* Agreement */}
                    <div className="terms-text">
                        By signing up you agree to our <a href="/terms">Terms and Conditions</a> and <a href="/privacy">Privacy
                        Policy</a>.
                    </div>

                    {/* Button Register */}
                    <Form.Item>
                        <Button
                        type="primary"
                        htmlType='submit'
                        className="sign-up-button"
                        block
                        loading={loading}
                        size={"large"}>

                            Register
                        </Button>
                    </Form.Item>
                    <div>
                        Already have an account <a onClick={()=>navigate('/login')}>Log in</a>
                    </div>

                    <Divider plain>Or register with</Divider>
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


export default Signup;