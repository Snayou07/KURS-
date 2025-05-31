package com.example.olx.domain.decorator;

public class PremiumAdDecorator extends AdDecorator {
    private static final double PREMIUM_MULTIPLIER = 1.05; // 5% надбавка за преміум

    public PremiumAdDecorator(AdComponent component) {
        super(component);
    }

    @Override
    public String getDisplayInfo() {
        return "⭐ ПРЕМІУМ ⭐\n" +
                super.getDisplayInfo() +
                "\n✨ Підвищена видимість у пошуку" +
                "\n🚀 Приоритетний показ";
    }

    @Override
    public double getCalculatedPrice() {
        return super.getCalculatedPrice() * PREMIUM_MULTIPLIER;
    }

    @Override
    public String getFormattedTitle() {
        return "⭐ " + super.getFormattedTitle() + " [ПРЕМІУМ]";
    }
}