package main.java.com.example.bankcards.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class JacksonConfig {

    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        
        // Регистрируем модуль для работы с Java 8 Date/Time API
        mapper.registerModule(new JavaTimeModule());
        
        // Отключаем запись дат как timestamps
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        
        // Настройки для красивого вывода JSON (можно убрать в production)
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        
        return mapper;
    }
}