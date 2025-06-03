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


    /**
     * Завантажує детальний вигляд оголошення з базовими параметрами
     */
    public static void loadAdDetailScene(Ad ad) throws IOException {
        // За замовчуванням використовуємо автоматичне визначення декораторів, якщо AdDetailController це підтримує
        loadAdDetailSceneWithAutoDecorators(ad);
    }


    /**
     * Завантажує детальний вигляд оголошення з автоматичним визначенням декораторів
     */
    public static void loadAdDetailSceneWithAutoDecorators(Ad ad) throws IOException {
        URL fxmlLocation = MainGuiApp.class.getResource("/com/example/olx/presentation/gui/view/AdDetailView.fxml");
        if (fxmlLocation == null) {
            throw new IOException("Cannot find FXML file: AdDetailView.fxml. Path: /com/example/olx/presentation/gui/view/AdDetailView.fxml");
        }
        FXMLLoader fxmlLoader = new FXMLLoader(fxmlLocation);
        Parent root = fxmlLoader.load();
        AdDetailController controller = fxmlLoader.getController();

        if (controller == null) {
            throw new IOException("Controller for AdDetailView.fxml is null. Check fx:controller attribute in FXML.");
        }

        // Перевірка чи контролер має метод initDataWithAutoDecorators
        // Цей підхід з рефлексією не найкращий, краще мати інтерфейс або базовий клас для контролерів
        if (hasAutoDecoratorsMethod(controller)) {
            controller.initDataWithAutoDecorators(ad);
        } else {
            // Fallback до стандартного методу
            controller.initData(ad); // Припускаємо, що initData(Ad ad) завжди існує
        }

        Scene scene = primaryStage.getScene();
        if (scene == null) {
            scene = new Scene(root, 800, 650); // Розміри за замовчуванням
            primaryStage.setScene(scene);
        } else {
            scene.setRoot(root);
        }
        primaryStage.setTitle("Деталі оголошення: " + (ad != null ? ad.getTitle() : "Невідоме оголошення"));
    }

    /**
     * Завантажує детальний вигляд оголошення з повним контролем декораторів
     * Цей метод може бути корисним, якщо декоратори визначаються не лише властивостями самого Ad.
     */
    public static void loadAdDetailSceneWithDecorators(Ad ad, boolean isPremium, boolean isUrgent,
                                                       Double discountPercentage, String discountReason,
                                                       Integer warrantyMonths, String warrantyType,
                                                       Boolean freeDelivery, Double deliveryCost, String deliveryInfo) throws IOException {
        URL fxmlLocation = MainGuiApp.class.getResource("/com/example/olx/presentation/gui/view/AdDetailView.fxml");
        if (fxmlLocation == null) {
            throw new IOException("Cannot find FXML file: AdDetailView.fxml. Path: /com/example/olx/presentation/gui/view/AdDetailView.fxml");
        }
        FXMLLoader fxmlLoader = new FXMLLoader(fxmlLocation);
        Parent root = fxmlLoader.load();
        AdDetailController controller = fxmlLoader.getController();

        if (controller == null) {
            throw new IOException("Controller for AdDetailView.fxml is null. Check fx:controller attribute in FXML.");
        }

        // Перевірка чи контролер має метод з декораторами
        if (hasDecoratorsMethod(controller)) {
            controller.initData(ad, isPremium, isUrgent, discountPercentage, discountReason,
                    warrantyMonths, warrantyType, freeDelivery, deliveryCost, deliveryInfo);
        } else {
            // Fallback до стандартного методу
            controller.initData(ad);
            System.out.println("Warning: AdDetailController doesn't support explicit decorators via initData overload, using basic initialization.");
        }

        Scene scene = primaryStage.getScene();
        if (scene == null) {
            scene = new Scene(root, 800, 650);
            primaryStage.setScene(scene);
        } else {
            scene.setRoot(root);
        }

        // Використовуємо декорований заголовок для вікна
        String windowTitle = "Деталі оголошення: " + (ad != null ? ad.getTitle() : "Невідоме оголошення");
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
        URL fxmlLocation = MainGuiApp.class.getResource("/com/example/olx/presentation/gui/view/CreateAdView.fxml");
        if (fxmlLocation == null) {
            throw new IOException("Cannot find FXML file: CreateAdView.fxml for editing. Path: /com/example/olx/presentation/gui/view/CreateAdView.fxml");
        }
        FXMLLoader fxmlLoader = new FXMLLoader(fxmlLocation);
        Parent root = fxmlLoader.load();
        CreateAdController controller = fxmlLoader.getController();

        if (controller == null) {
            throw new IOException("Controller for CreateAdView.fxml is null. Check fx:controller attribute in FXML.");
        }
        controller.initDataForEdit(adToEdit);

        Scene scene = primaryStage.getScene();
        if (scene == null) {
            scene = new Scene(root, 700, 600); // Розміри для форми створення/редагування
            primaryStage.setScene(scene);
        } else {
            scene.setRoot(root);
        }
        primaryStage.setTitle("Редагувати оголошення: " + (adToEdit != null ? adToEdit.getTitle() : ""));
    }


    /**
     * Перевіряє чи контролер має метод initDataWithAutoDecorators(Ad ad)
     */
    private static boolean hasAutoDecoratorsMethod(AdDetailController controller) {
        if (controller == null) return false;
        try {
            controller.getClass().getMethod("initDataWithAutoDecorators", Ad.class);
            return true;
        } catch (NoSuchMethodException e) {
            return false;
        }
    }

    /**
     * Перевіряє чи контролер має метод initData з усіма параметрами декораторів
     */
    private static boolean hasDecoratorsMethod(AdDetailController controller) {
        if (controller == null) return false;
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
        super.init(); // Важливо викликати super.init()
        System.out.println("Initializing backend services...");
// Ініціалізуємо SessionManager
        sessionManager = SessionManager.getInstance();
        sessionManager.setStorageFilePath("olx_gui_data.dat"); // Назва файлу для збереження даних
// Завантажуємо збережений стан
        sessionManager.loadState();
        System.out.println("Session state loaded successfully.");

        PasswordHasher passwordHasher = new DemoPasswordHasherImpl();
// Ініціалізуємо репозиторії
        UserRepository userRepository = new FileUserRepositoryImpl(sessionManager);
        CategoryRepository categoryRepository = new FileCategoryRepositoryImpl(sessionManager);
        AdRepository adRepository = new FileAdRepositoryImpl(sessionManager);

        // Приклад реалізації NotificationService, якщо AdState не використовується активно
        NotificationServicePort notificationService = new ConsoleNotificationServiceImpl() {
            @Override
            public void notifyAdStateChanged(Ad ad, AdState newState) {
                // System.out.println("Ad " + ad.getId() + " state changed to " + newState);
            }
        };
        AdSearchStrategy adSearchStrategy = new DefaultAdSearchStrategy();

        // Ініціалізуємо сервіси
        userService = new UserServiceImpl(userRepository, passwordHasher);
        categoryService = new CategoryServiceImpl(categoryRepository);
        adService = new AdServiceImpl(adRepository, userRepository, categoryRepository, notificationService, adSearchStrategy);
// Ініціалізація Command паттерну
        commandInvoker = new CommandInvoker();
        commandFactory = new CommandFactory(adService); // Передаємо вже ініціалізований adService
        adCommandManager = new AdCommandManager(commandInvoker, commandFactory);

        // Ініціалізуємо категорії (якщо їх ще немає)
        initializeDefaultCategories();
        System.out.println("Backend services initialized successfully.");
    }

    @Override
    public void start(Stage stage) throws IOException {
        primaryStage = stage;
        primaryStage.setTitle("OLX-дошка оголошень");
        loadLoginScene(); // Починаємо з екрану логіну
        primaryStage.show();
    }

    public static void loadLoginScene() throws IOException {
        loadScene("/com/example/olx/presentation/gui/view/LoginView.fxml", "Вхід / Реєстрація", 600, 400);
    }

    public static void loadMainScene() throws IOException {
        loadScene("/com/example/olx/presentation/gui/view/MainView.fxml", "Головна - OLX", 1200, 800);
    }

    public static void loadCreateAdScene() throws IOException {
        loadScene("/com/example/olx/presentation/gui/view/CreateAdView.fxml", "Створити оголошення - OLX", 700, 650);
    }

    private static void loadScene(String fxmlFile, String title, double width, double height) throws IOException {
        System.out.println("Attempting to load FXML: " + fxmlFile);
        URL fxmlLocation = MainGuiApp.class.getResource(fxmlFile);

        // Видалено спробу альтернативного шляху, оскільки це часто вказує на проблеми з конфігурацією проекту.
        // FXML файли мають бути в classpath у відповідній структурі папок.
        if (fxmlLocation == null) {
            throw new IOException("Cannot find FXML file at: " + fxmlFile +
                    ". Make sure FXML files are in the correct resources directory and the path starts with '/' and reflects the package structure.");
        }
        System.out.println("Found FXML at: " + fxmlLocation);

        FXMLLoader fxmlLoader = new FXMLLoader(fxmlLocation);
        Parent root = fxmlLoader.load();
        Scene scene = primaryStage.getScene();
        if (scene == null) {
            scene = new Scene(root, width, height);
            primaryStage.setScene(scene);
        } else {
            scene.setRoot(root);
            // Опціонально: можна налаштувати розмір сцени, якщо він змінився
            // primaryStage.setWidth(width);
            // primaryStage.setHeight(height);
        }
        primaryStage.setTitle(title);
    }
    // Перевантажений метод для сумісності, якщо розміри не вказані
    private static void loadScene(String fxmlFile, String title) throws IOException {
        loadScene(fxmlFile, title, 800, 600); // Розміри за замовчуванням
    }


    @Override
    public void stop() throws Exception {
        System.out.println("Application stopping. Saving session state...");
        if (sessionManager != null) {
            sessionManager.saveState();
            System.out.println("Session state saved successfully.");
        }
        super.stop(); // Важливо викликати super.stop()
    }

    private static void initializeDefaultCategories() {
        if (categoryService == null) {
            System.err.println("CategoryService is not initialized. Cannot initialize default categories.");
            return;
        }
        try {
            List<CategoryComponent> existingCategories = categoryService.getAllRootCategories();
            if (existingCategories != null && !existingCategories.isEmpty()) {
                System.out.println("Categories already exist (" + existingCategories.size() + " root categories found), skipping initialization.");
// Проблема [cite: 386] тут може бути, якщо getAllRootCategories() не повертає актуальні дані,
                // або якщо дані пошкоджені. Потрібно забезпечити надійність categoryService.
                return;
            }

            System.out.println("Initializing default categories...");
            List<CategoryComponent> rootCategoriesToInitialize = new ArrayList<>();

            // ВИПРАВЛЕНО: Конструктор Category(id, name, parentId)
            // Для кореневих категорій parentId має бути null.
            Category electronics = new Category("electronics", "Електроніка", null);
            // Додамо кілька підкатегорій для прикладу (якщо ваш Category це підтримує)
            // Category phones = new Category("phones", "Телефони", "electronics");
            // Category computers = new Category("computers", "Комп'ютери", "electronics");
            // electronics.addChild(phones); // Якщо є такий метод
            // electronics.addChild(computers);
            rootCategoriesToInitialize.add(electronics);

            Category clothing = new Category("clothing", "Одяг і взуття", null);
            rootCategoriesToInitialize.add(clothing);

            Category home = new Category("home", "Дім і сад", null);
            rootCategoriesToInitialize.add(home);

            Category auto = new Category("auto", "Авто", null);
            rootCategoriesToInitialize.add(auto);

            Category sport = new Category("sport", "Спорт і хобі", null);
            rootCategoriesToInitialize.add(sport);

            // Передаємо список КОРЕНЕВИХ категорій.
            // Метод initializeCategories має обробити їх та їхні дочірні елементи, якщо такі є.
            categoryService.initializeCategories(rootCategoriesToInitialize);
            System.out.println("Default categories initialization requested for " + rootCategoriesToInitialize.size() + " root categories.");

// Перевіряємо чи категорії справді додалися
            List<CategoryComponent> checkCategories = categoryService.getAllRootCategories();
            System.out.println("Categories after initialization attempt: " +
                    (checkCategories != null ? checkCategories.size() + " root categories found." : "null (service error)"));
        } catch (Exception e) { // Ловимо будь-які винятки під час ініціалізації
            System.err.println("Error initializing default categories: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Методи для доступу до команд з GUI (без змін)
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