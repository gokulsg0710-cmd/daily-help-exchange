package com.Gokul.daily_help_exchange.Config;

import com.Gokul.daily_help_exchange.Model.HelpTask;
import com.Gokul.daily_help_exchange.Model.TaskStatus;
import com.Gokul.daily_help_exchange.Repository.HelpTaskRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDate;

@Configuration
public class DataLoader {

    @Bean
    CommandLineRunner loadSampleData(HelpTaskRepository repository) {
        return args -> {
            if (repository.count() == 0) {
                repository.save(HelpTask.builder() 
                        .title("Buy milk")
                        .description("Buy two packets of milk while returning home.")
                        .createdBy("Gokul")
                        .dueDate(LocalDate.now().plusDays(1))
                        .priority(3)
                        .rewardPoints(30)
                        .status(TaskStatus.OPEN)
                        .build());

                repository.save(HelpTask.builder()
                        .title("Water balcony plants")
                        .description("Water all six plants before 7 PM.")
                        .createdBy("Amma")
                        .dueDate(LocalDate.now())
                        .priority(2)
                        .rewardPoints(20)
                        .status(TaskStatus.OPEN)
                        .build());        
            }
        };
    }
}