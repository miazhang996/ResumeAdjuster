package org.example.resumeadjuster.repository;

import org.example.resumeadjuster.model.Education;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EducationRepository extends JpaRepository<Education, Long> {
}
