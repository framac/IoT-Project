package com.example.user.iot.model;

import android.graphics.PointF;

public class Node {
    private PointF point;
    private int id, piano; //id è il simbolo del nodo

    public Node(PointF point, int id, int piano){
        this.point = point;
        this.id = id;
        this.piano = piano;
    }

    public Node(float x, float y, int id, int piano){
        this.point = new PointF(x,y);
        this.id = id;
        this.piano= piano;
    }

    public void setPoint(PointF point){this.point = point;}

    public PointF getPoint(){return point;}

    public void setId(int id){
        this.id = id;
    }

    public int getId(){
        return id;
    }

    public void setPiano(int piano){
        this.piano = piano;
    }

    public int getPiano(){
        return piano;
    }

}
