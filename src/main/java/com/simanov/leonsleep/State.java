package com.simanov.leonsleep;

public enum State {
    UP("встал"),
    DOWN("уснул");

    public final String label;

    State(String label) {
        this.label = label;
    }
}
