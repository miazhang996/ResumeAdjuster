package org.example.resumeadjuster.Model.ResumeParserMapper;

import org.example.resumeadjuster.Model.ResumeParserDTO.CourseDTO;
import org.example.resumeadjuster.Model.ResumeParserEntity.Course;

public class CourseMapper {

    public static Course toEntity(CourseDTO dto) {
        Course course = new Course();
        course.setCourseId(dto.getCourseId()); // 可选：插入时一般为 null
        course.setCourseName(dto.getCourseName());
        // 注意：不在这里设置 education，交由 EducationMapper 处理
        return course;
    }

    public static CourseDTO toDto(Course entity) {
        CourseDTO dto = new CourseDTO();
        dto.setCourseId(entity.getCourseId());
        dto.setCourseName(entity.getCourseName());
        return dto;
    }
}
