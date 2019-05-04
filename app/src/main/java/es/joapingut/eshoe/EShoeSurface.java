package es.joapingut.eshoe;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.util.ArrayMap;
import android.util.Log;
import android.view.SurfaceHolder;

import java.util.Map;

import es.joapingut.eshoe.dto.EShoeColorPoint;
import es.joapingut.eshoe.dto.EShoeData;
import es.joapingut.eshoe.dto.EShoeUtils;

public class EShoeSurface implements SurfaceHolder.Callback {

    private static final int bitmapHeight = 230;
    private static final int bitmapWidth = 130;

    private static final int sensorRadius = 27;

    private static final float MAX_DISTANCE_SIMPLE_FLOAT = 13F;
    private static final double MAX_DISTANCE_DOUBLE = 27.0;

    private boolean simpleMode = true;

    private Map<Integer, Map<Integer, Double>> distances;

    private static final EShoeColorPoint[] sensorCoordinates = {
            new EShoeColorPoint(15 + 50,15 + 180),
            new EShoeColorPoint(15 + 14,15 + 60),
            new EShoeColorPoint(15 + 40,15 + 20),
            new EShoeColorPoint(15 + 30,15 + 40),
            new EShoeColorPoint(15 + 60,15 + 20),
            new EShoeColorPoint(15 + 86,15 + 60),
            new EShoeColorPoint(15 + 70,15 + 40)
    };

    private final Bitmap siluet;
    private final Bitmap flippedSiluet;
    private final int defaultColor;

    private Context context;

    private SurfaceHolder holder;

    public EShoeSurface(Context context){
        this.context = context;
        this.distances = new ArrayMap<>();
        this.siluet = BitmapFactory.decodeResource(context.getResources(),
                R.drawable.silueta);
        Matrix matrix = new Matrix();
        matrix.postScale(-1,1);
        this.flippedSiluet = Bitmap.createBitmap(siluet, 0, 0, siluet.getWidth(), siluet.getHeight(), matrix, true);
        this.defaultColor = Color.argb(255,153,255,255);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        this.holder = holder;
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
    }

    public void updateInfo(EShoeData data){
        if (!holder.isCreating()){
            Canvas canvas;
            try {
                canvas = holder.lockCanvas();
            } catch (IllegalArgumentException e){
                Log.e("EShoeSurface", "Cannot lock the surface", e);
                canvas = null;
            }

            if (canvas != null){
                drawTheCanvas(data, canvas);
                holder.unlockCanvasAndPost(canvas);
            } else {
                Log.e("EShoeSurface", "Cannot draw the surface, the canvas is null");
            }
        }
    }

    private void drawTheCanvas(EShoeData data, Canvas canvas) {
        Paint paint = new Paint();

        canvas.drawColor(Color.WHITE);
        if (data != null){
            Bitmap bitmap = null;
            Canvas simpleCV = null;
            if (simpleMode){
                bitmap = Bitmap.createBitmap(bitmapWidth, bitmapHeight, Bitmap.Config.ARGB_8888);
                simpleCV = new Canvas(bitmap);
            }
            for (int i = 0; i < sensorCoordinates.length; i++){
                EShoeColorPoint p = sensorCoordinates[i];
                float force = data.getData(i+1);
                p.setColor(generatePaintFromScale(EShoeUtils.clipInRange(force, 1F, 0F)));
                p.setForce(force);
                if (simpleMode){
                    paint.setColor(p.getColor());
                    simpleCV.drawCircle(p.x, p.y, MAX_DISTANCE_SIMPLE_FLOAT, paint);
                }
            }
            if (!simpleMode){
                int[] rawcolors = new int[bitmapHeight * bitmapWidth];
                for (EShoeColorPoint p : sensorCoordinates){
                    for (int x = -sensorRadius; x < sensorRadius; x++){
                        for (int y = -sensorRadius; y < sensorRadius; y++){
                            int height = p.y + y;
                            int width = p.x + x;
                            rawcolors[height * bitmapWidth + width] = calculateColorForPoint(width, height);
                        }
                    }
                }
                bitmap = Bitmap.createBitmap(rawcolors, bitmapWidth, bitmapHeight, Bitmap.Config.ARGB_8888);
            }
            canvas.drawBitmap(bitmap, null, canvas.getClipBounds(), null);

            /*Drawable d = context.getDrawable(R.drawable.silueta);
            d.setBounds(21, 0, 109, 230);
            d.draw(canvas);*/
            if (data.isRight()){
                canvas.drawBitmap(siluet, null, canvas.getClipBounds(), null);
            } else {
                canvas.drawBitmap(flippedSiluet, null, canvas.getClipBounds(), null);
            }
        }
    }

    private int calculateColorForPoint(int x, int y){
        float acum = 0;
        for(EShoeColorPoint p:sensorCoordinates){
            if (x == p.x && y == p.y){
                return Color.BLUE;
            }
            double distance = EShoeUtils.clipInRange(distance(x, y, p.x, p.y), MAX_DISTANCE_DOUBLE, 0.0);
            double percent = (MAX_DISTANCE_DOUBLE - distance) / MAX_DISTANCE_DOUBLE;
            acum += percent * p.getForce();
        }
        //acum = acum / sensorCoordinates.length;
        return generatePaintFromScale(acum);
    }

    private double distance(int firstX, int firstY, int secondX, int secondY){
        int key = firstY * bitmapWidth + firstX;
        int innerKey = secondY * bitmapWidth + secondX;
        if (distances.containsKey(key)){
            Map<Integer, Double> innerMap = distances.get(key);
            if (innerMap.containsKey(innerKey)){
                return innerMap.get(innerKey);
            }
        }
        double distance = Math.sqrt(Math.pow(secondX - firstX, 2) + Math.pow(secondY - firstY, 2));
        if (distances.containsKey(key)){
            distances.get(key).put(innerKey, distance);
        } else {
            Map<Integer, Double> innerMap = new ArrayMap<>();
            innerMap.put(innerKey, distance);
            distances.put(key, innerMap);
        }
        return distance;
    }

    private int generatePaintFromScale(float force){
        if (force == 0){
            return defaultColor;
        }
        boolean lowerhalf = force < 0.5F;
        //float percent = force * (lowerhalf ? 2F : 0.5F);
        float percent;
        int r,g,b;

        if (lowerhalf){
            percent = force * 2;
            // AZUL
            /*r = color;
            g = color;
            b = 255 - color;*/
            // CELESTE
            r = 153 + (int)(102 * percent);
            g = 255;
            b = 255 - (int)(255 * percent);
        } else {
            percent = (force - 0.5F) * 2;
            r = 255;
            g = 255 - (int)(255 * (percent > 1.0F ? 1.0F : percent));
            b = 0;
        }

        return Color.argb(255, r, g, b);
    }

    public void alterMode() {
        this.simpleMode = !this.simpleMode;
    }
}
