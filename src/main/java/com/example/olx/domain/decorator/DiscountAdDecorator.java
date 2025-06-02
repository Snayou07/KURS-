package com.example.olx.domain.decorator;

public class DiscountAdDecorator extends AdDecorator {
    private double discountPercentage;
    private String discountReason;

    public DiscountAdDecorator(AdComponent component) {
        super(component);
        this.discountPercentage = Math.max(0, Math.min(100, discountPercentage)); // від 0% до 100%
        this.discountReason = discountReason != null ? discountReason : "Спеціальна пропозиція";
    }

    @Override
    public String getDisplayInfo() {
        double originalPrice = super.getCalculatedPrice();
        double discountedPrice = getCalculatedPrice();
        double savedAmount = originalPrice - discountedPrice;

        return "💰 ЗНИЖКА " + (int)discountPercentage + "% 💰\n" +
                super.getDisplayInfo() +
                String.format("\n❌ Стара ціна: %.2f грн", originalPrice) +
                String.format("\n✅ Нова ціна: %.2f грн", discountedPrice) +
                String.format("\n💸 Ви економите: %.2f грн", savedAmount) +
                "\n🎯 Причина знижки: " + discountReason;
    }

    @Override
    public double getCalculatedPrice() {
        return super.getCalculatedPrice() * (1 - discountPercentage / 100);
    }

    @Override
    public String getFormattedTitle() {
        return "💰 " + super.getFormattedTitle() + " (-" + (int)discountPercentage + "%)";
    }
}