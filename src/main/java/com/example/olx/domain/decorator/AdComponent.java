package com.example.olx.domain.decorator;

import com.example.olx.domain.model.Ad;

public interface AdComponent {
    String getDisplayInfo();
    double getCalculatedPrice();
    String getFormattedTitle();
    Ad getAd();
}