package es.joapingut.eshoe;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.Log;
import android.view.SurfaceHolder;

import es.joapingut.eshoe.dto.EShoeData;

public class EShoeSurface implements SurfaceHolder.Callback {

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

        if (data != null){
            canvas.drawRect(75,75,125,125, paint);
            paint.setARGB(255,255,0, cutColorScale(data.getFsr2()));
            canvas.drawRect(75,75,125,125, paint);
            paint.setARGB(255,255,0,cutColorScale(data.getFsr6()));
            canvas.drawRect(75,125,250,250, paint);
        }

        canvas.drawArc(0,0,50,50,0,25,false, paint);
    }

    private int cutColorScale(float number){
        float mul = number * 100;
        return mul > 255 ? 255 : (int) mul;
    }
}
