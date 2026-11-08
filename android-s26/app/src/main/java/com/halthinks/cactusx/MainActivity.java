package com.halthinks.cactusx;

import android.Manifest;
import android.app.Activity;
import android.os.Bundle;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.net.Uri;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.content.Context;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import org.json.JSONObject;
import org.json.JSONArray;
import android.speech.tts.TextToSpeech;
import java.util.Locale;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends Activity {
    static { System.loadLibrary("cactusx"); }
    private native String loadModel(byte[] model);
    private native String initNeedle();
    private native String complete(String query);
    private native String transcribe(float[] samples);
    private native String describe(byte[] rgb, int width, int height);

    private static final int BG=0xff080f1b, PANEL=0xff121e2e, SURFACE=0xff1b2c3f;
    private static final int TEXT=0xfff3f7fc, MUTED=0xff9fb0c6, TEAL=0xff52e4ce;
    private static final int PICK_IMAGE=100, MIC_PERMISSION=101;
    private final ExecutorService worker=Executors.newSingleThreadExecutor();
    private LinearLayout messages;
    private ScrollView transcript;
    private EditText composer;
    private TextView state;
    private volatile boolean ready=false;
    private volatile boolean busy=false;
    private int messageCount=0;
    private TextToSpeech speaker;
    private boolean speakEnabled=false;
    private boolean historyEnabled=false;
    private Button speakToggle, historyToggle;

    @Override public void onCreate(Bundle saved) {
        super.onCreate(saved);
        getWindow().setStatusBarColor(BG);
        getWindow().setNavigationBarColor(BG);
        historyEnabled=getPreferences(MODE_PRIVATE).getBoolean("history_enabled",false);
        drawInterface();
        restoreHistory();
        worker.execute(this::loadModels);
    }

    private int dp(float d) { return Math.round(getResources().getDisplayMetrics().density*d); }
    private GradientDrawable fill(int color,int radius) {
        GradientDrawable d=new GradientDrawable();
        d.setColor(color); d.setCornerRadius(dp(radius)); return d;
    }
    private TextView label(String value,int size,int color,boolean bold) {
        TextView t=new TextView(this); t.setText(value); t.setTextSize(size); t.setTextColor(color);
        if(bold) t.setTypeface(Typeface.DEFAULT,Typeface.BOLD);
        return t;
    }
    private LinearLayout vertical() {
        LinearLayout l=new LinearLayout(this); l.setOrientation(LinearLayout.VERTICAL); return l;
    }
    private LinearLayout horizontal() {
        LinearLayout l=new LinearLayout(this); l.setOrientation(LinearLayout.HORIZONTAL); l.setGravity(Gravity.CENTER_VERTICAL); return l;
    }
    private LinearLayout.LayoutParams space(int w,int h,int top) {
        LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(w,h);
        p.topMargin=dp(top); return p;
    }
    private Button action(String text,int bg) {
        Button b=new Button(this);
        b.setText(text); b.setTextColor(TEXT); b.setAllCaps(false); b.setTextSize(14);
        b.setPadding(dp(8),dp(4),dp(8),dp(4)); b.setBackground(fill(bg,18));
        return b;
    }
    private void drawInterface() {
        LinearLayout root=vertical(); root.setBackgroundColor(BG);
        root.setPadding(dp(18),dp(16),dp(18),dp(12)); setContentView(root);
        LinearLayout header=horizontal();
        TextView mark=label("✦",27,TEAL,true);
        mark.setGravity(Gravity.CENTER); mark.setBackground(fill(SURFACE,14));
        header.addView(mark,new LinearLayout.LayoutParams(dp(46),dp(46)));
        LinearLayout heading=vertical(); heading.setPadding(dp(12),0,0,0);
        heading.addView(label("CactusX",24,TEXT,true));
        heading.addView(label("PRIVATE  •  ON-DEVICE ASSISTANT",10,TEAL,true));
        header.addView(heading,new LinearLayout.LayoutParams(0,-2,1));
        TextView offline=label("● LOCAL",11,TEAL,true);
        offline.setPadding(dp(10),dp(8),dp(10),dp(8)); offline.setBackground(fill(SURFACE,20));
        header.addView(offline);
        root.addView(header);

        LinearLayout models=horizontal();
        models.setPadding(dp(12),dp(11),dp(12),dp(11)); models.setBackground(fill(PANEL,18));
        for(String name:new String[]{"NEEDLE 3","WHISTLE","MIRAGE"}) {
            TextView chip=label("● "+name,10,TEAL,true); chip.setGravity(Gravity.CENTER);
            models.addView(chip,new LinearLayout.LayoutParams(0,dp(20),1));
        }
        root.addView(models,space(-1,-2,18));

        LinearLayout settings=horizontal();
        speakToggle=action("🔈 Voice off",SURFACE);
        historyToggle=action(historyEnabled?"History on":"History off",SURFACE);
        Button clear=action("Clear",SURFACE);
        settings.addView(speakToggle,new LinearLayout.LayoutParams(0,dp(42),1));
        LinearLayout.LayoutParams hp=new LinearLayout.LayoutParams(0,dp(42),1);hp.leftMargin=dp(7);
        settings.addView(historyToggle,hp);
        LinearLayout.LayoutParams cp=new LinearLayout.LayoutParams(0,dp(42),1);cp.leftMargin=dp(7);
        settings.addView(clear,cp);
        root.addView(settings,space(-1,-2,9));
        speakToggle.setOnClickListener(v -> toggleSpeech());
        historyToggle.setOnClickListener(v -> {
            historyEnabled=!historyEnabled;
            getPreferences(MODE_PRIVATE).edit().putBoolean("history_enabled",historyEnabled).apply();
            historyToggle.setText(historyEnabled?"History on":"History off");
            if(!historyEnabled) getPreferences(MODE_PRIVATE).edit().remove("chat_history").apply();
        });
        clear.setOnClickListener(v -> {
            getPreferences(MODE_PRIVATE).edit().remove("chat_history").apply();
            messages.removeAllViews();messageCount=0;addWelcome();
            Toast.makeText(this,"Local conversation cleared",Toast.LENGTH_SHORT).show();
        });
        state=label("Starting offline engine…",12,MUTED,false);
        state.setPadding(dp(4),dp(12),dp(4),dp(8));
        root.addView(state);

        transcript=new ScrollView(this); transcript.setFillViewport(true);
        messages=vertical(); messages.setPadding(dp(3),dp(14),dp(3),dp(18));
        transcript.addView(messages);
        root.addView(transcript,new LinearLayout.LayoutParams(-1,0,1));

        addWelcome();

        LinearLayout composerCard=vertical(); composerCard.setPadding(dp(12),dp(12),dp(12),dp(12));
        composerCard.setBackground(fill(PANEL,22));
        composer=new EditText(this);
        composer.setSingleLine(false); composer.setMaxLines(4);
        composer.setTextSize(15); composer.setTextColor(TEXT); composer.setHintTextColor(MUTED);
        composer.setHint("Ask, describe, or request an action…");
        composer.setBackgroundColor(Color.TRANSPARENT);
        composerCard.addView(composer,new LinearLayout.LayoutParams(-1,dp(58)));

        LinearLayout toolbar=horizontal();
        Button photo=action("＋ Photo",SURFACE);
        Button mic=action("◉ Voice",SURFACE);
        Button send=action("Send  ➤",0xff126e70);
        toolbar.addView(photo,new LinearLayout.LayoutParams(0,dp(48),1));
        LinearLayout.LayoutParams mp=new LinearLayout.LayoutParams(0,dp(48),1);mp.leftMargin=dp(8);
        toolbar.addView(mic,mp);
        LinearLayout.LayoutParams sp=new LinearLayout.LayoutParams(0,dp(48),1);sp.leftMargin=dp(8);
        toolbar.addView(send,sp);
        composerCard.addView(toolbar);
        root.addView(composerCard);
        TextView footer=label("Offline by default  •  Actions require your approval",11,MUTED,false);
        footer.setGravity(Gravity.CENTER); footer.setPadding(0,dp(12),0,dp(1));
        root.addView(footer);
        send.setOnClickListener(v -> sendText());
        photo.setOnClickListener(v -> choosePhoto());
        mic.setOnClickListener(v -> startRecording());
    }
    private void addWelcome() {
        LinearLayout card=vertical(); card.setPadding(dp(20),dp(20),dp(20),dp(20));
        card.setBackground(fill(PANEL,22));
        card.addView(label("Your pocket AI, entirely yours.",22,TEXT,true));
        TextView sub=label("Ask a question, speak naturally, or show me something. Needle routes requests, Whistle hears speech, and Mirage interprets images.",14,MUTED,false);
        sub.setPadding(0,dp(10),0,dp(14));card.addView(sub);
        card.addView(label("NO CLOUD REQUIRED   •   YOU CONTROL ACTIONS",10,TEAL,true));
        messages.addView(card);
    }
    private void bubble(String value,boolean user) {
        runOnUiThread(() -> {
            TextView message=label(value,15,TEXT,false);
            message.setTextIsSelectable(true);
            message.setPadding(dp(15),dp(12),dp(15),dp(12));
            message.setBackground(fill(user?0xff175a61:SURFACE,18));
            LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-2,-2);
            p.gravity=user?Gravity.RIGHT:Gravity.LEFT;p.topMargin=dp(14);
            messages.addView(message,p); messageCount++;
            persistMessage(value,user);
            if(!user && speakEnabled && speaker!=null && value.length()<1000)
                speaker.speak(value,TextToSpeech.QUEUE_FLUSH,null,"cactusx-response");
            transcript.post(() -> transcript.fullScroll(View.FOCUS_DOWN));
        });
    }
    private void toggleSpeech() {
        speakEnabled=!speakEnabled;
        speakToggle.setText(speakEnabled?"🔊 Voice on":"🔈 Voice off");
        if(speakEnabled && speaker==null) {
            speaker=new TextToSpeech(this,status -> {
                if(status==TextToSpeech.SUCCESS) speaker.setLanguage(Locale.getDefault());
                else runOnUiThread(() -> { speakEnabled=false;speakToggle.setText("Voice unavailable"); });
            });
        }
        if(!speakEnabled && speaker!=null) speaker.stop();
    }
    private void persistMessage(String text,boolean user) {
        if(!historyEnabled)return;
        try {
            String prior=getPreferences(MODE_PRIVATE).getString("chat_history","[]");
            JSONArray arr=new JSONArray(prior);
            JSONObject event=new JSONObject();
            event.put("user",user);event.put("text",text);
            arr.put(event);
            // Bounded local history, never copied to a server.
            JSONArray bounded=new JSONArray();
            for(int i=Math.max(0,arr.length()-80);i<arr.length();i++)bounded.put(arr.getJSONObject(i));
            getPreferences(MODE_PRIVATE).edit().putString("chat_history",bounded.toString()).apply();
        } catch(Exception ignored) { }
    }
    private void restoreHistory() {
        if(!historyEnabled)return;
        try {
            JSONArray events=new JSONArray(getPreferences(MODE_PRIVATE).getString("chat_history","[]"));
            for(int i=0;i<events.length();i++) {
                JSONObject e=events.getJSONObject(i);
                TextView message=label(e.optString("text"),15,TEXT,false);
                message.setTextIsSelectable(true);message.setPadding(dp(15),dp(12),dp(15),dp(12));
                boolean user=e.optBoolean("user",false);
                message.setBackground(fill(user?0xff175a61:SURFACE,18));
                LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-2,-2);
                p.gravity=user?Gravity.RIGHT:Gravity.LEFT;p.topMargin=dp(14);
                messages.addView(message,p);messageCount++;
            }
        } catch(Exception ignored) { getPreferences(MODE_PRIVATE).edit().remove("chat_history").apply(); }
    }
    private void setStatus(String text) { runOnUiThread(() -> state.setText(text)); }
    private void loadModels() {
        try {
            for(String name:new String[]{"needle3","whistle","mirage"}) {
                setStatus("Loading "+name+"…");
                String result=loadModel(readAll(getAssets().open(name+".cact")));
                if(!"loaded".equals(result)) { setStatus("Engine unavailable: "+result); return; }
            }
            String initialized=initNeedle();
            if(!"ready".equals(initialized)) { setStatus("Needle initialization failed: "+initialized); return; }
            ready=true;setStatus("● All three models loaded  •  On-device");
        } catch(Exception ex) { setStatus("Could not start offline engine: "+ex.getMessage()); }
    }
    private void sendText() {
        String prompt=composer.getText().toString().trim();
        if(prompt.isEmpty()) return;
        if(!ready || busy) { Toast.makeText(this,ready?"Please wait":"Models still loading",Toast.LENGTH_SHORT).show();return; }
        composer.setText("");
        ((InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(composer.getWindowToken(),0);
        bubble(prompt,true);
        execute("Needle is interpreting your request…",()->complete(prompt));
    }
    private void execute(String progress,Callable<String> call) {
        if(!ready||busy) { Toast.makeText(this,"Engine busy or not ready",Toast.LENGTH_SHORT).show();return; }
        busy=true; setStatus(progress);
        worker.execute(() -> {
            try { bubble(call.call(),false); }
            catch(Exception ex) { bubble("Unable to complete: "+ex.getMessage(),false); }
            finally { busy=false;setStatus("● Ready  •  On-device"); }
        });
    }
    private void choosePhoto() {
        if(!ready || busy) { Toast.makeText(this,"Engine busy or not ready",Toast.LENGTH_SHORT).show();return; }
        Intent i=new Intent(Intent.ACTION_GET_CONTENT);
        i.setType("image/*");i.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(i,PICK_IMAGE);
    }
    private void startRecording() {
        if(checkSelfPermission(Manifest.permission.RECORD_AUDIO)!=PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO},MIC_PERMISSION); return;
        }
        bubble("🎙 Voice note · 3 seconds",true);
        execute("Whistle is listening…",()-> {
            int rate=16000;
            int min=AudioRecord.getMinBufferSize(rate,AudioFormat.CHANNEL_IN_MONO,AudioFormat.ENCODING_PCM_16BIT);
            if(min<=0) return "Microphone unavailable";
            AudioRecord recorder=new AudioRecord(MediaRecorder.AudioSource.MIC,rate,AudioFormat.CHANNEL_IN_MONO,AudioFormat.ENCODING_PCM_16BIT,Math.max(min,4096));
            if(recorder.getState()!=AudioRecord.STATE_INITIALIZED){recorder.release();return "Microphone initialization failed";}
            short[] shorts=new short[rate*3];int filled=0;
            try {
                recorder.startRecording();
                while(filled<shorts.length) {
                    int n=recorder.read(shorts,filled,Math.min(2048,shorts.length-filled));
                    if(n<=0)break;filled+=n;
                }
            } finally { if(recorder.getRecordingState()==AudioRecord.RECORDSTATE_RECORDING)recorder.stop();recorder.release(); }
            if(filled==0)return "No microphone samples received";
            float[] samples=new float[filled];
            for(int i=0;i<filled;i++)samples[i]=shorts[i]/32768f;
            String result=transcribe(samples);
            // The transcript stays visible. Model-proposed tools are never executed automatically.
            String text=result;
            try { JSONObject decoded=new JSONObject(result); text=decoded.optString("text",result); } catch(Exception ignored) {}
            if(text.trim().isEmpty()) return "Whistle detected no speech";
            String routed=complete(text);
            return "Heard: "+text+"\n\nNeedle result:\n"+routed;
        });
    }
    @Override public void onRequestPermissionsResult(int request,String[] permissions,int[] results) {
        super.onRequestPermissionsResult(request,permissions,results);
        if(request==MIC_PERMISSION) {
            if(results.length>0 && results[0]==PackageManager.PERMISSION_GRANTED) startRecording();
            else bubble("Microphone permission denied. Voice input remains disabled.",false);
        }
    }
    @Override protected void onActivityResult(int request,int result,Intent data) {
        super.onActivityResult(request,result,data);
        if(request!=PICK_IMAGE||result!=RESULT_OK||data==null||data.getData()==null)return;
        Uri uri=data.getData();
        bubble("▧ Photo attached",true);
        execute("Mirage is examining the image…",()-> {
            try(InputStream stream=getContentResolver().openInputStream(uri)) {
                Bitmap bitmap=BitmapFactory.decodeStream(stream);
                if(bitmap==null)return "Unable to decode selected image";
                Bitmap resized=Bitmap.createScaledBitmap(bitmap,256,256,true);
                int[] pixels=new int[256*256];resized.getPixels(pixels,0,256,0,0,256,256);
                byte[] rgb=new byte[pixels.length*3];
                for(int i=0;i<pixels.length;i++){
                    rgb[i*3]=(byte)(pixels[i]>>16);
                    rgb[i*3+1]=(byte)(pixels[i]>>8);
                    rgb[i*3+2]=(byte)pixels[i];
                }
                if(resized!=bitmap)resized.recycle();bitmap.recycle();
                return describe(rgb,256,256);
            }
        });
    }
    private static byte[] readAll(InputStream stream)throws Exception {
        try(InputStream in=stream;ByteArrayOutputStream out=new ByteArrayOutputStream()){
            byte[] bytes=new byte[65536];int n;
            while((n=in.read(bytes))!=-1)out.write(bytes,0,n);
            return out.toByteArray();
        }
    }
    @Override protected void onDestroy(){
        worker.shutdown();
        if(speaker!=null){speaker.stop();speaker.shutdown();speaker=null;}
        super.onDestroy();
    }
}
