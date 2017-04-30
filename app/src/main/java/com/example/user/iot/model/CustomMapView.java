package com.example.user.iot.model;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;
import android.util.AttributeSet;

import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;

import java.util.ArrayList;


public class CustomMapView extends SubsamplingScaleImageView {
    private Node node;
    private Bitmap pin;
    private ArrayList<Node> nodeList;

    public CustomMapView(Context context) {

        this(context, null);
    }

    public CustomMapView(Context context, AttributeSet attr) {

        super(context, attr);
    }

    private void initialise() {
        //viene creata e scalata l'immagine del pin in base alla dpi dello schermo del tel
        float density = getResources().getDisplayMetrics().densityDpi;
        pin = BitmapFactory.decodeResource(this.getResources(), node.getDrawable());
        float w = (density/420f) * pin.getWidth();
        float h = (density/420f) * pin.getHeight();
        pin = Bitmap.createScaledBitmap(pin, (int)w, (int)h, true);
    }

    private PointF coordConverter(PointF point, int piano){
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

    public void setNode(Node node) {
        PointF point = coordConverter(node.getPoint(),node.getFloor());
        node.setPoint(point);
        nodeList = new ArrayList<>();
        nodeList.add(node);
    }

    public void addNode(Node node){
        nodeList.add(node);
    }

    public void setRoute(ArrayList<Node> list){
        nodeList = list;
        for (int i = 0; i < nodeList.size(); i++) {
            node = nodeList.get(i);
            PointF point = coordConverter(node.getPoint(),node.getFloor());
            nodeList.get(i).setPoint(point);
        }
    }

    public Node getNode() {
        return node;
    }

    public ArrayList<Node> getRoute(){return nodeList;}


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        // verifica che la piantina sia stata caricata
        if (!isReady()) {
            return;
        }
        @SuppressLint("DrawAllocation") Paint paint = new Paint();
        paint.setAntiAlias(true);
        PointF sPin;
        if (nodeList != null) {
            for (int i = 0; i < nodeList.size(); i++) {
                node = nodeList.get(i);
                sPin = node.getPoint();
                //viene creata e scalata l'immagine del pin in base alla dpi dello schermo del tel
                initialise();
                //vengono convertite le coordinate della posizione del pin sulla mappa
                PointF vPin = sourceToViewCoord(sPin);
                //viene regolato quale punto DELL'IMMAGINE DEL PIN verrà ancorato alla mappa
                float vX = vPin.x - (pin.getWidth() / 2);
                float vY = vPin.y - pin.getHeight();
                //viene disegnato il pin sulla mappa
                canvas.drawBitmap(pin, vX, vY, paint);
            }
        }

    }

}
