package com.utp.parkutp;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
@SpringBootApplication
public class ParkutpApplication extends SpringBootServletInitializer {
 public static void main(String[] args) { SpringApplication.run(ParkutpApplication.class, args); }
 @Override protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) { return builder.sources(ParkutpApplication.class); }
}
