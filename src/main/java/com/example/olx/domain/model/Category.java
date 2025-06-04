// src/main/java/com/example/olx/domain/model/Category.java
package com.example.olx.domain.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.HashMap;
import java.util.Objects;
import java.util.logging.Logger;
import java.util.logging.Level;

public class Category extends CategoryComponent {
    private static final long serialVersionUID = 6L;
    private static final Logger logger = Logger.getLogger(Category.class.getName());

    private List<CategoryComponent> subCategories = new ArrayList<>();
    private Map<Locale, String> localizedNames = new HashMap<>();
    private String parentId;

    // Исправленный конструктор
    public Category(String id, String name) {
        super(name);
        this.id = id;
    }

    // Конструктор с родительской категорией
    public Category(String id, String name, String parentId) {
        super(name);
        this.id = id;
        this.parentId = parentId;
    }

    // Конструктор по умолчанию
    public Category() {
        super("");
    }

    @Override
    public void add(CategoryComponent categoryComponent) {
        if (categoryComponent != null && !subCategories.contains(categoryComponent)) {
            subCategories.add(categoryComponent);

            // Если добавляется категория, устанавливаем для нее родительский ID
            if (categoryComponent instanceof Category) {
                ((Category) categoryComponent).setParentId(this.getId());
            }
        }
    }

    @Override
    public void remove(CategoryComponent categoryComponent) {
        if (subCategories.remove(categoryComponent)) {
            // Если удаляется категория, очищаем родительский ID
            if (categoryComponent instanceof Category) {
                ((Category) categoryComponent).setParentId(null);
            }
        }
    }

    @Override
    public CategoryComponent getChild(int index) {
        if (index >= 0 && index < subCategories.size()) {
            return subCategories.get(index);
        }
        return null;
    }

    public List<CategoryComponent> getSubCategories() {
        return new ArrayList<>(subCategories);
    }

    // Метод для совместимости с существующим кодом
    public CategoryComponent[] getChildren() {
        if (subCategories == null || subCategories.isEmpty()) {
            return new CategoryComponent[0];
        }
        return subCategories.toArray(new CategoryComponent[0]);
    }

    public void setChildren(CategoryComponent[] children) {
        this.subCategories.clear();
        if (children != null) {
            for (CategoryComponent child : children) {
                this.add(child);
            }
        }
    }

    @Override
    public void displayCategory(String indent) {
        System.out.println(indent + "Категорія: " + getName() + " (ID: " + getId() + ")");
        subCategories.forEach(c -> c.displayCategory(indent + "  "));
    }

    public void addSubCategory(CategoryComponent subCategory) {
        if (subCategory == null) {
            throw new IllegalArgumentException("Підкатегорія не може бути null");
        }

        if (this.subCategories == null) {
            this.subCategories = new ArrayList<>();
        }

        // Используем метод add для правильной настройки связей
        this.add(subCategory);
    }

    /**
     * Возвращает название категории с учетом локализации.
     * Если название отсутствует, возвращает "Без назви" или ID категории.
     */
    @Override
    public String getName() {
        // 1. Проверка базового названия (не локализованного)
        if (this.name != null && !this.name.trim().isEmpty()) {
            return this.name.trim();
        }

        // 2. Обработка локализованных названий (если они есть)
        if (this.localizedNames != null && !this.localizedNames.isEmpty()) {
            try {
                // Получаем текущую локаль пользователя
                Locale userLocale = getCurrentLocale();

                // Попытка получить название для текущей локали
                String localizedName = localizedNames.get(userLocale);
                if (localizedName != null && !localizedName.trim().isEmpty()) {
                    return localizedName.trim();
                }

                // Если нет для текущей локали - ищем любую доступную
                for (Map.Entry<Locale, String> entry : localizedNames.entrySet()) {
                    if (entry.getValue() != null && !entry.getValue().trim().isEmpty()) {
                        return entry.getValue().trim();
                    }
                }
            } catch (Exception e) {
                logger.log(Level.WARNING, "Помилка при отриманні локалізованої назви для категорії " + this.id, e);
            }
        }

        // 3. Резервные варианты, если название не найдено
        return this.id != null ? "Категорія #" + this.id : "Без назви";
    }

    /**
     * Получает текущую локаль. В случае недоступности GlobalContext возвращает локаль по умолчанию.
     */
    private Locale getCurrentLocale() {
        try {
            // Попытка получить локаль из GlobalContext
            // В реальном приложении здесь может быть вызов GlobalContext.getCurrentLocale()
            return Locale.getDefault();
        } catch (Exception e) {
            logger.log(Level.WARNING, "Не вдалося отримати поточну локаль, використовується локаль за замовчуванням", e);
            return Locale.getDefault();
        }
    }

    // Метод для добавления локализованных названий
    public void addLocalizedName(Locale locale, String name) {
        if (locale != null && name != null && !name.trim().isEmpty()) {
            this.localizedNames.put(locale, name.trim());
        }
    }

    // Метод для получения всех локализованных названий
    public Map<Locale, String> getLocalizedNames() {
        return new HashMap<>(localizedNames);
    }

    // Метод для получения локализованного названия для конкретной локали
    public String getLocalizedName(Locale locale) {
        if (locale != null && localizedNames.containsKey(locale)) {
            return localizedNames.get(locale);
        }
        return getName(); // Возвращаем стандартное название
    }

    // Геттеры и сеттеры для parentId
    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    // Методы для работы с иерархией
    public boolean hasSubCategories() {
        return subCategories != null && !subCategories.isEmpty();
    }

    public int getSubCategoriesCount() {
        return subCategories != null ? subCategories.size() : 0;
    }

    public boolean isRootCategory() {
        return parentId == null || parentId.trim().isEmpty();
    }

    // Поиск подкатегории по ID
    public CategoryComponent findSubCategoryById(String id) {
        if (id == null) return null;

        for (CategoryComponent subCategory : subCategories) {
            if (id.equals(subCategory.getId())) {
                return subCategory;
            }

            // Рекурсивный поиск в подкатегориях
            if (subCategory instanceof Category) {
                CategoryComponent found = ((Category) subCategory).findSubCategoryById(id);
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }

    // Получение пути к категории (для breadcrumbs)
    public List<String> getCategoryPath() {
        List<String> path = new ArrayList<>();
        path.add(getName());
        return path;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Category category = (Category) o;
        return Objects.equals(getId(), category.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }

    @Override
    public String toString() {
        return "Category{" +
                "id='" + getId() + '\'' +
                ", name='" + getName() + '\'' +
                ", parentId='" + parentId + '\'' +
                ", subCategoriesCount=" + getSubCategoriesCount() +
                ", localizedNamesCount=" + localizedNames.size() +
                '}';
    }
}