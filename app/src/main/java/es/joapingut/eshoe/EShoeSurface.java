package es.joapingut.eshoe;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;
import android.view.SurfaceHolder;

import es.joapingut.eshoe.dto.EShoeColorPoint;
import es.joapingut.eshoe.dto.EShoeData;
import es.joapingut.eshoe.dto.EShoeUtils;

public class EShoeSurface implements SurfaceHolder.Callback {

    private static final int bitmapHeight = 230;
    private static final int bitmapWidth = 130;

    private static final int MAX_DISTANCE_INT = 27;
    private static final float MAX_DISTANCE_FLOAT = 27F;
    private static final double MAX_DISTANCE_DOUBLE = 27.0;

    /*
    *
    * new EShoeColorPoint(25,90),
            new EShoeColorPoint(7,30),
            new EShoeColorPoint(20,10),
            new EShoeColorPoint(15,20),
            new EShoeColorPoint(30,10),
            new EShoeColorPoint(43,30),
            new EShoeColorPoint(35,20)
    *
    * */

    private static final EShoeColorPoint[] sensorCoordinates = {
            new EShoeColorPoint(15 + 50,15 + 180),
            new EShoeColorPoint(15 + 14,15 + 60),
            new EShoeColorPoint(15 + 40,15 + 20),
            new EShoeColorPoint(15 + 30,15 + 40),
            new EShoeColorPoint(15 + 60,15 + 20),
            new EShoeColorPoint(15 + 86,15 + 60),
            new EShoeColorPoint(15 + 70,15 + 40)
    };

    private Manager manager;

    private SurfaceHolder holder;

    public EShoeSurface(Manager manager){
        this.manager = manager;
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
        paint.setARGB(255,255,0,0);

        int[] rawcolors = new int[bitmapHeight * bitmapWidth];

        if (data != null){
            for (int i = 0; i < sensorCoordinates.length; i++){
                EShoeColorPoint p = sensorCoordinates[i];
                float force = data.getData(i+1);
                p.setColor(generatePaintFromScale(EShoeUtils.clipInRange(force, 1F, 0F)));
                p.setForce(force);
            }
            for (int height = 0; height < bitmapHeight; height++){
                for (int width = 0; width < bitmapWidth; width++){
                    //Log.i("Raw", "Index X " + width + " index Y " + height);
                    rawcolors[height * bitmapWidth + width] = calculateColorForPoint(width, height);
                }
            }
            Bitmap bitmap = Bitmap.createBitmap(rawcolors, bitmapWidth, bitmapHeight, Bitmap.Config.ARGB_8888);
            //Bitmap.createScaledBitmap(bitmap, canvas.getWidth(), canvas.getHeight(), false);
            canvas.drawBitmap(bitmap, null, canvas.getClipBounds(), null);
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
        return Math.sqrt(Math.pow(secondX - firstX, 2) + Math.pow(secondY - firstY, 2));
    }

    private int generatePaintFromScale(float force){
        if (force == 0){
            return Color.WHITE;
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
            percent = EShoeUtils.extrapolate(force, 0.5F,1F,0F,1F);
            r = 255;
            g = 255 - (int)(255 * percent);
            b = 0;
        }

        return Color.argb(255, r, g, b);
    }
}
