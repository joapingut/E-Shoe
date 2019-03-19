package es.joapingut.eshoe;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;
import android.view.SurfaceHolder;

import es.joapingut.eshoe.dto.EShoeColorPoint;
import es.joapingut.eshoe.dto.EShoeData;

public class EShoeSurface implements SurfaceHolder.Callback {

    private static final int bitmapHeight = 100;
    private static final int bitmapWidth = 50;

    private static final EShoeColorPoint[] sensorCoordinates = {
            new EShoeColorPoint(20,10),
            new EShoeColorPoint(15,20),
            new EShoeColorPoint(7,30),
            new EShoeColorPoint(30,10),
            new EShoeColorPoint(35,20),
            new EShoeColorPoint(43,30),
            new EShoeColorPoint(25,90)
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
                float force = data.getData(i);
                p.setColor(generatePaintFromScale(100, cutColorScale(force)));
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
        double acum = 0;
        float forceAcum = 0;
        for(EShoeColorPoint p:sensorCoordinates){
            if (x == p.x && y == p.y){
                return Color.BLUE;
            }
            acum += distance(x, y, p.x, p.y);
            forceAcum += p.getForce();
        }
        acum = acum / sensorCoordinates.length;
        forceAcum = forceAcum / sensorCoordinates.length;
        return generatePaintFromScale((int)acum, cutColorScale(forceAcum));
    }

    private double distance(int firstX, int firstY, int secondX, int secondY){
        return Math.sqrt(Math.pow(secondX - firstX, 2) + Math.pow(secondY - firstY, 2));
    }

    private int generatePaintFromScale(int scale, int force){
        int value;
        if (force > 255){
            value = 255;
        } else {
            value = force;
        }

        int percent;
        if (scale > 100){
            percent = 100;
        } else if (scale < 0){
            percent = 0;
        } else {
            percent = 100 - scale;
        }

        value = (percent * value) / 100;

        return Color.argb(255, value,0, 0);
    }

    private int cutColorScale(float number){
        float mul = number * 100;
        return mul > 255 ? 255 : (int) mul;
    }
}
