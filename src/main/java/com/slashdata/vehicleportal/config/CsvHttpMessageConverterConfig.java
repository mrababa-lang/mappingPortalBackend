package com.slashdata.vehicleportal.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.AbstractJackson2HttpMessageConverter;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CsvHttpMessageConverterConfig implements WebMvcConfigurer {

    @Override
    public void extendMessageConverters(java.util.List<HttpMessageConverter<?>> converters) {
        converters.add(csvHttpMessageConverter());
    }

    @Bean
    public HttpMessageConverter<Object> csvHttpMessageConverter() {
        CsvMapper csvMapper = Jackson2ObjectMapperBuilder.csv()
            .featuresToDisable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .build();
        return new AbstractJackson2HttpMessageConverter(
            csvMapper,
            new MediaType("text", "csv"),
            new MediaType("application", "csv"),
            MimeTypeUtils.parseMimeType("application/vnd.ms-excel")
        ) {
            @Override
            protected boolean canWrite(MediaType mediaType) {
                return false;
            }
        };
    }
}
