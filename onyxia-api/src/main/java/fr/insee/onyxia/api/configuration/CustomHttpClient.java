package fr.insee.onyxia.api.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import okhttp3.OkHttpClient;

@Configuration
public class CustomHttpClient {

    @Bean
    public OkHttpClient httpClient(){
        return new OkHttpClient();
    }
    
}