package com.itheima.config;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * LocalDateTime 默认的时间格式是: yyyy-MM-ddTHH:mm:ss，中间多了一个 T 字，
 * 但是对于往常的数据来说是没有 T 字的，这就会造成后端在接受或返回时间数据的时候出现异常，
 * 解决方案如下（添加一个配置类，千万不要忘了 @Configuration 注解）：
 * @author : 111
 * @program : Mall-Project
 * @description : 时间配置类
 */
@Configuration
public class LocalDateTimeSerializerConfig {
    @org.springframework.beans.factory.annotation.Value("${spring.jackson.date-format:yyyy-MM-dd HH:mm:ss}")
    private String pattern;

    @Bean
    public LocalDateTimeSerializer localDateTimeDeserializer() {
        return new LocalDateTimeSerializer(DateTimeFormatter.ofPattern(pattern));
    }
    //接收时间数据反序列化
    @Bean
    public Jackson2ObjectMapperBuilderCustomizer jackson2ObjectMapperBuilderCustomizer() {
        return builder -> builder.serializerByType(LocalDateTime.class, localDateTimeDeserializer());
    }
}
