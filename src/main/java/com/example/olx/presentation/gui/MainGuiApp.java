// src/main/java/com/example/olx/presentation/gui/MainGuiApp.java
package com.example.olx.presentation.gui;

import com.example.olx.application.service.impl.AdServiceImpl;
import com.example.olx.application.service.impl.CategoryServiceImpl;
import com.example.olx.application.service.impl.UserServiceImpl;
import com.example.olx.application.service.port.AdServicePort;
import com.example.olx.application.service.port.CategoryServicePort;
import com.example.olx.application.service.port.NotificationServicePort;
import com.example.olx.application.service.port.UserService;
import com.example.olx.application.service.strategy.AdSearchStrategy;
import com.example.olx.application.service.strategy.DefaultAdSearchStrategy;
import com.example.olx.domain.model.Ad;
import com.example.olx.domain.model.Category;
import com.example.olx.domain.model.CategoryComponent;
import com.example.olx.domain.repository.AdRepository;
import com.example.olx.domain.repository.CategoryRepository;
import com.example.olx.domain.repository.UserRepository;
import com.example.olx.infrastructure.notification.ConsoleNotificationServiceImpl; // Тимчасово, для GUI може бути інший
import com.example.olx.infrastructure.persistence.FileAdRepositoryImpl;
import com.example.olx.infrastructure.persistence.FileCategoryRepositoryImpl;
import com.example.olx.infrastructure.persistence.FileUserRepositoryImpl;
import com.example.olx.infrastructure.persistence.SessionManager;
import com.example.olx.infrastructure.security.DemoPasswordHasherImpl;
import com.example.olx.infrastructure.security.PasswordHasher;
import com.example.olx.presentation.gui.controller.AdDetailController;
import com.example.olx.presentation.gui.controller.CreateAdController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MainGuiApp extends Application {

    private static Stage primaryStage; // Для зміни сцен

    // Сервіси (статичні для доступу з контролерів або через DI фреймворк)
    public static UserService userService;
    public static AdServicePort adService;
    public static CategoryServicePort categoryService;
    public static SessionManager sessionManager; // Для збереження при закритті

    public static void loadAdDetailScene(Ad ad) throws IOException {
        URL fxmlLocation = MainGuiApp.class.getResource("view/AdDetailView.fxml");
        if (fxmlLocation == null) {
            throw new IOException("Cannot find FXML file: view/AdDetailView.fxml");
        }
        FXMLLoader fxmlLoader = new FXMLLoader(fxmlLocation);
        Parent root = fxmlLoader.load();

        // Отримуємо контролер та передаємо дані
        AdDetailController controller = fxmlLoader.getController();
        controller.initData(ad);

        Scene scene = primaryStage.getScene();
        if (scene == null) {
            scene = new Scene(root, 800, 650);
            primaryStage.setScene(scene);
        } else {
            scene.setRoot(root);
        }
        primaryStage.setTitle("Деталі оголошення: " + ad.getTitle());
    }

    public static void loadEditAdScene(Ad adToEdit) throws IOException {
        URL fxmlLocation = MainGuiApp.class.getResource("view/CreateAdView.fxml"); // Використовуємо ту ж FXML
        if (fxmlLocation == null) {
            throw new IOException("Cannot find FXML file: view/CreateAdView.fxml for editing");
        }
        FXMLLoader fxmlLoader = new FXMLLoader(fxmlLocation);
        Parent root = fxmlLoader.load();

        CreateAdController controller = fxmlLoader.getController();
        controller.initDataForEdit(adToEdit); // Передаємо дані для редагування

        Scene scene = primaryStage.getScene();
        if (scene == null) {
            scene = new Scene(root, 700, 600); // Розміри можуть бути ті ж, що й для CreateAdView
            primaryStage.setScene(scene);
        } else {
            scene.setRoot(root);
        }
        primaryStage.setTitle("Редагувати оголошення: " + adToEdit.getTitle());
    }

    @Override
    public void init() throws Exception {
        // Ініціалізація бекенду тут, до того, як GUI почне завантажуватися
        System.out.println("Initializing backend services...");
        sessionManager = SessionManager.getInstance();
        sessionManager.setStorageFilePath("olx_gui_data.dat"); // Новий файл для GUI версії
        sessionManager.loadState();

        PasswordHasher passwordHasher = new DemoPasswordHasherImpl();

        UserRepository userRepository = new FileUserRepositoryImpl(sessionManager);
        CategoryRepository categoryRepository = new FileCategoryRepositoryImpl(sessionManager);
        AdRepository adRepository = new FileAdRepositoryImpl(sessionManager);

        // Для GUI, можливо, варто мати NullNotificationService або спеціальний GUI нотифікатор
        NotificationServicePort notificationService = new ConsoleNotificationServiceImpl();
        AdSearchStrategy adSearchStrategy = new DefaultAdSearchStrategy();

        userService = new UserServiceImpl(userRepository, passwordHasher);
        categoryService = new CategoryServiceImpl(categoryRepository);
        adService = new AdServiceImpl(adRepository, userRepository, categoryRepository, notificationService, adSearchStrategy);

        // Ініціалізація категорій, якщо їх ще немає
        initializeDefaultCategories();

        System.out.println("Backend services initialized.");
    }

    @Override
    public void start(Stage stage) throws IOException {
        primaryStage = stage;
        primaryStage.setTitle("OLX-дошка оголошень");
        loadLoginScene(); // Починаємо з екрану входу
        primaryStage.show();
    }

    public static void loadLoginScene() throws IOException {
        loadScene("view/LoginView.fxml", "Вхід / Реєстрація");
    }

    public static void loadMainScene() throws IOException {
        loadScene("view/MainView.fxml", "Головна - OLX");
    }

    public static void loadCreateAdScene() throws IOException {
        loadScene("view/CreateAdView.fxml", "Створити оголошення - OLX");
    }

    private static void loadScene(String fxmlFile, String title) throws IOException {
        // Переконуємося, що шлях до FXML правильний відносно classpath
        URL fxmlLocation = MainGuiApp.class.getResource(fxmlFile);
        if (fxmlLocation == null) {
            System.err.println("Cannot find FXML file: " + fxmlFile);
            throw new IOException("Cannot find FXML file: " + fxmlFile + ". Make sure it's in the correct resources/com/example/olx/presentation/gui/view/ directory.");
        }
        FXMLLoader fxmlLoader = new FXMLLoader(fxmlLocation);
        Parent root = fxmlLoader.load();
        Scene scene = primaryStage.getScene();
        if (scene == null) {
            scene = new Scene(root, 800, 600); // Початковий розмір
            primaryStage.setScene(scene);
        } else {
            scene.setRoot(root);
        }
        primaryStage.setTitle(title);
        // Можна додати застосування CSS тут, якщо є глобальний CSS
        // scene.getStylesheets().add(MainGuiApp.class.getResource("style/global.css").toExternalForm());
    }

    @Override
    public void stop() throws Exception {
        // Збереження стану при закритті програми
        System.out.println("Application stopping. Saving session state...");
        if (sessionManager != null) {
            sessionManager.saveState();
            System.out.println("Session state saved.");
        }
        super.stop();
    }

    private static void initializeDefaultCategories() {
        // Перевіряємо, чи є вже категорії
        List<CategoryComponent> existingCategories = categoryService.getAllRootCategories();
        if (existingCategories != null && !existingCategories.isEmpty()) {
            System.out.println("Categories already exist, skipping initialization.");
            return;
        }

        System.out.println("Initializing default categories...");

        // Створюємо базові категорії
        List<CategoryComponent> rootCategories = new ArrayList<>();

        // Електроніка
        Category electronics = new Category( "Електроніка");

        rootCategories.add(electronics);

        // Одяг і взуття
        Category clothing = new Category( "Одяг і взуття");

        rootCategories.add(clothing);

        // Дім і сад
        Category home = new Category( "Дім і сад");

        rootCategories.add(home);

        // Авто
        Category auto = new Category( "Авто");

        rootCategories.add(auto);

        // Спорт і хобі
        Category sport = new Category( "Спорт і хобі");

        rootCategories.add(sport);

        // Ініціалізуємо категорії в сервісі
        try {
            categoryService.initializeCategories(rootCategories);
            System.out.println("Default categories initialized successfully.");
        } catch (Exception e) {
            System.err.println("Error initializing categories: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}