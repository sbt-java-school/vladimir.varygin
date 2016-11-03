package lesson24.db.components;

import lesson24.db.DatabaseMigrations;
import lesson24.db.configuration.JdbcConfig;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.sql.SQLException;
import java.util.Collection;
import java.util.stream.Collectors;

@Repository
public class DatabaseMigrationsDao implements DatabaseMigrations {
    private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseMigrationsDao.class);

    private final JdbcTemplate template;
    private boolean needClear;

    @Autowired
    public DatabaseMigrationsDao(JdbcTemplate template) {
        this.template = template;
    }

    @Override
    public void migrate() {
        String driver = JdbcConfig.getInstance().getDriver();
        if (needClear) {
            LOGGER.info("Drop");
            executeScript("sql/drop");
        }
        LOGGER.info("Create");
        executeScript("sql/" + driver);
        LOGGER.info("Import");
        executeScript("sql/import");
    }

    /**
     * Выполняет sql скрипт/скрипты из файла/папки path
     *
     * @param path путь до файла/папки
     */
    private void executeScript(String path) {
        URL resource = this.getClass().getClassLoader().getResource(path);
        try {
            if (resource != null) {
                File pathFile = new File(resource.toURI());
                LOGGER.debug(pathFile.toString());
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
                LOGGER.debug("Directory or file " + path + " isn't exist");
            }
        } catch (URISyntaxException e) {
            throw new IllegalStateException("Wrong path: " + resource.toString());
        }
    }

    private void executeFile(File file) {
        try {
            LOGGER.info("New file execution: {}", file.getName());
            executeSql(FileUtils.readFileToString(file));
        } catch (DuplicateKeyException e) {
            LOGGER.debug("Попытка дублирования записи");
        } catch (SQLException e) {
            throw new IllegalStateException("Bad script: " + file.getName(), e);
        } catch (IOException e) {
            throw new IllegalStateException("Can't read file: " + file.getName(), e);
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
