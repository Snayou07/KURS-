// src/main/java/com/example/olx/domain/model/CategoryComponent.java
package com.example.olx.domain.model;
import java.io.Serializable;
import java.util.UUID;
public abstract class CategoryComponent implements Serializable {
    private static final long serialVersionUID = 5L;
    protected String id;
    protected static String name;
    public CategoryComponent(String name) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
    }
    public String getId() { return id; }
    public String getName() {
        // return the name without requiring parameters
        return name; // or whatever field stores the name
    }
    public void setName(String name) { this.name = name; }
    public void add(CategoryComponent categoryComponent) { throw new UnsupportedOperationException(); }
    public void remove(CategoryComponent categoryComponent) { throw new UnsupportedOperationException(); }
    public CategoryComponent getChild(int i) { throw new UnsupportedOperationException(); }
    public abstract void displayCategory(String indent);
    @Override
    public boolean equals(Object o) { /* ... реалізація ... */ return super.equals(o); }
    @Override
    public int hashCode() { /* ... реалізація ... */ return super.hashCode(); }
    @Override
    public String toString() { return name; } // Спрощено для TreeView/ComboBox
}