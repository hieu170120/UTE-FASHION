package com.example.demo.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.format.FormatterRegistry;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.CharacterEncodingFilter;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.charset.StandardCharsets;
import java.util.List;

import com.example.demo.dto.ColorDTO;
import com.example.demo.dto.SizeDTO;
import com.example.demo.service.ColorService;
import com.example.demo.service.SizeService;

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

	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		registry.addResourceHandler("/static/**").addResourceLocations("classpath:/static/");
	}
	
	/**
	 * Configure UTF-8 encoding for HTTP message converters
	 */
	@Override
	public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
		StringHttpMessageConverter stringConverter = new StringHttpMessageConverter(StandardCharsets.UTF_8);
		stringConverter.setWriteAcceptCharset(false);
		converters.add(stringConverter);
	}
	
	/**
	 * Character encoding filter for UTF-8
	 */
	@Bean
	public CharacterEncodingFilter characterEncodingFilter() {
		CharacterEncodingFilter filter = new CharacterEncodingFilter();
		filter.setEncoding("UTF-8");
		filter.setForceEncoding(true);
		return filter;
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
