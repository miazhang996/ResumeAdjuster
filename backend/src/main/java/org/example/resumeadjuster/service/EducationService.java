package org.example.resumeadjuster.service;

import org.example.resumeadjuster.model.Education;
import org.springframework.stereotype.Service;

@Service
public class EducationService {
    public Education parseEducationFromDocx(String path, Integer resumeId) {
        return EducationParser.parseEducationFromDocx(path, resumeId);
    }
}
