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
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import static com.feiyunsoft.linghangcalculator.MainActivity.DATABASE_NAME;
import static com.feiyunsoft.linghangcalculator.MainActivity.DATABASE_PATH;

public class HangDianActivity extends AppCompatActivity {

    private SQLiteDatabase sqLiteDatabase;
    private ListView hangdianlistView;
    private String hangxian_id;
    private String fangxiang;
/*    private static String DATABASE_PATH;
    private static String DATABASE_NAME;*/



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.hang_dian_layout);

        Intent i = getIntent();
        Bundle b = i.getBundleExtra("hangxianbd");
        hangxian_id = b.getString("hangxian_ID");

        Button xinzengButton = (Button) findViewById(R.id.add_button);
        hangdianlistView = (ListView) findViewById(R.id.hangdian_listview);
        TextView zhunshi1 = (TextView) findViewById(R.id.zhunshi1);
        TextView zhunshi2 = (TextView) findViewById(R.id.zhunshi2);

        DATABASE_PATH = getFilesDir() + "/databases";
        DATABASE_NAME = "shuju.db";
        String f = DATABASE_PATH + "/" + DATABASE_NAME;
        sqLiteDatabase = openOrCreateDatabase(f, MODE_PRIVATE, null);

        fangxiang = qifeifangxiang();
        if (fangxiang.contains("东")) {
            zhunshi1.setText("向东准时");
            zhunshi2.setText("向西准时");
        } else {
            zhunshi1.setText("向南准时");
            zhunshi2.setText("向北准时");
        }

        tianchonglistview();
        itemLongClick_hangdian();
        itemClick_hangdian();

        xinzengButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                xinzenghangdian();
            }
        });

    }

    private void itemClick_hangdian() {
        hangdianlistView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final HashMap<String, String> hangdianmap = (HashMap<String, String>) hangdianlistView.getItemAtPosition(position);
                bianjihangdian(hangdianmap);

            }
        });
    }

    private void itemLongClick_hangdian() {//对机场列表长按响应的方法
        hangdianlistView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                final HashMap<String, String> hangdianmap = (HashMap<String, String>) hangdianlistView.getItemAtPosition(position);
                final String hangdian_name = hangdianmap.get("hangdianname");
                final String hangdian_id = hangdianmap.get("hangdian_ID");


                new AlertDialog.Builder(HangDianActivity.this)
                        .setTitle("来吧来吧")
                        .setItems(R.array.bianji,
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        String[] PK = getResources().getStringArray(R.array.bianji);
                                        //Toast.makeText(JiChangGuanliActivity.this,PK[which],Toast.LENGTH_LONG).show();
                                        switch (PK[which]) {
                                            case "编辑":
                                                bianjihangdian(hangdianmap);
                                                break;
                                            case "删除":

                                                AlertDialog.Builder build = new AlertDialog.Builder(HangDianActivity.this);
                                                build.setTitle("注意")
                                                        .setMessage("确定要删除吗？")
                                                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                                            @Override
                                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                                shanchuhangdian(hangdian_id, hangdianmap);
                                                            }
                                                        })
                                                        .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                                                            @Override
                                                            public void onClick(DialogInterface dialogInterface, int i) {

                                                            }
                                                        })
                                                        .show();


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

    private void shanchuhangdian(String hangdian_id, HashMap<String, String> hangdianmap) {

        sqLiteDatabase.execSQL("update HangDian set used = '0' where hangdian_ID = '" + hangdian_id + "'");


        int i = Integer.parseInt(hangdianmap.get("hangdianshunxu"));//选中航点的顺序为i
        Cursor cursor1 = sqLiteDatabase.rawQuery("select shunxu from HangDian where used = '1' and hangxian_ID = '" + hangxian_id + "'", null);
        while (cursor1.moveToNext()) {
            int j = cursor1.getInt(cursor1.getColumnIndex("shunxu"));
            if (j > i) {
                int a = j - 1;
                sqLiteDatabase.execSQL("update HangDian set shunxu = '" + a + "' where shunxu = '" + j + "'and hangxian_ID = '" + hangxian_id + "' and used = '1'");
            }
        }
        cursor1.close();


        tianchonglistview();
    }

    @SuppressLint("SetTextI18n")
    private void xinzenghangdian() {
        LayoutInflater factory = LayoutInflater.from(this);
        @SuppressLint("InflateParams") View textEntryView = factory.inflate(R.layout.hangdiandetail, null);
        final EditText hangdiannamedetailEditText = (EditText) textEntryView.findViewById(R.id.hangdianname_detail_edittext);
        final EditText hangdianshunxudetailEditText = (EditText) textEntryView.findViewById(R.id.hangdianshunxu_detail_edittext);
        final EditText weidu_detailEditText = (EditText) textEntryView.findViewById(R.id.weidu_detail_edittext);
        final EditText jingdu_detailEditText = (EditText) textEntryView.findViewById(R.id.jingdu_detail_edittext);
        final EditText zhunshi_dong_detailEditText = (EditText) textEntryView.findViewById(R.id.zhunshi_dong_detail_edittext);
        final EditText zhunshi_dong_tuji_detailEditText = (EditText) textEntryView.findViewById(R.id.tujizhunshi_dong_detail_edittext);
        final EditText zhunshi_nan_detailEditText = (EditText) textEntryView.findViewById(R.id.zhunshi_nan_detail_edittext);
        final EditText zhunshi_nan_tuji_detailEditText = (EditText) textEntryView.findViewById(R.id.tujizhunshi_nan_detail_edittext);
        final EditText zhunshi_xi_detailEditText = (EditText) textEntryView.findViewById(R.id.zhunshi_xi_detail_edittext);
        final EditText zhunshi_xi_tuji_detailEditText = (EditText) textEntryView.findViewById(R.id.tujizhunshi_xi_detail_edittext);
        final EditText zhunshi_bei_detailEditText = (EditText) textEntryView.findViewById(R.id.zhunshi_bei_detail_edittext);
        final EditText zhunshi_bei_tuji_detailEditText = (EditText) textEntryView.findViewById(R.id.tujizhunshi_bei_detail_edittext);
        /*final CheckBox dong_checkbox = (CheckBox) textEntryView.findViewById(R.id.tujizhunshi_dong_detail_checkbox);
        final CheckBox nan_checkbox = (CheckBox) textEntryView.findViewById(R.id.tujizhunshi_nan_detail_checkbox);
        final CheckBox xi_checkbox = (CheckBox) textEntryView.findViewById(R.id.tujizhunshi_xi_detail_checkbox);
        final CheckBox bei_checkbox = (CheckBox) textEntryView.findViewById(R.id.tujizhunshi_bei_detail_checkbox);*/
        final TextView xiangdong = (TextView) textEntryView.findViewById(R.id.qifeifangxiang_dong_detail_tv);
        final TextView xiangnan = (TextView) textEntryView.findViewById(R.id.qifeifangxiang_nan_detail_tv);
        final TextView xiangxi = (TextView) textEntryView.findViewById(R.id.qifeifangxiang_xi_detail_tv);
        final TextView xiangbei = (TextView) textEntryView.findViewById(R.id.qifeifangxiang_bei_detail_tv);
        final LinearLayout xiangdongli = (LinearLayout) textEntryView.findViewById(R.id.xiangdong_li);
        final LinearLayout xiangnanli = (LinearLayout) textEntryView.findViewById(R.id.xiangnan_li);
        final LinearLayout xiangxili = (LinearLayout) textEntryView.findViewById(R.id.xiangxi_li);
        final LinearLayout xiangbeili = (LinearLayout) textEntryView.findViewById(R.id.xiangbei_li);

        if (fangxiang.contains("东")) {
            xiangdong.setVisibility(View.VISIBLE);
            xiangdongli.setVisibility(View.VISIBLE);
            xiangxi.setVisibility(View.VISIBLE);
            xiangxili.setVisibility(View.VISIBLE);
        } else {
            xiangnan.setVisibility(View.VISIBLE);
            xiangnanli.setVisibility(View.VISIBLE);
            xiangbei.setVisibility(View.VISIBLE);
            xiangbeili.setVisibility(View.VISIBLE);
        }


        final int shunxu = findzuidashunxu(hangxian_id) + 1;
        hangdianshunxudetailEditText.setText("" + shunxu);

        new AlertDialog.Builder(this)
                .setTitle("航点：")
                .setView(textEntryView)
                .setPositiveButton("确定",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                String hangdianname_insert = hangdiannamedetailEditText.getText().toString();
                                String shunxu_insert = hangdianshunxudetailEditText.getText().toString();
                                Double weidu_insert = jingweiToDouble(weidu_detailEditText.getText().toString());
                                Double jingdu_insert = jingweiToDouble(jingdu_detailEditText.getText().toString());
                                String zhunshidong = zhunshi_dong_detailEditText.getText().toString();
                                String zhunshinan = zhunshi_nan_detailEditText.getText().toString();
                                String zhunshixi = zhunshi_xi_detailEditText.getText().toString();
                                String zhunshibei = zhunshi_bei_detailEditText.getText().toString();
                                String tujidong;
                                String tujinan;
                                String tujixi;
                                String tujibei;

                                if (!TextUtils.isEmpty(zhunshi_dong_tuji_detailEditText.getText())) {
                                    tujidong = zhunshi_dong_tuji_detailEditText.getText().toString();
                                } else {
                                    tujidong = zhunshidong;
                                }

                                if (!TextUtils.isEmpty(zhunshi_nan_tuji_detailEditText.getText())) {
                                    tujinan = zhunshi_nan_tuji_detailEditText.getText().toString();
                                } else {
                                    tujinan = zhunshinan;
                                }

                                if (!TextUtils.isEmpty(zhunshi_xi_tuji_detailEditText.getText())) {
                                    tujixi = zhunshi_xi_tuji_detailEditText.getText().toString();
                                } else {
                                    tujixi = zhunshixi;
                                }
                                if (!TextUtils.isEmpty(zhunshi_bei_tuji_detailEditText.getText())) {
                                    tujibei = zhunshi_bei_tuji_detailEditText.getText().toString();
                                } else {
                                    tujibei = zhunshibei;
                                }





                                Zhunshishijiancha zhunshishijiancha = new Zhunshishijiancha(zhunshidong, zhunshinan, zhunshixi, zhunshibei);
                                Long hangdianzhunshi_insert = zhunshishijiancha.getZuixiaozhunshi();
                                String shijiancha_insert = zhunshishijiancha.getZhunshicha();

                                Zhunshishijiancha tujishijiancha = new Zhunshishijiancha(tujidong, tujinan, tujixi, tujibei);
                                Long tujishijian_insert = tujishijiancha.getZuixiaozhunshi();
                                String tujishijiancha_insert = tujishijiancha.getZhunshicha();




                                ContentValues values = new ContentValues();
                                values.put("hangdian_NAME", hangdianname_insert);
                                values.put("hangxian_ID", hangxian_id);
                                values.put("shunxu", shunxu_insert);
                                values.put("zhunshishijian", hangdianzhunshi_insert);
                                values.put("zhunshishijiancha", shijiancha_insert);
                                values.put("tujishijian", tujishijian_insert);
                                values.put("tujishijiancha", tujishijiancha_insert);
                                values.put("weidu", weidu_insert);
                                values.put("jingdu", jingdu_insert);
                                values.put("used", 1);
                                xiugaishunxu("" + shunxu, shunxu_insert);
                                sqLiteDatabase.insert("HangDian", "hangdian_NAME", values);
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

    private void bianjihangdian(HashMap<String, String> hangdianmap) {
        LayoutInflater factory = LayoutInflater.from(this);
        @SuppressLint("InflateParams") View textEntryView = factory.inflate(R.layout.hangdiandetail, null);
        final EditText hangdiannamedetailEditText = (EditText) textEntryView.findViewById(R.id.hangdianname_detail_edittext);
        final EditText hangdianshunxudetailEditText = (EditText) textEntryView.findViewById(R.id.hangdianshunxu_detail_edittext);
        final EditText weidu_detailEditText = (EditText) textEntryView.findViewById(R.id.weidu_detail_edittext);
        final EditText jingdu_detailEditText = (EditText) textEntryView.findViewById(R.id.jingdu_detail_edittext);
        final EditText zhunshi_dong_detailEditText = (EditText) textEntryView.findViewById(R.id.zhunshi_dong_detail_edittext);
        final EditText zhunshi_dong_tuji_detailEditText = (EditText) textEntryView.findViewById(R.id.tujizhunshi_dong_detail_edittext);
        final EditText zhunshi_nan_detailEditText = (EditText) textEntryView.findViewById(R.id.zhunshi_nan_detail_edittext);
        final EditText zhunshi_nan_tuji_detailEditText = (EditText) textEntryView.findViewById(R.id.tujizhunshi_nan_detail_edittext);
        final EditText zhunshi_xi_detailEditText = (EditText) textEntryView.findViewById(R.id.zhunshi_xi_detail_edittext);
        final EditText zhunshi_xi_tuji_detailEditText = (EditText) textEntryView.findViewById(R.id.tujizhunshi_xi_detail_edittext);
        final EditText zhunshi_bei_detailEditText = (EditText) textEntryView.findViewById(R.id.zhunshi_bei_detail_edittext);
        final EditText zhunshi_bei_tuji_detailEditText = (EditText) textEntryView.findViewById(R.id.tujizhunshi_bei_detail_edittext);
        /*final CheckBox dong_checkbox = (CheckBox) textEntryView.findViewById(R.id.tujizhunshi_dong_detail_checkbox);
        final CheckBox nan_checkbox = (CheckBox) textEntryView.findViewById(R.id.tujizhunshi_nan_detail_checkbox);
        final CheckBox xi_checkbox = (CheckBox) textEntryView.findViewById(R.id.tujizhunshi_xi_detail_checkbox);
        final CheckBox bei_checkbox = (CheckBox) textEntryView.findViewById(R.id.tujizhunshi_bei_detail_checkbox);*/
        final TextView xiangdong = (TextView) textEntryView.findViewById(R.id.qifeifangxiang_dong_detail_tv);
        final TextView xiangnan = (TextView) textEntryView.findViewById(R.id.qifeifangxiang_nan_detail_tv);
        final TextView xiangxi = (TextView) textEntryView.findViewById(R.id.qifeifangxiang_xi_detail_tv);
        final TextView xiangbei = (TextView) textEntryView.findViewById(R.id.qifeifangxiang_bei_detail_tv);
        final LinearLayout xiangdongli = (LinearLayout) textEntryView.findViewById(R.id.xiangdong_li);
        final LinearLayout xiangnanli = (LinearLayout) textEntryView.findViewById(R.id.xiangnan_li);
        final LinearLayout xiangxili = (LinearLayout) textEntryView.findViewById(R.id.xiangxi_li);
        final LinearLayout xiangbeili = (LinearLayout) textEntryView.findViewById(R.id.xiangbei_li);

        final String hangdian_name = hangdianmap.get("hangdianname");
        final String hangdian_id = hangdianmap.get("hangdian_ID");
        final String hangdianshunxu = hangdianmap.get("hangdianshunxu");
        final String hangdianweidu = hangdianmap.get("hangdianweidu");
        final String hangdianjingdu = hangdianmap.get("hangdianjingdu");
        final String hangdianzhunshi1 = hangdianmap.get("hangdianzhunshi1");
        final String hangdianzhunshi1_tuji = hangdianmap.get("hangdianzhunshi1tuji");
        final String hangdianzhunshi2 = hangdianmap.get("hangdianzhunshi2");
        final String hangdianzhunshi2_tuji = hangdianmap.get("hangdianzhunshi2tuji");


        hangdiannamedetailEditText.setText(hangdian_name);
        hangdianshunxudetailEditText.setText("0");
        weidu_detailEditText.setText(hangdianweidu);
        jingdu_detailEditText.setText(hangdianjingdu);
        hangdianshunxudetailEditText.setText(hangdianshunxu);


        if (fangxiang.contains("东")) {
            xiangdong.setVisibility(View.VISIBLE);
            xiangdongli.setVisibility(View.VISIBLE);
            zhunshi_dong_detailEditText.setText(hangdianzhunshi1);
            zhunshi_dong_tuji_detailEditText.setText(hangdianzhunshi1_tuji);
            xiangxi.setVisibility(View.VISIBLE);
            xiangxili.setVisibility(View.VISIBLE);
            zhunshi_xi_detailEditText.setText(hangdianzhunshi2);
            zhunshi_xi_tuji_detailEditText.setText(hangdianzhunshi2_tuji);
        } else {
            xiangnan.setVisibility(View.VISIBLE);
            xiangnanli.setVisibility(View.VISIBLE);
            zhunshi_nan_detailEditText.setText(hangdianzhunshi1);
            zhunshi_nan_tuji_detailEditText.setText(hangdianzhunshi1_tuji);
            xiangbei.setVisibility(View.VISIBLE);
            xiangbeili.setVisibility(View.VISIBLE);
            zhunshi_bei_detailEditText.setText(hangdianzhunshi2);
            zhunshi_bei_tuji_detailEditText.setText(hangdianzhunshi2_tuji);
        }



        new AlertDialog.Builder(this)
                .setTitle("航点编辑：")
                .setView(textEntryView)
                .setPositiveButton("确定",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                String hangdianname_insert = hangdiannamedetailEditText.getText().toString();
                                String shunxu_insert = hangdianshunxudetailEditText.getText().toString();
                                Double weidu_insert = jingweiToDouble(weidu_detailEditText.getText().toString());
                                Double jingdu_insert = jingweiToDouble(jingdu_detailEditText.getText().toString());
                                String zhunshidong = zhunshi_dong_detailEditText.getText().toString();
                                String zhunshinan = zhunshi_nan_detailEditText.getText().toString();
                                String zhunshixi = zhunshi_xi_detailEditText.getText().toString();
                                String zhunshibei = zhunshi_bei_detailEditText.getText().toString();
                                String tujidong = zhunshi_dong_tuji_detailEditText.getText().toString();
                                String tujinan = zhunshi_nan_tuji_detailEditText.getText().toString();
                                String tujixi = zhunshi_xi_tuji_detailEditText.getText().toString();
                                String tujibei = zhunshi_bei_tuji_detailEditText.getText().toString();
                                //      Integer shifoutuji_insert = 0 ;


                                if (!TextUtils.isEmpty(zhunshi_dong_tuji_detailEditText.getText())) {
                                    tujidong = zhunshi_dong_tuji_detailEditText.getText().toString();
                                } else {
                                    tujidong = zhunshidong;
                                }

                                if (!TextUtils.isEmpty(zhunshi_nan_tuji_detailEditText.getText())) {
                                    tujinan = zhunshi_nan_tuji_detailEditText.getText().toString();
                                } else {
                                    tujinan = zhunshinan;
                                }

                                if (!TextUtils.isEmpty(zhunshi_xi_tuji_detailEditText.getText())) {
                                    tujixi = zhunshi_xi_tuji_detailEditText.getText().toString();
                                } else {
                                    tujixi = zhunshixi;
                                }

                                if (!TextUtils.isEmpty(zhunshi_bei_tuji_detailEditText.getText())) {
                                    tujibei = zhunshi_bei_tuji_detailEditText.getText().toString();
                                } else {
                                    tujibei = zhunshibei;
                                }


                                Zhunshishijiancha zhunshishijiancha = new Zhunshishijiancha(zhunshidong, zhunshinan, zhunshixi, zhunshibei);
                                Long hangdianzhunshi_insert = zhunshishijiancha.getZuixiaozhunshi();
                                String shijiancha_insert = zhunshishijiancha.getZhunshicha();

                                Zhunshishijiancha tujishijiancha = new Zhunshishijiancha(tujidong, tujinan, tujixi, tujibei);
                                Long tujishijian_insert = tujishijiancha.getZuixiaozhunshi();
                                String tujishijiancha_insert = tujishijiancha.getZhunshicha();


                                ContentValues values = new ContentValues();
                                values.put("hangdian_NAME", hangdianname_insert);
                                values.put("hangxian_ID", hangxian_id);
                                values.put("shunxu", shunxu_insert);
                                values.put("zhunshishijian", hangdianzhunshi_insert);
                                values.put("zhunshishijiancha", shijiancha_insert);
                                values.put("tujishijian", tujishijian_insert);
                                values.put("tujishijiancha", tujishijiancha_insert);
                                values.put("weidu", weidu_insert);
                                values.put("jingdu", jingdu_insert);
                                values.put("used", 1);
                                xiugaishunxu(hangdianshunxu, shunxu_insert);
                                sqLiteDatabase.update("HangDian", values, "hangdian_ID = '" + hangdian_id + "'", null);

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

    private Integer findzuidashunxu(String hangxian_id) {
        Cursor cursor = sqLiteDatabase.rawQuery("select * from HangDian where hangxian_ID = ? and used = ? ", new String[]{hangxian_id, "1"});
        List<Integer> shunxu = new ArrayList<Integer>();
        shunxu.add(0);
        while (cursor.moveToNext()) {
            int a = cursor.getInt(cursor.getColumnIndex("shunxu"));
            shunxu.add(a);
        }
        cursor.close();
        return Collections.max(shunxu);
    }

    private void xiugaishunxu(String hangdianshunxu_befor, String hangdianshunxu_after) {
        int i = Integer.parseInt(hangdianshunxu_befor);
        int j = Integer.parseInt(hangdianshunxu_after);
        if (i > j) {
            while (j < i) {
                int a = i - 1;
                sqLiteDatabase.execSQL("update HangDian set shunxu = '" + i + "' where shunxu = '" + a + "'and hangxian_ID = '" + hangxian_id + "' and used = '1'");
                i--;
            }
        } else {
            while (j > i) {
                int a = i + 1;
                sqLiteDatabase.execSQL("update HangDian set shunxu = '" + i + "' where shunxu = '" + a + "'and hangxian_ID = '" + hangxian_id + "'and used = '1'");
                i++;
            }

        }

    }

    private void tianchonglistview() {
        Cursor cursor = sqLiteDatabase.rawQuery("select * from HangDian where used = '1' and hangxian_ID = '" + hangxian_id + "' order by shunxu", null);
        ArrayList<HashMap<String, String>> hangdianlist = new ArrayList<HashMap<String, String>>();
        //jichanglist.clear();
        while (cursor.moveToNext()) {
            String hangdian_ID = cursor.getString(cursor.getColumnIndex("hangdian_ID"));
            String hangdianshunxu = cursor.getString(cursor.getColumnIndex("shunxu"));
            String hangdianName = cursor.getString(cursor.getColumnIndex("hangdian_NAME"));
            Double hangdianWeidu = cursor.getDouble(cursor.getColumnIndex("weidu"));
            Double hangdianJingdu = cursor.getDouble(cursor.getColumnIndex("jingdu"));
            Long hangdianZhunshi = cursor.getLong(cursor.getColumnIndex("zhunshishijian"));
            String hangdianZhunshicha = cursor.getString(cursor.getColumnIndex("zhunshishijiancha"));
            Long tujishijian = cursor.getLong(cursor.getColumnIndex("tujishijian"));
            String tujishijiacha = cursor.getString(cursor.getColumnIndex("tujishijiancha"));
            ShijianchaTolong hangdianzhunshichaTolong = new ShijianchaTolong(hangdianZhunshicha);
            Long hangdianZhunshicha_dong = hangdianzhunshichaTolong.getDong();
            Long hangdianZhunshicha_nan = hangdianzhunshichaTolong.getNan();
            Long hangdianZhunshicha_xi = hangdianzhunshichaTolong.getXi();
            Long hangdianZhunshicha_bei = hangdianzhunshichaTolong.getBei();

            ShijianchaTolong tujishijianchaTolong = new ShijianchaTolong(tujishijiacha);
            Long tujishijiancha_dong = tujishijianchaTolong.getDong();
            Long tujishijiancha_nan = tujishijianchaTolong.getNan();
            Long tujishijiancha_xi = tujishijianchaTolong.getXi();
            Long tujishijiancha_bei = tujishijianchaTolong.getBei();

            Long hangdianzhunshi1 = hangdianZhunshi;
            Long hangdianzhunshi2 = hangdianZhunshi;
            Long hangdianzhunshi1_tuji = 0L;
            Long hangdianzhunshi2_tuji = 0L;

            if (fangxiang.contains("东")) {
                hangdianzhunshi1 = hangdianZhunshi + hangdianZhunshicha_dong;
                hangdianzhunshi1_tuji = tujishijian + tujishijiancha_dong;
                hangdianzhunshi2 = hangdianZhunshi + hangdianZhunshicha_xi;
                hangdianzhunshi2_tuji = tujishijian + tujishijiancha_xi;
            } else {
                hangdianzhunshi1 = hangdianZhunshi + hangdianZhunshicha_nan;
                hangdianzhunshi1_tuji = tujishijian + tujishijiancha_nan;
                hangdianzhunshi2 = hangdianZhunshi + hangdianZhunshicha_bei;
                hangdianzhunshi2_tuji = tujishijian + tujishijiancha_bei;
            }


            TimeFormat timeFormat = new TimeFormat();
            String hangdianzhunshi1_str = timeFormat.BeiJingtime(hangdianzhunshi1 - 28800000);
            String hangdianzhunshi2_str = timeFormat.BeiJingtime(hangdianzhunshi2 - 28800000);
            String hangdianzhunshi1_tuji_str = timeFormat.BeiJingtime(hangdianzhunshi1_tuji - 28800000);
            String hangdianzhunshi2_tuji_str = timeFormat.BeiJingtime(hangdianzhunshi2_tuji - 28800000);

            JingweiduFormat jingweiduFormat = new JingweiduFormat();


            HashMap<String, String> hangdianmap = new HashMap<String, String>();
            hangdianmap.put("hangdian_ID", hangdian_ID);
            hangdianmap.put("hangdianshunxu", hangdianshunxu);
            hangdianmap.put("hangdianname", hangdianName);
            hangdianmap.put("hangdianweidu", jingweiduFormat.WeiDu(hangdianWeidu));
            hangdianmap.put("hangdianjingdu", jingweiduFormat.WeiDu(hangdianJingdu));
            hangdianmap.put("hangdianzhunshi1", hangdianzhunshi1_str);
            hangdianmap.put("hangdianzhunshi1tuji", hangdianzhunshi1_tuji_str);
            hangdianmap.put("hangdianzhunshi2", hangdianzhunshi2_str);
            hangdianmap.put("hangdianzhunshi2tuji", hangdianzhunshi2_tuji_str);
            hangdianlist.add(hangdianmap);
        }

        SimpleAdapter hangdiansimpleAdapter = new SimpleAdapter(this,
                hangdianlist, R.layout.hangdianlist,
                new String[]{"hangdian_ID",
                        "hangdianshunxu",
                        "hangdianname",
                        "hangdianweidu",
                        "hangdianjingdu",
                        "hangdianzhunshi1",
                        "hangdianzhunshi1tuji",
                        "hangdianzhunshi2",
                        "hangdianzhunshi2tuji",},
                new int[]{R.id.hangdian_id_textview,
                        R.id.shunxu_textview,
                        R.id.hangdianname_textview,
                        R.id.hangdianweidu_textview,
                        R.id.hangdianjingdu_textview,
                        R.id.hangdianzhunshi1_feituji_textview,
                        R.id.hangdianzhunshi1_tuji_textview,
                        R.id.hangdianzhunshi2_feituji_textview,
                        R.id.hangdianzhunshi2_tuji_textview});
        hangdianlistView.setAdapter(hangdiansimpleAdapter);
    }

    private String qifeifangxiang() {
        String qifeifangxiang = "";
        String jichang_id = "";
        Cursor cursor = sqLiteDatabase.rawQuery("select * from HangXian where hangxian_ID = '" + hangxian_id + "'", null);
        cursor.moveToFirst();
        jichang_id = cursor.getString(cursor.getColumnIndex("jichang_ID"));
        cursor.close();
        Cursor cursor1 = sqLiteDatabase.rawQuery("select * from JiChang where jichang_ID = '" + jichang_id + "'", null);
        cursor1.moveToFirst();
        qifeifangxiang = cursor1.getString(cursor1.getColumnIndex("qifeifangxiang"));
        cursor1.close();
        return qifeifangxiang;
    }

    private Double jingweiToDouble(String jingweidu) {
        String daichuli_str = jingweidu + "    ";
        int i = 0;
        int j = 1;
        String a;
        String du = "0";
        String fen = "0";
        String miao = "0";
        Double jingweidu_double = 0D;

        do {
            a = daichuli_str.substring(i, j);
            if (!a.equals(" ") && !a.equals("°")) {
                du += a;
            }
            i++;
            j++;
        } while (!a.equals(" ") && !a.equals("°"));
        do {
            a = daichuli_str.substring(i, j);
            if (!a.equals(" ") && !a.equals("'")) {
                fen += a;
            }
            i++;
            j++;
        } while (!a.equals(" ") && !a.equals("'"));
        do {
            a = daichuli_str.substring(i, j);
            if (!a.equals(" ") && !a.equals("\"")) {
                miao += a;
            }
            i++;
            j++;
        } while (!a.equals(" ") && !a.equals("\""));

        jingweidu_double = Double.parseDouble(du) + Double.parseDouble(fen) / 60 + Double.parseDouble(miao) / 3600;
        return jingweidu_double;
    }

    private Long timeToms(String time) {
        String daichuli_str = time + "      ";
        int i = 0;
        int j = 1;
        String a;
        String xiaoshi = "0";
        String fen = "0";
        String miao = "0";

        if (!TextUtils.isEmpty(time) && !time.contains(" ") && !time.contains(":")) {
            xiaoshi = time.substring(0, 2);
            fen = time.substring(2, 4);
            miao = time.substring(4, 6);
        } else {
            do {
                a = daichuli_str.substring(i, j);
                if (!a.equals(" ") && !a.equals(":")) {
                    xiaoshi += a;
                }
                i++;
                j++;
            } while (!a.equals(" ") && !a.equals(":"));
            do {
                a = daichuli_str.substring(i, j);
                if (!a.equals(" ") && !a.equals(":")) {
                    fen += a;
                }
                i++;
                j++;
            } while (!a.equals(" ") && !a.equals(":"));
            do {
                a = daichuli_str.substring(i, j);
                if (!a.equals(" ")) {
                    miao += a;
                }
                i++;
                j++;
            } while (!a.equals(" "));
        }


        Long time_long = Long.parseLong(xiaoshi) * 3600000 + Long.parseLong(fen) * 60000 + Long.parseLong(miao) * 1000;
        return time_long;
    }

    private class ShijianchaTolong {
        String daichuli_str;
        String shijiancha_str;
        int i = 0;
        int j = 1;
        String a;
        String dong = "0";
        String nan = "0";
        String xi = "0";
        String bei = "0";

        ShijianchaTolong(String shijiancha_str) {

            daichuli_str = shijiancha_str + "|||||";
            do {
                a = daichuli_str.substring(i, j);
                if (!a.equals("|")) {
                    dong += a;
                }
                i++;
                j++;
            } while (!a.equals("|"));
            do {
                a = daichuli_str.substring(i, j);
                if (!a.equals("|")) {
                    nan += a;
                }
                i++;
                j++;
            } while (!a.equals("|"));
            do {
                a = daichuli_str.substring(i, j);
                if (!a.equals("|")) {
                    xi += a;
                }
                i++;
                j++;
            } while (!a.equals("|"));
            do {
                a = daichuli_str.substring(i, j);
                if (!a.equals("|")) {
                    bei += a;
                }
                i++;
                j++;
            } while (!a.equals("|"));
        }

        Long getDong() {
            return Long.parseLong(dong);
        }

        Long getNan() {
            return Long.parseLong(nan);
        }

        Long getXi() {
            return Long.parseLong(xi);
        }

        Long getBei() {
            return Long.parseLong(bei);
        }
    }

    private class Zhunshishijiancha {
        Long zuixiaozhunshi = 0L;
        Long shijiancha;
        String r = "0|0|0|0";
        String zuixiaozhunshifangxiang = "";

        Zhunshishijiancha(String zhunshidong, String zhunshinan, String zhunshixi, String zhunshibei) {
            Long zhunshidong_long = timeToms(zhunshidong);
            Long zhunshinan_long = timeToms(zhunshinan);
            Long zhunshixi_long = timeToms(zhunshixi);
            Long zhunshibei_long = timeToms(zhunshibei);


            if (zhunshidong_long <= zhunshinan_long) {

                zuixiaozhunshi = zhunshidong_long;
                zuixiaozhunshifangxiang = "东";
            } else {
                zuixiaozhunshi = zhunshinan_long;
                zuixiaozhunshifangxiang = "南";

            }
            if (zhunshixi_long < zuixiaozhunshi) {
                zuixiaozhunshi = zhunshixi_long;
                zuixiaozhunshifangxiang = "西";
            }
            if (zhunshibei_long < zuixiaozhunshi) {
                zuixiaozhunshi = zhunshibei_long;
                zuixiaozhunshifangxiang = "北";
            }

            long shijiancha_dong;
            long shijiancha_nan;
            long shijiancha_xi;
            long shijiancha_bei;

            switch (zuixiaozhunshifangxiang) {
                case "东":

                    shijiancha_nan = zhunshinan_long - zuixiaozhunshi;
                    shijiancha_xi = zhunshixi_long - zuixiaozhunshi;
                    shijiancha_bei = zhunshibei_long - zuixiaozhunshi;

                    r = "0|" + shijiancha_nan + "|" + shijiancha_xi + "|" + shijiancha_bei;

                    break;

                case "南":
                    shijiancha_dong = zhunshidong_long - zuixiaozhunshi;
                    shijiancha_xi = zhunshixi_long - zuixiaozhunshi;
                    shijiancha_bei = zhunshibei_long - zuixiaozhunshi;

                    r = shijiancha_dong + "|0|" + shijiancha_xi + "|" + shijiancha_bei;

                    break;

                case "西":
                    shijiancha_dong = zhunshidong_long - zuixiaozhunshi;
                    shijiancha_nan = zhunshinan_long - zuixiaozhunshi;
                    shijiancha_bei = zhunshibei_long - zuixiaozhunshi;

                    r = shijiancha_dong + shijiancha_nan + "|0|" + shijiancha_bei;

                    break;

                case "北":
                    shijiancha_dong = zhunshidong_long - zuixiaozhunshi;
                    shijiancha_nan = zhunshixi_long - zuixiaozhunshi;
                    shijiancha_xi = zhunshibei_long - zuixiaozhunshi;

                    r = shijiancha_dong + shijiancha_nan + shijiancha_xi + "|0";
                    break;
                default:
                    break;
            }
        }

        String getZhunshicha() {
            return r;
        }

        Long getZuixiaozhunshi() {
            return zuixiaozhunshi;
        }


    }

    private class JingweiduFormat {
        private double wd;
        private double jd;

        String WeiDu(double wd1) {
            int du;
            String du_str, fen_str;
            wd = wd1;
            du = (int) Math.floor(wd);
            if (du < 10) {
                du_str = "0" + du;
            } else {
                du_str = "" + du;
            }
            double fen = (wd - du) * 60;
            if (fen < 10) {
                fen_str = "0" + String.format("%.1f", fen);
            } else {
                fen_str = "" + String.format("%.1f", fen);
            }
            return du_str + "°" + fen_str + "'";
        }

        public String JingDu(double jd1) {
            int du;
            String du_str, fen_str;
            jd = jd1;
            du = (int) Math.floor(jd);
            if (du < 10) {
                du_str = "0" + du;
            } else {
                du_str = "" + du;
            }
            double fen = (jd - du) * 60;
            if (fen < 10) {
                fen_str = "0" + String.format("%.1f", fen);
            } else {
                fen_str = "" + String.format("%.1f", fen);
            }
            return du_str + "°" + fen_str + "'";

        }
    }

    private class TimeFormat {
        private double GPStime;

        String BeiJingtime(long GPStime1) {
            int tian;
            int xiaoshi;
            int fen;
            int miao;
            String xiaoshi_str, fen_str, miao_str;
            GPStime = GPStime1 + 28800000;
            tian = (int) Math.floor(GPStime / 86400000);
            xiaoshi = (int) Math.floor(GPStime / 3600000 - tian * 24);
            fen = (int) Math.floor(GPStime / 60000 - (tian * 24 + xiaoshi) * 60);
            miao = (int) Math.floor(GPStime / 1000 - ((tian * 24 + xiaoshi) * 60 + fen) * 60);
            if (xiaoshi < 10) {
                xiaoshi_str = "0" + xiaoshi;
            } else {
                xiaoshi_str = "" + xiaoshi;
            }
            if (fen < 10) {
                fen_str = "0" + fen;
            } else {
                fen_str = "" + fen;
            }
            if (miao < 10) {
                miao_str = "0" + miao;
            } else {
                miao_str = "" + miao;
            }
            return xiaoshi_str + ":" + fen_str + ":" + miao_str;
        }
    }

/*    public Long tujicha(Long tujichadong, Long tujichanan, Long tujichaxi, Long tujichabei) {

        Long tujicha = 0L;
        if (tujichanan != 0 && tujichadong < tujichanan) {
            if (tujichadong != 0) {
                tujicha = tujichadong;
            } else {
                tujicha = tujichanan;
            }
        }
        if (tujichaxi != 0 && tujichaxi < tujicha) {
            tujicha = tujichaxi;
        }
        if (tujichabei != 0 && tujichabei < tujicha) {
            tujicha = tujichabei;
        }
        return tujicha;
    }*/


}
