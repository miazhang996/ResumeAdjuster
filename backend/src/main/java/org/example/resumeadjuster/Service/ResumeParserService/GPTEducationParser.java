package org.example.resumeadjuster.Service.ResumeParserService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.example.resumeadjuster.Model.ResumeParserMapper.EducationMapper;
import org.example.resumeadjuster.Model.ResumeParserDTO.CourseDTO;
import org.example.resumeadjuster.Model.ResumeParserEntity.Education;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.example.resumeadjuster.Model.ResumeParserDTO.EducationRequestDTO;

import java.io.FileInputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.io.IOException;

@Service
public class GPTEducationParser {
    @Value("${openai.api.key}")
    private String apiKey;

    EducationRequestDTO dto = new EducationRequestDTO();
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

        return parseGptResponseToEntity(response, resumeId);
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

                if (text.toLowerCase().contains("education")) {
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
                "• degree (string or null) – Extract the academic level only, such as 'Bachelor', 'Master', or 'PhD'.\n" +
                "• major (string or array of strings or null) – Field(s) of study. If it's 'Bachelor of Computer Science', return degree='Bachelor' and major='Computer Science'.\n" +
                "• gpa (number or null)\n" +
                "• course_work (string or null) – List of courses (comma- or semicolon-separated)\n" +
                "• start_date (string in YYYY-MM format or null)\n" +
                "• end_date (string in YYYY-MM format or null)\n\n" +
                "Return a valid JSON array. If any field is missing, use null.\n\n" +
                "Now extract information from this resume EDUCATION section:\n" + raw;
    }

    private String callGpt(String prompt){
        try
        {
            String requestBody = mapper.writeValueAsString(new GPTRequest(prompt));
            HttpRequest request = HttpRequest.newBuilder()
                    // 构建一个新的HTTP请求
                    .uri(URI.create(GPT_URL))
                    // 告诉这个请求要访问哪个网址。
                    .header("Authorization","Bearer " + apiKey)
                    // 密钥
                    .header("Content-Type", "application/json")
                    // 再加一个请求头，告诉对方我发的数据是JSON格式，所以告诉OpenAI我传的不是表单
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();
            HttpClient client = HttpClient.newHttpClient();
            //“我要发请求了，先准备一个 HTTP 客户端工具。”
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            // 发送你刚刚构造好的请求，并等待响应结果。

            if (response.statusCode()!=200){
                // status code 200 means successful
                System.err.println("GPT API call failed. Status: " + response.statusCode());
                System.err.println("Body: " + response.body());
                return "";
            }
            return response.body();
        }catch (Exception e){
            System.err.println("Error while calling GPT: " + e.getMessage());
            e.printStackTrace();
            return"";
        }
    }


    private List<Education> parseGptResponseToEntity(String response, Integer resumeId){
        List<Education> result = new ArrayList<>();

        try {
            JsonNode root = mapper.readTree(response);
            // Turn the response into a JSON data structure
            String content = root.at("/choices/0/message/content").asText();
            // get the "answer" part from the response given back by GPT
            JsonNode dataArray = mapper.readTree(content);
            // readTree(content) parses the JSON array string
            // inside content into a JSON array structure.

            for (JsonNode item : dataArray) {
                // 遍历GPT返回的JSON数组dataArray，每一个item是一个教育经历的JSON对象
                EducationRequestDTO dto = new EducationRequestDTO();
                //创建一个空的 EducationDTO 实例，准备填入每一项数据。
                dto.setSchool(parseNullableText(item, "school"));
                // 从 item 中读取 "school" 字段，调用你封装好的 parseNullableText()
                // 方法进行安全解析（防止空值或 "null" 字符串）。
                dto.setLocation(parseNullableText(item, "location"));
                dto.setLevel(parseNullableText(item, "degree"));
                dto.setGPA(parseNullableDouble(item, "gpa"));

                // 开始set major进DTO
                JsonNode majorNode = item.get("major");
                if(majorNode != null && !majorNode.isNull()){
                    // 第一个判断是不是指向null，第二个判断majorNode的内容是不是null
                    //{
                    //  "major": null
                    //}
                    List<String> majors = new ArrayList<>();
                    if(majorNode.isArray()){
                        for(JsonNode m:majorNode) {
                            if (!m.isNull()) {
                                majors.add(m.asText());
                            }
                        }
                        dto.setMajor(String.join(", ", majors));
                    } else {
                        majors.add(majorNode.asText());
                    }
                }
                //Start/End Date
                dto.setStartDate(parseDate(item.get("start_date")));
                dto.setEndDate(parseDate(item.get("end_date")));

                String courseRaw = parseNullableText(item, "course_work");
                if (courseRaw != null) {
                    String[] split = courseRaw.split("[,;]");
                    List<CourseDTO> courseList = new ArrayList<>();

                    for (String name : split) {
                        if (!name.isBlank()) {
                            CourseDTO course = new CourseDTO();
                            course.setCourseName(name.trim());
                            courseList.add(course);
                        }
                    }
                    dto.setCourses(courseList);
                }

                // Convert DTO to entity
                Education education = EducationMapper.toEntity(dto);
                education.setResumeId(resumeId);
                result.add(education);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }


    private String parseNullableText(JsonNode node, String fieldName){
        if(node.hasNonNull(fieldName)){
            //判断一个字段是否存在，并且它的值 不是 JSON 的 null。
            String text = node.get(fieldName).asText();
            return(text == null || text.equalsIgnoreCase("null") || text.isBlank() ? null : text.trim());
            //The equalsIgnoreCase() method compares two strings,
            // ignoring lower case and upper case differences.
        }
        return null;
    }

    private Double parseNullableDouble(JsonNode node, String fieldName) {
        try {
            String text = parseNullableText(node, fieldName);
            return (text != null) ? Double.parseDouble(text) : null;
        } catch (Exception e) {
            return null;
        }
    }

    private LocalDate parseDate(JsonNode dateNode) {
        if (dateNode == null || dateNode.isNull()) return null;
        String text = dateNode.asText();
        if (text.isBlank() || text.equalsIgnoreCase("null")) return null;
        return LocalDate.parse(text + "-01");
    }



}
