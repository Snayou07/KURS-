// src/main/java/com/example/olx/domain/model/CategoryComponent.java
package com.example.olx.domain.model;
import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

public abstract class CategoryComponent implements Serializable {
    private static final long serialVersionUID = 5L;
    protected String id;
    protected String name; // Removed static modifier

    public CategoryComponent(String name) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void add(CategoryComponent categoryComponent) {
        throw new UnsupportedOperationException("Add operation not supported");
    }

    public void remove(CategoryComponent categoryComponent) {
        throw new UnsupportedOperationException("Remove operation not supported");
    }

    public CategoryComponent getChild(int i) {
        throw new UnsupportedOperationException("GetChild operation not supported");
    }

    public abstract void displayCategory(String indent);

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CategoryComponent that = (CategoryComponent) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return name != null ? name : "Unnamed Category";
    }
}