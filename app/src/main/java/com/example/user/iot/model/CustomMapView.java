package com.example.user.iot.model;

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
    private PointF sPin;
    private Bitmap pin;
    private ArrayList<Node> nodeList;
    private boolean plotRoute;

    public CustomMapView(Context context) {

        this(context, null);
    }

    public CustomMapView(Context context, AttributeSet attr) {

        super(context, attr);
    }

    private void initialise() {
        //viene creata e scalata l'immagine del pin in base alla dpi dello schermo del tel
        float density = getResources().getDisplayMetrics().densityDpi;
        pin = BitmapFactory.decodeResource(this.getResources(), node.getId());
        float w = (density/420f) * pin.getWidth();
        float h = (density/420f) * pin.getHeight();
        pin = Bitmap.createScaledBitmap(pin, (int)w, (int)h, true);
    }

    public void setNode(Node node) {
        this.node = node;
        initialise();
        plotRoute = false;
    }

    public void setRoute(ArrayList<Node> nodeList){
        this.nodeList = nodeList;
        plotRoute = true;
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

        Paint paint = new Paint();
        paint.setAntiAlias(true);

        //punto singolo
        if (sPin != null && pin != null && !plotRoute) {
            sPin = node.getPoint();

            //vengono convertite le coordinate della posizione del pin sulla mappa
            PointF vPin = sourceToViewCoord(sPin);

            //viene regolato quale punto DELL'IMMAGINE DEL PIN verrà ancorato alla mappa
            float vX = vPin.x - (pin.getWidth() / 2);
            float vY = vPin.y - pin.getHeight();

            //viene disegnato il pin sulla mappa
            canvas.drawBitmap(pin, vX, vY, paint);
        }

        //punti multipli
        if (nodeList != null && plotRoute) {
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
