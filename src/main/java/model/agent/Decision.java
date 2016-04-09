package model.agent;

import model.crossroad.Edge;

public class Decision {
    public static enum Acceleration {
        ACCELERATE,
        KEEP,
        BRAKE;

        public float getVelocityDifference(Car car) {
            float value = 0;
            switch (this) {
                case ACCELERATE:
                    value = car.getAccelerationSpeed();
                    break;
                case KEEP:
                    value = 0;
                    break;
                case BRAKE:
                    value = -car.getDecelerationSpeed();
                    break;
                default:
                    throw new RuntimeException("Fuckup!!!!!");
            }
            return value;
        }
    }

    public Acceleration acceleration = Acceleration.KEEP;
    public Edge nextEdge;
}
