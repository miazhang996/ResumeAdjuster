package org.example.resumeadjuster.service;

import org.example.resumeadjuster.model.Education;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EducationService {

    private GPTEducationParser gptEducationParser;

    // ✅ 构造函数注入推荐写法，更利于测试
    @Autowired
    public EducationService(GPTEducationParser gptEducationParser) {
        this.gptEducationParser = gptEducationParser;
    }

    // ✅ 提供对外的解析接口，内部调用注入的 Parser
    public List<Education> parseEducationFromDocx(String path, Integer resumeId) {
        return gptEducationParser.parseEducationFromDocx(path, resumeId);
    }
}
