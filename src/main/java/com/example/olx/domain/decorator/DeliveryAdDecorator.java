// com/example/olx/domain/decorator/DeliveryAdDecorator.java
package com.example.olx.domain.decorator;

public class DeliveryAdDecorator extends AdDecorator {
    private boolean freeDelivery;
    private double deliveryCost;
    private String deliveryInfo;

    public DeliveryAdDecorator(AdComponent component, boolean freeDelivery, double deliveryCost, String deliveryInfo) {
        super(component);
        this.freeDelivery = freeDelivery;
        this.deliveryCost = this.freeDelivery ? 0 : Math.max(0, deliveryCost);
        this.deliveryInfo = deliveryInfo != null ? deliveryInfo : "Стандартна доставка";
    }

    @Override
    public String getDisplayInfo() {
        String deliveryText = freeDelivery ?
                "\n🚚 БЕЗКОШТОВНА ДОСТАВКА! 🎉" :
                String.format("\n🚚 Доставка: %.2f грн", this.deliveryCost);

        return super.getDisplayInfo() +
                deliveryText +
                "\n📦 " + this.deliveryInfo;
    }

    @Override
    public double getCalculatedPrice() {
        // Додаємо вартість доставки тільки якщо вона не безкоштовна
        return super.getCalculatedPrice() + (this.freeDelivery ? 0 : this.deliveryCost);
    }

    @Override
    public String getFormattedTitle() {
        String deliveryLabel = freeDelivery ? " 🚚 [Безкоштовна доставка]" : " 🚚 [З доставкою]";
        return super.getFormattedTitle() + deliveryLabel;
    }
}