package org.example.resumeadjuster.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "education")
public class Education {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "education_id")
    private Long educationId;

    @Column(name = "resume_id")
    private Integer resumeId;

    @Column(name = "level", length = 50)
    private String level;

    @Column(name = "school", length = 200)
    private String school;

    @Column(name = "location", length = 200)
    private String location;

    @Column(name = "program", length = 200)
    private String program;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "GPA")
    private Double GPA;

    // -------- Getter & Setter ----------
    public Long getEducationId() { return educationId; }
    public void setEducationId(Long educationId) { this.educationId = educationId; }

    public Integer getResumeId() { return resumeId; }
    public void setResumeId(Integer resumeId) { this.resumeId = resumeId; }

    public String getLevel() { return level; }
    public void setLevel(String level) { this.level = level; }

    public String getSchool() { return school; }
    public void setSchool(String school) { this.school = school; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getProgram() { return program; }
    public void setProgram(String program) { this.program = program; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    public Double getGPA() { return GPA; }
    public void setGPA(Double GPA) { this.GPA = GPA; }
}
