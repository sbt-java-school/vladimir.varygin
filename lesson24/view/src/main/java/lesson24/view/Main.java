package lesson24.view;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import lesson24.services.MainService;
import lesson24.view.views.home.HomePage;

import java.io.IOException;

/**
 * Класс для запуска программы
 */
public class Main extends Application {
    private Stage primaryStage;

    @Override
    public void start(Stage primaryStage) throws Exception {
        this.primaryStage = primaryStage;
        primaryStage.setTitle("Рецепты");

        initRootLayOut();
    }

    /**
     * Создание окна-меню приложения
     */
    private void initRootLayOut() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass()
                    .getResource("views/home/home.fxml"));
            AnchorPane rootLayout = loader.load();

            Scene scene = new Scene(rootLayout);
            primaryStage.setScene(scene);
            primaryStage.show();

            HomePage controller = loader.getController();
            controller.setStage(primaryStage);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {
        MainService.createTables();
        launch(args);
    }
}