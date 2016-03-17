package com.example.saturn.flashit;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.Bundle;
import android.test.AndroidTestRunner;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.security.spec.ECField;


public class MainActivity extends Activity {

    private ImageButton button;
    private  boolean isFlashOn=false;
    private Camera camera;
    private SeekBar seekBar;
    private TextView textView;
    private int freq;
    private Thread t;
    private StroboRunner stroboRunner;
    private boolean stopFlicker=false;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Context context = this;
        PackageManager pm = context.getPackageManager();

        /*Checking availability of required camera hardware*/
        if (!pm.hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            Log.e("err", "Device has no camera!");
            Toast.makeText(getApplicationContext(),
                    "Your device doesn't have camera!",
                    Toast.LENGTH_SHORT).show();

            return;
        }

        /*Opening camera access for accessing torch feature of camera*/
             camera = Camera.open();
           final Camera.Parameters p = camera.getParameters();

        textView = (TextView)findViewById(R.id.textView_progress);
        seekBar = (SeekBar)findViewById(R.id.seekBar);

        /*Button for switching ON/OFF the torch*/
        button = (ImageButton)findViewById(R.id.imageButton);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isFlashOn) {
                    Log.i("info", "torch is turned off!");
                    p.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                    camera.setParameters(p);
                    isFlashOn = false;
                    seekBar.setProgress(0);
                    textView.setText(0 + "");
                    if (t != null) {
                        stopFlicker = true;
                        t = null;
                    }
                } else {
                    Log.i("info", "torch is turned on!");
                    p.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                    camera.setParameters(p);
                    isFlashOn = true;
                    stopFlicker=false;
                }
            }
        });


       SeekBarMethod();
    }

    private void SeekBarMethod(){
         /*SeekBar which will indicate value of brightness of torch*/
        seekBar.setMax(10);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int progressValue;

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                progressValue = progress;
                textView.setText(progressValue + "");

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {


            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                textView.setText(progressValue + "");
                flashFlicker();

            }

            private void flashFlicker(){
                if(isFlashOn){
                    freq = Math.abs(seekBar.getProgress());
                    stroboRunner = new StroboRunner();
                    t = new Thread(stroboRunner);
                    t.start();
                    return;
                }else{
                    Toast.makeText(MainActivity.this,"Switch on flash",Toast.LENGTH_SHORT).show();
                    seekBar.setProgress(0);
                    textView.setText(0+"");
                }
            }


        });

    }

    private class StroboRunner implements Runnable{

        public void run(){
            try{
                while(!stopFlicker){
                    final Camera.Parameters p = camera.getParameters();
                    p.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                    camera.setParameters(p);
                    Thread.sleep(10 - freq);
                    p.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                    camera.setParameters(p);
                    Thread.sleep(freq);
                }

            }catch (Exception e){
                e.getStackTrace();
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        /*Releasing camera resources*/
        if (camera != null) {
            camera.release();
        }
    }

}
