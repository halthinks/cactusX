package com.halthinks.cactusx;

import android.Manifest;
import android.app.Activity;
import android.os.Bundle;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.net.Uri;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.EditText;
import android.widget.Toast;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends Activity {
    static { System.loadLibrary("cactusx"); }
    private native String loadModel(byte[] model);
    private native String initNeedle();
    private native String complete(String query);
    private native String transcribe(float[] samples);
    private native String describe(byte[] rgb, int width, int height);

    private final ExecutorService worker = Executors.newSingleThreadExecutor();
    private TextView status;
    private EditText query;
    private volatile boolean ready = false;
    private static final int PICK_IMAGE=100;
    private static final int MIC_PERMISSION=101;

    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        LinearLayout content=new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(24,24,24,24);
        ScrollView page=new ScrollView(this);
        page.addView(content);
        setContentView(page);
        TextView title=new TextView(this);
        title.setText("CactusX S26 • Offline AI");
        title.setTextSize(23);
        content.addView(title);
        status=new TextView(this);
        status.setText("Loading three local models…");
        status.setTextIsSelectable(true);
        content.addView(status);
        query=new EditText(this);
        query.setHint("Try: What time is it?");
        content.addView(query);
        addButton(content,"Needle: route command", () -> execute(() -> complete(query.getText().toString())));
        addButton(content,"Whistle: record 3 seconds", this::startRecording);
        addButton(content,"Mirage: select photo", () -> {
            if(!ready) { show("Models still loading"); return; }
            Intent intent=new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            startActivityForResult(intent,PICK_IMAGE);
        });
        worker.execute(() -> {
            try {
                StringBuilder sb=new StringBuilder();
                for(String name:new String[]{"needle3","whistle","mirage"}) {
                    byte[] data=readAll(getAssets().open(name+".cact"));
                    String outcome=loadModel(data);
                    sb.append(name).append(": ").append(outcome).append("\n");
                    update(sb.toString());
                    if(!"loaded".equals(outcome)) return;
                }
                sb.append("Needle init: ").append(initNeedle());
                ready=true;
                update(sb.toString()+"\nAll models ready.");
            } catch(Exception ex) { update("Model loading failed: "+ex); }
        });
    }
    private void addButton(LinearLayout parent,String label,Runnable action) {
        Button button=new Button(this);
        button.setText(label);
        button.setOnClickListener(v -> action.run());
        parent.addView(button);
    }
    private void execute(java.util.concurrent.Callable<String> fn) {
        if(!ready) { show("Models still loading"); return; }
        update("Processing locally…");
        worker.execute(() -> {
            try { update(fn.call()); }
            catch(Exception ex) { update("Error: "+ex); }
        });
    }
    private void startRecording() {
        if(checkSelfPermission(Manifest.permission.RECORD_AUDIO)!=PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO},MIC_PERMISSION);
            return;
        }
        execute(() -> {
            final int rate=16000;
            int min=AudioRecord.getMinBufferSize(rate,AudioFormat.CHANNEL_IN_MONO,AudioFormat.ENCODING_PCM_16BIT);
            if(min<=0) return "Microphone unavailable";
            AudioRecord recorder=new AudioRecord(MediaRecorder.AudioSource.MIC,rate,AudioFormat.CHANNEL_IN_MONO,AudioFormat.ENCODING_PCM_16BIT,Math.max(min,4096));
            if(recorder.getState()!=AudioRecord.STATE_INITIALIZED) { recorder.release(); return "Microphone initialization failed"; }
            short[] buffer=new short[rate*3];
            int filled=0;
            try {
                recorder.startRecording();
                while(filled<buffer.length) {
                    int n=recorder.read(buffer,filled,Math.min(2048,buffer.length-filled));
                    if(n<=0) break;
                    filled+=n;
                }
            } finally { recorder.stop(); recorder.release(); }
            float[] pcm=new float[filled];
            for(int i=0;i<filled;i++) pcm[i]=buffer[i]/32768f;
            return transcribe(pcm);
        });
    }
    @Override protected void onActivityResult(int request,int result,Intent data) {
        super.onActivityResult(request,result,data);
        if(request!=PICK_IMAGE || result!=RESULT_OK || data==null) return;
        Uri uri=data.getData();
        execute(() -> {
            try(InputStream stream=getContentResolver().openInputStream(uri)) {
                Bitmap bitmap=BitmapFactory.decodeStream(stream);
                if(bitmap==null) return "Image decode failed";
                // Bound image size: Mirage consumes a compact pixel grid.
                Bitmap resized=Bitmap.createScaledBitmap(bitmap,256,256,true);
                int[] pixels=new int[256*256];
                resized.getPixels(pixels,0,256,0,0,256,256);
                byte[] rgb=new byte[pixels.length*3];
                for(int i=0;i<pixels.length;i++) {
                    rgb[i*3]=(byte)(pixels[i]>>16);
                    rgb[i*3+1]=(byte)(pixels[i]>>8);
                    rgb[i*3+2]=(byte)pixels[i];
                }
                if(resized!=bitmap) resized.recycle();
                bitmap.recycle();
                return describe(rgb,256,256);
            }
        });
    }
    private static byte[] readAll(InputStream stream) throws Exception {
        try(InputStream in=stream; ByteArrayOutputStream out=new ByteArrayOutputStream()) {
            byte[] buffer=new byte[65536]; int n;
            while((n=in.read(buffer))!=-1) out.write(buffer,0,n);
            return out.toByteArray();
        }
    }
    private void update(String value) { runOnUiThread(() -> status.setText(value)); }
    private void show(String value) { Toast.makeText(this,value,Toast.LENGTH_SHORT).show(); }
    @Override protected void onDestroy() { worker.shutdown(); super.onDestroy(); }
}
