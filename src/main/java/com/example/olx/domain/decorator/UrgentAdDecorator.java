package com.example.olx.domain.decorator;

public class UrgentAdDecorator extends AdDecorator {

    public UrgentAdDecorator(AdComponent component) {
        super(component);
    }

    @Override
    public String getDisplayInfo() {
        return "🚨 ТЕРМІНОВО! 🚨\n" + super.getDisplayInfo();
    }

    @Override
    public String getFormattedTitle() {
        return "🚨 " + super.getFormattedTitle();
    }
}
