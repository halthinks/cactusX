package com.halthinks.cactusx.smoke;

import android.Manifest;
import android.app.Activity;
import android.os.Bundle;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public class SmokeActivity extends Activity {
    private TextView status;
    private EditText input;
    private static final int PICK_IMAGE=100, MIC_PERMISSION=101;
    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        LinearLayout content=new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(24,24,24,24);
        ScrollView page=new ScrollView(this);
        page.addView(content);
        setContentView(page);
        TextView title=new TextView(this);
        title.setText("CactusX S26 • UI-only smoke test");
        title.setTextSize(23);
        content.addView(title);
        status=new TextView(this);
        status.setText("UI-only: no model or native engine is loaded.");
        content.addView(status);
        input=new EditText(this);
        input.setHint("Try: What time is it?");
        content.addView(input);
        addButton(content,"Needle: route command", () -> status.setText("UI route invoked: "+input.getText()+" (NO INFERENCE)"));
        addButton(content,"Whistle: record 3 seconds", () -> {
            if(checkSelfPermission(Manifest.permission.RECORD_AUDIO)!=PackageManager.PERMISSION_GRANTED)
                requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO},MIC_PERMISSION);
            else status.setText("Microphone permission granted (NO TRANSCRIPTION)");
        });
        addButton(content,"Mirage: select photo", () -> {
            Intent intent=new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            startActivityForResult(intent,PICK_IMAGE);
        });
    }
    private void addButton(LinearLayout parent,String label,Runnable action) {
        Button button=new Button(this);
        button.setText(label);
        button.setOnClickListener(v -> action.run());
        parent.addView(button);
    }
    @Override public void onRequestPermissionsResult(int request,String[] permissions,int[] results) {
        super.onRequestPermissionsResult(request,permissions,results);
        if(request==MIC_PERMISSION) status.setText(results.length>0&&results[0]==PackageManager.PERMISSION_GRANTED?
          "Microphone permission granted (NO TRANSCRIPTION)":"Microphone permission denied");
    }
    @Override protected void onActivityResult(int request,int result,Intent data) {
        super.onActivityResult(request,result,data);
        if(request==PICK_IMAGE) status.setText(result==RESULT_OK&&data!=null&&data.getData()!=null?
          "Photo selected (NO VISION INFERENCE)":"Photo picker cancelled");
    }
}
