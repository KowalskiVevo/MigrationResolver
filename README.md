Сервис-пример для реализации сохранения порядка выполнения repeatable скриптов. 

На данным момент времени, дефолтный Flyway после разрешения миграций сортирует скрипты и не дает задавать определенный порядок проведения миграций (подробнее https://github.com/flyway/flyway/issues/1403).
Поэтому, был реализован свой модуль https://github.com/KowalskiVevo/flywayWithCustomOrdering с изменениями этого пулл-реквеста https://github.com/flyway/flyway/pull/1405/commits/fd7a87b38f1e35d6fec3f792092a9955723160f5.
