// src/main/java/com/example/olx/domain/decorator/UrgentAdDecorator.java
package com.example.olx.domain.decorator;

public class UrgentAdDecorator extends AdDecorator {

    public UrgentAdDecorator(AdComponent component) {
        super(component);
    }
    @Override
    public String getFormattedTitle() {
        return getTitle();
    }
    @Override
    public String getTitle() {
        return "🔥 ТЕРМІНОВО! " + super.getTitle();
    }

    @Override
    public String getDisplayInfo() {
        return super.getDisplayInfo() + "\n🚨 ТЕРМІНОВО! Потрібно продати швидко!";
    }
}