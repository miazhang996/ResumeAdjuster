import React, { useState } from 'react';
import backgroundImage from '../Images/background_upload.jpg';
import axios from 'axios';
import {
    Card,
    Typography,
    Upload,
    Button,
    Space,
    Row,
    Col,
    List,
    message
} from 'antd';
import {
    UploadOutlined,
    FileTextOutlined,
    CalendarOutlined,
    FileOutlined
} from '@ant-design/icons';
import { useNavigate } from 'react-router-dom';

const { Title, Text } = Typography;
const { Dragger } = Upload;

function UploadedPage1() {
    const [fileList, setFileList] = useState([]);
    const [uploading, setUploading] = useState(false);
    const navigate = useNavigate();

    // 处理文件变更
    const handleChange = ({ fileList }) => {
        // 只保留最后一个文件
        const newFileList = fileList.slice(-1);
        setFileList(newFileList);
    };

    // 上传文件
    const handleUpload = () => {
        const file = fileList[0]?.originFileObj;

        if (!file) {
            message.error('Please select a file first');
            return;
        }

        setUploading(true);

        const formData = new FormData();
        formData.append('myFile', file, file.name);

        // 存储文件信息到localstorage
        const fileInfo={
            name:file.name,
            uploadTime:new Date().toISOString(),
            type:file.type||"unkown type",
            lastModified: new Date(file.lastModified).toLocaleString()

        }
        // 获取现有文件列表或创建新的
        const existingFiles = JSON.parse(localStorage.getItem('uploadedFiles') || '[]');

        // 添加新文件到列表
        existingFiles.push(fileInfo);

        // 保存回 localStorage
        localStorage.setItem('uploadedFiles', JSON.stringify(existingFiles));

        axios.post('http://localhost:8080/api/uploadfile', formData)
            .then(response => {
                message.success('Upload successful');
                setFileList([]);
                setUploading(false);
                navigate('/display');
            })
            .catch(error => {
                console.error('Upload error:', error);
                message.error('Upload failed');
                setUploading(false);
            });
    };

    // 文件详情显示
    const fileDetails = () => {
        const file = fileList[0]?.originFileObj;

        if (!file) return null;

        const data = [
            {
                title: 'File Name',
                value: file.name,
                icon: <FileTextOutlined />
            },
            {
                title: 'File Type',
                value: file.type || 'Unknown type',
                icon: <FileOutlined />
            },
            {
                title: 'Last Modified',
                value: new Date(file.lastModified).toLocaleString(),
                icon: <CalendarOutlined />
            }
        ];

        return (
            <Card
                title="File Details"
                bordered={false}
                style={{ marginTop: 16 }}
            >
                <List
                    itemLayout="horizontal"
                    dataSource={data}
                    renderItem={(item) => (
                        <List.Item>
                            <List.Item.Meta
                                avatar={item.icon}
                                title={item.title}
                                description={item.value}
                            />
                        </List.Item>
                    )}
                />
            </Card>
        );
    };

    // 上传组件props
    const uploadProps = {
        onRemove: () => {
            setFileList([]);
        },
        beforeUpload: (file) => {
            setFileList([
                {
                    uid: '1',
                    name: file.name,
                    status: 'done',
                    originFileObj: file,
                }
            ]);
            return false;
        },
        fileList,
        onChange: handleChange
    };

    return (
        <div style={{
            backgroundImage: `url(${backgroundImage})`,
            backgroundSize: 'cover',
            backgroundPosition: 'center',
            backgroundRepeat: 'no-repeat',
            width: '100vw', // 使用视窗宽度单位
            height: 'calc(100vh - 64px)', // 使用视窗高度单位
            position: 'fixed', // 固定位置
            top: '64px',
            left: 0,
            zIndex: 0
        }}>
            <div style={{
                position: 'absolute',
                top: '50%',
                left: '8%', // 这里控制左侧距离，较小的值表示更靠左
                transform: 'translateY(-50%)',
                maxWidth: '400px',
                width: '100%',
                backgroundColor: 'white',
                padding: '30px',
                borderRadius: '8px',
                boxShadow: '0 4px 12px rgba(0, 0, 0, 0.1)',
                textAlign: 'center'
            }}>
                <Title level={2} style={{ marginBottom: 0 }}>Resume Scan</Title>
                <Title level={3} style={{ marginTop: 10, marginBottom: 30 }}>with AI</Title>

                <Title level={4} style={{ marginBottom: 20 }}>Upload Your Resume</Title>

                <Dragger {...uploadProps}>
                    <p className="ant-upload-drag-icon">
                        <UploadOutlined style={{ fontSize: '28px', color: '#1890ff' }} />
                    </p>
                    <p className="ant-upload-text">Click or drag file to this area to upload</p>
                    <p className="ant-upload-hint">
                        Support for a single file upload. PDF or Word resume formats recommended.
                    </p>
                </Dragger>

                <Button
                    type="primary"
                    onClick={handleUpload}
                    disabled={fileList.length === 0}
                    loading={uploading}
                    style={{ marginTop: '20px', width: '100%' }}
                >
                    Upload Resume
                </Button>

                {fileList.length > 0 && fileDetails()}
            </div>
        </div>
    );
}

export default UploadedPage1;