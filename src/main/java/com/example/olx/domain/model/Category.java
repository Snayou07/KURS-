// src/main/java/com/example/olx/domain/model/Category.java
package com.example.olx.domain.model;
import java.util.ArrayList;
import java.util.List;
public class Category extends CategoryComponent {
    private static final long serialVersionUID = 6L;
    private List<CategoryComponent> subCategories = new ArrayList<>();
    public Category(String name) { super(name); }
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
}