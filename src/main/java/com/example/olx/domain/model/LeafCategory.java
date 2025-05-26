// src/main/java/com/example/olx/domain/model/LeafCategory.java
package com.example.olx.domain.model;
public class LeafCategory extends CategoryComponent {
    private static final long serialVersionUID = 7L;
    public LeafCategory(String name) { super(name); }
    @Override
    public void displayCategory(String indent) { System.out.println(indent + "Кінцева категорія: " + getName() + " (ID: " + getId() + ")"); }
}