package com.example.olx.presentation.gui.mediator;

/**
 * Базовий клас для UI компонентів, які працюють через медіатор
 */
public abstract class UIComponent {
    protected UIMediator mediator;

    public UIComponent(UIMediator mediator) {
        this.mediator = mediator;
    }

    public void setMediator(UIMediator mediator) {
        this.mediator = mediator;
    }
}