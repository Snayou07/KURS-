package com.example.olx.domain.model;

public interface AdState {
    void publish(Ad ad);
    void archive(Ad ad);
    void markAsSold(Ad ad);
    String getStatusName();
}
