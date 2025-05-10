package org.example.resumeadjuster.Model.ResumeParserDTO;

public class CourseDTO {
    private Long courseId; // 可选：响应用；新增时可为空
    private String courseName;

    // Getter & Setter
    public Long getCourseId() { return courseId; }
    public void setCourseId(Long courseId) { this.courseId = courseId; }

    public String getCourseName() { return courseName; }
    public void setCourseName(String courseName) { this.courseName = courseName; }
}


