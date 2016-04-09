package model.agent;

public class Driver {
    private float safeDistanceToNextCar = 0.1f;
    private float safeDistanceToNextStayingCar = 0.01f;
    private float madnessFactor = 0;

    public Driver(float safeDistanceToNextCar, float safeDistanceToNextStayingCar, float madnessFactor) {
        this.safeDistanceToNextCar = safeDistanceToNextCar;
        this.safeDistanceToNextStayingCar = safeDistanceToNextStayingCar;
        this.madnessFactor = madnessFactor;
    }

    public float getSafeDistanceToNextCar() {
        return safeDistanceToNextCar;
    }

    public float getSafeDistanceToNextStayingCar() {
        return safeDistanceToNextStayingCar;
    }

    public float getMadnessFactor() {
        return madnessFactor;
    }
}
