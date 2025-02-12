package org.example;


import org.example.faker.FakeDataGenerator;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class Main {
    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }

    @Bean
    public CommandLineRunner commandLineRunner(FakeDataGenerator fakeDataGenerator) {
        return args -> {
            // Call the generateFakeData method to populate the database with fake data
            fakeDataGenerator.generateFakeData();
            System.out.println("Fake data generation completed!");
        };
    }
}