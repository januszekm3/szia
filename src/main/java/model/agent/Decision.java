package model.agent;

import model.crossroad.Edge;

public class Decision {
    public enum Acceleration {
        ACCELERATE,
        KEEP,
        BRAKE;

        public float getVelocityDifference(Car car) {
            switch (this) {
                case ACCELERATE:
                    return car.getAccelerationSpeed();
                case KEEP:
                    return 0;
                case BRAKE:
                    return -car.getDecelerationSpeed();
                default:
                    throw new RuntimeException("Fuckup!!!!!");
            }
        }
    }

    public Acceleration acceleration = Acceleration.KEEP;
    public Edge nextEdge;
}
