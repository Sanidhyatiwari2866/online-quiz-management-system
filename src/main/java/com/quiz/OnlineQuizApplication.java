package com.quiz;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class OnlineQuizApplication {

    public static void main(String[] args) {
        SpringApplication.run(OnlineQuizApplication.class, args);
        System.out.println("\n=======================================================");
        System.out.println(" Online Quiz Management System is Running!");
        System.out.println(" Frontend UI:        http://localhost:8080");
        System.out.println(" H2 Web Console:     http://localhost:8080/h2-console");
        System.out.println(" Admin Credentials:  admin / admin123");
        System.out.println(" Student Creds:      student / student123");
        System.out.println("=======================================================\n");
    }
}
