package lesson24.db.components;

import lesson24.db.DatabaseMigrations;
import lesson24.db.configuration.JdbcConfig;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * Класс для создания структуры таблиц базы данных приложения.
 * Осуществляет удаление всех созданных таблиц,
 * если это необходимо (определяется при помощи флага needClear).
 * После создания таблиц - импортирует начальные данных, если таковые имеются.
 * <p>
 * Таблицы создаются только один раз. Данная возможность осуществляется при
 * помощи файла конфигурации, в который записывается факт создания таблиц.
 * <p>
 * <p>Контракт на именование:<p>
 * <ul>
 * <ol>Все sql файлы и папки для их хранения должны находится в
 * директории ресурсов модуля dao: src/main/resources/sql (далее корень)</ol>
 * <ol>Все совместимые запросы на удаление таблиц должны находиться
 * в папке drop корневой директории</ol>
 * <ol>Все скрипты по созданию таблиц должны находиться в директории, соответствующей
 * наименованию драйвера для соединения с базой данных (относительно корня),
 * в этой же директории может находиться папка drop со специфическими
 * скриптами на удаление таблиц</ol>
 * <ol>В случае необходимости создания / удаления таблиц / импорту данных
 * в определённом порядке - необходимо именовать файлы со скриптами начиная
 * с порядкового номера в цепочке по созданию таблиц.</ol>
 * <ol>Все файлы импорта должны находиться в директории import относительно корня</ol>
 * </ul>
 */
@Repository
public class DatabaseMigrationsDao implements DatabaseMigrations {
    private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseMigrationsDao.class);
    private static final String CONFIG_FILE = "config.properties";
    private Properties properties = new Properties();

    private final JdbcTemplate template;
    private boolean needClear;

    @Autowired
    public DatabaseMigrationsDao(JdbcTemplate template) {
        this.template = template;
    }

    @Override
    public void migrate() {
        try {
            if (isExist()) {
                return;
            }
            String driver = JdbcConfig.getInstance().getDriver();
            if (needClear) {
                LOGGER.info("Удаление таблиц");
                executeScript("sql/drop");
                executeScript("sql/" + driver + "/ drop");
            }
            LOGGER.info("Создание таблиц");
            executeScript("sql/" + driver);
            LOGGER.info("Импорт данных");
            executeScript("sql/import");

            setMigrated();
        } catch (URISyntaxException e) {
            throw new IllegalStateException(e);
        }
    }

    private void setMigrated() {
        try (FileOutputStream outputStream = new FileOutputStream(CONFIG_FILE)) {
            properties.setProperty("exist", "true");
            properties.store(outputStream, null);
            outputStream.close();
        } catch (IOException e) {
            LOGGER.info("Ошибка сохранения данных конфигурации");
            throw new IllegalStateException(e);
        }
    }

    private boolean isExist() {
        try (InputStream input = new FileInputStream(CONFIG_FILE)) {
            properties.load(input);
            return Boolean.parseBoolean(properties.getProperty("exist"));
        } catch (FileNotFoundException e) {
            return false;
        } catch (IOException e) {
            LOGGER.info("Ошибка чтения файла конфигурации");
        }
        throw new IllegalStateException();
    }

    /**
     * Передаёт на чтение файлы из директории path
     *
     * @param path путь до директории / файла
     */
    private void executeScript(String path) throws URISyntaxException {
        URL resource = this.getClass().getClassLoader().getResource(path);
        if (resource != null) {
            File pathFile = new File(resource.toURI());
            if (pathFile.isFile()) {
                executeFile(pathFile);
            } else if (pathFile.isDirectory()) {
                Collection<File> files = FileUtils
                        .listFiles(pathFile, null, false)
                        .stream()
                        .sorted((file1, file2) -> file1.getName().compareTo(file2.getName()))
                        .collect(Collectors.toList());
                files.forEach(this::executeFile);
            }
        } else {
            LOGGER.info("Директория или файл '" + path + "' не существует");
            throw new IllegalStateException();
        }
    }

    /**
     * Метод передаёт на выполнение скприт из файла
     *
     * @param file файл скрипта
     */
    private void executeFile(File file) {
        try {
            LOGGER.debug("Файл: {}", file.getName());
            executeSql(FileUtils.readFileToString(file));
        } catch (DuplicateKeyException e) {
            LOGGER.debug("Попытка дублирования записи");
        } catch (SQLException | IOException e) {
            LOGGER.debug("Ошибка в скрипте: " + file.getName(), e);
            throw new IllegalStateException();
        }
    }

    private void executeSql(String sql) throws SQLException {
        LOGGER.debug("-------------Start Migration-------------------");
        LOGGER.debug("SQL: " + sql);
        template.execute(sql);
        LOGGER.debug("-------------Migration complete----------------");
    }

    @Override
    public void needClear(boolean needClear) {
        this.needClear = needClear;
    }
}
