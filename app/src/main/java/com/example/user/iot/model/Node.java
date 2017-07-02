package com.example.user.iot.model;

import android.graphics.PointF;

import com.example.user.iot.R;

import java.util.List;

public class Node {

    private PointF point;
    private int drawable, floor;
    private String id, macAddress;

    public Node(PointF point, int drawable, int floor){
        this.point = point;
        this.drawable = drawable;
        this.floor = floor;
        this.macAddress = null;
        this.id = null;
    }

    public Node(float x, float y, int drawable, int floor){
        this.point = new PointF(x,y);
        this.drawable = drawable;
        this.floor = floor;
        this.macAddress = null;
        this.id = null;
    }

    public Node(float x, float y, String type, int floor, String id){
        this.point = new PointF(x,y);
        setDrawable(type);
        this.floor = floor;
        this.macAddress = null;
        this.id = id;
    }

    public Node(float x, float y, String type, int floor, String id, String macAddress){
        this.point = new PointF(x,y);
        this.floor = floor;
        setDrawable(type);
        this.macAddress = macAddress;
        this.id = id;
    }

    public boolean isSelected(PointF point1){
        if(point1.x >= (point.x-20) && point1.x <= (point.x+20)){
            if(point1.y >= (point.y-20) && point1.y <= (point.y+20)){
                return true;
            }
        }
        return false;
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
            case "Illuminazione": drawable = R.drawable.light;
                break;
            default: drawable = R.drawable.purple;
                break;
        }
    }

    public void setPoint(PointF point){ this.point = point; }

    public void setFloor(int floor){
        this.floor = floor;
    }

    public void setMacAddress(String macAddress){ this.macAddress = macAddress;}

    public void setId(String id){ this.id = id; }

    public PointF getPoint(){return point;}

    public int getDrawable(){
        return drawable;
    }

    public int getFloor(){
        return floor;
    }

    public String getMacAddress(){ return macAddress; }

    public String getId(){ return id;}
}
