// src/main/java/com/example/olx/domain/model/Category.java
package com.example.olx.domain.model;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Logger;
import com.example.olx.presentation.gui.context.GlobalContext;
public class Category extends CategoryComponent {
    private static final long serialVersionUID = 6L;
    private List<CategoryComponent> subCategories = new ArrayList<>();
    private CategoryComponent[] children;
    private JsonNode localizedNames;

    public Category(String root, String всіКатегорії, String name) { super(name); }
    @Override
    public void add(CategoryComponent categoryComponent) { if (categoryComponent != null && !subCategories.contains(categoryComponent)) { subCategories.add(categoryComponent); } }
    @Override
    public void remove(CategoryComponent categoryComponent) { subCategories.remove(categoryComponent); }
    @Override
    public CategoryComponent getChild(int i) { return subCategories.get(i); }
    public List<CategoryComponent> getSubCategories() { return new ArrayList<>(subCategories); }
    @Override
    public void displayCategory(String indent) {
        System.out.println(indent + "Категорія: " + getName() + " (ID: " + getId() + ")");
        subCategories.forEach(c -> c.displayCategory(indent + "  "));
    }

    public void addSubCategory(Category category) {
        // Перевірка на null
        if (category == null) {
            throw new IllegalArgumentException("Category cannot be null");
        }

        // Ініціалізація списку підкategorій, якщо він ще не створений
        if (this.subCategories == null) {
            this.subCategories = new ArrayList<>();
        }

        // Додавання підкategorії до списку
        this.subCategories.add(category);

        // Встановлення батьківської категорії для доданої підкategorії

    }

    public CategoryComponent[] getChildren() {
        return children;
    }

    public void setChildren(CategoryComponent[] children) {
        this.children = children;
    }

    /**
     * Повертає назву категорії з урахуванням локалізації.
     * Якщо назва відсутня, повертає "Без назви" або ID категорії.
     *
     * @return Назва категорії або резервний варіант, якщо назва відсутня
     */
    public String getName() {
        // 1. Перевірка базової назви (не локалізованої)
        if (this.name != null && !this.name.trim().isEmpty()) {
            return this.name.trim();
        }

        // 2. Обробка локалізованих назв (якщо вони є)
        if (this.localizedNames != null && !this.localizedNames.isEmpty()) {
            try {
                // Отримуємо поточну локаль користувача
                Locale userLocale = GlobalContext.getCurrentLocale();

                // Спроба отримати назву для поточної локалі
                String localizedName = localizedNames.get(userLocale);
                if (localizedName != null && !localizedName.trim().isEmpty()) {
                    return localizedName.trim();
                }

                // Якщо немає для поточної локалі - шукаємо будь-яку доступну
                for (Map.Entry<Locale, String> entry : localizedNames.entrySet()) {
                    if (entry.getValue() != null && !entry.getValue().trim().isEmpty()) {
                        return entry.getValue().trim();
                    }
                }
            } catch (Exception e) {
                System.err.println("Помилка при отриманні локалізованої назви: " + e.getMessage());
                // Продовжуємо далі, спробуємо інші варіанти
            }
        }

        // 3. Резервні варіанти, якщо назва не знайдена
        return this.id != null ? "Категорія #" + this.id : "Без назви";
    }
}