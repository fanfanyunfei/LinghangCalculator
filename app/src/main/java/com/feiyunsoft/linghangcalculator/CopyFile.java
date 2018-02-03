package com.feiyunsoft.linghangcalculator;

import android.os.Message;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;

/**
 * Created by lusaihua on 2017/6/21.
 */

public class CopyFile {
    public static void copyfile(File fromFile,File toFile,Boolean rewrite){
        if (!fromFile.exists()){
            return;
        }
        if (!fromFile.isFile()){
            return;
        }
        if (!fromFile.canRead()){
            return;
        }
        if (!toFile.getParentFile().exists()){
            toFile.getParentFile().mkdir();
        }
        if (toFile.exists()&& rewrite){
            toFile.delete();
        }
        if (!toFile.canWrite()){
            //Toast.makeText(this,"不能够写入要复制的文件到位置",Toast.LENGTH_SHORT);
            return;
        }
        try {
            java.io.FileInputStream fosfrom = new java.io.FileInputStream(fromFile);
            java.io.FileOutputStream fosto = new FileOutputStream(toFile);
            byte bt[] = new byte[1024];
            int c;
            while ((c = fosfrom.read(bt))>0){
                fosto.write(bt,0,c);
            }
            fosfrom.close();
            fosto.close();
        }catch (Exception ex){
            //Log.e("readfile",ex.getMessage());
        }
    }

}
