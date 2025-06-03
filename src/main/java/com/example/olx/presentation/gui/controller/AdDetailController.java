// src/main/java/com/example/olx/presentation/gui/controller/AdDetailController.java
package com.example.olx.presentation.gui.controller;

import com.example.olx.domain.decorator.AdComponent;
import com.example.olx.domain.decorator.AdDecoratorFactory;
import com.example.olx.domain.exception.AdNotFoundException;
import com.example.olx.domain.exception.UnauthorizedActionException;
import com.example.olx.domain.model.Ad;
import com.example.olx.domain.model.CategoryComponent;
import com.example.olx.domain.model.User;
import com.example.olx.domain.model.UserType;
import com.example.olx.presentation.gui.AdDisplayConfig;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.util.Duration;
import java.util.ArrayList;
import java.util.List;
import com.example.olx.presentation.gui.MainGuiApp;
import com.example.olx.presentation.gui.util.GlobalContext;
import javafx.fxml.FXML;
import javafx.scene.text.Text;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.application.Platform;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

public class AdDetailController {

    @FXML private Label titleLabel;
    @FXML private ImageView adImageView;
    @FXML private Label noImageLabel;
    @FXML private Label priceLabel;
    @FXML private Text descriptionText;
    @FXML private Label categoryLabel;
    @FXML private Label sellerLabel;
    @FXML private Label adIdLabel;
    @FXML private Button backButton;
    @FXML private HBox actionButtonsBox;
    @FXML private Button editButton;
    @FXML private Button deleteButton;

    // Додаткові елементи для декорованої інформації
    private VBox decoratedInfoContainer;
    private Text decoratedInfoText;

    // Галерея зображень
    private VBox imageGalleryContainer;
    private HBox thumbnailContainer;
    private ImageView currentMainImage;
    private int currentImageIndex = 0;
    private List<String> imagePaths;

    // Основний контейнер з FXML
    @FXML
    private VBox mainContainer;

    // Додаткові FXML елементи для методів initializeWithConfig
    @FXML private TextArea descriptionTextArea;
    @FXML private Label authorLabel;
    @FXML private Label statusLabel;
    @FXML private Label createdDateLabel;
    @FXML private VBox decoratorsContainer;
    @FXML private VBox premiumFeaturesContainer;
    @FXML private VBox imageContainer;
    @FXML private Button contactButton;
    @FXML private Button favoriteButton;
    @FXML private Button shareButton;

    private Ad currentAd;
    private AdComponent decoratedAd;

    public void initialize() {
        // Ініціалізуємо кнопки
        if (editButton != null) {
            editButton.setVisible(false);
            editButton.setManaged(false);
        }
        if (deleteButton != null) {
            deleteButton.setVisible(false);
            deleteButton.setManaged(false);
        }

        setupImageGallery();
        setupDecoratedInfoContainer();

        // Відкладена ініціалізація для коректного знаходження контейнера
        Platform.runLater(this::findMainContainer);
    }

    private void findMainContainer() {
        try {
            // Пробуємо знайти основний контейнер через різні шляхи
            if (mainContainer == null && titleLabel != null && titleLabel.getParent() != null) {
                // Піднімаємося по ієрархії до VBox
                var parent = titleLabel.getParent();
                while (parent != null && !(parent instanceof VBox)) {
                    parent = parent.getParent();
                }
                if (parent instanceof VBox) {
                    mainContainer = (VBox) parent;
                }
            }

            // Якщо все ще null, пробуємо через інші елементи
            if (mainContainer == null && descriptionText != null && descriptionText.getParent() != null) {
                var parent = descriptionText.getParent();
                while (parent != null && !(parent instanceof VBox)) {
                    parent = parent.getParent();
                }
                if (parent instanceof VBox) {
                    mainContainer = (VBox) parent;
                }
            }
        } catch (Exception e) {
            System.err.println("Помилка при пошуку основного контейнера: " + e.getMessage());
        }
    }
    private void setupDecoratedInfoContainer() {
        try {
            // Створюємо контейнер для декорованої інформації
            decoratedInfoContainer = new VBox(10);
            decoratedInfoContainer.setAlignment(Pos.CENTER_LEFT);
            decoratedInfoContainer.setPadding(new Insets(15));
            decoratedInfoContainer.setStyle(
                    "-fx-background-color: linear-gradient(to bottom, #e8f4fd, #f8fbff); " +
                            "-fx-border-color: #4a90e2; " +
                            "-fx-border-width: 2; " +
                            "-fx-border-radius: 10; " +
                            "-fx-background-radius: 10; " +
                            "-fx-effect: dropshadow(gaussian, rgba(74, 144, 226, 0.3), 8, 0.6, 0, 2);"
            );

            // Створюємо текстовий елемент для декорованої інформації
            decoratedInfoText = new Text();
            decoratedInfoText.setStyle(
                    "-fx-font-size: 14px; " +
                            "-fx-fill: #2c3e50; " +
                            "-fx-font-family: 'System';"
            );
            decoratedInfoText.setWrappingWidth(700.0);

            // Заголовок для декорованої інформації
            Label decoratedInfoLabel = new Label("✨ Спеціальні умови та особливості:");
            decoratedInfoLabel.setStyle(
                    "-fx-font-size: 16px; " +
                            "-fx-font-weight: bold; " +
                            "-fx-text-fill: #2c3e50; " +
                            "-fx-padding: 0 0 10 0;"
            );

            decoratedInfoContainer.getChildren().addAll(decoratedInfoLabel, decoratedInfoText);
        } catch (Exception e) {
            System.err.println("Помилка при створенні контейнера декорованої інформації: " + e.getMessage());
        }
    }

    private void setupImageGallery() {
        try {
            imageGalleryContainer = new VBox(10);
            imageGalleryContainer.setAlignment(Pos.CENTER);

            if (adImageView != null) {
                currentMainImage = adImageView;
                currentMainImage.setFitHeight(300.0);
                currentMainImage.setFitWidth(400.0);
                currentMainImage.setPreserveRatio(true);
                currentMainImage.setSmooth(true);
            }

            thumbnailContainer = new HBox(10);
            thumbnailContainer.setAlignment(Pos.CENTER);
            thumbnailContainer.setPadding(new Insets(10, 0, 10, 0));

            ScrollPane thumbnailScrollPane = new ScrollPane(thumbnailContainer);
            thumbnailScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
            thumbnailScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
            thumbnailScrollPane.setFitToHeight(true);
            thumbnailScrollPane.setPrefHeight(100);
            thumbnailScrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");

            imageGalleryContainer.getChildren().add(thumbnailScrollPane);
        } catch (Exception e) {
            System.err.println("Помилка при створенні галереї зображень: " + e.getMessage());
        }
    }

    public void initData(Ad ad) {
        initData(ad, false, false, null, null, null, null, null, null, null);
    }

    /**
     * Ініціалізація з декораторами
     */
    public void initData(Ad ad, boolean isPremium, boolean isUrgent,
                         Double discountPercentage, String discountReason,
                         Integer warrantyMonths, String warrantyType,
                         Boolean freeDelivery, Double deliveryCost, String deliveryInfo) {
        if (ad == null) {
            showErrorAndGoBack("Не вдалося завантажити деталі оголошення (дані не передано).");
            return;
        }

        this.currentAd = ad;

        try {
            // Створюємо декорований компонент
            this.decoratedAd = AdDecoratorFactory.createFullyDecoratedAd(ad,
                    isPremium,
                    isUrgent,
                    discountPercentage,
                    discountReason,
                    warrantyMonths,
                    warrantyType,
                    freeDelivery,
                    deliveryCost,
                    deliveryInfo);

            populateAdDetails();
            setupActionButtons();
            loadImages();

            // Відкладене додавання декорованої інформації
            Platform.runLater(this::displayDecoratedInfo);
        } catch (Exception e) {
            System.err.println("Помилка при ініціалізації даних: " + e.getMessage());
            e.printStackTrace();
            // Намагаємося завантажити базову версію без декораторів
            try {
                populateBasicAdDetails();
                setupActionButtons();
                loadImages();
            } catch (Exception fallbackException) {
                showErrorAndGoBack("Критична помилка завантаження оголошення: " + fallbackException.getMessage());
            }
        }
    }
    private void populateBasicAdDetails() {
        if (currentAd == null) return;

        if (titleLabel != null) {
            titleLabel.setText(currentAd.getTitle());
        }

        if (priceLabel != null) {
            priceLabel.setText(String.format("%.2f грн", currentAd.getPrice()));
        }

        if (descriptionText != null) {
            descriptionText.setText(currentAd.getDescription() != null ?
                    currentAd.getDescription() : "Опис відсутній.");
        }

        if (adIdLabel != null) {
            adIdLabel.setText(currentAd.getAdId());
        }
        if (categoryLabel != null && MainGuiApp.categoryService != null) {
            try {
                Optional<CategoryComponent> catOpt = MainGuiApp.categoryService.findCategoryById(currentAd.getCategoryId());
                categoryLabel.setText(catOpt.map(CategoryComponent::getName).orElse("Невідома категорія"));
            } catch (Exception e) {
                categoryLabel.setText("Невідома категорія");
                System.err.println("Помилка при завантаженні категорії: " + e.getMessage());
            }
        }

// Обробка продавця
        if (sellerLabel != null && MainGuiApp.userService != null) {
            try {
                User seller = MainGuiApp.userService.getUserById(currentAd.getSellerId());
                sellerLabel.setText(seller != null ? seller.getUsername() : "Невідомий продавець");
            } catch (Exception e) {
                sellerLabel.setText("Невідомий продавець");
                System.err.println("Помилка при завантаженні продавця: " + e.getMessage());
            }
        }
        // Інші базові поля...
    }
    /**
     * Альтернативний метод для швидкого створення декорованого оголошення
     */
    public void initDataWithAutoDecorators(Ad ad) {
        if (ad == null) {
            showErrorAndGoBack("Оголошення не знайдено.");
            return;
        }

        try {
            // Автоматично визначаємо декоратори на основі властивостей оголошення
            String titleLower = ad.getTitle() != null ? ad.getTitle().toLowerCase() : "";
            String descLower = ad.getDescription() != null ? ad.getDescription().toLowerCase() : "";

            boolean isPremium = titleLower.contains("преміум") || descLower.contains("преміум");
            boolean isUrgent = titleLower.contains("терміново") || descLower.contains("терміново");

            Double discount = null;
            String discountReason = null;
            if (descLower.contains("знижка")) {
                discount = 15.0; // 15% знижка за замовчуванням
                discountReason = "Сезонна розпродаж";
            }

            Integer warranty = null;
            String warrantyType = null;
            if (descLower.contains("гарантія")) {
                warranty = 12; // 12 місяців за замовчуванням
                warrantyType = "Офіційна гарантія виробника";
            }

            Boolean freeDelivery = null;
            Double deliveryCost = null;
            String deliveryInfo = null;
            if (descLower.contains("доставка") || descLower.contains("безкоштовна доставка")) {
                if (descLower.contains("безкоштовна")) {
                    freeDelivery = true;
                    deliveryInfo = "Безкоштовна доставка по всій Україні протягом 2-3 робочих днів";
                } else {
                    freeDelivery = false;
                    deliveryCost = 50.0;
                    deliveryInfo = "Доставка Новою Поштою або кур'єрською службою";
                }
            }

            initData(ad, isPremium, isUrgent, discount, discountReason,
                    warranty, warrantyType, freeDelivery, deliveryCost, deliveryInfo);
        } catch (Exception e) {
            System.err.println("Помилка при автоматичному створенні декораторів: " + e.getMessage());
            // Fallback до базової ініціалізації
            initData(ad);
        }
    }

    /**
     * Метод для тестування декораторів з випадковими значеннями
     */
    public void initDataWithTestDecorators(Ad ad) {
        initData(ad, true, true, 20.0, "Розпродаж залишків",
                24, "Розширена гарантія", true, 0.0, "Експрес доставка безкоштовно");
    }

    private void populateAdDetails() {

        try {
            if (decoratedAd == null || currentAd == null) {
                System.err.println("Дані оголошення не ініціалізовані");
                return; // замість throw new IllegalStateException
            }

            // Використовуємо декорований заголовок
            if (titleLabel != null) {
                titleLabel.setText(decoratedAd.getFormattedTitle());
            }

            // Обробка ціни
            if (priceLabel != null) {
                double calculatedPrice = decoratedAd.getCalculatedPrice();

                // Якщо ціна змінилася через декоратори, показуємо це
                if (Math.abs(calculatedPrice - currentAd.getPrice()) > 0.01) {
                    priceLabel.setText(String.format("%.2f грн", calculatedPrice));
                    priceLabel.setStyle("-fx-text-fill: #28a745; -fx-font-weight: bold; -fx-font-size: 18px;");
                } else {
                    priceLabel.setText(String.format("%.2f грн", calculatedPrice));
                }
            }

            // Основна інформація залишається з оригінального оголошенн
            if (descriptionText != null) {
                descriptionText.setText(currentAd.getDescription() != null ? currentAd.getDescription() : "Опис відсутній.");
            }

            if (adIdLabel != null) {
                adIdLabel.setText(currentAd.getAdId());
            }

            // Обробка категорії
            if (categoryLabel != null && MainGuiApp.categoryService != null) {
                try {
                    Optional<CategoryComponent> catOpt = MainGuiApp.categoryService.findCategoryById(currentAd.getCategoryId());
                    categoryLabel.setText(catOpt.map(CategoryComponent::getName).orElse("Невідома категорія"));
                } catch (Exception e) {
                    categoryLabel.setText("Невідома категорія");
                    System.err.println("Помилка при завантаженні категорії: " + e.getMessage());
                }
            }

            // Обробка продавця
            if (sellerLabel != null && MainGuiApp.userService != null) {
                try {
                    User seller = MainGuiApp.userService.getUserById(currentAd.getSellerId());
                    sellerLabel.setText(seller != null ? seller.getUsername() : "Невідомий продавець");
                } catch (Exception e) {
                    sellerLabel.setText("Невідомий продавець");
                    System.err.println("Помилка при завантаженні продавця: " + e.getMessage());
                }
            }
        } catch (Exception e) {
            System.err.println("Помилка при заповненні деталей оголошення: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void displayDecoratedInfo() {
        try {
            if (decoratedAd == null || decoratedInfoText == null) {
                return;
            }

            // Отримуємо повну декоровану інформацію
            String decoratedInfo = decoratedAd.getDisplayInfo();

            if (decoratedInfo != null && !decoratedInfo.trim().isEmpty()) {
                // Обробляємо текст для кращого відображення
                String processedInfo = processDecoratedText(decoratedInfo);
                decoratedInfoText.setText(processedInfo);

                // Додаємо контейнер до основного макету
                addDecoratedInfoToMainContainer();
            }
        } catch (Exception e) {
            System.err.println("Помилка при відображенні декорованої інформації: " + e.getMessage());
        }
    }
    /**
     * Універсальний метод ініціалізації контролера деталей оголошення
     * з підтримкою всіх декораторів через конфігурацію
     */
    public void initializeWithConfig(Ad ad, AdDisplayConfig config) {
        if (ad == null) {
            System.err.println("Warning: Ad is null in initializeWithConfig");
            return;
        }

        if (config == null) {
            System.err.println("Warning: Config is null, using default config");
            config = AdDisplayConfig.defaultConfig();
        }

        // Базова ініціалізація оголошення
        initializeBasicAdInfo(ad);

        // Застосовуємо декоратори на основі конфігурації
        applyDecorators(ad, config);

        // Налаштовуємо інтерактивні елементи
        setupInteractiveElements(ad, config);

        System.out.println("AdDetailController initialized for ad: " + ad.getTitle() +
                " with config: " + getConfigDescription(config));
    }

    /**
     * Ініціалізує базову інформацію про оголошення
     */
    private void initializeBasicAdInfo(Ad ad) {
        if (titleLabel != null) {
            titleLabel.setText(ad.getTitle());
        }

        if (descriptionTextArea != null) {
            descriptionTextArea.setText(ad.getDescription() != null ? ad.getDescription() : "Опис відсутній.");
        }

        if (priceLabel != null) {
            priceLabel.setText(String.format("%.2f грн", ad.getPrice()));
        }

        // Обробка автора (seller)
        if (authorLabel != null && MainGuiApp.userService != null) {
            try {
                User author = MainGuiApp.userService.getUserById(ad.getSellerId());
                authorLabel.setText("Автор: " + (author != null ? author.getUsername() : "Невідомий"));
            } catch (Exception e) {
                authorLabel.setText("Автор: Невідомий");
                System.err.println("Помилка при завантаженні автора: " + e.getMessage());
            }
        }

        // Обробка категорії
        if (categoryLabel != null && MainGuiApp.categoryService != null) {
            try {
                Optional<CategoryComponent> categoryOpt = MainGuiApp.categoryService.findCategoryById(ad.getCategoryId());
                categoryLabel.setText("Категорія: " + categoryOpt.map(CategoryComponent::getName).orElse("Невідома"));
            } catch (Exception e) {
                categoryLabel.setText("Категорія: Невідома");
                System.err.println("Помилка при завантаженні категорії: " + e.getMessage());
            }
        }

        if (statusLabel != null) {
            statusLabel.setText("Статус: " + (ad.getState() != null ? ad.getState().toString() : "Невідомий"));
        }

        if (createdDateLabel != null) {
            createdDateLabel.setText("Створено: " + (ad.getCreatedAt() != null ? ad.getCreatedAt().toString() : "Невідомо"));
        }

        // Завантажуємо зображення якщо є
        loadAdImages(ad);
    }

    /**
     * Застосовує декоратори на основі конфігурації
     */
    private void applyDecorators(Ad ad, AdDisplayConfig config) {
        // Контейнер для декораторів
        if (decoratorsContainer != null) {
            decoratorsContainer.getChildren().clear();
        }

        // Преміум декоратор
        if (config.isPremium()) {
            applyPremiumDecorator();
        }

        // Терміновий декоратор
        if (config.isUrgent()) {
            applyUrgentDecorator();
        }

        // Декоратор знижки
        if (config.getDiscountPercentage() != null && config.getDiscountPercentage() > 0) {
            applyDiscountDecorator(ad.getPrice(), config.getDiscountPercentage(), config.getDiscountReason());
        }

        // Декоратор гарантії
        if (config.getWarrantyMonths() != null && config.getWarrantyMonths() > 0) {
            applyWarrantyDecorator(config.getWarrantyMonths(), config.getWarrantyType());
        }

        // Декоратор доставки
        if (config.getFreeDelivery() != null || config.getDeliveryCost() != null) {
            applyDeliveryDecorator(config.getFreeDelivery(), config.getDeliveryCost(), config.getDeliveryInfo());
        }
    }

    /**
     * Застосовує преміум декоратор
     */
    private void applyPremiumDecorator() {
        Label premiumLabel = new Label("⭐ ПРЕМІУМ ОГОЛОШЕННЯ");
        premiumLabel.setStyle("-fx-background-color: #FFD700; -fx-text-fill: #000000; " +
                "-fx-font-weight: bold; -fx-padding: 5px 10px; " +
                "-fx-background-radius: 5px; -fx-font-size: 14px;");

        if (decoratorsContainer != null) {
            decoratorsContainer.getChildren().add(premiumLabel);
        }

        // Додаємо золотий бордер до основного контейнера
        if (mainContainer != null) {
            mainContainer.setStyle(mainContainer.getStyle() +
                    "-fx-border-color: #FFD700; -fx-border-width: 2px; -fx-border-radius: 5px;");
        }
    }

    /**
     * Застосовує терміновий декоратор
     */
    private void applyUrgentDecorator() {
        Label urgentLabel = new Label("🚨 ТЕРМІНОВО!");
        urgentLabel.setStyle("-fx-background-color: #FF4444; -fx-text-fill: white; " +
                "-fx-font-weight: bold; -fx-padding: 5px 10px; " +
                "-fx-background-radius: 5px; -fx-font-size: 14px;");

        if (decoratorsContainer != null) {
            decoratorsContainer.getChildren().add(urgentLabel);
        }

        // Додаємо анімацію миготіння (опціонально)
        addBlinkingEffect(urgentLabel);
    }

    /**
     * Застосовує декоратор знижки
     */
    private void applyDiscountDecorator(double originalPrice, double discountPercentage, String reason) {
        VBox discountBox = new VBox(5);
        discountBox.setStyle("-fx-background-color: #E8F5E8; -fx-border-color: #4CAF50; " +
                "-fx-border-width: 1px; -fx-border-radius: 5px; -fx-padding: 10px;");

        double discountAmount = originalPrice * (discountPercentage / 100);
        double newPrice = originalPrice - discountAmount;

        Label discountTitle = new Label("💰 ЗНИЖКА " + String.format("%.0f%%", discountPercentage));
        discountTitle.setStyle("-fx-font-weight: bold; -fx-text-fill: #2E7D32; -fx-font-size: 12px;");

        Label oldPriceLabel = new Label(String.format("Було: %.2f грн", originalPrice));
        oldPriceLabel.setStyle("-fx-text-fill: #666666; -fx-strikethrough: true; -fx-font-size: 11px;");

        Label newPriceLabel = new Label(String.format("Тепер: %.2f грн", newPrice));
        newPriceLabel.setStyle("-fx-text-fill: #2E7D32; -fx-font-weight: bold; -fx-font-size: 13px;");

        discountBox.getChildren().addAll(discountTitle, oldPriceLabel, newPriceLabel);

        if (reason != null && !reason.trim().isEmpty()) {
            Label reasonLabel = new Label("Причина: " + reason);
            reasonLabel.setStyle("-fx-text-fill: #555555; -fx-font-size: 10px; -fx-font-style: italic;");
            discountBox.getChildren().add(reasonLabel);
        }

        if (decoratorsContainer != null) {
            decoratorsContainer.getChildren().add(discountBox);
        }

        // Оновлюємо основну ціну
        if (priceLabel != null) {
            priceLabel.setText(String.format("%.2f грн", newPrice));
            priceLabel.setStyle(priceLabel.getStyle() + "-fx-text-fill: #2E7D32; -fx-font-weight: bold;");
        }
    }

    /**
     * Застосовує декоратор гарантії
     */
    private void applyWarrantyDecorator(int warrantyMonths, String warrantyType) {
        HBox warrantyBox = new HBox(10);
        warrantyBox.setStyle("-fx-background-color: #E3F2FD; -fx-border-color: #2196F3; " +
                "-fx-border-width: 1px; -fx-border-radius: 5px; -fx-padding: 8px;");

        Label warrantyIcon = new Label("🛡️");
        warrantyIcon.setStyle("-fx-font-size: 16px;");

        VBox warrantyInfo = new VBox(2);

        Label warrantyTitle = new Label("ГАРАНТІЯ " + warrantyMonths + " міс.");
        warrantyTitle.setStyle("-fx-font-weight: bold; -fx-text-fill: #1976D2; -fx-font-size: 12px;");

        warrantyInfo.getChildren().add(warrantyTitle);

        if (warrantyType != null && !warrantyType.trim().isEmpty()) {
            Label warrantyTypeLabel = new Label(warrantyType);
            warrantyTypeLabel.setStyle("-fx-text-fill: #555555; -fx-font-size: 10px;");
            warrantyInfo.getChildren().add(warrantyTypeLabel);
        }

        warrantyBox.getChildren().addAll(warrantyIcon, warrantyInfo);

        if (decoratorsContainer != null) {
            decoratorsContainer.getChildren().add(warrantyBox);
        }
    }

    /**
     * Застосовує декоратор доставки
     */
    private void applyDeliveryDecorator(Boolean freeDelivery, Double deliveryCost, String deliveryInfo) {
        HBox deliveryBox = new HBox(10);
        deliveryBox.setStyle("-fx-background-color: #FFF3E0; -fx-border-color: #FF9800; " +
                "-fx-border-width: 1px; -fx-border-radius: 5px; -fx-padding: 8px;");

        Label deliveryIcon = new Label("🚚");
        deliveryIcon.setStyle("-fx-font-size: 16px;");

        VBox deliveryInfo_vbox = new VBox(2);

        if (freeDelivery != null && freeDelivery) {
            Label freeDeliveryLabel = new Label("БЕЗКОШТОВНА ДОСТАВКА");
            freeDeliveryLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #E65100; -fx-font-size: 12px;");
            deliveryInfo_vbox.getChildren().add(freeDeliveryLabel);
        } else if (deliveryCost != null) {
            Label deliveryCostLabel = new Label(String.format("Доставка: %.2f грн", deliveryCost));
            deliveryCostLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #E65100; -fx-font-size: 12px;");
            deliveryInfo_vbox.getChildren().add(deliveryCostLabel);
        }

        if (deliveryInfo != null && !deliveryInfo.trim().isEmpty()) {
            Label infoLabel = new Label(deliveryInfo);
            infoLabel.setStyle("-fx-text-fill: #555555; -fx-font-size: 10px;");
            deliveryInfo_vbox.getChildren().add(infoLabel);
        }

        deliveryBox.getChildren().addAll(deliveryIcon, deliveryInfo_vbox);

        if (decoratorsContainer != null) {
            decoratorsContainer.getChildren().add(deliveryBox);
        }
    }

    /**
     * Налаштовує інтерактивні елементи
     */
    private void setupInteractiveElements(Ad ad, AdDisplayConfig config) {
        // Налаштовуємо кнопки в залежності від конфігурації
        if (contactButton != null) {
            contactButton.setOnAction(e -> handleContactSeller(ad));
        }

        if (favoriteButton != null) {
            favoriteButton.setOnAction(e -> handleAddToFavorites(ad));
        }

        if (shareButton != null) {
            shareButton.setOnAction(e -> handleShareAd(ad));
        }

        // Якщо це преміум оголошення, додаємо спеціальні функції
        if (config.isPremium() && premiumFeaturesContainer != null) {
            setupPremiumFeatures(ad);
        }
    }

    /**
     * Завантажує зображення оголошення
     */
    private void loadAdImages(Ad ad) {
        if (imageContainer != null && ad.getImagePaths() != null && !ad.getImagePaths().isEmpty()) {
            imageContainer.getChildren().clear();

            for (String imagePath : ad.getImagePaths()) {
                try {
                    ImageView imageView = new ImageView(new Image("file:" + imagePath));
                    imageView.setFitHeight(200);
                    imageView.setFitWidth(200);
                    imageView.setPreserveRatio(true);
                    imageView.setSmooth(true);
                    imageContainer.getChildren().add(imageView);
                } catch (Exception e) {
                    System.err.println("Error loading image: " + imagePath + " - " + e.getMessage());
                }
            }
        }
    }

    /**
     * Додає ефект миготіння до елемента
     */
    private void addBlinkingEffect(Label label) {
        Timeline timeline = new Timeline(
                new KeyFrame(Duration.seconds(0.5), e -> label.setVisible(false)),
                new KeyFrame(Duration.seconds(1.0), e -> label.setVisible(true))
        );
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }

    /**
     * Створює опис конфігурації для логування
     */
    private String getConfigDescription(AdDisplayConfig config) {
        List<String> features = new ArrayList<>();

        if (config.isPremium()) features.add("Premium");
        if (config.isUrgent()) features.add("Urgent");
        if (config.getDiscountPercentage() != null) features.add("Discount");
        if (config.getWarrantyMonths() != null) features.add("Warranty");
        if (config.getFreeDelivery() != null) features.add("Delivery");

        return features.isEmpty() ? "Basic" : String.join(", ", features);
    }

    // Обробники подій (приклади)
    private void handleContactSeller(Ad ad) {
        // Логіка контакту з продавцем
        System.out.println("Contacting seller for ad: " + ad.getTitle());
    }

    private void handleAddToFavorites(Ad ad) {
        // Логіка додавання в обрані
        System.out.println("Adding to favorites: " + ad.getTitle());
    }

    private void handleShareAd(Ad ad) {
        // Логіка поширення оголошення
        System.out.println("Sharing ad: " + ad.getTitle());
    }

    private void setupPremiumFeatures(Ad ad) {
        // Налаштування преміум функцій
        System.out.println("Setting up premium features for: " + ad.getTitle());
    }
    private String processDecoratedText(String originalText) {
        if (originalText == null) {
            return "";
        }

        // Замінюємо деякі символи для кращого відображення
        return originalText
                .replace("💰", "💰 ")
                .replace("⭐", "⭐ ")
                .replace("🚨", "🚨 ")
                .replace("🛡️", "🛡️ ")
                .replace("🚚", "🚚 ")
                .replace("✨", "✨ ")
                .replace("🚀", "🚀 ")
                .replace("⚡", "⚡ ")
                .replace("❌", "❌ ")
                .replace("✅", "✅ ")
                .replace("💸", "💸 ")
                .replace("🎯", "🎯 ")
                .replace("📦", "📦 ")
                .replace("📋", "📋 ");
    }

    private void addDecoratedInfoToMainContainer() {
        if (mainContainer == null || decoratedInfoContainer == null) {
            return;
        }

        try {
            // Перевіряємо, чи вже не додали контейнер
            if (mainContainer.getChildren().contains(decoratedInfoContainer)) {
                return;
            }

            // Знаходимо позицію після блоку з описом
            int insertIndex = findInsertPosition();

            // Додаємо декоровану інформацію
            if (insertIndex >= 0 && insertIndex < mainContainer.getChildren().size()) {
                mainContainer.getChildren().add(insertIndex, decoratedInfoContainer);
            } else if (insertIndex == mainContainer.getChildren().size()) {
                mainContainer.getChildren().add(decoratedInfoContainer);
            } else {
                // В крайньому випадку додаємо перед кнопками або в кінець
                if (actionButtonsBox != null) {
                    int buttonBoxIndex = mainContainer.getChildren().indexOf(actionButtonsBox);
                    if (buttonBoxIndex > 0) {
                        mainContainer.getChildren().add(buttonBoxIndex, decoratedInfoContainer);
                    } else {
                        mainContainer.getChildren().add(decoratedInfoContainer);
                    }
                } else {
                    mainContainer.getChildren().add(decoratedInfoContainer);
                }
            }
        } catch (Exception e) {
            System.err.println("Помилка при додаванні декорованої інформації до контейнера: " + e.getMessage());
        }
    }

    private int findInsertPosition() {
        // Шукаємо позицію після опису
        for (int i = 0; i < mainContainer.getChildren().size(); i++) {
            var child = mainContainer.getChildren().get(i);
            if (child instanceof VBox) {
                VBox vbox = (VBox) child;
                if (containsDescriptionText(vbox)) {
                    return i + 1;
                }
            }
        }

        // Шукаємо через мета-інформацію
        for (int i = 0; i < mainContainer.getChildren().size(); i++) {
            var child = mainContainer.getChildren().get(i);
            if (child instanceof VBox) {
                VBox vbox = (VBox) child;
                if (vbox.getStyleClass().contains("detail-section-meta")) {
                    return i + 1;
                }
            }
        }

        return -1;
    }

    private boolean containsDescriptionText(VBox container) {
        if (container == null || descriptionText == null) {
            return false;
        }

        for (var child : container.getChildren()) {
            if (child == descriptionText) {
                return true;
            }
            if (child instanceof VBox || child instanceof HBox) {
                if (containsTextInContainer(child, descriptionText)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean containsTextInContainer(Object container, Text target) {
        if (container == null || target == null) {
            return false;
        }

        try {
            if (container instanceof VBox) {
                VBox vbox = (VBox) container;
                for (var child : vbox.getChildren()) {
                    if (child == target) {
                        return true;
                    }
                    if (child instanceof VBox || child instanceof HBox) {
                        if (containsTextInContainer(child, target)) {
                            return true;
                        }
                    }
                }
            } else if (container instanceof HBox) {
                HBox hbox = (HBox) container;
                for (var child : hbox.getChildren()) {
                    if (child == target) {
                        return true;
                    }
                    if (child instanceof VBox || child instanceof HBox) {
                        if (containsTextInContainer(child, target)) {
                            return true;
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Помилка при пошуку тексту в контейнері: " + e.getMessage());
        }
        return false;
    }

    // Методи для роботи з зображеннями
    private void loadImages() {
        if (currentAd == null) {
            return;
        }

        try {
            imagePaths = currentAd.getImagePaths();

            if (imagePaths == null || imagePaths.isEmpty()) {
                showNoImageMessage();
                return;
            }

            loadMainImage(0);
            loadThumbnails();

            if (imagePaths.size() > 1) {
                addGalleryToMainContainer();
            }
        } catch (Exception e) {
            System.err.println("Помилка при завантаженні зображень: " + e.getMessage());
            showNoImageMessage();
        }
    }

    private void showNoImageMessage() {
        if (adImageView != null) {
            adImageView.setVisible(false);
        }
        if (noImageLabel != null) {
            noImageLabel.setText("Фото не завантажено");
            noImageLabel.setVisible(true);
        }
        if (thumbnailContainer != null) {
            thumbnailContainer.getChildren().clear();
        }
    }

    private void loadMainImage(int index) {
        if (imagePaths == null || index < 0 || index >= imagePaths.size() || currentMainImage == null) {
            return;
        }

        try {
            String imagePath = imagePaths.get(index);
            Image image = loadImageFromPath(imagePath);

            if (image != null) {
                currentMainImage.setImage(image);
                currentMainImage.setVisible(true);
                if (noImageLabel != null) {
                    noImageLabel.setVisible(false);
                }
                currentImageIndex = index;
            } else {
                if (index + 1 < imagePaths.size()) {
                    loadMainImage(index + 1);
                } else {
                    showNoImageMessage();
                }
            }
        } catch (Exception e) {
            System.err.println("Помилка при завантаженні основного зображення: " + e.getMessage());
            if (index + 1 < imagePaths.size()) {
                loadMainImage(index + 1);
            } else {
                showNoImageMessage();
            }
        }
    }

    private void loadThumbnails() {

        if (thumbnailContainer == null || imagePaths == null) {
            return;
        }

        try {
            thumbnailContainer.getChildren().clear();

            for (int i = 0; i < imagePaths.size(); i++) {
                String imagePath = imagePaths.get(i);
                if (imagePath == null || imagePath.trim().isEmpty()) {
                    continue; // пропускаємо порожні шляхи
                }
                Image thumbnailImage = loadImageFromPath(imagePath);

                if (thumbnailImage != null) {
                    ImageView thumbnail = createThumbnail(thumbnailImage, i);
                    thumbnailContainer.getChildren().add(thumbnail);
                }
            }
        } catch (Exception e) {
            System.err.println("Помилка при завантаженні мініатюр: " + e.getMessage());
        }
    }

    private ImageView createThumbnail(Image image, int index) {
        ImageView thumbnail = new ImageView(image);
        thumbnail.setFitHeight(80);
        thumbnail.setFitWidth(80);
        thumbnail.setPreserveRatio(true);
        thumbnail.setSmooth(true);

        updateThumbnailStyle(thumbnail, index == currentImageIndex);

        thumbnail.setOnMouseClicked(event -> {
            loadMainImage(index);
            updateAllThumbnailStyles();
        });

        thumbnail.setOnMouseEntered(event -> {
            if (index != currentImageIndex) {
                thumbnail.setStyle("-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.4), 5, 0.5, 0, 0); -fx-cursor: hand;");
            }
        });

        thumbnail.setOnMouseExited(event -> {
            updateThumbnailStyle(thumbnail, index == currentImageIndex);
        });

        return thumbnail;
    }

    private void updateThumbnailStyle(ImageView thumbnail, boolean isSelected) {
        if (thumbnail == null) return;

        if (isSelected) {
            thumbnail.setStyle("-fx-effect: dropshadow(gaussian, rgba(0,100,255,0.8), 8, 0.6, 0, 0); -fx-cursor: default;");
        } else {
            thumbnail.setStyle("-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 3, 0.3, 0, 0); -fx-cursor: hand;");
        }
    }

    private void updateAllThumbnailStyles() {
        if (thumbnailContainer == null) return;

        try {
            for (int i = 0; i < thumbnailContainer.getChildren().size(); i++) {
                var child = thumbnailContainer.getChildren().get(i);
                if (child instanceof ImageView thumbnail) {
                    updateThumbnailStyle(thumbnail, i == currentImageIndex);
                }
            }
        } catch (Exception e) {
            System.err.println("Помилка при оновленні стилів мініатюр: " + e.getMessage());
        }
    }

    private Image loadImageFromPath(String imagePath) {
        if (imagePath == null || imagePath.trim().isEmpty()) {
            return null;
        }

        try {
            File imageFile = new File(imagePath);

            if (!imageFile.exists()) {
                imageFile = new File("user_images", new File(imagePath).getName());
            }

            if (imageFile.exists()) {
                return new Image(imageFile.toURI().toString());
            } else {
                System.err.println("Image file not found: " + imagePath);
                return null;
            }
        } catch (Exception e) {
            System.err.println("Error loading image: " + imagePath + " - " + e.getMessage());
            return null;
        }
    }

    private void addGalleryToMainContainer() {
        try {
            if (adImageView != null && adImageView.getParent() instanceof VBox) {
                VBox parentContainer = (VBox) adImageView.getParent();
                int imageViewIndex = parentContainer.getChildren().indexOf(adImageView);

                if (imageViewIndex >= 0 && imageViewIndex + 1 <= parentContainer.getChildren().size()
                        && imageGalleryContainer != null && !imageGalleryContainer.getChildren().isEmpty()) {
                    parentContainer.getChildren().add(imageViewIndex + 1, imageGalleryContainer.getChildren().get(0));
                }
            }
        } catch (Exception e) {
            System.err.println("Помилка при додаванні галереї до контейнера: " + e.getMessage());
        }
    }

    private void setupActionButtons() {
        try {
            User loggedInUser = GlobalContext.getInstance().getLoggedInUser();
            if (loggedInUser != null && currentAd != null && editButton != null && deleteButton != null) {
                boolean isOwner = loggedInUser.getUserId().equals(currentAd.getSellerId());
                boolean isAdmin = loggedInUser.getUserType() == UserType.ADMIN;

                if (isOwner) {
                    editButton.setVisible(true);
                    editButton.setManaged(true);
                }
                if (isOwner || isAdmin) {
                    deleteButton.setVisible(true);
                    deleteButton.setManaged(true);
                }
            }
        } catch (Exception e) {
            System.err.println("Помилка при налаштуванні кнопок дій: " + e.getMessage());
        }
    }

    @FXML
    private void handleEditAd() {
        if (currentAd == null) return;
        try {
            MainGuiApp.loadEditAdScene(currentAd);
        } catch (IOException e) {
            e.printStackTrace();
            showErrorAlert("Помилка редагування", "Не вдалося завантажити форму редагування: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            showErrorAlert("Помилка редагування", "Сталася непередбачена помилка: " + e.getMessage());
        }
    }




    @FXML
    private void handleDeleteAd() {
        if (currentAd == null) return;
        User loggedInUser = GlobalContext.getInstance().getLoggedInUser();
        if (loggedInUser == null) {
            showErrorAlert("Помилка", "Ви не авторизовані для цієї дії.");
            return;
        }

        Alert confirmationDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmationDialog.setTitle("Підтвердження видалення");
        confirmationDialog.setHeaderText("Ви дійсно бажаєте видалити оголошення '" + currentAd.getTitle() + "'?");
        confirmationDialog.setContentText("Цю дію неможливо буде скасувати.");

        Optional<ButtonType> result = confirmationDialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                MainGuiApp.adService.deleteAd(currentAd.getAdId(), loggedInUser.getUserId());
                showInfoAlert("Успіх", "Оголошення '" + currentAd.getTitle() + "' було успішно видалено.");
                handleBack();
            } catch (AdNotFoundException e) {
                showErrorAlert("Помилка видалення", "Оголошення не знайдено. Можливо, його вже видалили.");
            } catch (UnauthorizedActionException e) {
                showErrorAlert("Помилка доступу", "Ви не маєте прав для видалення цього оголошення.");
            } catch (Exception e) {
                showErrorAlert("Помилка видалення", "Сталася непередбачена помилка: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void handleBack() {
        try {
            MainGuiApp.loadMainScene();
        } catch (IOException e) {
            e.printStackTrace();
            showErrorAlert("Помилка навігації", "Не вдалося повернутися до головного списку.");
        }
    }

    // Utility методи
    private void showErrorAndGoBack(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Помилка");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
        handleBack();
    }

    private void showErrorAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showInfoAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Додатковий метод для оновлення декорованої інформації
    public void refreshDecoratedInfo() {
        if (decoratedAd != null && decoratedInfoText != null) {
            String decoratedInfo = decoratedAd.getDisplayInfo();
            String processedInfo = processDecoratedText(decoratedInfo);
            decoratedInfoText.setText(processedInfo);
        }
    }
}