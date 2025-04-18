package org.example.resumeadjuster.controller; // 确保包路径正确

import org.example.resumeadjuster.model.Education;
import org.example.resumeadjuster.model.Experience;
import org.example.resumeadjuster.repository.EducationRepository;
import org.example.resumeadjuster.repository.ExperienceRepository;
import org.example.resumeadjuster.service.EducationParser;
import org.example.resumeadjuster.service.EducationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;

@RestController
@RequestMapping("/api")
public class FileUploadController {

    @Autowired
    private ExperienceRepository experienceRepository;

    @Autowired
    private EducationRepository educationRepository;

    @Autowired
    private EducationService educationService;


    @PostMapping("/uploadfile")
    public String uploadFile(@RequestParam("myFile") MultipartFile file) {
        if (file.isEmpty()) {
            return "上传失败，请选择文件";
        }

        try {
            // 1. 保存上传的文件到临时路径
            String uploadDir = System.getProperty("user.dir") + "/uploads/";
            File uploadFolder = new File(uploadDir);
            if (!uploadFolder.exists()) {
                uploadFolder.mkdirs();
            }
            String filePath = uploadDir + file.getOriginalFilename();
            file.transferTo(new File(filePath));

            // 2. 调用 Parser 解析教育经历（resumeId 暂定写死为 1）
            var educationList = educationService.parseEducationFromDocx(filePath, 1);

            // 3. 保存解析结果进数据库
            educationRepository.saveAll(educationList);

            return "教育经历解析并写入成功！文件名: " + file.getOriginalFilename();
        } catch (IOException e) {
            e.printStackTrace();
            return "上传失败: " + e.getMessage();
        }
    }


}
