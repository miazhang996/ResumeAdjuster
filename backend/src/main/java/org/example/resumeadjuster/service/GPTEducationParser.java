package org.example.resumeadjuster.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.resumeadjuster.model.Course;
import org.example.resumeadjuster.model.Education;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.http.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class GPTEducationParser {

    @Value("${openai.api.key}")
    private String apiKey;

    private static final String GPT_URL = "https://api.openai.com/v1/chat/completions";
    private static final ObjectMapper mapper = new ObjectMapper();

    public List<Education> parseEducationFromDocx(String filePath, Integer resumeId) {
        String educationText = extractEducationSection(filePath);
        System.out.println("========== EDUCATION SECTION EXTRACTED ==========");
        System.out.println(educationText);

        String prompt = buildPrompt(educationText);
        String response = callGpt(prompt);
        System.out.println("========== GPT RAW RESPONSE ==========");
        System.out.println(response);

        return parseGptResponse(response, resumeId);
    }

    private String extractEducationSection(String filePath) {
        StringBuilder builder = new StringBuilder();
        boolean inEducation = false;

        try (FileInputStream fis = new FileInputStream(filePath)) {
            var document = new org.apache.poi.xwpf.usermodel.XWPFDocument(fis);
            for (var para : document.getParagraphs()) {
                String text = para.getText().trim();
                if (text.equalsIgnoreCase("EDUCATION")) {
                    inEducation = true;
                    continue;
                }
                if (inEducation) {
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
        return "You are an intelligent resume parser. Extract structured education information from the following resume EDUCATION section. " +
                "The layout may vary, so infer carefully. For each education experience, extract the following fields:\n\n" +
                "• school (string) – Name of the university or institution\n" +
                "• location (string or null)\n" +
                "• degree (string or null) – Extract the academic level only, such as 'Bachelor', 'Master', or 'PhD'. If the text says 'Bachelor of Engineering', extract 'Bachelor'.\n" +
                "• major (string or array of strings or null) – Major(s) or field(s) of study. "  +
                "For example, if the text says 'Bachelor of Computer Science', return 'Bachelor' as degree and 'Computer Science' as major. " +
                "If it says 'Master of Information', extract 'Master' as degree and 'Information' as major.\n" +
                "If multiple majors are listed (e.g., 'Major in Computer Science; Major in Statistics'), return them as a list of strings.\n" +
                "• gpa (number or null)\n" +
                "• course_work (string or null) – List of courses if available (can come after 'Courses:', bullets, or inline)\n" +
                "• start_date (string in YYYY-MM format or null)\n" +
                "• end_date (string in YYYY-MM format or null)\n\n" +
                "Return the result as a JSON array. If any field is not available, return null. Use best effort to separate degree and major, even if written together.\n\n" +
                "Example output:\n" +
                "[{\n" +
                "  \"school\": \"University of Toronto\",\n" +
                "  \"location\": null,\n" +
                "  \"degree\": \"Master\",\n" +
                "  \"major\": \"Information\",\n" +
                "  \"gpa\": 3.7,\n" +
                "  \"course_work\": \"Programming for Data Science, Data Science and Algorithm, Experimental Design for Data Science\",\n" +
                "  \"start_date\": \"2020-09\",\n" +
                "  \"end_date\": \"2022-12\"\n" +
                "},\n" +
                "{\n" +
                "  \"school\": \"University of Waterloo\",\n" +
                "  \"location\": null,\n" +
                "  \"degree\": \"Bachelor\",\n" +
                "  \"major\": \"Engineering\",\n" +
                "  \"gpa\": 3.7,\n" +
                "  \"start_date\": \"2015-09\",\n" +
                "  \"end_date\": \"2020-10\"\n" +
                "}]\n\n" +
                "Now extract information from the following resume EDUCATION section:\n" + raw;
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

            return response.body();
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    private List<Education> parseGptResponse(String response, Integer resumeId) {
        List<Education> educations = new ArrayList<>();
        try {
            JsonNode root = mapper.readTree(response);
            String content = root.at("/choices/0/message/content").asText();
            JsonNode dataArray = mapper.readTree(content);

            for (JsonNode item : dataArray) {
                Education edu = new Education();
                edu.setResumeId(resumeId);
                edu.setSchool(parseNullableText(item, "school"));
                edu.setLocation(parseNullableText(item, "location"));
                edu.setLevel(parseNullableText(item, "degree"));

                // ✅ program: support string or array format
                JsonNode programNode = item.get("major");
                if (programNode != null && !programNode.isNull()) {
                    if (programNode.isArray()) {
                        List<String> majors = new ArrayList<>();
                        for (JsonNode major : programNode) {
                            if (major != null && !major.asText().equalsIgnoreCase("null")) {
                                majors.add(major.asText().trim());
                            }
                        }
                        edu.setMajor(majors.isEmpty() ? null : String.join(", ", majors));
                    } else {
                        String raw = programNode.asText();
                        edu.setMajor((raw == null || raw.equalsIgnoreCase("null") || raw.isBlank()) ? null : raw.trim());
                    }
                } else {
                    edu.setMajor(null);
                }


                edu.setGPA(parseNullableDouble(item, "gpa"));
                String rawCourses = parseNullableText(item, "course_work");
                edu.setCourses(new ArrayList<>()); // 初始化

                if (rawCourses != null) {
                    // 按逗号或者分号分割
                    String[] courseNames = rawCourses.split("[,;]");
                    for (String courseName : courseNames) {
                        String trimmed = courseName.trim();
                        if (!trimmed.isEmpty()) {
                            Course course = new Course();
                            course.setCourseName(trimmed);
                            course.setEducation(edu); // 建立外键关系
                            edu.getCourses().add(course);
                        }
                    }
                }


                String start = item.get("start_date").asText();
                String end = item.get("end_date").asText();

                if (start != null && !start.equals("null")) {
                    edu.setStartDate(LocalDate.parse(start + "-01"));
                } else {
                    edu.setStartDate(null);
                }

                if (end != null && !end.equals("null")) {
                    edu.setEndDate(LocalDate.parse(end + "-01"));
                } else {
                    edu.setEndDate(null);
                }

                educations.add(edu);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return educations;
    }


    private String parseNullableText(JsonNode node, String fieldName) {
        JsonNode valueNode = node.get(fieldName);
        if (valueNode == null || valueNode.isNull()) return null;

        String text = valueNode.asText();
        return (text == null || text.equalsIgnoreCase("null") || text.isBlank()) ? null : text.trim();
    }

    private Double parseNullableDouble(JsonNode node, String fieldName) {
        JsonNode valueNode = node.get(fieldName);
        if (valueNode == null || valueNode.isNull()) return null;

        String text = valueNode.asText();
        if (text == null || text.equalsIgnoreCase("null") || text.isBlank()) return null;

        try {
            return Double.parseDouble(text);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
