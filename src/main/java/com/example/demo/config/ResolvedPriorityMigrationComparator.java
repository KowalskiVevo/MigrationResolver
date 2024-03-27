package com.example.demo.config;

import lombok.RequiredArgsConstructor;
import org.flywaydb.core.api.resolver.ResolvedMigration;

import java.util.Comparator;
import java.util.Objects;
import java.util.stream.Stream;

@RequiredArgsConstructor
public class ResolvedPriorityMigrationComparator implements Comparator<ResolvedMigration> {

  private final AppConfig appConfig;

  @Override
  public int compare(ResolvedMigration o1, ResolvedMigration o2) {
    if (Stream.of(o1, o2).map(ResolvedMigration::getVersion).allMatch(Objects::isNull)) {
      for (String priority : appConfig.getMigrationQueue()) {
        if (priority.equals(o1.getScript())) {
          return -1;
        }
        else if (priority.equals(o2.getScript())) {
          return 1;
        }
      }
    }
    return defaultMigrationCompare(o1, o2);
  }

  private int defaultMigrationCompare(ResolvedMigration o1, ResolvedMigration o2) {
    if ((o1.getVersion() != null) && o2.getVersion() != null) {
      int v = o1.getVersion().compareTo(o2.getVersion());
      if (v == 0) {
        if (o1.getType().isUndo() && o2.getType().isUndo()) {
          return 0;
        }
        if (o1.getType().isUndo()) {
          return 1;
        }
        if (o2.getType().isUndo()) {
          return -1;
        }
      }
      return v;
    }
    if (o1.getVersion() != null) {
      return -1;
    }
    if (o2.getVersion() != null) {
      return 1;
    }
    return o1.getDescription().compareTo(o2.getDescription());
  }
}
