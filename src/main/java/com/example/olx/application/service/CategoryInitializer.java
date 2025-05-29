/*
package com.example.olx.application.service;

import com.example.olx.domain.model.Category;
import com.example.olx.domain.model.CategoryComponent;
import com.example.olx.application.service.CategoryService;

import java.util.ArrayList;
import java.util.List;

public class CategoryInitializer {

    public static void initializeDefaultCategories(CategoryService categoryService) {
        List<CategoryComponent> rootCategories = createDefaultCategories();
        categoryService.initializeCategories(rootCategories);
        System.out.println("Ініціалізовано " + rootCategories.size() + " основних категорій");
    }

    private static List<CategoryComponent> createDefaultCategories() {
        List<CategoryComponent> categories = new ArrayList<>();

        // Створюємо основні категорії (10 штук)
        categories.add(createElectronicsCategory());
        categories.add(createVehiclesCategory());
        categories.add(createRealEstateCategory());
        categories.add(createClothingCategory());
        categories.add(createHomeGardenCategory());
        categories.add(createSportsCategory());
        categories.add(createBooksCategory());
        categories.add(createPetsCategory());
        categories.add(createServicesCategory());
        categories.add(createOtherCategory());

        return categories;
    }

    private static Category createElectronicsCategory() {
        Category electronics = new Category("electronics", "📱 Електроніка");
        electronics.addSubCategory(new Category("phones", "Мобільні телефони"));
        electronics.addSubCategory(new Category("computers", "Комп'ютери та ноутбуки"));
        electronics.addSubCategory(new Category("tv", "Телевізори"));
        electronics.addSubCategory(new Category("audio", "Аудіо техніка"));
        return electronics;
    }

    private static Category createVehiclesCategory() {
        Category vehicles = new Category("vehicles", "🚗 Транспорт");
        vehicles.addSubCategory(new Category("cars", "Легкові автомобілі"));
        vehicles.addSubCategory(new Category("motorcycles", "Мотоцикли"));
        vehicles.addSubCategory(new Category("parts", "Запчастини"));
        vehicles.addSubCategory(new Category("bicycles", "Велосипеди"));
        return vehicles;
    }

    private static Category createRealEstateCategory() {
        Category realEstate = new Category("real_estate", "🏠 Нерухомість");
        realEstate.addSubCategory(new Category("apartments", "Квартири"));
        realEstate.addSubCategory(new Category("houses", "Будинки"));
        realEstate.addSubCategory(new Category("commercial", "Комерційна нерухомість"));
        return realEstate;
    }

    private static Category createClothingCategory() {
        Category clothing = new Category("clothing", "👕 Одяг та взуття");
        clothing.addSubCategory(new Category("men_clothing", "Чоловічий одяг"));
        clothing.addSubCategory(new Category("women_clothing", "Жіночий одяг"));
        clothing.addSubCategory(new Category("shoes", "Взуття"));
        clothing.addSubCategory(new Category("accessories", "Аксесуари"));
        return clothing;
    }

    private static Category createHomeGardenCategory() {
        Category homeGarden = new Category("home_garden", "🏡 Дім і сад");
        homeGarden.addSubCategory(new Category("furniture", "Меблі"));
        homeGarden.addSubCategory(new Category("appliances", "Побутова техніка"));
        homeGarden.addSubCategory(new Category("garden", "Сад та город"));
        return homeGarden;
    }

    private static Category createSportsCategory() {
        Category sports = new Category("sports", "⚽ Спорт і відпочинок");
        sports.addSubCategory(new Category("fitness", "Фітнес"));
        sports.addSubCategory(new Category("outdoor", "Активний відпочинок"));
        sports.addSubCategory(new Category("sports_equipment", "Спортивне обладнання"));
        return sports;
    }

    private static Category createBooksCategory() {
        Category books = new Category("books", "📚 Книги та навчання");
        books.addSubCategory(new Category("textbooks", "Підручники"));
        books.addSubCategory(new Category("fiction", "Художня література"));
        books.addSubCategory(new Category("courses", "Курси та навчання"));
        return books;
    }

    private static Category createPetsCategory() {
        Category pets = new Category("pets", "🐕 Тварини");
        pets.addSubCategory(new Category("dogs", "Собаки"));
        pets.addSubCategory(new Category("cats", "Коти"));
        pets.addSubCategory(new Category("pet_supplies", "Товари для тварин"));
        return pets;
    }

    private static Category createServicesCategory() {
        Category services = new Category("services", "🔧 Послуги");
        services.addSubCategory(new Category("repair", "Ремонт"));
        services.addSubCategory(new Category("cleaning", "Прибирання"));
        services.addSubCategory(new Category("tutoring", "Репетиторство"));
        return services;
    }

    private static Category createOtherCategory() {
        return new Category("other", "🔍 Інше");
    }
}

 */