package com.example.demo.config;

import lombok.RequiredArgsConstructor;
import org.flywaydb.core.Flyway;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

@Configuration
@RequiredArgsConstructor
public class BeanConfig {
  @ConditionalOnProperty(name = "spring.flyway.enabled", havingValue = "true")
  @Bean(initMethod = "migrate")
  public Flyway flyway(AppConfig appConfig) {
    var flywayDataSource = new DriverManagerDataSource();
    flywayDataSource.setUsername("username");
    flywayDataSource.setPassword("password");
    flywayDataSource.setUrl("jdbc:postgresql://localhost:5432/migres");

    return Flyway.configure()
        .resolvers(new CustomMigrationResolver(appConfig))
        .skipDefaultResolvers(true)
        .dataSource(flywayDataSource)
        .locations("classpath:db/migration")
        .schemas("main")
        .load();
  }
}
