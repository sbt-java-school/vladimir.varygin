# Задание по лекции №23

Реализовать кеширующий прокси с сохранением результата кеширования в базе данных.

## Реализация

Для реализации поставленой задачи были созданы два модуля: **database** и **core**.

Модуль **database** содержит в себе настройки для подключения к базе данных (далее БД), 
конфигурационные классы, считывающие эти настройки и предоставляющие
интерфейс по осуществлению соединения с БД MySQL, миграции таблиц БД
для быстрого создания / удаления / модификации структуры таблиц, шаблон для
осуществления запросов к БД по единому интерфейсу, а также абстрактную модель
для создания необходимых реализаций моделей для сохранения их в БД.

Файлы, предназначеные для миграции должны находиться в пакетах sql и sql/drop
в папке ресурсов модели database. В пакете sql/drop должны находиться запросы на удаление
таблицы если это необходимо.

Интерфейс TemplateManager предназначен для добавления единого интерфейса по
доступу к шаблонизатору запросов и для предоставления возможности подмены
шаблона для тестирования без базы данных.

Абстрактная модель **DBModel** содержит метод store при помощи которого можно
сохранять / обновлять модель в БД в зависимости от наличия идентификатора
в экземпляре класса модели. Для создания модели необходимо отнаследоваться
от абстрактной модели DBModel и определить поля, которые должны сохраняться
в БД. Все поля предназначеные для сохранения их в БД должны иметь аннотацию @TableField.
Поле id создавать не нужно, так как оно уже создано в абстрактной модели. Кроме
этого, данный клас предоставляет метод prepare для подготовки данных запроса к выполнению.
Для подготовки данных из любой модели использовалась рефлексия. Для корректной работы
модели, модели наследники должны иметь геттеры для всех полей с аннотацией @TableField.


Модуль **core** предназначен для непосредственной реализации некоторого сервиса
и его кеширующего прокси. 

В качестве сервиса был выбран простой калькулятор с двумя методами 
 - sum - для вычисления суммы двух чисел
 - multiple для вычисления произведения двух чисел.
 
Для работы с таблицей базы данных была создана модель CacheModel расширяющая
функциональность абстрактной модели при помощи метода getCacheByKey при помощи
которого в БД ищется запись по ключу кеша.

### Реализация кеширующего прокси

Реализация находится в классе CacheInvocationHandler модуля core, пакета proxy.
В БД хранится объект Value<T>, который содержит значение типа T и метку времени.
Процесс работы метода invoke этого класса следующий:

1. Проверяем необходимость кеширования данных класса / метода при помощи аннотации @Cache у вызываемого метода
2. Если время кеширования задано
    - Формируем ключ кеша из названия метода и строкового предстваления аргументов метода
    - Проверяем наличие кеша текущей сессии и если кеш есть и время кеширования не истекло - возвращаем значение
    - Иначе проверяем наличие кеша в БД и если есть и время кеширования не истекло - возвращаем значение
    - Иначе вычисляем значение вызванного метода и сохраняем его в сессии и в БД.
3. Иначе возвращаем значение вызванного метода.


За время выполенения домашнего задания был получен опыт работы с базой данных MySQL в java,
опыт настройки подключения к БД при помощи файла с параметрами, опыт написания моделей
по модификации данных таблиц БД.
