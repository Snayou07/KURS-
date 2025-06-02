// com/example/olx/domain/decorator/DiscountAdDecorator.java
package com.example.olx.domain.decorator;

public class DiscountAdDecorator extends AdDecorator {
    private double discountPercentage;
    private String discountReason;

    public DiscountAdDecorator(AdComponent component, double discountPercentage, String discountReason) {
        super(component);
        this.discountPercentage = Math.max(0, Math.min(100, discountPercentage)); // від 0% до 100%
        this.discountReason = discountReason != null ? discountReason : "Спеціальна пропозиція";
    }

    public DiscountAdDecorator(BasicAdComponent component) {
        super(component);
    }

    @Override
    public String getDisplayInfo() {
        // Переконайтеся, що originalPrice береться з попереднього компонента ДО застосування поточної знижки
        double priceBeforeThisDiscount = super.getCalculatedPrice(); // Ціна до цієї знижки
        double finalDiscountedPrice = getCalculatedPrice(); // Ціна після цієї знижки
        double savedAmount = priceBeforeThisDiscount * (this.discountPercentage / 100.0); // Сума саме цієї знижки

        // Якщо у вас можуть бути кілька знижок, логіка відображення "Стара ціна"
        // може потребувати перегляду. Можливо, вам потрібен доступ до originalPrice з BasicAdComponent.
        // Поточний super.getDisplayInfo() вже може містити інформацію про ціну.

        String originalInfo = super.getDisplayInfo();
        // Можливо, потрібно буде видалити або замінити інформацію про ціну з originalInfo,
        // щоб уникнути дублювання або плутанини.

        return "💰 ЗНИЖКА " + (int)this.discountPercentage + "% 💰 (" + this.discountReason + ")\n" +
                originalInfo + // Тут може бути вже ціна з попередніх декораторів або базова
                String.format("\n❌ Початкова ціна для цієї знижки: %.2f грн", priceBeforeThisDiscount) + // Для ясності
                String.format("\n✅ Ціна після цієї знижки: %.2f грн", finalDiscountedPrice) +
                String.format("\n💸 Заощаджено цією знижкою: %.2f грн", savedAmount);
    }

    @Override
    public double getCalculatedPrice() {
        return super.getCalculatedPrice() * (1 - discountPercentage / 100.0);
    }

    @Override
    public String getFormattedTitle() {
        return "💰 " + super.getFormattedTitle() + " (-" + (int)discountPercentage + "%)";
    }
}