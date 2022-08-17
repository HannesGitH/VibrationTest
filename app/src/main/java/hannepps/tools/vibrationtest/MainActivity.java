package hannepps.tools.vibrationtest;

import static java.lang.Math.max;
import static java.lang.Math.min;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.MyLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.ToggleButton;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import com.google.android.material.color.DynamicColors;
import com.google.android.material.slider.Slider;


public class MainActivity extends AppCompatActivity{//Application {

    //TextView wavelength_textView;
    //TextView hardness_textView;
    //SeekBar wavelength_slider;
    //SeekBar hardness_slider;

    TextView waveform_textView;
    VerticalSeekBar[] waveform_seekbars;
    MyLayout waveform_container;
    TextView frequency_textView;
    TextView precision_textView;
    Slider frequency_slider;
    Slider precision_slider;

    Button startstop_button;
    //ToggleButton advanced_toggleButton;
    SwitchCompat repeat_switch;
    //boolean isInAdvancedMode =false;

    boolean isCurrentlyVibrating =false;

    Vibrator mVibrator;

    private boolean alreadyset;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //DynamicColors.applyToActivitiesIfAvailable(this);
        setContentView(R.layout.activity_main);
        //wavelength_slider = findViewById(R.id.wl);
        //hardness_slider = findViewById(R.id.hn);
        startstop_button = findViewById(R.id.button);
        repeat_switch = findViewById(R.id.switch1);
        //advanced_toggleButton = findViewById(R.id.toggleButton);
        //wavelength_textView = findViewById(R.id.wltext);
        //hardness_textView = findViewById(R.id.hntext);
        waveform_textView = findViewById(R.id.wftext);
        waveform_container = findViewById(R.id.container);
        precision_slider = findViewById(R.id.pc);
        precision_textView = findViewById(R.id.pctext);
        frequency_slider = findViewById(R.id.fc);
        frequency_textView = findViewById(R.id.fctext);

        //wavelength_slider.setProgress(75);
        //hardness_slider.setProgress(50);
        frequency_slider.setValue(50);

        waveform_seekbars = new VerticalSeekBar[(int)(precision_slider.getValueTo()+1)];
        for(int i = 0; i< waveform_seekbars.length; i++){
            VerticalSeekBar w = new VerticalSeekBar(this);
            w.setValueTo(255);
            //if (Build.VERSION.SDK_INT >= 21) w.setProgressTintList(ColorStateList.valueOf( getResources().getColor(R.color.colorPrimary)));
            w.setLayoutParams(new ViewGroup.LayoutParams(10, ViewGroup.LayoutParams.MATCH_PARENT));
            waveform_seekbars[i]=w;
        }

        //setActionbarTextColor(getSupportActionBar(), Color.WHITE);

        repeat_switch.setChecked(true);

        mVibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        waveform_container.setOnTouchListener(new View.OnTouchListener(){

            class SliderManager{

                final int width = (waveform_container.getWidth()) / waveform_container.getChildCount();

                private Slider sliderAtX(int x){
                    return (Slider) waveform_container.getChildAt((x-50) / width);
                }
                void setValueForTouchAt(Point p){
                    Slider sliderX = sliderAtX(p.x);
                    if (sliderX != null){
                        float newProgress =  ((1 - p.y / (float) waveform_container.getHeight())*sliderX.getValueTo()) ;
                        sliderAtX(p.x).setValue(min(max(newProgress,0),sliderX.getValueTo()));
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

                if(isCurrentlyVibrating){
                    vib();
                }

                return true;
            }
        });

        /*
        advanced_toggleButton.setOnCheckedChangeListener((compoundButton, b) -> {
            isInAdvancedMode =b;

            int  visibilityIfAdvanced = isInAdvancedMode ? View.VISIBLE : View.GONE;
            int nvisibilityIfAdvanced = isInAdvancedMode ? View.GONE : View.VISIBLE;

            hardness_slider.setVisibility(nvisibilityIfAdvanced);
            wavelength_slider.setVisibility(nvisibilityIfAdvanced);
            hardness_textView.setVisibility(nvisibilityIfAdvanced);
            wavelength_textView.setVisibility(nvisibilityIfAdvanced);

            waveform_textView.setVisibility(visibilityIfAdvanced);
            waveform_container.setVisibility(visibilityIfAdvanced);
            precision_slider.setVisibility(visibilityIfAdvanced);
            precision_textView.setVisibility(visibilityIfAdvanced);
            frequency_slider.setVisibility(visibilityIfAdvanced);
            frequency_textView.setVisibility(visibilityIfAdvanced);

            //weird workaround
            if(!alreadyset) {
                final Handler handler = new Handler(Looper.getMainLooper());
                handler.postDelayed(() -> {
                    precision_slider.setProgress(15);
                    setPrecision(15);
                }, 100);
                alreadyset=true;
            }
        });
        */

        startstop_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isCurrentlyVibrating) {
                    MainActivity.this.vib();
                    if (repeat_switch.isChecked()) {
                        startstop_button.setText(R.string.StopVibratingButtonText);
                        isCurrentlyVibrating = true;
                    }
                } else {
                    mVibrator.cancel();
                    startstop_button.setText(R.string.StartVibratingButtonText);
                    isCurrentlyVibrating = false;
                }
            }
        });
        precision_slider.addOnChangeListener((slider, value, fromUser) -> {
            setPrecision(max((int)value,2));
            if(isCurrentlyVibrating){
                vib();
            }
        });
        frequency_slider.addOnChangeListener((slider, value, fromUser) -> {
                if(isCurrentlyVibrating){
                    vib();
                }
            }
        );
        /*wavelength_slider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(isCurrentlyVibrating){
                    vib();
                }
            }
            public void onStartTrackingTouch(SeekBar seekBar) { }
            public void onStopTrackingTouch(SeekBar seekBar) { }
        });*/

    }

    private void setActionbarTextColor(ActionBar actBar, int color) {

        String title = actBar.getTitle().toString();
        Spannable spannablerTitle = new SpannableString(title);
        spannablerTitle.setSpan(new ForegroundColorSpan(color), 0, spannablerTitle.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        actBar.setTitle(spannablerTitle);

    }

    void vib(){
        boolean repeat= repeat_switch.isChecked();
        /*if(!isInAdvancedMode || oldPrecision<=1){
            int hardness = hardness_slider.getProgress();
            long wave = (wavelength_slider.getProgress()^2)+1;
            vibrate(new int[]{hardness,0},new long[]{wave,wave},repeat);
        }else{*/
            int len = oldPrecision;
            long[] wls = new long[len];
            int [] hns = new int [len];
            long stepsize=((int)(frequency_slider.getValueTo()- frequency_slider.getValue())^3)* 10L /(len+1)+2;
            for(int i=0;i<len-1;i++){
                wls[i]= stepsize;
                int progress_i = (int) waveform_seekbars[i].getValue();
                hns[i]= progress_i < 0 || progress_i > 255 ? 0 : progress_i;
            }
            vibrate(hns,wls,repeat);
        //}
    }


    void vibrate(int[] hn,long[] wave,boolean repeat){
        int i=repeat?0:-1;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // API 26 and above
            mVibrator.vibrate(VibrationEffect.createWaveform(wave,hn, i));
        } else {
            // Below API 26
            mVibrator.vibrate(wave, i);
        }
    }

    private int oldPrecision=1;

    void setPrecision(int p){


        p= max(1,p);

        int i = 0;
        for(VerticalSeekBar w: waveform_seekbars){
            ViewGroup.LayoutParams lp = w.getLayoutParams();
            float width = (waveform_container.getWidth()) / ((float)waveform_container.getChildCount()+0.00002f);
            //lp.height = 300;
            //float width = (waveform_container.getWidth()-100)/((float)p);

            w.setX((i+++0.5f)*width);
            //lp.width = 300;
            w.requestLayout();
        }

        while(p!= oldPrecision){
            VerticalSeekBar w = waveform_seekbars[oldPrecision-1];
            if (p<oldPrecision){
                try{
                    waveform_container.removeView(w);
                }catch(Exception ignored){}
                oldPrecision--;
            }else{
                try{
                    waveform_container.addView(w);
                }catch(Exception ignored){}
                oldPrecision++;
            }
        }
    }

}
