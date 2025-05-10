package org.example.resumeadjuster.Model.ResumeParserMapper;

import org.example.resumeadjuster.Model.ResumeParserDTO.CourseDTO;
import org.example.resumeadjuster.Model.ResumeParserDTO.EducationRequestDTO;
import org.example.resumeadjuster.Model.ResumeParserEntity.Course;
import org.example.resumeadjuster.Model.ResumeParserEntity.Education;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class EducationMapper {

    public static Education toEntity(EducationRequestDTO dto) {
        Education entity = new Education();
        entity.setResumeId(dto.getResumeId());
        entity.setLevel(dto.getLevel());
        entity.setSchool(dto.getSchool());
        entity.setLocation(dto.getLocation());
        entity.setMajor(dto.getMajor());
        entity.setStartDate(dto.getStartDate());
        entity.setEndDate(dto.getEndDate());
        entity.setGPA(dto.getGPA());

        if (dto.getCourses() != null) {
            List<Course> courseList = new ArrayList<>();
            for (CourseDTO courseDTO : dto.getCourses()) {
                Course course = CourseMapper.toEntity(courseDTO);
                course.setEducation(entity); // 关键：绑定父对象
                courseList.add(course);
            }
            entity.setCourses(courseList);
        }

        return entity;
    }

    public static EducationRequestDTO toDto(Education entity) {
        EducationRequestDTO dto = new EducationRequestDTO();
        dto.setResumeId(entity.getResumeId());
        dto.setLevel(entity.getLevel());
        dto.setSchool(entity.getSchool());
        dto.setLocation(entity.getLocation());
        dto.setMajor(entity.getMajor());
        dto.setStartDate(entity.getStartDate());
        dto.setEndDate(entity.getEndDate());
        dto.setGPA(entity.getGPA());

        if (entity.getCourses() != null) {
            List<CourseDTO> courseDTOList = entity.getCourses().stream()
                    .map(CourseMapper::toDto)
                    .collect(Collectors.toList());
            dto.setCourses(courseDTOList);
        }

        return dto;
    }
}
