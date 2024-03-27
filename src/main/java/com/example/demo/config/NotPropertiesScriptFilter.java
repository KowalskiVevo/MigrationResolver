package com.example.demo.config;

import liquibase.changelog.IncludeAllFilter;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class NotPropertiesScriptFilter implements IncludeAllFilter {
  @Value("${app.migration_queue}")
  private List<String> migrationQueue;

  @Override
  public boolean include(String changeLogPath) {
//    return !changeLogPath.contains(".bak.");
    return migrationQueue.stream().noneMatch(changeLogPath::contains);
  }
}
