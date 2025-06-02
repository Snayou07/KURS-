package com.example.olx.domain.decorator;

public class DeliveryAdDecorator extends AdDecorator {
    private boolean freeDelivery;
    private double deliveryCost;
    private String deliveryInfo;

    public DeliveryAdDecorator(AdComponent component) {
        super(component);
        this.freeDelivery = freeDelivery;
        this.deliveryCost = freeDelivery ? 0 : Math.max(0, deliveryCost);
        this.deliveryInfo = deliveryInfo != null ? deliveryInfo : "Стандартна доставка";
    }

    @Override
    public String getDisplayInfo() {
        String deliveryText = freeDelivery ?
                "\n🚚 БЕЗКОШТОВНА ДОСТАВКА! 🎉" :
                String.format("\n🚚 Доставка: %.2f грн", deliveryCost);

        return super.getDisplayInfo() +
                deliveryText +
                "\n📦 " + deliveryInfo;
    }

    @Override
    public double getCalculatedPrice() {
        return super.getCalculatedPrice() + deliveryCost;
    }

    @Override
    public String getFormattedTitle() {
        String deliveryLabel = freeDelivery ? " 🚚 [Безкоштовна доставка]" : " 🚚 [З доставкою]";
        return super.getFormattedTitle() + deliveryLabel;
    }
}