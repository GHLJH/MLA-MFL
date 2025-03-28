package com.example.pdrdemo;

public class Accelerate_info {
    public long id ;
    public long time;
    public int value;
    public float x;
    public float y;
    public float z;

    public float KF_angle;
    public float EKF_angle;
    public float StepCount;
    public float StepLength;
    public float NormalAngle;


    public int sensor;

    public Accelerate_info(long time, float x,float y,float z,float KF_angle,float EKF_angle, float StepCount, float StepLength,int sensor,float NormalAngle) {
        this.time = time;
        this.y = y;
        this.x = x;
        this.z = z;
        this.KF_angle = KF_angle;
        this.EKF_angle = EKF_angle;
        this.StepCount = StepCount;
        this.StepLength = StepLength;
        this.NormalAngle=NormalAngle;

        this.sensor = sensor;
    }
    public Accelerate_info(){};
    public long getId() {
        return id;
    }
    public void setId(long id) {
        this.id = id;
    }
    public long getTime() {
        return time;
    }
    public void setTime(long time) {
        this.time = time;
    }
    public float getEKF_angle() {
        return EKF_angle;
    }
    public void setEKF_angle(float EKF_angle) {
        this.EKF_angle = EKF_angle;
    }
    public float getKF_angle() {
        return KF_angle;
    }
    public void setKF_angle(float KF_angle) {
        this.KF_angle = KF_angle;
    }
    public float getNormalAngle() {
        return NormalAngle;
    }
    public void setNormalAngle(float NormalAngle) {
        this.NormalAngle = NormalAngle;
    }
    public float getStepCount() {
        return StepCount;
    }
    public void setStepCount(float StepCount) {
        this.StepCount = StepCount;
    }
    public float getStepLength() {
        return StepLength;
    }
    public void setStepLength(float StepLength) {
        this.StepLength = StepLength;
    }
    public int getSensor() {
        return sensor;
    }
    public void setSensor(int sensor) {
        this.sensor = sensor;
    }
    public int getValue() {
        return value;
    }
    public void setValue(int value) {
        this.value = value;
    }
    public float getX() {
        return x;
    }
    public void setX(float x) {
        this.x = x;
    }
    public float getY() {
        return y;
    }
    public void setY(float y) {
        this.y = y;
    }

    public float getZ() {
        return z;
    }
    public void setZ(float z) {
        this.z = z;
    }

    @Override
    public String toString() {
        return "Accelerate_info [id=" + id + ", time=" + time + "," +
                "x="+x+",y="+y+",z="+z+",KF_angle="+KF_angle+",EKF_angle="+EKF_angle+",StepCount="+StepCount+",StepLength="+StepLength+"," +
                "NormalAngle=" + NormalAngle +"value=" + value + ", sensor=" + sensor + "]";
    }


}
