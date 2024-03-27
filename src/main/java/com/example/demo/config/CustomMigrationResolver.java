//package com.example.demo.config;
//
//import lombok.extern.slf4j.Slf4j;
//import org.flywaydb.core.api.CoreMigrationType;
//import org.flywaydb.core.api.callback.Event;
//import org.flywaydb.core.api.resolver.MigrationResolver;
//import org.flywaydb.core.api.resolver.ResolvedMigration;
//import org.flywaydb.core.api.resource.LoadableResource;
//import org.flywaydb.core.internal.parser.ParsingContext;
//import org.flywaydb.core.internal.parser.PlaceholderReplacingReader;
//import org.flywaydb.core.internal.resolver.ChecksumCalculator;
//import org.flywaydb.core.internal.resolver.ResolvedMigrationImpl;
//import org.flywaydb.core.internal.resolver.sql.SqlMigrationExecutor;
//import org.flywaydb.core.internal.resource.ResourceName;
//import org.flywaydb.core.internal.resource.ResourceNameParser;
//import org.flywaydb.core.internal.sqlscript.SqlScript;
//import org.springframework.stereotype.Component;
//
//import java.io.Reader;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.stream.Collectors;
//
//@Component
//@Slf4j
//public class CustomMigrationResolver implements MigrationResolver {
//
//  private final ParsingContext parsingContext;
//  private final AppConfig appConfig;
//
//  public CustomMigrationResolver(AppConfig appConfig) {
//    this.parsingContext = new ParsingContext();
//    this.appConfig = appConfig;
//  }
//
//  @Override
//  public List<ResolvedMigration> resolveMigrations(Context context) {
//    List<ResolvedMigration> migrations = new ArrayList<>();
//    addMigrations(migrations, context, false);
//    addMigrations(migrations, context, true);
//
//    migrations.sort(new ResolvedPriorityMigrationComparator(appConfig));
//    log.info("\n{}", migrations.stream().map(ResolvedMigration::getScript).collect(Collectors.joining("\n")));
//    return migrations;
//  }
//
//  private LoadableResource[] createPlaceholderReplacingLoadableResources(List<LoadableResource> loadableResources,
//                                                                         Context context) {
//    List<LoadableResource> list = new ArrayList<>();
//
//    for (final LoadableResource loadableResource : loadableResources) {
//      LoadableResource placeholderReplacingLoadableResource = new LoadableResource() {
//        @Override
//        public Reader read() {
//          return PlaceholderReplacingReader.create(context.configuration, parsingContext, loadableResource.read());
//        }
//
//        @Override
//        public String getAbsolutePath() {
//          return loadableResource.getAbsolutePath();
//        }
//
//        @Override
//        public String getAbsolutePathOnDisk() {
//          return loadableResource.getAbsolutePathOnDisk();
//        }
//
//        @Override
//        public String getFilename() {
//          return loadableResource.getFilename();
//        }
//
//        @Override
//        public String getRelativePath() {
//          return loadableResource.getRelativePath();
//        }
//      };
//
//      list.add(placeholderReplacingLoadableResource);
//    }
//
//    return list.toArray(new LoadableResource[0]);
//  }
//
//  private Integer getChecksumForLoadableResource(boolean repeatable,
//                                                 List<LoadableResource> loadableResources,
//                                                 ResourceName resourceName, Context context) {
//    if (repeatable && context.configuration.isPlaceholderReplacement()) {
//      parsingContext.updateFilenamePlaceholder(resourceName, context.configuration);
//      return ChecksumCalculator.calculate(createPlaceholderReplacingLoadableResources(loadableResources, context));
//    }
//
//    return ChecksumCalculator.calculate(loadableResources.toArray(new LoadableResource[0]));
//  }
//
//  private Integer getEquivalentChecksumForLoadableResource(boolean repeatable,
//                                                           List<LoadableResource> loadableResources) {
//    if (repeatable) {
//      return ChecksumCalculator.calculate(loadableResources.toArray(new LoadableResource[0]));
//    }
//
//    return null;
//  }
//
//  private void addMigrations(List<ResolvedMigration> migrations,
//                             Context context,
//                             boolean repeatable) {
//    String[] suffixes = context.configuration.getSqlMigrationSuffixes();
//    String prefix = getPrefixByRepeatable(context, repeatable);
//    ResourceNameParser resourceNameParser = new ResourceNameParser(context.configuration);
//
//    for (LoadableResource resource : context.resourceProvider.getResources(prefix, suffixes)) {
//      String filename = resource.getFilename();
//      ResourceName resourceName = resourceNameParser.parse(filename);
//      if (!resourceName.isValid() || isSqlCallback(resourceName) || !prefix.equals(resourceName.getPrefix())) {
//        continue;
//      }
//
//      SqlScript sqlScript = context.sqlScriptFactory.createSqlScript(
//          resource, context.configuration.isMixed(), context.resourceProvider);
//
//      List<LoadableResource> resources = new ArrayList<>();
//      resources.add(resource);
//      Integer checksum = getChecksumForLoadableResource(repeatable, resources, resourceName, context);
//      Integer equivalentChecksum = getEquivalentChecksumForLoadableResource(repeatable, resources);
//
//      migrations.add(new ResolvedMigrationImpl(
//          resourceName.getVersion(),
//          resourceName.getDescription(),
//          resource.getRelativePath(),
//          checksum,
//          equivalentChecksum,
//          CoreMigrationType.SQL,
//          resource.getAbsolutePathOnDisk(),
//          new SqlMigrationExecutor(context.sqlScriptExecutorFactory, sqlScript, false,
//              context.configuration.isBatch())));
//    }
//  }
//
//  /**
//   * Checks whether this filename is actually a sql-based callback instead of a regular migration.
//   *
//   * @param result The parsing result to check.
//   */
//  protected static boolean isSqlCallback(ResourceName result) {
//    return Event.fromId(result.getPrefix()) != null;
//  }
//
//  private String getPrefixByRepeatable(Context context, boolean repeatable) {
//    if (Boolean.FALSE.equals(repeatable)) {
//      return context.configuration.getSqlMigrationPrefix();
//    }
//    return context.configuration.getRepeatableSqlMigrationPrefix();
//  }
//}
