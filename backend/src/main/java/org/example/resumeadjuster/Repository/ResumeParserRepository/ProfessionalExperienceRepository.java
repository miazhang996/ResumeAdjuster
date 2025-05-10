package org.example.resumeadjuster.Repository.ResumeParserRepository;

import org.example.resumeadjuster.Model.ResumeParserEntity.ProfessionalExperience;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProfessionalExperienceRepository extends JpaRepository<ProfessionalExperience, Long> {
    // JpaRepository 提供的增删查改方法：save, saveAll, findById, findAll 等
}
