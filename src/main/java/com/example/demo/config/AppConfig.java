package com.example.demo.config;

import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.stream.Collectors;

@Data
@ConfigurationProperties(prefix = "spring.flyway")
public class AppConfig {
  private LinkedHashSet<String> migrationQueue;

  //Тут можно описать, как правильно мы можем распарсить файл migrationQueueFile
  @ConstructorBinding
  public AppConfig(LinkedHashSet<String> migrationQueue, Path migrationQueueFile) throws IOException {
    if (Objects.isNull(migrationQueueFile) || StringUtils.isBlank(migrationQueueFile.toString())) {
      this.migrationQueue = migrationQueue;
    }
    this.migrationQueue = Files.lines(migrationQueueFile).collect(Collectors.toCollection(LinkedHashSet::new));
  }
}
