package model.crossroad;

public class TrafficLight {
    public static enum Color {
        GREEEN(java.awt.Color.GREEN),
        YELLOW_BEFORE_GREEN(java.awt.Color.YELLOW),
        YELLOW_BEFORE_RED(java.awt.Color.YELLOW),
        RED(java.awt.Color.RED),
        RED_WITH_GREEN_ARROW(java.awt.Color.RED);

        private java.awt.Color color;

        Color(java.awt.Color color) {
            this.color = color;
        }

        public java.awt.Color getColor() {
            return this.color;
        }
    }

    private Color color;

    public TrafficLight(Color color) {
        this.color = color;
    }

    public java.awt.Color getLightColor() {
        return color.getColor();
    }
}
