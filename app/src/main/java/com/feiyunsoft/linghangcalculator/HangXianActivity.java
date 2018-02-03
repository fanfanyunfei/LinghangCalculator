package com.feiyunsoft.linghangcalculator;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import java.util.ArrayList;
import java.util.HashMap;

import static com.feiyunsoft.linghangcalculator.MainActivity.DATABASE_NAME;
import static com.feiyunsoft.linghangcalculator.MainActivity.DATABASE_PATH;

public class HangXianActivity extends AppCompatActivity {


    private SQLiteDatabase sqLiteDatabase;
    private ListView hangxianlistView;
    private String jichang_id;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.hang_xian_layout);

        Intent i = getIntent();
        Bundle b = i.getBundleExtra("jichangbd");
        jichang_id = b.getString("jichang_ID");

        //Intent intentresult = new Intent(HangXianActivity.this,MainActivity.class);
        //setResult(RESULT_OK,intentresult);

        Button xinzengButton = (Button) findViewById(R.id.add_button);
        hangxianlistView = (ListView) findViewById(R.id.hangxian_listview);

        /*String DATABASE_PATH = getFilesDir() + "/databases";
        String DATABASE_NAME = "shuju.db";*/
        String f = DATABASE_PATH + "/" + DATABASE_NAME;
        sqLiteDatabase = openOrCreateDatabase(f, MODE_PRIVATE, null);

        tianchonglistview();
        itemLongClick_jichang();
        itemClick_jichang();
        xinzengButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                xinzenghangxian();
            }
        });

    }


    private void itemClick_jichang() {
        hangxianlistView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                HashMap<String, String> hangxianmap = (HashMap<String, String>) hangxianlistView.getItemAtPosition(position);
                final String hangxian_name = hangxianmap.get("hangxianname");
                final String hangxian_id = hangxianmap.get("hangxian_ID");
                Intent intent = new Intent(getApplicationContext(), HangDianActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("hangxian_ID", hangxian_id);
                intent.putExtra("hangxianbd", bundle);
                startActivity(intent);
            }
        });
    }

    private void itemLongClick_jichang() {//对机场列表长按响应的方法
        hangxianlistView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                HashMap<String, String> jichangmap = (HashMap<String, String>) hangxianlistView.getItemAtPosition(position);
                final String hangxian_name = jichangmap.get("hangxianname");
                final String hangxian_id = jichangmap.get("hangxian_ID");


                new AlertDialog.Builder(HangXianActivity.this)
                        .setTitle("请选择：")
                        .setItems(R.array.bianji,
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        String[] PK = getResources().getStringArray(R.array.bianji);
                                        //Toast.makeText(JiChangGuanliActivity.this,PK[which],Toast.LENGTH_LONG).show();
                                        switch (PK[which]) {
                                            case "删除":
                                                AlertDialog.Builder build = new AlertDialog.Builder(HangXianActivity.this);
                                                build.setTitle("注意")
                                                        .setMessage("确定要删除吗？")
                                                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                                            @Override
                                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                                shanchu(hangxian_id);
                                                            }
                                                        })
                                                        .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                                                            @Override
                                                            public void onClick(DialogInterface dialogInterface, int i) {

                                                            }
                                                        })
                                                        .show();


                                                break;
                                            case "编辑":
                                                bianjijichang(hangxian_id, hangxian_name);
                                                break;
                                            default:
                                                break;
                                        }
                                    }
                                })
                        .setNegativeButton("取消",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {

                                    }
                                }).show();
                return true;
            }
        });
    }

    private void shanchu(String hangxian_id) {

        sqLiteDatabase.execSQL("update HangXian set used = '0' where hangxian_ID = '" + hangxian_id + "'");
        tianchonglistview();
    }

    private void xinzenghangxian() {
        LayoutInflater factory = LayoutInflater.from(this);
        @SuppressLint("InflateParams") View textEntryView = factory.inflate(R.layout.hangxiandetail, null);
        final EditText hangxiannamedetailEditText = (EditText) textEntryView.findViewById(R.id.hangxianname_detail_edittext);

        new AlertDialog.Builder(this)
                .setTitle("航线名称：")
                .setView(textEntryView)
                .setPositiveButton("确定",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String hangxianname_insert = hangxiannamedetailEditText.getText().toString();
                                ContentValues values = new ContentValues();
                                values.put("hangxia_NAME", hangxianname_insert);
                                values.put("jichang_ID", jichang_id);
                                values.put("used", 1);
                                sqLiteDatabase.insert("HangXian", "hangxia_NAME", values);
                                tianchonglistview();
                            }
                        })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).show();
    }

    private void bianjijichang(String hangxian_id, String hangxian_name) {
        LayoutInflater factory = LayoutInflater.from(this);
        @SuppressLint("InflateParams") View textEntryView = factory.inflate(R.layout.hangxiandetail, null);
        final EditText hangxiannamedetailEditText = (EditText) textEntryView.findViewById(R.id.hangxianname_detail_edittext);

        hangxiannamedetailEditText.setText(hangxian_name);

        final int id = Integer.parseInt(hangxian_id);
        new AlertDialog.Builder(this)
                .setTitle("航线名称：")
                .setView(textEntryView)
                .setPositiveButton("确定",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String hangxianname_insert = hangxiannamedetailEditText.getText().toString();

                                sqLiteDatabase.execSQL("update HangXian set hangxia_NAME = '" + hangxianname_insert + "' where hangxian_ID = " + id);
                                tianchonglistview();
                            }
                        })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).show();
    }

    private void tianchonglistview() {
        @SuppressLint("Recycle") Cursor cursor = sqLiteDatabase.rawQuery("select * from HangXian where used = '1' and jichang_ID = '" + jichang_id + "'", null);
        ArrayList<HashMap<String, String>> hangxianlist = new ArrayList<>();
        //jichanglist.clear();
        while (cursor.moveToNext()) {
            String hangxian_ID = cursor.getString(cursor.getColumnIndex("hangxian_ID"));
            String hangxianName = cursor.getString(cursor.getColumnIndex("hangxia_NAME"));

            HashMap<String, String> jichangmap = new HashMap<>();
            jichangmap.put("hangxian_ID", hangxian_ID);
            jichangmap.put("hangxianname", hangxianName);
            hangxianlist.add(jichangmap);
        }

        SimpleAdapter hangxiansimpleAdapter = new SimpleAdapter(this,
                hangxianlist, R.layout.hangxianlist,
                new String[]{"hangxian_ID", "hangxianname"},
                new int[]{R.id.hangxian_id_textview, R.id.hangxian_textview});
        hangxianlistView.setAdapter(hangxiansimpleAdapter);
    }

}
