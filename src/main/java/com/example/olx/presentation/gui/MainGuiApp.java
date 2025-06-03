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
import com.example.olx.domain.model.AdState;
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

    // ===== ПОКРАЩЕНІ МЕТОДИ ДЛЯ ВІДОБРАЖЕННЯ ОГОЛОШЕНЬ =====

    /**
     * Універсальний метод для завантаження деталей оголошення
     * Використовує паттерн Builder для конфігурації
     */
    public static void loadAdDetailScene(Ad ad, AdDisplayConfig config) throws IOException {
        if (ad == null) {
            throw new IllegalArgumentException("Ad cannot be null");
        }

        ViewResult<AdDetailController> adDetailView = loadView(
                "/com/example/olx/presentation/gui/view/AdDetailView.fxml",
                AdDetailController.class
        );

        // Ініціалізуємо контролер з конфігурацією
        adDetailView.controller.initializeWithConfig(ad, config);

        // Налаштовуємо заголовок вікна
        String windowTitle = buildWindowTitle(ad, config);

        showScene(adDetailView.root, windowTitle, 800, 650);
    }

    /**
     * Спрощений метод для звичайного відображення оголошення
     */
    public static void loadAdDetailScene(Ad ad) throws IOException {
        loadAdDetailScene(ad, AdDisplayConfig.defaultConfig());
    }

    /**
     * Метод для преміум оголошень
     */
    public static void loadPremiumAdDetailScene(Ad ad) throws IOException {
        AdDisplayConfig config = AdDisplayConfig.builder()
                .premium(true)
                .warranty(12, "Розширена гарантія")
                .freeDelivery(true)
                .deliveryInfo("Безкоштовна експрес-доставка")
                .build();

        loadAdDetailScene(ad, config);
    }

    /**
     * Метод для термінових оголошень зі знижкою
     */
    public static void loadUrgentDiscountAdDetailScene(Ad ad, double discountPercent) throws IOException {
        AdDisplayConfig config = AdDisplayConfig.builder()
                .urgent(true)
                .discount(discountPercent, "Термінова розпродаж")
                .delivery(30.0, "Швидка доставка")
                .build();

        loadAdDetailScene(ad, config);
    }

    /**
     * Завантажує сцену редагування оголошення
     */
    public static void loadEditAdScene(Ad adToEdit) throws IOException {
        if (adToEdit == null) {
            throw new IllegalArgumentException("Ad to edit cannot be null");
        }

        ViewResult<CreateAdController> createAdView = loadView(
                "/com/example/olx/presentation/gui/view/CreateAdView.fxml",
                CreateAdController.class
        );

        createAdView.controller.initDataForEdit(adToEdit);

        String title = "Редагувати оголошення: " + adToEdit.getTitle();
        showScene(createAdView.root, title, 700, 600);
    }

    // ===== ДОПОМІЖНІ МЕТОДИ =====

    /**
     * Універсальний метод для завантаження будь-якого виду
     */
    private static <T> ViewResult<T> loadView(String fxmlPath, Class<T> controllerClass) throws IOException {
        try {
            URL fxmlLocation = MainGuiApp.class.getResource(fxmlPath);
            if (fxmlLocation == null) {
                throw new IOException("FXML file not found: " + fxmlPath +
                        ". Make sure FXML files are in the correct resources directory.");
            }

            System.out.println("Loading FXML from: " + fxmlLocation);

            FXMLLoader fxmlLoader = new FXMLLoader(fxmlLocation);
            Parent root = fxmlLoader.load();
            T controller = fxmlLoader.getController();

            if (controller == null) {
                throw new IOException("Controller is null for: " + fxmlPath +
                        ". Check fx:controller attribute in FXML file.");
            }

            return new ViewResult<>(root, controller);

        } catch (IOException e) {
            System.err.println("Error loading view: " + fxmlPath + " - " + e.getMessage());
            throw e;
        }
    }

    /**
     * Універсальний метод для показу сцени
     */
    private static void showScene(Parent root, String title, double width, double height) {
        Scene scene = primaryStage.getScene();
        if (scene == null) {
            scene = new Scene(root, width, height);
            primaryStage.setScene(scene);
        } else {
            scene.setRoot(root);
        }
        primaryStage.setTitle(title);
    }

    /**
     * Будує заголовок вікна на основі оголошення та конфігурації
     */
    private static String buildWindowTitle(Ad ad, AdDisplayConfig config) {
        StringBuilder title = new StringBuilder();

        // Додаємо префікси для спеціальних типів
        if (config.isUrgent()) {
            title.append("🚨 ");
        }
        if (config.isPremium()) {
            title.append("⭐ ");
        }

        title.append("Деталі оголошення: ").append(ad.getTitle());

        return title.toString();
    }

    // ===== ОСНОВНІ МЕТОДИ ЗАВАНТАЖЕННЯ СЦЕН =====

    public static void loadLoginScene() throws IOException {
        loadScene("/com/example/olx/presentation/gui/view/LoginView.fxml",
                "Вхід / Реєстрація", 600, 400);
    }

    public static void loadMainScene() throws IOException {
        loadScene("/com/example/olx/presentation/gui/view/MainView.fxml",
                "Головна - OLX", 1200, 800);
    }

    public static void loadCreateAdScene() throws IOException {
        loadScene("/com/example/olx/presentation/gui/view/CreateAdView.fxml",
                "Створити оголошення - OLX", 700, 650);
    }

    /**
     * Універсальний метод для завантаження простих сцен
     */
    private static void loadScene(String fxmlFile, String title, double width, double height) throws IOException {
        try {
            System.out.println("Attempting to load FXML: " + fxmlFile);
            URL fxmlLocation = MainGuiApp.class.getResource(fxmlFile);

            if (fxmlLocation == null) {
                throw new IOException("Cannot find FXML file at: " + fxmlFile +
                        ". Make sure FXML files are in the correct resources directory and the path starts with '/' and reflects the package structure.");
            }

            System.out.println("Found FXML at: " + fxmlLocation);

            FXMLLoader fxmlLoader = new FXMLLoader(fxmlLocation);
            Parent root = fxmlLoader.load();

            showScene(root, title, width, height);

        } catch (IOException e) {
            System.err.println("Error loading scene: " + fxmlFile + " - " + e.getMessage());
            throw e;
        }
    }

    // ===== МЕТОДИ ЖИТТЄВОГО ЦИКЛУ ДОДАТКУ =====

    @Override
    public void init() throws Exception {
        super.init();
        System.out.println("Initializing backend services...");

        // Ініціалізуємо SessionManager
        sessionManager = SessionManager.getInstance();
        sessionManager.setStorageFilePath("olx_gui_data.dat");

        // Завантажуємо збережений стан
        sessionManager.loadState();
        System.out.println("Session state loaded successfully.");

        PasswordHasher passwordHasher = new DemoPasswordHasherImpl();

        // Ініціалізуємо репозиторії
        UserRepository userRepository = new FileUserRepositoryImpl(sessionManager);
        CategoryRepository categoryRepository = new FileCategoryRepositoryImpl(sessionManager);
        AdRepository adRepository = new FileAdRepositoryImpl(sessionManager);

        // Ініціалізуємо NotificationService
        NotificationServicePort notificationService = new ConsoleNotificationServiceImpl() {
            @Override
            public void notifyAdStateChanged(Ad ad, AdState newState) {
                System.out.println("Ad " + ad.getId() + " state changed to " + newState);
            }
        };

        AdSearchStrategy adSearchStrategy = new DefaultAdSearchStrategy();

        // Ініціалізуємо сервіси
        userService = new UserServiceImpl(userRepository, passwordHasher);
        categoryService = new CategoryServiceImpl(categoryRepository);
        adService = new AdServiceImpl(adRepository, userRepository, categoryRepository,
                notificationService, adSearchStrategy);

        // Ініціалізація Command паттерну
        commandInvoker = new CommandInvoker();
        commandFactory = new CommandFactory(adService);
        adCommandManager = new AdCommandManager(commandInvoker, commandFactory);

        // Ініціалізуємо категорії (якщо їх ще немає)
        initializeDefaultCategories();
        System.out.println("Backend services initialized successfully.");
    }

    @Override
    public void start(Stage stage) throws IOException {
        primaryStage = stage;
        primaryStage.setTitle("OLX-дошка оголошень");
        loadLoginScene();
        primaryStage.show();
    }

    @Override
    public void stop() throws Exception {
        System.out.println("Application stopping. Saving session state...");
        if (sessionManager != null) {
            sessionManager.saveState();
            System.out.println("Session state saved successfully.");
        }
        super.stop();
    }

    // ===== ІНІЦІАЛІЗАЦІЯ ДАНИХ =====

    private static void initializeDefaultCategories() {
        if (categoryService == null) {
            System.err.println("CategoryService is not initialized. Cannot initialize default categories.");
            return;
        }

        try {
            List<CategoryComponent> existingCategories = categoryService.getAllRootCategories();
            if (existingCategories != null && !existingCategories.isEmpty()) {
                System.out.println("Categories already exist (" + existingCategories.size() +
                        " root categories found), skipping initialization.");
                return;
            }

            System.out.println("Initializing default categories...");
            List<CategoryComponent> rootCategoriesToInitialize = new ArrayList<>();

            // Створюємо кореневі категорії
            Category electronics = new Category("electronics", "Електроніка", null);
            rootCategoriesToInitialize.add(electronics);

            Category clothing = new Category("clothing", "Одяг і взуття", null);
            rootCategoriesToInitialize.add(clothing);

            Category home = new Category("home", "Дім і сад", null);
            rootCategoriesToInitialize.add(home);

            Category auto = new Category("auto", "Авто", null);
            rootCategoriesToInitialize.add(auto);

            Category sport = new Category("sport", "Спорт і хобі", null);
            rootCategoriesToInitialize.add(sport);

            // Ініціалізуємо категорії
            categoryService.initializeCategories(rootCategoriesToInitialize);
            System.out.println("Default categories initialization requested for " +
                    rootCategoriesToInitialize.size() + " root categories.");

            // Перевіряємо результат
            List<CategoryComponent> checkCategories = categoryService.getAllRootCategories();
            System.out.println("Categories after initialization: " +
                    (checkCategories != null ? checkCategories.size() + " root categories found." :
                            "null (service error)"));

        } catch (Exception e) {
            System.err.println("Error initializing default categories: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ===== ГЕТТЕРИ ДЛЯ ДОСТУПУ ДО КОМАНД =====

    public static AdCommandManager getAdCommandManager() {
        return adCommandManager;
    }

    public static CommandInvoker getCommandInvoker() {
        return commandInvoker;
    }

    // ===== MAIN МЕТОД =====

    public static void main(String[] args) {
        launch(args);
    }

    // ===== ДОПОМІЖНІ КЛАСИ =====

    /**
     * Допоміжний клас для результату завантаження виду
     */
    private static class ViewResult<T> {
        final Parent root;
        final T controller;

        ViewResult(Parent root, T controller) {
            this.root = root;
            this.controller = controller;
        }
    }
}

/**
 * Конфігурація для відображення оголошення
 * Використовує паттерн Builder для гнучкого налаштування
 */
class AdDisplayConfig {
    private final boolean premium;
    private final boolean urgent;
    private final Double discountPercentage;
    private final String discountReason;
    private final Integer warrantyMonths;
    private final String warrantyType;
    private final Boolean freeDelivery;
    private final Double deliveryCost;
    private final String deliveryInfo;

    private AdDisplayConfig(Builder builder) {
        this.premium = builder.premium;
        this.urgent = builder.urgent;
        this.discountPercentage = builder.discountPercentage;
        this.discountReason = builder.discountReason;
        this.warrantyMonths = builder.warrantyMonths;
        this.warrantyType = builder.warrantyType;
        this.freeDelivery = builder.freeDelivery;
        this.deliveryCost = builder.deliveryCost;
        this.deliveryInfo = builder.deliveryInfo;
    }

    /**
     * Створює конфігурацію за замовчуванням
     */
    public static AdDisplayConfig defaultConfig() {
        return builder().build();
    }

    /**
     * Створює новий Builder
     */
    public static Builder builder() {
        return new Builder();
    }

    // Геттери
    public boolean isPremium() { return premium; }
    public boolean isUrgent() { return urgent; }
    public Double getDiscountPercentage() { return discountPercentage; }
    public String getDiscountReason() { return discountReason; }
    public Integer getWarrantyMonths() { return warrantyMonths; }
    public String getWarrantyType() { return warrantyType; }
    public Boolean getFreeDelivery() { return freeDelivery; }
    public Double getDeliveryCost() { return deliveryCost; }
    public String getDeliveryInfo() { return deliveryInfo; }

    /**
     * Builder для AdDisplayConfig
     */
    public static class Builder {
        private boolean premium = false;
        private boolean urgent = false;
        private Double discountPercentage;
        private String discountReason;
        private Integer warrantyMonths;
        private String warrantyType;
        private Boolean freeDelivery;
        private Double deliveryCost;
        private String deliveryInfo;

        public Builder premium(boolean premium) {
            this.premium = premium;
            return this;
        }

        public Builder urgent(boolean urgent) {
            this.urgent = urgent;
            return this;
        }

        public Builder discount(double percentage, String reason) {
            this.discountPercentage = percentage;
            this.discountReason = reason;
            return this;
        }

        public Builder warranty(int months, String type) {
            this.warrantyMonths = months;
            this.warrantyType = type;
            return this;
        }

        public Builder freeDelivery(boolean free) {
            this.freeDelivery = free;
            if (free) {
                this.deliveryCost = 0.0;
            }
            return this;
        }

        public Builder delivery(double cost, String info) {
            this.deliveryCost = cost;
            this.deliveryInfo = info;
            this.freeDelivery = (cost == 0.0);
            return this;
        }

        public Builder deliveryInfo(String info) {
            this.deliveryInfo = info;
            return this;
        }

        public AdDisplayConfig build() {
            return new AdDisplayConfig(this);
        }
    }
}