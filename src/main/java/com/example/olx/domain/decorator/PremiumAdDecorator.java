// src/main/java/com/example/olx/domain/decorator/PremiumAdDecorator.java
package com.example.olx.domain.decorator;

public class PremiumAdDecorator extends AdDecorator {

    public PremiumAdDecorator(AdComponent component) {
        super(component);
    }

    @Override
    public String getTitle() {
        return "⭐ ПРЕМІУМ ⭐ " + super.getTitle();
    }

    @Override
    public String getDisplayInfo() {
        return super.getDisplayInfo() + "\n🌟 ПРЕМІУМ ОГОЛОШЕННЯ - підвищена видимість!";
    }
}