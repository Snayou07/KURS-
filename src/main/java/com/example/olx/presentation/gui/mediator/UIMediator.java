package com.example.olx.presentation.gui.mediator;

/**
 * Інтерфейс медіатора для координації взаємодії між UI компонентами
 */
public interface UIMediator {
    void notify(Object sender, String event, Object data);
}