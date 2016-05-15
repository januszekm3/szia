package model.agent;

import java.util.Date;

public class Driver {
    private float safeDistanceToNextCar = 0.1f;
    private float safeDistanceToNextStayingCar = 0.01f;
    private float madnessFactor = 0;
    private int timeout;
    private Date date = new Date();

    public Driver(float safeDistanceToNextCar, float safeDistanceToNextStayingCar, float madnessFactor, int timeout) {
        this.safeDistanceToNextCar = safeDistanceToNextCar;
        this.safeDistanceToNextStayingCar = safeDistanceToNextStayingCar;
        this.madnessFactor = madnessFactor;
        this.timeout = timeout;
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

    public int getTimeout() {
        return timeout;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }
}
