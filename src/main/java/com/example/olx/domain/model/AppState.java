// src/main/java/com/example/olx/domain/model/AppState.java
package com.example.olx.domain.model;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
public class AppState implements Serializable {
    private static final long serialVersionUID = 101L;
    private List<User> users = new ArrayList<>();
    private List<Ad> ads = new ArrayList<>();
    private List<CategoryComponent> categories = new ArrayList<>();
    public List<User> getUsers() { return users; }
    public List<Ad> getAds() { return ads; }
    public List<CategoryComponent> getCategories() { return categories; }
    public void setUsers(List<User> users) { this.users = users; }
    public void setAds(List<Ad> ads) { this.ads = ads; }
    public void setCategories(List<CategoryComponent> categories) { this.categories = categories; }
    public void clear() { users.clear(); ads.clear(); categories.clear(); }
}