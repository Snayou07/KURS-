// src/main/java/com/example/olx/domain/decorator/DeliveryAdDecorator.java
package com.example.olx.domain.decorator;

public class DeliveryAdDecorator extends AdDecorator {
    private boolean freeDelivery;
    private double deliveryCost;
    private String deliveryInfo;

    public DeliveryAdDecorator(AdComponent component, boolean freeDelivery, double deliveryCost, String deliveryInfo) {
        super(component);
        this.freeDelivery = freeDelivery;
        this.deliveryCost = deliveryCost;
        this.deliveryInfo = deliveryInfo != null ? deliveryInfo : "Доставка доступна";
    }

    @Override
    public String getTitle() {
        if (freeDelivery) {
            return super.getTitle() + " 🚚 БЕЗКОШТОВНА ДОСТАВКА";
        } else {
            return super.getTitle() + String.format(" 🚚 Доставка %.2f грн", deliveryCost);
        }
    }

    @Override
    public String getDisplayInfo() {
        String deliveryText;
        if (freeDelivery) {
            deliveryText = "🚚 БЕЗКОШТОВНА ДОСТАВКА";
        } else {
            deliveryText = String.format("🚚 Доставка: %.2f грн", deliveryCost);
        }

        return super.getDisplayInfo() +
                String.format("\n%s - %s", deliveryText, deliveryInfo);
    }

    public boolean isFreeDelivery() {
        return freeDelivery;
    }

    public double getDeliveryCost() {
        return deliveryCost;
    }

    public String getDeliveryInfo() {
        return deliveryInfo;
    }
}