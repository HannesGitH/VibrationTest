package com.example.vibrationtest;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.MyLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.ToggleButton;
import android.widget.VerticalSeekBar;

import static java.lang.Math.abs;

public class MainActivity extends AppCompatActivity {

    TextView wlt;
    TextView hnt;
    SeekBar wl;
    SeekBar hn;

    TextView wft;
    VerticalSeekBar[] wfs;
    MyLayout wfh;
    TextView fct;
    TextView pct;
    SeekBar fc;
    SeekBar pc;

    Button fun;
    ToggleButton mode;
    Switch switvh;
    boolean advanced=false;

    boolean vib=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        wl= findViewById(R.id.wl);
        hn= findViewById(R.id.hn);
        fun= findViewById(R.id.button);
        switvh=findViewById(R.id.switch1);
        mode= findViewById(R.id.toggleButton);
        wlt= findViewById(R.id.wltext);
        hnt= findViewById(R.id.hntext);
        wft= findViewById(R.id.wftext);
        wfh= findViewById(R.id.container);
        pc= findViewById(R.id.pc);
        pct= findViewById(R.id.pctext);
        fc= findViewById(R.id.fc);
        fct= findViewById(R.id.fctext);

        wfs=new VerticalSeekBar[1];
        wfs[0]=findViewById(R.id.vertical_Seekbar);

        wl.setProgress(75);
        hn.setProgress(50);
        fc.setProgress(50);

        mode.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                advanced=b;
                if(advanced){
                    hn.setVisibility(View.GONE);
                    wl.setVisibility(View.GONE);
                    hnt.setVisibility(View.GONE);
                    wlt.setVisibility(View.GONE);

                    wft.setVisibility(View.VISIBLE);
                    wfh.setVisibility(View.VISIBLE);
                    pc.setVisibility(View.VISIBLE);
                    pct.setVisibility(View.VISIBLE);
                    fc.setVisibility(View.VISIBLE);
                    fct.setVisibility(View.VISIBLE);
                    //for(VerticalSeekBar w:wfs){w.setVisibility(View.VISIBLE);}

                }else{
                    hn.setVisibility(View.VISIBLE);
                    wl.setVisibility(View.VISIBLE);
                    hnt.setVisibility(View.VISIBLE);
                    wlt.setVisibility(View.VISIBLE);

                    wft.setVisibility(View.GONE);
                    wfh.setVisibility(View.GONE);
                    pc.setVisibility(View.GONE);
                    pct.setVisibility(View.GONE);
                    fc.setVisibility(View.GONE);
                    fct.setVisibility(View.GONE);
                    //for(VerticalSeekBar w:wfs){w.setVisibility(View.GONE);}
                }

            }
        });

        fun.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if(!vib) {
                    vib();
                    fun.setText("Stop");
                    vib=true;
                }else{
                    vibrate(new int[]{1,0},new long[]{10,10},false);
                    fun.setText("Have Fun");
                    vib=false;
                }
            }
        });
        pc.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                setPrecision(progress);
            }
            public void onStartTrackingTouch(SeekBar seekBar) { }
            public void onStopTrackingTouch(SeekBar seekBar) { }
        });
        fc.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(vib){
                    vib();
                }
            }
            public void onStartTrackingTouch(SeekBar seekBar) { }
            public void onStopTrackingTouch(SeekBar seekBar) { }
        });
        wl.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(vib){
                    vib();
                }
            }
            public void onStartTrackingTouch(SeekBar seekBar) { }
            public void onStopTrackingTouch(SeekBar seekBar) { }
        });


    }

    void vib(){
        boolean repeat=switvh.isChecked();
        if(!advanced){
            int hardness=hn.getProgress();
            long wave=(wl.getProgress())^2+1;
            vibrate(new int[]{hardness,0},new long[]{wave,wave},repeat);
        }else{
            int len=wfs.length;
            long[]wls=new long[len];
            int[] hns=new int[len];
            long stepsize=((fc.getMax()-fc.getProgress())^3)*10/len+1;
            for(int i=0;i<len;i++){
                wls[i]=stepsize;
                hns[i]=wfs[i].getProgress();
            }
            vibrate(hns,wls,repeat);
        }
    }


    void vibrate(int[] hn,long[] wave,boolean repeat){
        long[] VIBRATE_PATTERN = wave;
        Vibrator mVibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        int i=repeat?0:1;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // API 26 and above
            mVibrator.vibrate(VibrationEffect.createWaveform(VIBRATE_PATTERN,hn, i));
        } else {
            // Below API 26
            mVibrator.vibrate(VIBRATE_PATTERN, i);
        }
    }



    void setPrecision(int p){
        for(VerticalSeekBar w:wfs)
        {
            wfh.removeView(w);
        }
        wfs=new VerticalSeekBar[p];

        for(int i=0;i<p;i++){
            VerticalSeekBar w=new VerticalSeekBar(this);
            w.setMax(255);
            w.setLayoutParams(new ViewGroup.LayoutParams(wfh.getWidth()/p, ViewGroup.LayoutParams.MATCH_PARENT));
            w.setProgressTintList(ColorStateList.valueOf(Color.parseColor("#009688")));

            wfs[i]=w;
            wfh.addView(w);
        }
        wfh.setOnTouchListener(new View.OnTouchListener(){
            @Override
            public boolean onTouch(View v, MotionEvent event){
                if(event.getHistorySize()>0){
                    //got some skipped values
                    for(int j=0;j<event.getHistorySize();j++){
                        int x=(int)event.getHistoricalX(j);
                        int width = wfh.getWidth() / wfh.getChildCount();
                        int newp = (int) ((wfh.getBottom() - event.getHistoricalY(j)) * 260 / (wfh.getBottom() - wfh.getTop()) - 380);
                        newp = (newp >= 255) ? 255 : newp;
                        newp = (newp <= 0) ? 0 : newp;
                        System.out.println(newp);
                        try {
                            ((VerticalSeekBar) wfh.getChildAt(x / width)).setProgress(newp);
                        } catch (Exception e) {
                        }
                    }
                }
                    int x = ((int) event.getX());
                    int width = wfh.getWidth() / wfh.getChildCount();
                    int newp = (int) ((wfh.getBottom() - event.getY()) * 260 / (wfh.getBottom() - wfh.getTop()) - 380);
                    newp = (newp >= 255) ? 255 : newp;
                    newp = (newp <= 0) ? 0 : newp;
                    System.out.println(newp);
                    try {
                        ((VerticalSeekBar) wfh.getChildAt(x / width)).setProgress(newp);
                    } catch (Exception e) {
                    }


                return true;
            }
        });

    }
}
