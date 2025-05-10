package org.example.resumeadjuster.Model.ResumeParserEntity;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import javax.print.attribute.Attribute;
import java.util.HashMap;
import java.util.Map;


//我写了一个转换器类，用来告诉 JPA：当我遇到 Map<String, Object> 类型的字段时，
// 要怎么把它转换成数据库可以存的 String（通常是 JSON 字符串），反之亦然。

//This is a converter class to tell JPA
//if I ever meet a variable of type Map<String,Object>
//How should I change it into storable string JSON string, and vice versa




@Converter
public class JsonMapConverter implements AttributeConverter<Map<String, Object>, String> {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(Map<String, Object> attribute) {
        try {
            return objectMapper.writeValueAsString(attribute);
        } catch (Exception e) {
            return "{}"; // fallback to empty JSON
        }
    }

    @Override
    public Map<String, Object> convertToEntityAttribute(String dbData) {
        try {
            return objectMapper.readValue(dbData, new TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            return new HashMap<>(); // fallback to empty map
        }
    }
}
