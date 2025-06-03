// src/main/java/com/example/olx/domain/decorator/AdComponent.java
package com.example.olx.domain.decorator;

import com.example.olx.domain.model.Ad;

public interface AdComponent {
    String getTitle();
    String getDescription();
    double getPrice();
    String getDisplayInfo();
    Ad getAd();
}