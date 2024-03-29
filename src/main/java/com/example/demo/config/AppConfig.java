package com.example.demo.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.LinkedHashSet;

@Data
@ConfigurationProperties(prefix = "app")
public class AppConfig {
  private LinkedHashSet<String> migrationQueue = new LinkedHashSet<>();
}
