package lesson24.db;

/**
 * Интерфейс для осуществления миграций БД
 */
public interface DatabaseMigrations {
    /**
     * Запуск создания (при необходимости - удаления теблиц базы данных
     * из файлов, расположенных в папке src/main/resources/sql/#driver_name#
     * После создания таблиц, будет произведён импорт стандартных единиц измерения
     * и нечальный список ингредиентов
     */
    void migrate();

    /**
     * Установка необходимости отчистки базы данных от всех таблиц
     *
     * @param needClear если <code>true</code> таблицы принудительно удаляются,
     *                  иначе таблицы создаются только в случае их отсутствия
     */
    void needClear(boolean needClear);
}
