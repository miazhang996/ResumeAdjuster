package org.example.resumeadjuster.Repository.ResumeParserRepository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.example.resumeadjuster.Model.ResumeParserEntity.Education;

@Repository
public interface EducationRepository extends JpaRepository<Education, Long> {
    // JpaRepository 提供了很多现成的数据库操作方法，比如 save, saveAll, findAll 等
}
