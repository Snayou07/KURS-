package com.example.olx.domain.decorator;

public class UrgentAdDecorator extends AdDecorator {

    public UrgentAdDecorator(AdComponent component) {
        super(component);
    }

    @Override
    public String getDisplayInfo() {
        return "🚨 ТЕРМІНОВО! 🚨\n" +
                super.getDisplayInfo() +
                "\n⚡ Потребує швидкого продажу";
    }

    @Override
    public String getFormattedTitle() {
        return "🚨 ТЕРМІНОВО: " + super.getFormattedTitle();
    }
}