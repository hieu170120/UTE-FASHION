package com.example.demo.config;

import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.hibernate.collection.spi.PersistentCollection;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ModelMapperConfig {

    @Bean
    public ModelMapper modelMapper() {
        ModelMapper modelMapper = new ModelMapper();
        modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);

        modelMapper.getConfiguration().setPropertyCondition(context ->
                !(context.getSource() instanceof PersistentCollection) || ((PersistentCollection<?>) context.getSource()).wasInitialized()
        );

        return modelMapper;
    }
}
