package com.secondsight.sample.sbsapi;

import android.os.Bundle;
import android.app.Activity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends Activity {

    private boolean mIsSBSEnabled;
    private Button mBtn;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        mBtn = (Button)this.findViewById(R.id.button1);
        mBtn.setOnClickListener(new OnClickListener(){

            @Override
            public void onClick(View arg0) {
                mIsSBSEnabled = !mIsSBSEnabled;
                updateButton();
            }
            
        });
        updateButton();
    }

    private void updateButton() {
        try {
            setSBSEnable(mIsSBSEnabled);
        } catch(Throwable e) {
            Toast.makeText(this, "Please install second sight ROM", Toast.LENGTH_SHORT).show();
        }
        
        if (mIsSBSEnabled) {
            mBtn.setText("Disable Side by Side");
        } else {
            mBtn.setText("Eable Side by Side");
        }
    }
}
