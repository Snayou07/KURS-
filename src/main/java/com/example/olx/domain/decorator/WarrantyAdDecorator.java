// com/example/olx/domain/decorator/WarrantyAdDecorator.java
package com.example.olx.domain.decorator;

public class WarrantyAdDecorator extends AdDecorator {
    private int warrantyMonths;
    private String warrantyType;

    public WarrantyAdDecorator(AdComponent component, int warrantyMonths, String warrantyType) {
        super(component);
        this.warrantyMonths = Math.max(0, warrantyMonths);
        this.warrantyType = warrantyType != null ? warrantyType : "Стандартна гарантія";
    }

    @Override
    public String getDisplayInfo() {
        return super.getDisplayInfo() +
                "\n🛡️ ГАРАНТІЯ: " + warrantyMonths + " місяців" +
                "\n📋 Тип гарантії: " + warrantyType +
                "\n✅ Гарантований сервіс та підтримка";
    }

    @Override
    public String getFormattedTitle() {
        return super.getFormattedTitle() + " 🛡️ [" + warrantyMonths + " міс. гарантії]";
    }
}