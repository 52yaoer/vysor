package vysor.koushikdutta.com.vysor;

import java.util.ArrayList;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends Activity implements View.OnClickListener {

    private Button btn_record;
    private boolean isServiceRunning;
    private Handler mHandler = new Handler(new Handler.Callback() {

        @Override
        public boolean handleMessage(Message msg) {
            if (isServiceRunning) {
                btn_record.setText("停止");
                btn_record.setBackgroundResource(R.drawable.shape_btn_running);
            } else {
                btn_record.setText("开始");
                btn_record.setBackgroundResource(R.drawable.shape_btn_stopped);
            }
            return false;
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btn_record = (Button) findViewById(R.id.btn_record);
        btn_record.setOnClickListener(this);
        if (AutoTool.isRoot() && AutoTool.getRootAuth()) {
            btn_record.setEnabled(true);

        } else {
            btn_record.setEnabled(false);
            Toast.makeText(this, "请先获取Root权限", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        isServiceRunning = isServiceRunning(this);
        if (isServiceRunning) {
            btn_record.setText("停止");
            btn_record.setBackgroundResource(R.drawable.shape_btn_running);
        } else {
            btn_record.setText("开始");
            btn_record.setBackgroundResource(R.drawable.shape_btn_stopped);
        }
    }

    private boolean isServiceRunning(Context context) {
        ActivityManager myManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        ArrayList<ActivityManager.RunningServiceInfo> runningService = (ArrayList<ActivityManager.RunningServiceInfo>) myManager.getRunningServices(Integer.MAX_VALUE);
        for (int i = 0; i < runningService.size(); i++) {
            if (runningService.get(i).service.getClassName().toString().equals("vysor.koushikdutta.com.vysor.SimulateService")) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        switch (v.getId()) {
            case R.id.btn_record:
                isServiceRunning = !isServiceRunning;
                if (isServiceRunning) {
                    DisplayMetrics displayMetrics = AutoTool.getScreenMetrix(this);
                    int width = displayMetrics.widthPixels;
                    int height = displayMetrics.heightPixels;
                    Intent simulateIntent = new Intent(this, SimulateService.class);
                    simulateIntent.putExtra("width", width);
                    simulateIntent.putExtra("height", height);
                    startService(simulateIntent);
                } else {
                    Intent simulateIntent = new Intent(this, SimulateService.class);
                    stopService(simulateIntent);
                }
                applyRotation(0, 180);
                break;

            default:
                break;
        }
    }

    private void applyRotation(float start, float end) {
        // 计算中心点
        final float centerX = btn_record.getWidth() / 2.0f;
        final float centerY = btn_record.getHeight() / 2.0f;

        final Rotate3dAnimation rotation = new Rotate3dAnimation(this, start, end, centerX, centerY, 1.0f, true);
        rotation.setDuration(900);
        rotation.setFillAfter(false);
        rotation.setInterpolator(new AccelerateInterpolator());

        rotation.setAnimationListener(new Animation.AnimationListener() {

            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mHandler.sendEmptyMessage(0);
            }
        });
        btn_record.startAnimation(rotation);
    }
}