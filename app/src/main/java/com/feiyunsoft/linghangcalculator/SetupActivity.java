package com.feiyunsoft.linghangcalculator;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class SetupActivity extends AppCompatActivity {

    private Button mHangdianguanli_button;



    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.setupactiviy_layout);

        TextView mVirsion_textview = (TextView) this.findViewById(R.id.virsion_textview);
        mHangdianguanli_button = (Button) this.findViewById(R.id.hangdianguanli_button);


        mVirsion_textview.setText("当前版本："+versionString());
        setlistener();



    }

    /**
     * 获取版本号
     * @return 当前应用的版本号
     */
    @NonNull
    private String versionString() {
        try {
            PackageManager manager = this.getPackageManager();
            PackageInfo info = manager.getPackageInfo(this.getPackageName(), 0);
            String version = info.versionName;
            return this.getString(R.string.version_name) + version;
        } catch (Exception e) {
            e.printStackTrace();
            return this.getString(R.string.can_not_find_version_name);
        }
    }

    private void setlistener(){
        mHangdianguanli_button.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){

                Intent intent = new Intent(SetupActivity.this,JiChangGuanliActivity.class);
                startActivityForResult(intent,1);
            }
        });



    }
}
