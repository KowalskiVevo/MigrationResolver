package com.example.demo.config;

import org.flywaydb.core.Flyway;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

@Configuration
public class BeanConfig {
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
