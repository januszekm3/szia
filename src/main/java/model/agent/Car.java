package model.agent;

public class Car {
    private float accelerationSpeed;
    private float decelerationSpeed;
    private float length;
    private float maxVelocity;

    public Car(float accelerationSpeed, float decelerationSpeed, float maxVelocity, float length) {
        this.accelerationSpeed = accelerationSpeed;
        this.decelerationSpeed = decelerationSpeed;
        this.maxVelocity = maxVelocity;
        this.length = length;
    }

    public float getAccelerationSpeed() {
        return accelerationSpeed;
    }

    public float getDecelerationSpeed() {
        return decelerationSpeed;
    }

    public float getLength() {
        return length;
    }

    public float getMaxVelocity() {
        return maxVelocity;
    }

    public void shorten(float deficit) {
        if (this.length >= deficit) {
            this.length -= deficit;
        } else {
            this.length = 0;
        }
    }
}
