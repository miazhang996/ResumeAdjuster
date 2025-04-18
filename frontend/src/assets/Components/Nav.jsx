import React from 'react';
import {Layout , Menu, Dropdown, Avatar, Space} from 'antd';
import {UserOutlined, LogoutOutlined, DownOutlined } from "@ant-design/icons";
import {useNavigate} from "react-router-dom";
import MenuDivider from "antd/es/menu/MenuDivider.js";

const {Header} =Layout


function Nav(currentUser){
    const navigate=useNavigate();

    function handleLogout(){
        localStorage.removeItem('token')
        navigate('/login')
    }

    // get display name
    function getDisplayName(){
        if(!currentUser) return null;
        if(currentUser==='google'){
            //google 登录显示首字母
            return currentUser.firstName ? currentUser.firstName[0] :'U';
        }else{
            // email login
            return currentUser.lastName || 'user';
        }
    }


    // user menu
    const userMenu=(
        <Menu>
            <Menu.Item key="profile" icon={<UserOutlined/>} onClick={()=>navigate('/profile')}>
                Profile
            </Menu.Item>
            <MenuDivider/>
            <Menu.Item key="logout" icon={<LogoutOutlined />} onClick={handleLogout}>
                Log out
            </Menu.Item>

        </Menu>
    );


    // 登录后显示的用户区域
    const userSection = currentUser ? (
        <Dropdown overlay={userMenu} trigger={['click']}>
            <div style={{ cursor: 'pointer', display: 'flex', alignItems: 'center' }}>
                <Avatar size={36} style={{ backgroundColor: '#bcaaf1' }}>
                    {getDisplayName()}
                </Avatar>
                <DownOutlined style={{ marginLeft: '8px' }} />
            </div>
        </Dropdown>
    ) : (
        <Button type="primary" onClick={() => navigate('/login')}>
            Login
        </Button>
    );

    return (
        <Header style={{
            display: 'flex',
            alignItems: 'center',
            background: '#fff',
            position: 'fixed',
            top: 0,
            left: 0,
            width: '100%',
            height: '64px',
            zIndex: 1001,
            boxShadow: '0 2px 4px rgba(0,0,0,0.1)',
            padding: '0 24px'
        }}>
            {/* Logo区域 */}
            <div style={{ display: 'flex', alignItems: 'center', marginRight: '40px' }}>
                <h1 style={{ margin: 0, cursor: 'pointer', fontSize: '22px', fontWeight: 'bold' }} onClick={() => navigate('/upload')}>
                    ResumeGenius
                </h1>
            </div>

            {/* 导航菜单 */}
            <Menu
                theme="light"
                mode="horizontal"
                defaultSelectedKeys={['home']}
                style={{
                    border: 'none'
                }}
            >
                <Menu.Item key="home" onClick={() => navigate('/')}>Home</Menu.Item>
                <Menu.Item key="features" onClick={() => navigate('/features')}>Features</Menu.Item>
                <Menu.Item key="about" onClick={() => navigate('/about')}>About</Menu.Item>
            </Menu>

            {/* 右侧用户区域 */}
            <div style={{ marginLeft: 'auto',
                paddingRight: '60px'
            }}>
                {userSection}
            </div>
        </Header>
    );


}

export default Nav;