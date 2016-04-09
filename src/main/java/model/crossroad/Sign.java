package model.crossroad;

import java.awt.*;

public enum Sign {
    GIVE_WAY(Color.CYAN),
    RIGHT_OF_WAY(Color.BLUE);

    private Color color;

    Sign(Color color) {
        this.color = color;
    }

    public Color getColor() {
        return color;
    }
}
