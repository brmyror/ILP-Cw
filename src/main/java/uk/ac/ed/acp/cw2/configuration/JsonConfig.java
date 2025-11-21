package uk.ac.ed.acp.cw2.configuration;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDate;
import java.time.LocalTime;

@Configuration
public class JsonConfig {
/**
    @Bean
    public Gson gsonDeSerializer() {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(LocalDate.class, new LocalDateController);
        gsonBuilder.registerTypeAdapter(LocalTime.class, new LocalTimeController);
        return gsonBuilder.setPrettyPrinting().create();
    }
    **/
}
