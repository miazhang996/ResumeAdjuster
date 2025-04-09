package org.example.resumeadjuster.controller; // 确保包路径正确

import org.springframework.web.bind.annotation.*;
// Rest Controller，Request Mapping，PostMapping,RequestParam 都来自Spring Web
// 用于创建RESTful 接口
import org.springframework.web.multipart.MultipartFile;
// Spring 处理上传文件的接口
import java.io.File;
import java.io.IOException;
// File和IOException是Java的类，用于文件操作。

@RestController
// 这个注解表示这是一个Rest风格的控制器，返回的内容是字符串或者JSON
// 不是HTML页面。它等价于@Controller + @ResponseBody
@RequestMapping("/api")
// RequestMapping（“/api）
// 为这个控制器类定义了统一的请求路径前缀，所有路径都以/api开头
public class FileUploadController {
    //声明一个名为FileUploadController的类，用来处理文件上传的请求
    @PostMapping("/uploadfile")
    //定义了一个POST请求的处理方法，对应的路径是api/uploadfile（前面有类上的/api）
    public String uploadFile(@RequestParam("myFile") MultipartFile file) {
        //这个方法用于接收上传的文件
        //@RequestParam("myFile")表示从前端表单中接收名为myFile的文件
        //参数类型是MultipartFile，用于表示上传的文件
        //返回类型是String，会作为响应体返回给前端（如“上传成功”或者“上传失败”）
        if (file.isEmpty()) {
            return "上传失败，请选择文件";
        }
        // 如果用户没有上传文件（即file为空），返回错误提示。

        try {
            // 使用try catch 包裹文件保存逻辑，避免出现IO异常
            String uploadDir = System.getProperty("user.dir") + "/uploads/";
            // 获取当前项目所在目录，并拼接出一个/uploads/文件夹，用来保存上传的文件
            // 例如：如果项目运行的路径是/Users/alex/project,那上传路径就是
            // /Users/alex/project/uploads/
            File uploadFolder = new File(uploadDir);
            // 创建一个文件夹对象
            if (!uploadFolder.exists()) {
                uploadFolder.mkdirs();
            }
            // 如果该文件夹不存在，则创建这个目录（支持递归创建）
            // mkdir() 只会创建最后一级目录，如果它的父目录不存在，会失败
            // mkdirs() 会递归地创建所有需要的父目录。
            String filePath = uploadDir + file.getOriginalFilename();
            // 获取上传文件的原始文件名，并与上传目录拼接，形成完整的文件目录
            // 例如：/Users/alex/project/uploads/resume.pdf
            file.transferTo(new File(filePath));
            // 将上传的文件保存到上述路径
            // transferTo 是 MultipartFile 提供的方法，会将内存或临时位置的文件写入目标文件
            return "文件上传成功: " + file.getOriginalFilename();
            // 如果保存成功，返回成功消息，并附上文件名
        } catch (IOException e) {
            // 如果在文件保存过程中发生了IO异常（例如磁盘写入失败、路径不存在等），会进入这里的异常处理逻辑
            e.printStackTrace();
            // 打印异常堆栈信息，方便开发者在控制台查看出错原因（适合调试阶段，正式环境中建议使用日志记录）
            return "上传失败: " + e.getMessage();
            // 将异常信息作为失败原因返回给前端，提示上传失败以及具体原因
        }
    }
}
