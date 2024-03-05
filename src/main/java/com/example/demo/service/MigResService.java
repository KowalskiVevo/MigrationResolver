package com.example.demo.service;

import com.example.demo.dao.SelectorTablesDao;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class MigResService {

  private final SelectorTablesDao selectorTablesDao;
  private static final String TABLE = "test_one";

  @EventListener(ApplicationReadyEvent.class)
  public void runAfterStartup(){
    var data = selectorTablesDao.selectTable(TABLE);
    var columns = Optional.ofNullable(data).map(dat -> dat.get(0))
        .map(Map::entrySet)
        .orElse(new HashSet<>()).stream()
        .map(Map.Entry::getKey)
        .collect(Collectors.joining(" | "));
    var values = Optional.ofNullable(data).stream()
        .flatMap(Collection::stream)
        .map(Map::entrySet)
        .flatMap(Collection::stream)
        .map(Map.Entry::getValue)
        .map(Object::toString)
        .collect(Collectors.joining(" | "));
    Optional.ofNullable(columns).filter(StringUtils::isNoneBlank).ifPresent(log::warn);
    Optional.ofNullable(values).filter(StringUtils::isNoneBlank).ifPresent(log::warn);
  }
}
