package com.example.user.iot.model;

import android.graphics.PointF;

import com.example.user.iot.R;

public class Node {
    private String id, macAdress;
    private PointF point;
    private int drawable, floor, batteryLevel;
    private double distance, temperatureLevel, accelerometerX, accelerometerY, accelerometerZ, lightLevel;

    public Node(PointF point, int drawable, int floor){
        this.point = point;
        this.drawable = drawable;
        this.floor = floor;
    }

    public Node(float x, float y, int drawable, int floor){
        this.point = new PointF(x,y);
        this.drawable = drawable;
        this.floor = floor;
    }

    public Node(float x, float y, String type, int floor){
        this.point = new PointF(x,y);
        this.floor = floor;
        setDrawable(type);
    }

    public void setDrawable(String type){
        switch(type){
            case "Aula": drawable = R.drawable.target;
                break;
            case "Uscita": drawable = R.drawable.exit;
                break;
            case "Beacon": drawable = R.drawable.beacon;
                break;
            case "Emergenza": drawable = R.drawable.flame;
                break;
            case "Utente": drawable = R.drawable.user;
                break;
            default: drawable = R.drawable.purple;
                break;
        }
    }

    public void setPoint(PointF point){this.point = point;}

    public PointF getPoint(){return point;}

    public int getDrawable(){
        return drawable;
    }

    public void setFloor(int floor){
        this.floor = floor;
    }

    public int getFloor(){
        return floor;
    }

    public boolean isNear(PointF point1){
        if(point1.x >= (point.x-20) && point1.x <= (point.x+20)){
            if(point1.y >= (point.y-20) && point1.y <= (point.y+20)){
                return true;
            }
        }
       return false;
    }

}
