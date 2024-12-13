package com.nucleodb.spring.config;

import com.nucleodb.library.NucleoDB;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class NDBInstance {
    @Bean
    public NucleoDB createNucleoDB(){
        return new NucleoDB();
    }
}
