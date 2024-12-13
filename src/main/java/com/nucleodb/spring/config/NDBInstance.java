package com.nucleodb.spring.config;

import com.nucleodb.library.NucleoDB;
import com.nucleodb.spring.mapping.NDBMappingContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class NDBInstance {
    @Bean
    public NucleoDB createNucleoDB(){
        return new NucleoDB();
    }
    @Bean
    @Primary
    public NDBMappingContext ndbMappingContext() {
        return new NDBMappingContext();
    }
}
