Этот репозиторий содержит модифицированную версию Flyway, популярного инструмента для миграции баз данных.
Основное изменение заключается в добавлении поддержки сохранения порядка выполнения
для repeatable миграций.

## Проблема
В стандартной версии Flyway повторяемые скрипты выполняются в алфавитном порядке и отключить это невозможно.
Это может быть неудобно, если есть скрипты, которые зависят друг от друга,
и необходимо контролировать порядок их выполнения.

## Решение
Данная версия добавляет возможность не менять порядок выполнения повторяемых скриптов.
При сборе миграций `CompositeMigrationResolver` не будет сортировать итоговый набор скриптов,
который формируется реализациями интерфейса `MigrationResolver`, сохраняя текущий порядок
миграций.

## Как использовать?
Чтобы использовать данную версию инструмента, нужно добавить в зависимости модули
`flyway-core` и `flyway-database-<требуемая БД>` и указать в `flyway-core` текущую версию данного проекта
(на данный момент 10.11.0-host).

Пример:

```xml
<dependency>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-core</artifactId>
    <version>${version.flyway}-host</version>
</dependency>
<dependency>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-database-postgresql</artifactId>
    <version>${version.flyway}</version>
</dependency>
```

После успешного подключения модулей, нужно включить в `Flyway` реализацию `MigrationResolver`,
позволяющую сортировать скрипты в указанном в конфиге порядке (см. ниже `CustomMigrationResolver`).
Для включения кастомного `MigrationResolver'a` в `Flyway` см. ниже `Создание бина Flyway`


### CustomMigrationResolver
Класс реализующий интерфейс `MigrationResolver`.
Для сортировки используется класс имплементирующий интерфейс `Comparator<ResolvedMigration>`
(см. ниже `ResolvedPriorityMigrationComparator`)

```java
@Component
@Slf4j
public class CustomMigrationResolver implements MigrationResolver {
  private final AppConfig appConfig;

  public CustomMigrationResolver(AppConfig appConfig) {
    this.appConfig = appConfig;
  }

  @Override
  public List<ResolvedMigration> resolveMigrations(Context context) {
    ParsingContext parsingContext = getParsingContext(context);
    List<ResolvedMigration> migrations = new ArrayList<>();
    addMigrations(migrations, context, parsingContext, false);
    addMigrations(migrations, context, parsingContext, true);

    migrations.sort(new ResolvedPriorityMigrationComparator(appConfig));
    log.debug("\n{}", migrations.stream().map(ResolvedMigration::getScript).collect(Collectors.joining("\n")));
    return migrations;
  }

  private LoadableResource[] createPlaceholderReplacingLoadableResources(List<LoadableResource> loadableResources,
                                                                         Context context,
                                                                         ParsingContext parsingContext) {
    List<LoadableResource> list = new ArrayList<>();

    for (final LoadableResource loadableResource : loadableResources) {
      LoadableResource placeholderReplacingLoadableResource = new LoadableResource() {
        @Override
        public Reader read() {
          return PlaceholderReplacingReader.create(context.configuration, parsingContext, loadableResource.read());
        }

        @Override
        public String getAbsolutePath() {
          return loadableResource.getAbsolutePath();
        }

        @Override
        public String getAbsolutePathOnDisk() {
          return loadableResource.getAbsolutePathOnDisk();
        }

        @Override
        public String getFilename() {
          return loadableResource.getFilename();
        }

        @Override
        public String getRelativePath() {
          return loadableResource.getRelativePath();
        }
      };

      list.add(placeholderReplacingLoadableResource);
    }

    return list.toArray(new LoadableResource[0]);
  }

  private Integer getChecksumForLoadableResource(boolean repeatable,
                                                 List<LoadableResource> loadableResources,
                                                 ResourceName resourceName,
                                                 Context context,
                                                 ParsingContext parsingContext) {
    if (repeatable && context.configuration.isPlaceholderReplacement()) {
      parsingContext.updateFilenamePlaceholder(resourceName, context.configuration);
      return ChecksumCalculator.calculate(createPlaceholderReplacingLoadableResources(
          loadableResources, context, parsingContext));
    }

    return ChecksumCalculator.calculate(loadableResources.toArray(new LoadableResource[0]));
  }

  private Integer getEquivalentChecksumForLoadableResource(boolean repeatable,
                                                           List<LoadableResource> loadableResources) {
    if (repeatable) {
      return ChecksumCalculator.calculate(loadableResources.toArray(new LoadableResource[0]));
    }

    return null;
  }

  private void addMigrations(List<ResolvedMigration> migrations,
                             Context context,
                             ParsingContext parsingContext,
                             boolean repeatable) {
    String[] suffixes = context.configuration.getSqlMigrationSuffixes();
    String prefix = getPrefixByRepeatable(context, repeatable);
    ResourceNameParser resourceNameParser = new ResourceNameParser(context.configuration);

    for (LoadableResource resource : context.resourceProvider.getResources(prefix, suffixes)) {
      String filename = resource.getFilename();
      ResourceName resourceName = resourceNameParser.parse(filename);
      if (!resourceName.isValid() || isSqlCallback(resourceName) || !prefix.equals(resourceName.getPrefix())) {
        continue;
      }

      SqlScript sqlScript = context.sqlScriptFactory.createSqlScript(
          resource, context.configuration.isMixed(), context.resourceProvider);

      List<LoadableResource> resources = new ArrayList<>();
      resources.add(resource);
      Integer checksum = getChecksumForLoadableResource(repeatable, resources, resourceName, context, parsingContext);
      Integer equivalentChecksum = getEquivalentChecksumForLoadableResource(repeatable, resources);

      migrations.add(new ResolvedMigrationImpl(
          resourceName.getVersion(),
          resourceName.getDescription(),
          resource.getRelativePath(),
          checksum,
          equivalentChecksum,
          CoreMigrationType.SQL,
          resource.getAbsolutePathOnDisk(),
          new SqlMigrationExecutor(context.sqlScriptExecutorFactory, sqlScript, false,
              context.configuration.isBatch())));
    }
  }

  /**
   * Checks whether this filename is actually a sql-based callback instead of a regular migration.
   *
   * @param result The parsing result to check.
   */
  protected static boolean isSqlCallback(ResourceName result) {
    return Event.fromId(result.getPrefix()) != null;
  }

  private String getPrefixByRepeatable(Context context, boolean repeatable) {
    if (Boolean.FALSE.equals(repeatable)) {
      return context.configuration.getSqlMigrationPrefix();
    }
    return context.configuration.getRepeatableSqlMigrationPrefix();
  }

  private ParsingContext getParsingContext(Context context) {
    ParsingContext parsingContext = new ParsingContext();
    JdbcConnectionFactory jdbcConnectionFactory =
        new JdbcConnectionFactory(context.configuration.getDataSource(),
            context.configuration, context.statementInterceptor);
    parsingContext.populate(jdbcConnectionFactory.getDatabaseType().createDatabase(
        context.configuration, jdbcConnectionFactory, context.statementInterceptor), context.configuration);
    return parsingContext;
  }
}
```

### ResolvedPriorityMigrationComparator
```java
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
```

### Создание бина `Flyway`
Flyway не позволяет заполнить параметр resolvers через конфиг, но позволяет через создание бина.

Добавьте в параметр `resolvers` объект вашего класса, чтобы включить свою реализацию.

Очень важно обратить внимание, чтобы включить свойство сохранения указанного порядка выполнения скриптов, нужно
передать в параметр `skipDefaultResolvers` значение `true`. В противном случае Flyway будет сортировать скрипты по дефолту.

```java
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
```

### Пример конфига
```yaml
spring:
  application:
    name: demo-migres
    schema: main
  datasource:
    driver-class-name: org.postgresql.Driver
    host: localhost:5432
    database: migres
    url: jdbc:postgresql://${spring.datasource.host}/${spring.datasource.database}?ApplicationName=${spring.application.name}
    username: username
    password: password
    hikari:
      maximum-pool-size: 3
    jpa:
      properties:
        hibernate:
          default_schema: ${spring.application.schema}
  flyway:
    enabled: true
    locations:
      - classpath:db/migration
      - classpath:db/migration/func
      - classpath:db/migration/view
app:
  migration_queue:
    - view/R__view.sql
    - func/R__func.sql
```