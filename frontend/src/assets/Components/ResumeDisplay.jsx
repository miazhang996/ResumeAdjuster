import React, { useState, useEffect } from 'react';
import {
    Card,
    Input,
    List,
    Typography,
    Layout,
    Row,
    Col,
    Space,
    Empty,
    Tooltip
} from 'antd';
import {
    SearchOutlined,
    FileTextOutlined,
    EyeOutlined,
    EditOutlined,
    DeleteOutlined,
    ClockCircleOutlined
} from '@ant-design/icons';

const { Title, Text } = Typography;
const { Content } = Layout;

function ResumeDisplay() {
    // 从localStorage获取文件列表
    const [files, setFiles] = useState([]);
    const [searchTerm, setSearchTerm] = useState('');
    const [selectedFile, setSelectedFile] = useState(null);

    useEffect(() => {
        // 从localStorage加载文件
        const storedFiles = JSON.parse(localStorage.getItem('uploadedFiles') || '[]');
        setFiles(storedFiles);
    }, []);

    // 根据搜索词过滤文件
    const filteredFiles = files.filter(file =>
        file.name.toLowerCase().includes(searchTerm.toLowerCase())
    );

    const handleFileSelect = (file) => {
        // 如果点击的是当前选中的文件，则取消选择
        if (selectedFile && selectedFile.name === file.name) {
            setSelectedFile(null);
        } else {
            setSelectedFile(file);
        }
    };

    const handleEdit = (file, e) => {
        e.stopPropagation(); // 阻止点击事件传播，避免触发选择文件
        console.log('Edit file:', file);
        // 这里添加编辑逻辑
    };

    const handleView = (file, e) => {
        e.stopPropagation(); // 阻止点击事件传播，避免触发选择文件
        setSelectedFile(file);
        console.log('View file:', file);
        // 这里添加查看逻辑
    };

    const handleDelete = (file, e) => {
        e.stopPropagation(); // 阻止点击事件传播

        // 从文件列表中删除
        const updatedFiles = files.filter(f => f.name !== file.name);

        // 更新状态
        setFiles(updatedFiles);

        // 如果删除的是当前选中的文件，取消选择
        if (selectedFile && selectedFile.name === file.name) {
            setSelectedFile(null);
        }

        // 更新localStorage
        localStorage.setItem('uploadedFiles', JSON.stringify(updatedFiles));

        console.log('Delete file:', file.name);
        // 后续可以在这里添加与后端的通信代码
    };

    // 格式化时间函数
    const formatDate = (dateString) => {
        const date = new Date(dateString);
        return date.toLocaleString();
    };

    // 处理文件类型显示，移除application/前缀
    const formatFileType = (fileType) => {
        if (!fileType) return "Unknown";

        // 移除application/前缀
        return fileType.replace('application/', '');
    };

    return (
        <Layout style={{
            height: '100vh',
            padding: '20px',
            display: 'flex',
            justifyContent: 'center',
            backgroundColor: '#f0f2f5'
        }}>
            <Content style={{ maxWidth: '700px', width: '100%' }}>
                {/* 标题区域放在卡片外部 */}
                <div style={{
                    display: 'flex',
                    alignItems: 'center',
                    marginBottom: '15px',
                    paddingLeft: '5px'
                }}>
                    <FileTextOutlined style={{ fontSize: '24px', color: '#1890ff', marginRight: '10px' }} />
                    <Title level={3} style={{ margin: 0 }}>Resume List</Title>
                </div>

                {/* 搜索框 */}
                <Input
                    placeholder="Search Files"
                    prefix={<SearchOutlined />}
                    value={searchTerm}
                    onChange={(e) => setSearchTerm(e.target.value)}
                    style={{
                        width: '100%',
                        marginBottom: '20px',
                        height: '40px',
                        borderRadius: '4px'
                    }}
                />

                <Card
                    style={{
                        width: '100%',
                        boxShadow: '0 1px 2px rgba(0, 0, 0, 0.1)',
                        borderRadius: '8px'
                    }}
                    bodyStyle={{ padding: '0' }}
                    bordered={false}
                >
                    {filteredFiles.length > 0 ? (
                        <List
                            itemLayout="horizontal"
                            dataSource={filteredFiles}
                            style={{ width: '100%' }}
                            renderItem={(file) => (
                                <List.Item
                                    onClick={() => handleFileSelect(file)}
                                    style={{
                                        padding: '16px 20px',
                                        cursor: 'pointer',
                                        backgroundColor: selectedFile && selectedFile.name === file.name ? '#e6f7ff' : 'transparent',
                                        borderRadius: '4px',
                                        borderBottom: '1px solid #f0f0f0',
                                        marginBottom: '0'
                                    }}
                                >
                                    <Row style={{ width: '100%', alignItems: 'center' }}>
                                        <Col span={3}>
                                            <FileTextOutlined style={{ fontSize: '22px', color: '#1890ff' }} />
                                        </Col>
                                        <Col span={15}> {/* 文件名和信息区域 */}
                                            <div>
                                                <Text style={{
                                                    fontSize: '16px',
                                                    fontWeight: '500',
                                                    display: 'block',
                                                    lineHeight: '1.5',
                                                    marginBottom: '5px'
                                                }}>
                                                    {file.name}
                                                </Text>
                                                <div style={{ display: 'flex', alignItems: 'center' }}>
                                                    <ClockCircleOutlined style={{ color: '#8c8c8c', marginRight: '5px', fontSize: '14px' }} />
                                                    <Text type="secondary" style={{ fontSize: '13px' }}>
                                                        Uploaded: {formatDate(file.uploadTime)}
                                                    </Text>
                                                </div>
                                            </div>
                                        </Col>
                                        <Col span={6} style={{ textAlign: 'right' }}> {/* 右侧图标 */}
                                            <Space size="middle">
                                                <Tooltip title="View">
                                                    <EyeOutlined
                                                        onClick={(e) => handleView(file, e)}
                                                        style={{ fontSize: '20px', color: '#1890ff' }}
                                                    />
                                                </Tooltip>
                                                <Tooltip title="Edit">
                                                    <EditOutlined
                                                        onClick={(e) => handleEdit(file, e)}
                                                        style={{ fontSize: '20px', color: '#52c41a' }}
                                                    />
                                                </Tooltip>
                                                <Tooltip title="Delete">
                                                    <DeleteOutlined
                                                        onClick={(e) => handleDelete(file, e)}
                                                        style={{ fontSize: '20px', color: '#ff4d4f' }}
                                                    />
                                                </Tooltip>
                                            </Space>
                                        </Col>
                                    </Row>
                                </List.Item>
                            )}
                        />
                    ) : (
                        <Empty
                            description="No files found"
                            image={Empty.PRESENTED_IMAGE_SIMPLE}
                            style={{ margin: '40px 0', padding: '20px' }}
                        />
                    )}
                </Card>

                {selectedFile && (
                    <Card
                        title={
                            <Space>
                                <FileTextOutlined style={{ fontSize: '20px', color: '#1890ff' }} />
                                <span style={{ fontWeight: 'bold' }}>{selectedFile.name}</span>
                            </Space>
                        }
                        style={{
                            marginTop: '20px',
                            boxShadow: '0 1px 2px rgba(0, 0, 0, 0.1)',
                            borderRadius: '8px'
                        }}
                    >
                        <div>
                            <Title level={4}>File Details</Title>
                            <List>
                                <List.Item>
                                    <Text strong>File Type:</Text> {formatFileType(selectedFile.type)}
                                </List.Item>
                                <List.Item>
                                    <Text strong>Upload Time:</Text> {formatDate(selectedFile.uploadTime)}
                                </List.Item>
                                <List.Item>
                                    <Text strong>Last Modified:</Text> {selectedFile.lastModified}
                                </List.Item>
                            </List>
                        </div>
                    </Card>
                )}
            </Content>
        </Layout>
    );
}

export default ResumeDisplay;