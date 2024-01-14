package com.simanov.leonSleep;

public enum State {
    UP("встал"),
    DOWN("уснул");

    public final String label;

    private State(String label) {
        this.label = label;
    }
}
