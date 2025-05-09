package org.example.resumeadjuster.controller; // 确保包路径正确

import org.example.resumeadjuster.Model.ResumeParserEntity.ProfessionalExperience;
import org.example.resumeadjuster.Repository.ResumeParserRepository.ProfessionalExperienceRepository;
import org.example.resumeadjuster.Service.ResumeParserService.GPTWorkExperienceParser;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.example.resumeadjuster.Model.ResumeParserEntity.Education;
import org.example.resumeadjuster.Repository.ResumeParserRepository.EducationRepository;
import org.example.resumeadjuster.Service.ResumeParserService.GPTEducationParser;

import java.io.File;
import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api")
public class FileUploadController {
    private final GPTEducationParser educationParser;
    private final EducationRepository educationRepository;
    private final GPTWorkExperienceParser workExperienceParser;

    private final ProfessionalExperienceRepository experienceRepository;

    public FileUploadController(
            GPTEducationParser educationParser,
            GPTWorkExperienceParser workExperienceParser,
            EducationRepository educationRepository,
            ProfessionalExperienceRepository experienceRepository
    ) {
        this.educationParser = educationParser;
        this.workExperienceParser = workExperienceParser;
        this.educationRepository = educationRepository;
        this.experienceRepository = experienceRepository;
    }


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

            List<Education> educationList = educationParser.parseEducationFromDocx(filePath, 1);
            educationRepository.saveAll(educationList);

            // 3. 解析工作经历并写入数据库
            List<ProfessionalExperience> experienceList = workExperienceParser.parseWorkExperienceFromDocx(filePath, 1);
            experienceRepository.saveAll(experienceList);
            System.out.println("✔️ Work experience saved to DB for resumeId = " + 1);

            return "教育经历解析并写入成功！文件名: " + file.getOriginalFilename();

        } catch (IOException e) {
            e.printStackTrace();
            return "上传失败: " + e.getMessage();
        }
    }
}
