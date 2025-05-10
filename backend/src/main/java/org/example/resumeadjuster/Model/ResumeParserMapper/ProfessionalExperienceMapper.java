package org.example.resumeadjuster.Model.ResumeParserMapper;

import org.example.resumeadjuster.Model.ResumeParserDTO.ProfessionalExperienceRequestDTO;
import org.example.resumeadjuster.Model.ResumeParserDTO.ProfessionalExperienceResponseDTO;
import org.example.resumeadjuster.Model.ResumeParserEntity.ProfessionalExperience;

public class ProfessionalExperienceMapper {

    // 将 RequestDTO 转为实体类（用于创建）
    public static ProfessionalExperience toEntity(ProfessionalExperienceRequestDTO dto) {
        ProfessionalExperience entity = new ProfessionalExperience();

        entity.setResumeId(dto.getResumeId());
        entity.setExperienceType(dto.getExperienceType());
        entity.setCompanyName(dto.getCompanyName());
        entity.setCompanyLocation(dto.getCompanyLocation());
        entity.setJobTitle(dto.getJobTitle());
        entity.setStartDate(dto.getStartDate());
        entity.setEndDate(dto.getEndDate());

        // ✅ 直接传 List<String>
        entity.setTechStack(dto.getTechStack());
        entity.setBulletPoints(dto.getBulletPoints());

        return entity;
    }

    // 将实体类转为 ResponseDTO（用于返回）
    public static ProfessionalExperienceResponseDTO toDto(ProfessionalExperience entity) {
        ProfessionalExperienceResponseDTO dto = new ProfessionalExperienceResponseDTO();

        dto.setExperienceId(entity.getExperienceId());
        dto.setResumeId(entity.getResumeId());
        dto.setExperienceType(entity.getExperienceType());
        dto.setCompanyName(entity.getCompanyName());
        dto.setCompanyLocation(entity.getCompanyLocation());
        dto.setJobTitle(entity.getJobTitle());
        dto.setStartDate(entity.getStartDate());
        dto.setEndDate(entity.getEndDate());

        // ✅ 直接取出 List<String>
        dto.setTechStack(entity.getTechStack());
        dto.setBulletPoints(entity.getBulletPoints());

        return dto;
    }
}
