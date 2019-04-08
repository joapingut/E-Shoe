package es.joapingut.eshoe;

import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.TextView;

import es.joapingut.eshoe.dto.EShoe;
import es.joapingut.eshoe.dto.EShoeData;
import es.joapingut.eshoe.dto.EShoeUtils;

public class ResultActivity extends AppCompatActivity {

    public static final String APP_MANAGER_EXTRA = "es.joapingut.eshoe.manager";

    private Manager manager;

    private TextView lblTotalSteps;
    private TextView lblTotalAverage;

    private SurfaceView surfaceData;
    private EShoeSurface eShoeSurface;

    private EShoeData result;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);
        this.manager = Manager.getManagerInstance(this, null);

        lblTotalSteps = findViewById(R.id.lblTotalSteps);
        lblTotalAverage = findViewById(R.id.lblTotalAverage);
        surfaceData = findViewById(R.id.surfaceResultScreenData);

        SurfaceHolder surfaceDataHolder = surfaceData.getHolder();
        eShoeSurface = new EShoeSurface(this);
        surfaceDataHolder.addCallback(eShoeSurface);
        result = manager.getAverageResult();
    }

    @Override
    protected void onResume() {
        super.onResume();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                eShoeSurface.updateInfo(result);
            }
        }, 2000);
        lblTotalSteps.setText(String.valueOf(manager.getNumSteps()));
        lblTotalAverage.setText(EShoeUtils.getStringByLocal(this, result.getFootPosition().getId()));
    }
}
