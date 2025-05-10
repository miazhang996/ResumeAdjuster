package org.example.resumeadjuster.Service.ResumeParserService;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.resumeadjuster.Model.ResumeParserDTO.ProfessionalExperienceRequestDTO;
import org.example.resumeadjuster.Model.ResumeParserEntity.ProfessionalExperience;
import org.example.resumeadjuster.Model.ResumeParserMapper.ProfessionalExperienceMapper;
import org.example.resumeadjuster.Repository.ResumeParserRepository.ProfessionalExperienceRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class GPTWorkExperienceParser {

    @Value("${openai.api.key}")
    private String apiKey;

    private static final String GPT_URL = "https://api.openai.com/v1/chat/completions";
    private static final ObjectMapper mapper = new ObjectMapper();

    @Autowired
    private ProfessionalExperienceRepository professionalExperienceRepository;

    @Transactional
    public List<ProfessionalExperience> parseWorkExperienceFromDocx(String filePath, Integer resumeId) {
        String workText = extractWorkSection(filePath);
        System.out.println("========== WORK EXPERIENCE SECTION EXTRACTED ==========");
        System.out.println(workText);

        String prompt = buildPrompt(workText);
        String response = callGpt(prompt);
        System.out.println("========== GPT RAW RESPONSE ==========");
        System.out.println(response);

        List<ProfessionalExperience> workList = parseGptResponseToEntity(response, resumeId);
        if (!workList.isEmpty()) {
            System.out.println("✅ Saving " + workList.size() + " professional experiences to DB...");
            professionalExperienceRepository.saveAll(workList);
        }

        return workList;
    }

    private String extractWorkSection(String filePath) {
        StringBuilder builder = new StringBuilder();
        boolean inWork = false;

        try (FileInputStream fis = new FileInputStream(filePath)) {
            var document = new org.apache.poi.xwpf.usermodel.XWPFDocument(fis);
            for (var para : document.getParagraphs()) {
                String text = para.getText().trim();
                if (text.equalsIgnoreCase("WORK EXPERIENCE") || text.equalsIgnoreCase("EXPERIENCE")) {
                    inWork = true;
                    continue;
                }
                if (inWork) {
                    if (text.matches("^[A-Z\\s]{5,}$")) break;
                    builder.append(text).append("\n");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return builder.toString();
    }

    private String buildPrompt(String raw) {
        return """
        You are an intelligent resume parser. Extract structured WORK EXPERIENCE data from the following text.

        For each experience, return an object with these fields:
        - company_name (string)
        - company_location (string or null)
        - job_title (string)
        - experience_type (string: "Work", "Internship", etc.)
        - start_date (YYYY-MM format or null)
        - end_date (YYYY-MM format or null)
        - tech_stack (array of strings)
        - bullet_points (array of strings)

        Return a VALID JSON array ONLY. Do NOT add explanation or markdown code block.

        Now parse the following work experience text:
        """ + raw;
    }

    private String callGpt(String prompt) {
        try {
            String requestBody = mapper.writeValueAsString(new GPTRequest(prompt));
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(GPT_URL))
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpClient client = HttpClient.newHttpClient();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                System.err.println("GPT API call failed. Status: " + response.statusCode());
                System.err.println("Body: " + response.body());
                return "";
            }

            return response.body();
        } catch (Exception e) {
            System.err.println("Error while calling GPT: " + e.getMessage());
            e.printStackTrace();
            return "";
        }
    }

    private List<ProfessionalExperience> parseGptResponseToEntity(String response, Integer resumeId) {
        List<ProfessionalExperience> result = new ArrayList<>();

        try {
            JsonNode root = mapper.readTree(response);
            String content = root.at("/choices/0/message/content").asText();

            String jsonBlock;
            int start = content.indexOf("```json");
            int end = content.lastIndexOf("```");
            if (start != -1 && end != -1 && end > start) {
                jsonBlock = content.substring(start + 7, end).trim();
            } else {
                jsonBlock = content.trim();
            }

            JsonNode dataArray = mapper.readTree(jsonBlock);
            for (JsonNode item : dataArray) {
                ProfessionalExperienceRequestDTO dto = new ProfessionalExperienceRequestDTO();
                dto.setCompanyName(parseNullableText(item, "company_name"));
                dto.setCompanyLocation(parseNullableText(item, "company_location"));
                dto.setJobTitle(parseNullableText(item, "job_title"));
                dto.setExperienceType(parseNullableText(item, "experience_type"));
                dto.setStartDate(parseDate(item.get("start_date")));
                dto.setEndDate(parseDate(item.get("end_date")));
                dto.setResumeId(resumeId);

                JsonNode techArray = item.get("tech_stack");
                if (techArray != null && techArray.isArray()) {
                    List<String> techs = new ArrayList<>();
                    for (JsonNode tech : techArray) {
                        techs.add(tech.asText());
                    }
                    dto.setTechStack(techs);
                }

                JsonNode bulletArray = item.get("bullet_points");
                if (bulletArray != null && bulletArray.isArray()) {
                    List<String> bullets = new ArrayList<>();
                    for (JsonNode bullet : bulletArray) {
                        bullets.add(bullet.asText());
                    }
                    dto.setBulletPoints(bullets);
                }

                ProfessionalExperience entity = ProfessionalExperienceMapper.toEntity(dto);
                result.add(entity);
            }

        } catch (Exception e) {
            System.err.println("Failed to parse GPT response:");
            e.printStackTrace();
        }

        return result;
    }

    private String parseNullableText(JsonNode node, String fieldName) {
        if (node.hasNonNull(fieldName)) {
            String text = node.get(fieldName).asText();
            return (text == null || text.equalsIgnoreCase("null") || text.isBlank()) ? null : text.trim();
        }
        return null;
    }

    private LocalDate parseDate(JsonNode dateNode) {
        if (dateNode == null || dateNode.isNull()) return null;
        String text = dateNode.asText();
        if (text.isBlank() || text.equalsIgnoreCase("null")) return null;
        return LocalDate.parse(text + "-01");
    }
}
