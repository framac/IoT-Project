package com.example.user.iot.model;

import android.graphics.PointF;

import com.example.user.iot.R;

import java.util.List;

public class Node {

    private PointF point;
    private int drawable, floor;
    private String id; //codice del nodo(quello sul file excel)
    private List<String> beacon;

    public Node(PointF point, int drawable, int floor){
        this.point = point;
        this.drawable = drawable;
        this.floor = floor;
        this.beacon = null;
    }

    public Node(float x, float y, int drawable, int floor){
        this.point = new PointF(x,y);
        this.drawable = drawable;
        this.floor = floor;
        this.beacon = null;
    }

    public Node(float x, float y, String type, int floor){
        this.point = new PointF(x,y);
        setDrawable(type);
        this.floor = floor;
        this.beacon = null;
    }

    public Node(float x, float y, String type, int floor,List<String> beacon){
        this.point = new PointF(x,y);
        this.floor = floor;
        setDrawable(type);
        this.beacon = beacon;
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

    public boolean isNear(PointF point1){
        if(point1.x >= (point.x-20) && point1.x <= (point.x+20)){
            if(point1.y >= (point.y-20) && point1.y <= (point.y+20)){
                return true;
            }
        }
        return false;
    }

    public void setPoint(PointF point){this.point = point;}

    public void setFloor(int floor){
        this.floor = floor;
    }

    public void setBeacon(List<String> beacon){this.beacon = beacon;}

    public PointF getPoint(){return point;}

    public int getDrawable(){
        return drawable;
    }

    public int getFloor(){
        return floor;
    }

    public List<String> getDatiBeacon(){ return beacon;}
}
