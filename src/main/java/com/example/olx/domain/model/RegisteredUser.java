// src/main/java/com/example/olx/domain/model/RegisteredUser.java
package com.example.olx.domain.model;
import java.util.ArrayList;
import java.util.List;
public class RegisteredUser extends User {
    private static final long serialVersionUID = 2L;
    private List<String> postedAdIds = new ArrayList<>();
    private List<String> favoriteAdIds = new ArrayList<>();
    public RegisteredUser(String username, String passwordHash, String email) {
        super(username, passwordHash, email, UserType.REGULAR_USER);
    }
    public List<String> getPostedAdIds() { return postedAdIds; }
    public void addPostedAdId(String adId) { if (adId != null && !adId.isEmpty()) { this.postedAdIds.add(adId); } }
    public List<String> getFavoriteAdIds() { return favoriteAdIds; }
    public void addFavoriteAdId(String adId) { if (adId != null && !adId.isEmpty()) { this.favoriteAdIds.add(adId); } }
    @Override
    public void viewDashboard() { System.out.println("Viewing Dashboard for REGULAR USER: " + getUsername()); }
    @Override
    public String toString() { return "RegisteredUser[" + super.toString() + ", postedAds=" + postedAdIds.size() + ", favorites=" + favoriteAdIds.size() + "]"; }
}