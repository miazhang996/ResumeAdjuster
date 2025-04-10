package org.example.resumeadjuster.service;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.example.resumeadjuster.model.Education;

import java.io.FileInputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

public class EducationParser {

    public static Education parseEducationFromDocx(String filePath, Integer resumeId) {
        Education education = new Education();

        try (FileInputStream fis = new FileInputStream(filePath);
             XWPFDocument document = new XWPFDocument(fis)) {

            List<XWPFParagraph> paragraphs = document.getParagraphs();
            boolean inEducationSection = false;

            for (XWPFParagraph para : paragraphs) {
                String text = para.getText().trim();

                if (text.equalsIgnoreCase("EDUCATION")) {
                    inEducationSection = true;
                    continue;
                }

                if (inEducationSection) {
                    // 找到大学行
                    if (text.contains("Western University")) {
                        education.setSchool("Western University");
                        education.setLocation("Canada"); // 可根据需要设定
                    }

                    // 找到学位信息和时间
                    if (text.contains("Bachelor of Science")) {
                        education.setLevel("Bachelor of Science");
                        education.setProgram("Computer Science and Scientific Computing and Numerical Methods");
                        education.setStartDate(LocalDate.of(2019, 9, 1));
                        education.setEndDate(LocalDate.of(2023, 5, 1));
                    }

                    // 教育部分只提取一次，提完就跳出
                    break;
                }
            }

            education.setResumeId(resumeId);

        } catch (IOException e) {
            e.printStackTrace();
        }

        return education;
    }
}
