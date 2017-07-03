package com.example.user.iot.controller;


import android.graphics.PointF;
import android.view.View;

import com.davemorrissey.labs.subscaleview.ImageSource;
import com.example.user.iot.R;
import com.example.user.iot.model.CustomMapView;
import com.example.user.iot.model.Node;

import java.util.ArrayList;

public class MapViewController {
    private CustomMapView building;
    private ArrayList<Node> floor_145, floor_150, floor_155;
    private Node node;
    private int currentFloor;

    public MapViewController(CustomMapView building){
        this.building = building;
        floor_145 = new ArrayList<>();
        floor_150 = new ArrayList<>();
        floor_155 = new ArrayList<>();
        node = new Node(0, 0, R.drawable.purple, 0);
        changeFloor(145);
    }

    private PointF coordConverter(PointF point, int piano){ //coverte le coordinate da metri a pixel dell'immagine in base al piano
        switch(piano) {
            case 145: point.x = Math.round((point.x - 52) * 6.5);
                point.y = Math.round((1600 - ((point.y - 261.5) * 6.5)));
                break;

            case 150: point.x = Math.round((point.x - 51)*6.2);
                point.y = Math.round( (1572-((point.y-255.6)*6.2)));
                break;

            case 155: point.x = Math.round((point.x - 53)*6.4);
                point.y = Math.round( (1600-((point.y-259)*6.4)));
                break;
            default: point.x = 0;
                point.y = 0;
                break;
        }
        return point;
    }

    public PointF sourceImgCoord(float x, float y){
        return building.viewToSourceCoord(x,y);
    }

    public boolean isMapReady(){
        return building.isReady();
    }

    public void setListener(View.OnTouchListener listener){
        building.setOnTouchListener(listener);
    }

    public void addNode(Node new_node){ //aggiunge un nodo verificando che non ci sia un duplicato, eventualmente lo cancella
          PointF point = coordConverter(new_node.getPoint(),new_node.getFloor());
          new_node.setPoint(point);
          deleteNode(new_node);
          switch(new_node.getFloor()){
            case 145: floor_145.add(new_node);
                break;

            case 150: floor_150.add(new_node);
                break;

            case 155: floor_155.add(new_node);
                break;
            default:
                break;
        }
    }

    private void deleteNode(Node delete){ //cancella un nodo già presente(non c'è la coordConv)
        ArrayList<Node> list = new ArrayList<>();
        switch(delete.getFloor()){
            case 145: list = floor_145;
                break;

            case 150: list = floor_150;
                break;

            case 155: list = floor_155;
                break;
            default:
                break;
        }
        for (int i = 0; i < list.size(); i++) {
            if(list.get(i).getPoint().equals(delete.getPoint())){
                list.remove(i);
            }
        }
    }

    public void deleteAula(){ //cancella l'aula cercata in precedenza
        this.deleteInPiano(145);
        this.deleteInPiano(150);
        this.deleteInPiano(155);
    }

    public void deleteInPiano(int floor){
        ArrayList<Node> list = new ArrayList<>();
        switch(floor){
            case 145: list = floor_145;
                break;

            case 150: list = floor_150;
                break;

            case 155: list = floor_155;
                break;
            default:
                break;
        }
        for (int i = 0; i < list.size(); i++) {
            if(list.get(i).getDrawable()==R.drawable.target){
                list.remove(i);
            }
        }
    }

    public void addNodes(ArrayList<Node> list){ //aggiunge nodi ad uno o più piani contemporaneamente
        for (int i = 0; i < list.size(); i++) { //utile per caricare i tutti i beacon e le uscite all'inizio
            node = list.get(i);
            PointF point = coordConverter(node.getPoint(),node.getFloor());
            list.get(i).setPoint(point);
            switch(node.getFloor()) {
                case 145: floor_145.add(node);
                    break;

                case 150: floor_150.add(node);
                    break;

                case 155: floor_155.add(node);
                    break;
                default:
                    break;
            }
        }
    }

    public ArrayList<Node> getCurrentList(){ //ritorna l'array contente i nodi del piano attualmente visualizzato
        ArrayList<Node> list = new ArrayList<>();
        switch(currentFloor) {
            case 145: list = floor_145;
                break;

            case 150: list = floor_150;
                break;

            case 155: list = floor_155;
                break;
            default:
                break;
        }
        return list;
    }

    public void changeFloor(int floor){ //cambia il piano da visualizzare
        switch(floor) {
            case 145: building.setImage(ImageSource.resource(R.drawable.floor145));
                      building.setList(floor_145);
                      currentFloor = 145;
                break;

            case 150: building.setImage(ImageSource.resource(R.drawable.floor150));
                      building.setList(floor_150);
                      currentFloor = 150;
                break;

            case 155: building.setImage(ImageSource.resource(R.drawable.floor155));
                      building.setList(floor_155);
                      currentFloor = 155;
                break;
            default:
                break;
        }
    }

    public void clearFloor(int floor){ //svuota l'array di nodi del piano scelto resettandolo
        ArrayList<Node> list = new ArrayList<>();
        switch(floor) {
            case 145: floor_145 = list;
                break;

            case 150: floor_150 = list;
                break;

            case 155: floor_155 = list;
                break;
            default:
                break;
        }
    }

}
