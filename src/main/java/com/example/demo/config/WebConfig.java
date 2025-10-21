package com.example.demo.config;

import com.example.demo.dto.ColorDTO;
import com.example.demo.dto.SizeDTO;
import com.example.demo.service.ColorService;
import com.example.demo.service.SizeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.format.FormatterRegistry;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private StringToColorDTOConverter stringToColorDTOConverter;

    @Autowired
    private StringToSizeDTOConverter stringToSizeDTOConverter;

    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverter(stringToColorDTOConverter);
        registry.addConverter(stringToSizeDTOConverter);
    }

    @Component
    public static class StringToColorDTOConverter implements Converter<String, ColorDTO> {
        @Autowired
        private ColorService colorService;

        @Override
        public ColorDTO convert(@NonNull String source) {
            if (source.trim().isEmpty()) {
                return null;
            }
            try {
                Integer id = Integer.parseInt(source);
                return colorService.findById(id);
            } catch (NumberFormatException e) {
                return null;
            }
        }
    }

    @Component
    public static class StringToSizeDTOConverter implements Converter<String, SizeDTO> {
        @Autowired
        private SizeService sizeService;

        @Override
        public SizeDTO convert(@NonNull String source) {
            if (source.trim().isEmpty()) {
                return null;
            }
            try {
                Integer id = Integer.parseInt(source);
                return sizeService.findById(id);
            } catch (NumberFormatException e) {
                return null;
            }
        }
    }
}
