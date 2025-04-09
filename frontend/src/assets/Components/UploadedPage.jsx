// UploadedPage.jsx
import React, { useEffect, useState } from "react";
import axios from "axios";

function UploadedPage() {
    const [files, setFiles] = useState([]);

    useEffect(() => {
        axios.get("http://localhost:8081/api/uploaded-files")
            .then((res) => setFiles(res.data))
            .catch((err) => console.error("获取文件列表失败:", err));
    }, []);

    return (
        <div style={{ display: "flex", height: "100vh" }}>
            {/* 左侧：文件列表 */}
            <div style={{ width: "300px", backgroundColor: "#f0f0f0", padding: "20px" }}>
                <h3>📁 已上传文件</h3>
                <ul>
                    {files.map((file, index) => (
                        <li key={index}>{file}</li>
                    ))}
                </ul>
            </div>

            {/* 右侧：欢迎或提示 */}
            <div style={{ flex: 1, padding: "40px" }}>
                <h2>请选择左侧文件，或继续上传简历</h2>
            </div>
        </div>
    );
}

export default UploadedPage;
