package com.example.demo.dao;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class SelectorTablesDao {
  private final JdbcTemplate jdbcTemplate;
  private static final String SELECT_QUERY = "select * from %s.%s";

  @Value("${spring.application.schema}")
  private String schema;

  public List<Map<String, Object>> selectTable(String tableName) {
    return jdbcTemplate.queryForList(String.format(SELECT_QUERY, schema, tableName));
  }
}
