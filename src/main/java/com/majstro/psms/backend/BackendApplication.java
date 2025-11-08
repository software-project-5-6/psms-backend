package com.majstro.psms.backend;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

@SpringBootApplication
public class BackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(BackendApplication.class, args);
	}

//		@Bean
//		CommandLineRunner testEmail(JavaMailSender mailSender) {
//		return args -> {
//			SimpleMailMessage message = new SimpleMailMessage();
//			message.setTo("niroshanb14@gmail.com"); // any valid recipient
//			message.setSubject("✅ Spring Boot Mail Test");
//			message.setText("Hello! This is a test email from your PSMS backend.");
//			mailSender.send(message);
//			System.out.println("✅ Test email sent successfully!");
//		};
	//}

}
