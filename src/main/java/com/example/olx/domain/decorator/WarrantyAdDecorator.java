// src/main/java/com/example/olx/domain/decorator/WarrantyAdDecorator.java
package com.example.olx.domain.decorator;

public class WarrantyAdDecorator extends AdDecorator {
    private int warrantyMonths;
    private String warrantyType;

    public WarrantyAdDecorator(AdComponent component, int warrantyMonths, String warrantyType) {
        super(component);
        this.warrantyMonths = warrantyMonths;
        this.warrantyType = warrantyType != null ? warrantyType : "Стандартна гарантія";
    }

    @Override
    public String getTitle() {
        return super.getTitle() + String.format(" 🛡️ Гарантія %d міс.", warrantyMonths);
    }

    @Override
    public String getDisplayInfo() {
        return super.getDisplayInfo() +
                String.format("\n🛡️ ГАРАНТІЯ: %d місяців (%s)", warrantyMonths, warrantyType);
    }

    public int getWarrantyMonths() {
        return warrantyMonths;
    }

    public String getWarrantyType() {
        return warrantyType;
    }
}