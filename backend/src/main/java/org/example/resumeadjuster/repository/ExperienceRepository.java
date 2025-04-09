package org.example.resumeadjuster.repository;

import org.example.resumeadjuster.model.Experience;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExperienceRepository extends JpaRepository<Experience, Long> {
    // 你可以添加自定义查询方法，例如根据 resumeId 查询某人的所有经历
}
