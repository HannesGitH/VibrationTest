package hannepps.tools.vibrationtest;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.MyLayout;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.ToggleButton;

import static java.lang.Math.abs;

public class MainActivity extends AppCompatActivity {

    TextView wavelength_textView;
    TextView hardness_textView;
    SeekBar wavelenght_slider;
    SeekBar hardness_slider;

    TextView waveform_textView;
    VerticalSeekBar[] waveform_seekbars;
    MyLayout waveform_container;
    TextView frequency_textView;
    TextView precision_textView;
    SeekBar frequency_slider;
    SeekBar precision_slider;

    Button startstop_button;
    ToggleButton advanced_toggleButton;
    SwitchCompat repeat_switch;
    boolean isInAdvancedMode =false;

    boolean isCurrentlyVibrating =false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        wavelenght_slider = findViewById(R.id.wl);
        hardness_slider = findViewById(R.id.hn);
        startstop_button = findViewById(R.id.button);
        repeat_switch = findViewById(R.id.switch1);
        advanced_toggleButton = findViewById(R.id.toggleButton);
        wavelength_textView = findViewById(R.id.wltext);
        hardness_textView = findViewById(R.id.hntext);
        waveform_textView = findViewById(R.id.wftext);
        waveform_container = findViewById(R.id.container);
        precision_slider = findViewById(R.id.pc);
        precision_textView = findViewById(R.id.pctext);
        frequency_slider = findViewById(R.id.fc);
        frequency_textView = findViewById(R.id.fctext);

        wavelenght_slider.setProgress(75);
        hardness_slider.setProgress(50);
        frequency_slider.setProgress(50);

        waveform_seekbars = new VerticalSeekBar[precision_slider.getMax()+1];
        for(int i = 0; i< waveform_seekbars.length; i++){
            VerticalSeekBar w = new VerticalSeekBar(this);
            w.setMax(255);
            w.setProgressTintList(ColorStateList.valueOf( getResources().getColor(R.color.colorPrimary)));
            w.setLayoutParams(new ViewGroup.LayoutParams(10, ViewGroup.LayoutParams.MATCH_PARENT));
            waveform_seekbars[i]=w;
        }


        repeat_switch.setChecked(true);


        advanced_toggleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                isInAdvancedMode =b;

                int  visibilityIfAdvanced = isInAdvancedMode ? View.VISIBLE : View.GONE;
                int nvisibilityIfAdvanced = isInAdvancedMode ? View.GONE : View.VISIBLE;

                hardness_slider.setVisibility(nvisibilityIfAdvanced);
                wavelenght_slider.setVisibility(nvisibilityIfAdvanced);
                hardness_textView.setVisibility(nvisibilityIfAdvanced);
                wavelength_textView.setVisibility(nvisibilityIfAdvanced);

                waveform_textView.setVisibility(visibilityIfAdvanced);
                waveform_container.setVisibility(visibilityIfAdvanced);
                precision_slider.setVisibility(visibilityIfAdvanced);
                precision_textView.setVisibility(visibilityIfAdvanced);
                frequency_slider.setVisibility(visibilityIfAdvanced);
                frequency_textView.setVisibility(visibilityIfAdvanced);

            }
        });

        startstop_button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if(!isCurrentlyVibrating) {
                    vib();
                    startstop_button.setText(R.string.StopVibratingButtonText);
                    isCurrentlyVibrating =true;
                }else{
                    vibrate(new int[]{1,0},new long[]{10,10},false);
                    startstop_button.setText(R.string.StartVibratingButtonText);
                    isCurrentlyVibrating =false;
                }
            }
        });
        precision_slider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                setPrecision(progress);
            }
            public void onStartTrackingTouch(SeekBar seekBar) { }
            public void onStopTrackingTouch(SeekBar seekBar) { }
        });
        frequency_slider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(isCurrentlyVibrating){
                    vib();
                }
            }
            public void onStartTrackingTouch(SeekBar seekBar) { }
            public void onStopTrackingTouch(SeekBar seekBar) { }
        });
        wavelenght_slider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(isCurrentlyVibrating){
                    vib();
                }
            }
            public void onStartTrackingTouch(SeekBar seekBar) { }
            public void onStopTrackingTouch(SeekBar seekBar) { }
        });

            /*
                    precision_slider.setProgress(10);
                    setPrecision(10);
            */

    }

    void vib(){
        boolean repeat= repeat_switch.isChecked();
        if(!isInAdvancedMode || oldPrecision==0){
            int hardness= hardness_slider.getProgress();
            long wave=(wavelenght_slider.getProgress()^2)+1;
            vibrate(new int[]{hardness,0},new long[]{wave,wave},repeat);
        }else{
            int len = oldPrecision;
            long[]wls=new long[len];
            int[] hns=new int[len];
            long stepsize=((frequency_slider.getMax()- frequency_slider.getProgress())^3)*10/(len+1)+1;
            for(int i=0;i<len;i++){
                wls[i]=stepsize;
                hns[i]= waveform_seekbars[i].getProgress();
            }
            vibrate(hns,wls,repeat);
        }
    }


    void vibrate(int[] hn,long[] wave,boolean repeat){
        Vibrator mVibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        int i=repeat?0:1;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // API 26 and above
            mVibrator.vibrate(VibrationEffect.createWaveform(wave,hn, i));
        } else {
            // Below API 26
            mVibrator.vibrate(wave, i);
        }
    }

    private int oldPrecision=1;

    @SuppressLint("ClickableViewAccessibility")
    void setPrecision(int p){
        p=Math.max(1,p);

        for(VerticalSeekBar w: waveform_seekbars){
            ViewGroup.LayoutParams lp = w.getLayoutParams();
            lp.width = waveform_container.getWidth()/p;
            w.requestLayout();
        }

        while(p!= oldPrecision-1){
            VerticalSeekBar w = waveform_seekbars[oldPrecision];
            if (p<oldPrecision){
                waveform_container.removeView(w);
                oldPrecision--;
            }else{
                waveform_container.addView(w);
                oldPrecision++;
            }
        }

        waveform_container.setOnTouchListener(new View.OnTouchListener(){

            class SliderManager{

                int width = waveform_container.getWidth() / waveform_container.getChildCount();

                private VerticalSeekBar sliderAtX(int x){
                    return (VerticalSeekBar) waveform_container.getChildAt(x / width);
                }
                void setValueForTouchAt(Point p){
                    VerticalSeekBar sliderX = sliderAtX(p.x);
                    if (sliderX != null){
                        int newProgress = (int) ((1 - p.y / (float) waveform_container.getHeight())*sliderX.getMax()) ;
                        sliderAtX(p.x).setProgress(newProgress);
                    }
                }
                void setValueForSlideBetween(Point p1, Point p2){

                    //System.out.printf("%d, %d\n",p1.x/width,p2.x/width);

                    if (p1.x == p2.x){
                        setValueForTouchAt(p2);
                        return;
                    }

                    //p1 shall be the one with smaller x
                    if (p1.x > p2.x){
                        Point tmp = p1; p1 = p2; p2 = tmp;
                    }

                    //steigung des dreiecks
                    float d = (float)(p2.y-p1.y)/(float)(p2.x-p1.x);

                    int currentX = p1.x;
                    while (currentX <= p2.x){
                        setValueForTouchAt(new Point(
                                currentX,
                                (int)((currentX-p1.x)*d + p1.y)
                        ));
                        currentX += width;
                    }
                    /**/
                }
            }

            Point prevEventLoc;

            @Override
            public boolean onTouch(View v, MotionEvent event){
                SliderManager sliderManager = new SliderManager();

                Point lastTouch = new Point((int)event.getX(),(int)event.getY());
                Point prevTouch = (event.getHistorySize() == 0)
                        ? lastTouch
                        : new Point((int)event.getHistoricalX(0),(int)event.getHistoricalY(0));
                for(int j=1;j<event.getHistorySize();j++){
                    Point currTouch = new Point((int)event.getHistoricalX(j),(int)event.getHistoricalY(j));
                    sliderManager.setValueForSlideBetween(prevTouch, currTouch);
                    prevTouch = currTouch;//new Point(currTouch.x,currTouch.y);
                }

                sliderManager.setValueForSlideBetween(prevTouch,lastTouch);

                //System.out.printf("--%d\n",event.getHistorySize());

                if(event.getAction() == MotionEvent.ACTION_MOVE && prevEventLoc != null){
                    sliderManager.setValueForSlideBetween(prevEventLoc, lastTouch);
                }
                prevEventLoc = lastTouch;

                return true;
            }
        });

    }
}
