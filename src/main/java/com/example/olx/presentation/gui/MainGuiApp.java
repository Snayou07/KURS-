// src/main/java/com/example/olx/presentation/gui/MainGuiApp.java
package com.example.olx.presentation.gui;

import com.example.olx.application.command.AdCommandManager;
import com.example.olx.application.command.CommandFactory;
import com.example.olx.application.command.CommandInvoker;
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
import com.example.olx.infrastructure.notification.ConsoleNotificationServiceImpl;
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

    private static Stage primaryStage;

    // Сервіси
    public static UserService userService;
    public static AdServicePort adService;
    public static CategoryServicePort categoryService;
    public static SessionManager sessionManager;

    // Command паттерн компоненти
    public static AdCommandManager adCommandManager;
    public static CommandInvoker commandInvoker;
    public static CommandFactory commandFactory;

    /**
     * Завантажує детальний вигляд оголошення з базовими параметрами
     */
    public static void loadAdDetailScene(Ad ad) throws IOException {
        loadAdDetailSceneWithDecorators(ad, false, false, null, null, null, null, null, null, null);
    }

    /**
     * Завантажує детальний вигляд оголошення з автоматичним визначенням декораторів
     */
    public static void loadAdDetailSceneWithAutoDecorators(Ad ad) throws IOException {
        URL fxmlLocation = MainGuiApp.class.getResource("view/AdDetailView.fxml");
        if (fxmlLocation == null) {
            throw new IOException("Cannot find FXML file: view/AdDetailView.fxml");
        }
        FXMLLoader fxmlLoader = new FXMLLoader(fxmlLocation);
        Parent root = fxmlLoader.load();

        AdDetailController controller = fxmlLoader.getController();
        // Перевірка чи контролер має метод initDataWithAutoDecorators
        if (hasAutoDecoratorsMethod(controller)) {
            controller.initDataWithAutoDecorators(ad);
        } else {
            // Fallback до стандартного методу
            controller.initData(ad);
        }

        Scene scene = primaryStage.getScene();
        if (scene == null) {
            scene = new Scene(root, 800, 650);
            primaryStage.setScene(scene);
        } else {
            scene.setRoot(root);
        }
        primaryStage.setTitle("Деталі оголошення: " + ad.getTitle());
    }

    /**
     * Завантажує детальний вигляд оголошення з повним контролем декораторів
     */
    public static void loadAdDetailSceneWithDecorators(Ad ad, boolean isPremium, boolean isUrgent,
                                                       Double discountPercentage, String discountReason,
                                                       Integer warrantyMonths, String warrantyType,
                                                       Boolean freeDelivery, Double deliveryCost, String deliveryInfo) throws IOException {
        URL fxmlLocation = MainGuiApp.class.getResource("view/AdDetailView.fxml");
        if (fxmlLocation == null) {
            throw new IOException("Cannot find FXML file: view/AdDetailView.fxml");
        }
        FXMLLoader fxmlLoader = new FXMLLoader(fxmlLocation);
        Parent root = fxmlLoader.load();

        AdDetailController controller = fxmlLoader.getController();

        // Перевірка чи контролер має метод з декораторами
        if (hasDecoratorsMethod(controller)) {
            controller.initData(ad, isPremium, isUrgent, discountPercentage, discountReason,
                    warrantyMonths, warrantyType, freeDelivery, deliveryCost, deliveryInfo);
        } else {
            // Fallback до стандартного методу
            controller.initData(ad);
            System.out.println("Warning: AdDetailController doesn't support decorators, using basic initialization");
        }

        Scene scene = primaryStage.getScene();
        if (scene == null) {
            scene = new Scene(root, 800, 650);
            primaryStage.setScene(scene);
        } else {
            scene.setRoot(root);
        }

        // Використовуємо декорований заголовок для вікна
        String windowTitle = "Деталі оголошення: " + ad.getTitle();
        if (isPremium) windowTitle = "⭐ " + windowTitle;
        if (isUrgent) windowTitle = "🚨 " + windowTitle;

        primaryStage.setTitle(windowTitle);
    }

    /**
     * Приклад використання декорованого оголошення для преміум товарів
     */
    public static void loadPremiumAdDetailScene(Ad ad) throws IOException {
        loadAdDetailSceneWithDecorators(ad, true, false, null, null,
                12, "Розширена гарантія", true, 0.0, "Безкоштовна експрес-доставка");
    }

    /**
     * Приклад використання декорованого оголошення для термінових товарів зі знижкою
     */
    public static void loadUrgentDiscountAdDetailScene(Ad ad, double discountPercent) throws IOException {
        loadAdDetailSceneWithDecorators(ad, false, true, discountPercent, "Термінова розпродаж",
                null, null, false, 30.0, "Швидка доставка");
    }

    /**
     * Завантажує сцену редагування оголошення
     */
    public static void loadEditAdScene(Ad adToEdit) throws IOException {
        URL fxmlLocation = MainGuiApp.class.getResource("view/CreateAdView.fxml");
        if (fxmlLocation == null) {
            throw new IOException("Cannot find FXML file: view/CreateAdView.fxml for editing");
        }
        FXMLLoader fxmlLoader = new FXMLLoader(fxmlLocation);
        Parent root = fxmlLoader.load();

        CreateAdController controller = fxmlLoader.getController();
        controller.initDataForEdit(adToEdit);

        Scene scene = primaryStage.getScene();
        if (scene == null) {
            scene = new Scene(root, 700, 600);
            primaryStage.setScene(scene);
        } else {
            scene.setRoot(root);
        }
        primaryStage.setTitle("Редагувати оголошення: " + adToEdit.getTitle());
    }

    /**
     * Перевіряє чи контролер має метод initDataWithAutoDecorators
     */
    private static boolean hasAutoDecoratorsMethod(AdDetailController controller) {
        try {
            controller.getClass().getMethod("initDataWithAutoDecorators", Ad.class);
            return true;
        } catch (NoSuchMethodException e) {
            return false;
        }
    }

    /**
     * Перевіряє чи контролер має метод initData з декораторами
     */
    private static boolean hasDecoratorsMethod(AdDetailController controller) {
        try {
            controller.getClass().getMethod("initData", Ad.class, boolean.class, boolean.class,
                    Double.class, String.class, Integer.class, String.class,
                    Boolean.class, Double.class, String.class);
            return true;
        } catch (NoSuchMethodException e) {
            return false;
        }
    }

    @Override
    public void init() throws Exception {
        System.out.println("Initializing backend services...");
        sessionManager = SessionManager.getInstance();
        sessionManager.setStorageFilePath("olx_gui_data.dat");
        sessionManager.loadState();

        PasswordHasher passwordHasher = new DemoPasswordHasherImpl();

        UserRepository userRepository = new FileUserRepositoryImpl(sessionManager);
        CategoryRepository categoryRepository = new FileCategoryRepositoryImpl(sessionManager);
        AdRepository adRepository = new FileAdRepositoryImpl(sessionManager);

        NotificationServicePort notificationService = new ConsoleNotificationServiceImpl();
        AdSearchStrategy adSearchStrategy = new DefaultAdSearchStrategy();

        userService = new UserServiceImpl(userRepository, passwordHasher);
        categoryService = new CategoryServiceImpl(categoryRepository);
        adService = new AdServiceImpl(adRepository, userRepository, categoryRepository, notificationService, adSearchStrategy);

        // Ініціалізація Command паттерну
        commandInvoker = new CommandInvoker();
        commandFactory = new CommandFactory(adService);
        adCommandManager = new AdCommandManager(commandInvoker, commandFactory);

        initializeDefaultCategories();

        System.out.println("Backend services initialized.");
    }

    @Override
    public void start(Stage stage) throws IOException {
        primaryStage = stage;
        primaryStage.setTitle("OLX-дошка оголошень");
        loadLoginScene();
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
        URL fxmlLocation = MainGuiApp.class.getResource(fxmlFile);
        if (fxmlLocation == null) {
            System.err.println("Cannot find FXML file: " + fxmlFile);
            throw new IOException("Cannot find FXML file: " + fxmlFile + ". Make sure it's in the correct resources/com/example/olx/presentation/gui/view/ directory.");
        }
        FXMLLoader fxmlLoader = new FXMLLoader(fxmlLocation);
        Parent root = fxmlLoader.load();
        Scene scene = primaryStage.getScene();
        if (scene == null) {
            scene = new Scene(root, 800, 600);
            primaryStage.setScene(scene);
        } else {
            scene.setRoot(root);
        }
        primaryStage.setTitle(title);
    }

    @Override
    public void stop() throws Exception {
        System.out.println("Application stopping. Saving session state...");
        if (sessionManager != null) {
            sessionManager.saveState();
            System.out.println("Session state saved.");
        }
        super.stop();
    }

    private static void initializeDefaultCategories() {
        List<CategoryComponent> existingCategories = categoryService.getAllRootCategories();
        if (existingCategories != null && !existingCategories.isEmpty()) {
            System.out.println("Categories already exist, skipping initialization.");
            return;
        }

        System.out.println("Initializing default categories...");

        List<CategoryComponent> rootCategories = new ArrayList<>();

        Category electronics = new Category("root", "Всі категорії", "Електроніка");
        rootCategories.add(electronics);

        Category clothing = new Category("root", "Всі категорії", "Одяг і взуття");
        rootCategories.add(clothing);

        Category home = new Category("root", "Всі категорії", "Дім і сад");
        rootCategories.add(home);

        Category auto = new Category("root", "Всі категорії", "Авто");
        rootCategories.add(auto);

        Category sport = new Category("root", "Всі категорії", "Спорт і хобі");
        rootCategories.add(sport);

        try {
            categoryService.initializeCategories(rootCategories);
            System.out.println("Default categories initialized successfully.");
        } catch (Exception e) {
            System.err.println("Error initializing categories: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Методи для доступу до команд з GUI
    public static AdCommandManager getAdCommandManager() {
        return adCommandManager;
    }

    public static CommandInvoker getCommandInvoker() {
        return commandInvoker;
    }

    public static void main(String[] args) {
        launch(args);
    }
}