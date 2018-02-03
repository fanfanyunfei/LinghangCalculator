package com.feiyunsoft.linghangcalculator;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;


public class MainActivity extends AppCompatActivity {


    private final String TAG = "LinghangCalculator";
    private TextView mGpsInfo;   //mGpsInfo用于在没有卫星信号的时候显示信息
    private TextView weiduTextView, jingduTextView;   //weiduTextView是主页上的纬度显示区
    private TextView daifeijuTextView, daifeishiTextView;
    private TextView disuTextView, bucangshijianTextView;
    private TextView qifeishikeTextView, weixingshijianTextView;
    private TextView shijianwuchaTextView, yudashikeTextView, mubiaoyudashikeTextView, yingfeidisuTextView;
    private TextView daifeiju_lableTextView, daifeishi_lableTextView, yudashike_lableTextView, disu_lableTextView, weidu_lableTextView, jingdu_lableTextView;

    private Button bucangshijianButton, jishiButton, jishichongzhiButton, shezhiButton, sheqifeishiButton, mubiaotuisuanButton, xianshixiangxiButton, yincangxiangxiButton;   //shezhiButton是主页上的设置按钮

    private long QFSK = 0;

    private Spinner hangxianSpinner, jichangSpinner;
    private Spinner hangdianSpinner, tujiSpinner;
    private Spinner qifeifangxiangSpinner;

    private SQLiteDatabase sqLiteDatabase;
    private ArrayAdapter<String> adapter;
    private ArrayAdapter<String> adapter1, adapter2, adapter3;
    private List<String> jichang_list = new ArrayList<>();
    private List<String> hangxian_list = new ArrayList<>();
    private List<String> hangdian_list = new ArrayList<>();
    private List<String> qifeifangxiang_list = new ArrayList<>();
    private List<hangdian_info> hangdian_info_list = new ArrayList<>();
    private ListView hangdianinfoListView;
    private String selected_jichangname;
    private String selected_jichangid;
    private String selected_hangxianname, selected_hangdianname, selected_qifeifangxiang;
    private String selected_hangxianid, f;
    private String selected_shifoutuji;
    private LocationManager GpsManager;
    private LocationListener locationListener;
    private String mProviderName;
    public static String DATABASE_PATH;
    public static String DATABASE_NAME;
    private Long mGpsTime;
    private int numberselectJichang, numberselectHangxian, numberselectHangdian, numberselectQifeifangxiang, numberselectshifoutuji;

    private SharedPreferences mSpSettings = null;
    private static final String PREFS_QFSK = "StoredQFSK";

    private int AAA;
    private int numberstoredJichang;
    private int numberstoredHangxian;
    private int numberstoredHangdian;
    private int numberstoredQifeifangxiang;
    private int numberstoredShifoutuji;

    /**
     * java
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_layout);
        DATABASE_PATH = getFilesDir() + "/databases";
        DATABASE_NAME = "shuju.db";
        writeDB();
        sqLiteDatabase = this.openOrCreateDatabase(DATABASE_PATH + "/" + DATABASE_NAME, Activity.MODE_PRIVATE, null);
        Timer timer = new Timer();
        locationListener = new GpsLocationListener();
        GpsManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        mProviderName = LocationManager.GPS_PROVIDER;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        final Location location = GpsManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);


        findview();
        getStoredDATA();
        spinner_tianchong();
        printGpsLocation(location);
        GpsManager.requestLocationUpdates(mProviderName, 1000, 5, locationListener);
        timer.schedule(task, 2000, 1000); //用来更新时钟的
        timer.schedule(jisuantask, 100, 500);
        setlistener();


    }

    TimerTask task = new TimerTask() {
        @Override
        public void run() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    LocationManager mGpsManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

                    Location mlocation = mGpsManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    mGpsTime = 0L;

                    if (mlocation != null) {
                        LocationUtils mlocationUtils = new LocationUtils();


                        mGpsTime = mlocation.getTime();
                    }


                    TimeFormat timeFormat = new TimeFormat();
                    String t = timeFormat.BeiJingtime(mGpsTime);
                    weixingshijianTextView.setText(t);
                }
            });
        }
    };

    TimerTask jisuantask = new TimerTask() {
        @Override
        public void run() {
            runOnUiThread(new Runnable() {
                @SuppressLint({"DefaultLocale", "SetTextI18n"})
                @Override
                public void run() {

                    try {


                        LocationManager GpsManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

                        Location location = GpsManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                        double disu;

                        if (location != null) {
                            LocationUtils locationUtils = new LocationUtils();
                            TimeFormat timeFormat = new TimeFormat();

                            disu = location.getSpeed();//////米每秒
                            disuTextView.setText(String.format("%.1f", disu * 3.6));


//                            disu=200;


                            long yudashike_biaozhun_final_jisuan = 0L;
                            long yudashike_biaozhun_jisuan = 0L;


                            Cursor cursor = sqLiteDatabase.rawQuery("select * from HangDian where hangdian_NAME = '" + selected_hangdianname + "'and hangxian_ID = '" + selected_hangxianid + "' and used = '1' ", null);
                            cursor.moveToFirst();
                            Double hangdianweidu_jisuan = cursor.getDouble(cursor.getColumnIndex("weidu"));
                            Double hangdianjingdu_jisuan = cursor.getDouble(cursor.getColumnIndex("jingdu"));
                            Long hangdianzhunshi_jisuan = cursor.getLong(cursor.getColumnIndex("zhunshishijian"));
                            String zhunshishijiancha_jisuan = cursor.getString(cursor.getColumnIndex("zhunshishijiancha"));
                            Long tujizhunshi_jisuan = cursor.getLong(cursor.getColumnIndex("tujishijian"));
                            String tujishijiancha_jisuan = cursor.getString(cursor.getColumnIndex("tujishijiancha"));
                            cursor.close();


                            Long zhunshi_jisuan = 0L;
                            String zhunshicha_jisuan = null;


                            switch (selected_shifoutuji) {
                                case "是":
                                    zhunshi_jisuan = tujizhunshi_jisuan;
                                    zhunshicha_jisuan = tujishijiancha_jisuan;
                                    break;
                                case "否":
                                    zhunshi_jisuan = hangdianzhunshi_jisuan;
                                    zhunshicha_jisuan = zhunshishijiancha_jisuan;
                                    break;
                                default:
                                    break;
                            }


//根据选择的起飞方向，将zhunshicha中的差值赋值给zhunshishijiancha_long
                            long zhunshishijiancha_long_jisuan = 0;
                            ShijianchaTolong shijianchaTolong = new ShijianchaTolong(zhunshicha_jisuan);
                            switch (selected_qifeifangxiang) {
                                case "东":
                                    zhunshishijiancha_long_jisuan = shijianchaTolong.getDong();
                                    break;
                                case "南":
                                    zhunshishijiancha_long_jisuan = shijianchaTolong.getNan();
                                    break;
                                case "西":
                                    zhunshishijiancha_long_jisuan = shijianchaTolong.getXi();
                                    break;
                                case "北":
                                    zhunshishijiancha_long_jisuan = shijianchaTolong.getBei();
                                    break;
                                default:
                                    break;
                            }

                            long zhunshi_final_jisuan = zhunshi_jisuan + zhunshishijiancha_long_jisuan;
                            yudashike_biaozhun_jisuan = zhunshi_final_jisuan + QFSK;


                            Double daifeiju = locationUtils.getDistance(location.getLatitude(), location.getLongitude(), hangdianweidu_jisuan, hangdianjingdu_jisuan) / 1000;   //得到待飞距 km
                            daifeijuTextView.setText("" + String.format("%.2f", daifeiju));  //显示待飞距 km

                            int number_list = hangdian_list.size();
                            int a = numberselectHangdian;
                            if (number_list == numberselectHangdian + 1) {
                                a = -1;
                            }
                            if (daifeiju < 5) {
                                hangdianSpinner.setSelection(a + 1);
                            }

                            if (disu != 0) {
                                long daifeishi = (long) (daifeiju * 1000 / disu) * 1000;  // long 待飞时  单位 毫秒
                                daifeishiTextView.setText(timeFormat.BeiJingtime(daifeishi - 28800000)); //显示待飞时


                                long yudashike_shiji;

                                switch (selected_shifoutuji) {
                                    case "是":
                                        if (disu < 237) {
                                            yudashike_biaozhun_final_jisuan = yudashike_biaozhun_jisuan + Long.parseLong(bucangshijianButton.getText().toString()) * 1000;
                                            yingfeidisuTextView.setTextColor(Color.RED);
                                            yudashikeTextView.setTextColor(Color.RED);
                                            shijianwuchaTextView.setTextColor(Color.RED);
                                            mubiaoyudashikeTextView.setTextColor(Color.RED);
                                        } else {
                                            yudashike_biaozhun_final_jisuan = yudashike_biaozhun_jisuan;
                                            yingfeidisuTextView.setTextColor(Color.BLACK);
                                            yudashikeTextView.setTextColor(Color.BLACK);
                                            shijianwuchaTextView.setTextColor(Color.BLACK);
                                            mubiaoyudashikeTextView.setTextColor(Color.BLACK);
                                        }
                                        break;
                                    case "否":
                                        yudashike_biaozhun_final_jisuan = yudashike_biaozhun_jisuan;
                                    default:
                                        break;
                                }

                                yudashike_shiji = daifeishi + timeToms(timeFormat.BeiJingtime(mGpsTime));


                                long wucha = yudashike_shiji - yudashike_biaozhun_final_jisuan;
                                long kefeishijian = 0;
                                kefeishijian = yudashike_biaozhun_final_jisuan - timeToms(timeFormat.BeiJingtime(mGpsTime));
                                yudashikeTextView.setText(timeFormat.BeiJingtime(yudashike_shiji - 28800000));
                                mubiaoyudashikeTextView.setText(timeFormat.BeiJingtime(yudashike_biaozhun_final_jisuan - 28800000));


                                String zaowan = "";
                                if (wucha > 0) {
                                    zaowan = "晚到 ";
                                } else {
                                    zaowan = "早到 ";
                                }

                                double yingfeidisu = daifeiju / kefeishijian * 3600000;
                                if (BuildConfig.DEBUG)

                                if (QFSK != 0) {
                                    shijianwuchaTextView.setText("" + zaowan + timeFormat.BeiJingtime(Math.abs(wucha) - 28800000));
                                    yingfeidisuTextView.setText(String.format("%.2f", yingfeidisu));
                                } else {
                                    shijianwuchaTextView.setText("无法计算");
                                    yingfeidisuTextView.setText("无法计算");
                                }
                            } else {
                                daifeishiTextView.setText("-- : -- : --");
                                yudashikeTextView.setText("-- : -- : --");
                                shijianwuchaTextView.setText("-- : -- : --");
                                yingfeidisuTextView.setText("--- . --");
                                mubiaoyudashikeTextView.setText("-- : -- : --");
                            }
                        } else {
                            daifeishiTextView.setText("-------");
                            yudashikeTextView.setText("-------");
                            shijianwuchaTextView.setText("-------");
                            yingfeidisuTextView.setText("-------");
                            disuTextView.setText("-------");
                            daifeijuTextView.setText("-------");
                            weiduTextView.setText("-------");
                            jingduTextView.setText("-------");
                            mubiaoyudashikeTextView.setText("无GPS信号");
                        }

                    } catch (Exception ignored) {
                    }

                }

            });
        }
    };

    /**
     * 设置监听器，都是以匿名类的方式。
     */
    private void setlistener() {
        shezhiButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AAA = 1;
                Intent intent = new Intent(MainActivity.this, SetupActivity.class);
                startActivityForResult(intent, 1);
            }
        });

        mubiaotuisuanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AAA = 1;
                Intent intent = new Intent(MainActivity.this, MubiaotuisuanActivity.class);
                startActivityForResult(intent, 1);
            }
        });


        jishiButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TimeFormat timeFormat = new TimeFormat();
                QFSK = timeToms(timeFormat.BeiJingtime(mGpsTime));
                 storedata();
                qifeishikeTextView.setText(timeFormat.BeiJingtime(QFSK - 8 * 3600000));
                biaogetianchong();
                jishiButton.setVisibility(View.GONE);
                jishichongzhiButton.setVisibility(View.VISIBLE);
                //shezhiButton.setVisibility(View.GONE);
                AAA = 1;
            }

        }
        );

        jishichongzhiButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openDialog();
            }
        });

        bucangshijianButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shezhibuchangshijian();
            }
        });

        sheqifeishiButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sheqifeishi();
            }
        });

        xianshixiangxiButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                daifeiju_lableTextView.setVisibility(View.VISIBLE);
                daifeishi_lableTextView.setVisibility(View.VISIBLE);
                yudashike_lableTextView.setVisibility(View.VISIBLE);
                disu_lableTextView.setVisibility(View.VISIBLE);
                weidu_lableTextView.setVisibility(View.VISIBLE);
                jingdu_lableTextView.setVisibility(View.VISIBLE);

                daifeijuTextView.setVisibility(View.VISIBLE);
                daifeishiTextView.setVisibility(View.VISIBLE);
                yudashikeTextView.setVisibility(View.VISIBLE);
                disuTextView.setVisibility(View.VISIBLE);
                weiduTextView.setVisibility(View.VISIBLE);
                jingduTextView.setVisibility(View.VISIBLE);

                yincangxiangxiButton.setVisibility(View.VISIBLE);
                xianshixiangxiButton.setVisibility(View.GONE);

            }
        });

        yincangxiangxiButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                daifeiju_lableTextView.setVisibility(View.GONE);
                daifeishi_lableTextView.setVisibility(View.GONE);
                yudashike_lableTextView.setVisibility(View.GONE);
                disu_lableTextView.setVisibility(View.GONE);
                weidu_lableTextView.setVisibility(View.GONE);
                jingdu_lableTextView.setVisibility(View.GONE);

                daifeijuTextView.setVisibility(View.GONE);
                daifeishiTextView.setVisibility(View.GONE);
                yudashikeTextView.setVisibility(View.GONE);
                disuTextView.setVisibility(View.GONE);
                weiduTextView.setVisibility(View.GONE);
                jingduTextView.setVisibility(View.GONE);

                yincangxiangxiButton.setVisibility(View.GONE);
                xianshixiangxiButton.setVisibility(View.VISIBLE);
            }
        });

    }


    private void spinner_tianchong() {

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, jichang_list);
        adapter1 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, hangxian_list);
        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, hangdian_list);
        adapter3 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, qifeifangxiang_list);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        adapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        adapter3.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);


        final Cursor cursor = sqLiteDatabase.rawQuery("select * from JiChang where used = 1", null);//从机场表找出所有机场
        final List<HashMap<String, String>> jichang_spinnerlist = new ArrayList<HashMap<String, String>>();
        jichang_list.clear();
        while (cursor.moveToNext())
        {
            String JiChangId = cursor.getString(cursor.getColumnIndex("jichang_ID"));
            String JiChangName = cursor.getString(cursor.getColumnIndex("jichang_NAME"));
            HashMap<String, String> jichang_spinnermap = new HashMap<String, String>();
            jichang_spinnermap.put("JiChangId",JiChangId);
            jichang_spinnermap.put("JiChangName",JiChangName);
            jichang_spinnerlist.add(jichang_spinnermap);
            jichang_list.add(JiChangName);
        }
        cursor.close();



        int number_list = jichang_list.size();
        if (number_list <= numberstoredJichang)
        {
            numberstoredJichang = 0;
        }

        jichangSpinner.setAdapter(adapter);
        jichangSpinner.setSelection(numberstoredJichang);
        jichangSpinner.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                selected_jichangname = adapter.getItem(arg2);
                numberselectJichang = arg2;
                storedata();

                HashMap<String, String> jichang_map_selected = new HashMap<String, String>();
                jichang_map_selected = jichang_spinnerlist.get(numberselectJichang);
                selected_jichangid = jichang_map_selected.get("JiChangId");
                /*Cursor cursor1 = sqLiteDatabase.rawQuery("select jichang_ID from JiChang where jichang_NAME = '" + selected_jichangname + "' and used = 1", null);
                if (BuildConfig.DEBUG) Log.d("ABC", "选择的机场是：" + selected_jichangname);
                cursor1.moveToFirst();
                selected_jichangid = cursor1.getString(cursor1.getColumnIndex("jichang_ID"));
                cursor1.close();*/


                hangxian_list.clear();
                final List<HashMap<String, String>> hangxian_spinnerlist = new ArrayList<HashMap<String, String>>();
                Cursor cursor2 = sqLiteDatabase.rawQuery("select * from HangXian where jichang_ID = '"+selected_jichangid +"' and used = 1 ",null);
                while (cursor2.moveToNext()) {
                    String HangXianId = cursor2.getString(cursor2.getColumnIndex("hangxian_ID"));
                    String HangXianName = cursor2.getString(cursor2.getColumnIndex("hangxia_NAME"));
                    HashMap<String, String> hangxian_spinnermap = new HashMap<String, String>();
                    hangxian_spinnermap.put("HangXianId",HangXianId);
                    hangxian_spinnermap.put("HangXianName",HangXianName);
                    hangxian_spinnerlist.add(hangxian_spinnermap);
                    hangxian_list.add(HangXianName);
                }
                cursor2.close();

                int number_list = hangxian_list.size();
                if (number_list <= numberstoredHangxian) {
                    numberstoredHangxian = 0;
                }
                hangxianSpinner.setAdapter(adapter1);
                hangxianSpinner.setSelection(numberstoredHangxian);
                hangxianSpinner.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {
                    public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                        selected_hangxianname = adapter1.getItem(arg2);
                        numberselectHangxian = arg2;
                        storedata();

                        HashMap<String, String> hangxian_map_selected = new HashMap<String, String>();
                        hangxian_map_selected = hangxian_spinnerlist.get(numberselectHangxian);
                        selected_hangxianid = hangxian_map_selected.get("HangXianId");

                        /*Cursor cursor3 = sqLiteDatabase.rawQuery("select hangxian_ID from HangXian where hangxia_NAME = '" + selected_hangxianname + "'and jichang_ID = '"+selected_jichangid +"' and used = 1", null);
                        while (cursor3.moveToNext()) {
                            selected_hangxianid = cursor3.getString(cursor3.getColumnIndex("hangxian_ID"));
                        }
                        cursor3.close();*/

                        biaogetianchong();
                        hangdian_list.clear();
                        Cursor cursor4 = sqLiteDatabase.rawQuery("select * from HangDian where hangxian_ID = '"+selected_hangxianid +"' and used = 1 order by shunxu", null);
                        while (cursor4.moveToNext()) {
                            String HangDianNanme = cursor4.getString(cursor4.getColumnIndex("hangdian_NAME"));
                            hangdian_list.add(HangDianNanme);
                        }
                        cursor4.close();
                        int number_list = hangdian_list.size();
                        if (number_list <= numberstoredHangdian) {
                            numberstoredHangdian = 0;
                        }
                        hangdianSpinner.setAdapter(adapter2);
                        hangdianSpinner.setSelection(numberstoredHangdian);
                        hangdianSpinner.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {
                            public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                                selected_hangdianname = adapter2.getItem(arg2);
                                numberselectHangdian = arg2;
                                storedata();
                                arg0.setVisibility(View.VISIBLE);
                                biaogetianchong();
                            }

                            public void onNothingSelected(AdapterView<?> arg0) {
                                arg0.setVisibility(View.VISIBLE);
                            }
                        });

                        arg0.setVisibility(View.VISIBLE);
                    }

                    public void onNothingSelected(AdapterView<?> arg0) {
                        arg0.setVisibility(View.VISIBLE);
                    }
                });

                Cursor cursor4 = sqLiteDatabase.rawQuery("select qifeifangxiang from JiChang where jichang_ID = '" + selected_jichangid + "'", null);
                qifeifangxiang_list.clear();
                while (cursor4.moveToNext()) {
                    String Qifeifangxiang = cursor4.getString(cursor4.getColumnIndex("qifeifangxiang"));
                    String Qifeifangxiang1 = "", Qifeifangxiang2 = "", Qifeifangxiang3 = "", Qifeifangxiang4 = "";
                    if (Qifeifangxiang.contains("东")) {
                        Qifeifangxiang1 = "东";
                    }
                    if (Qifeifangxiang.contains("南")) {
                        Qifeifangxiang2 = "南";
                    }
                    if (Qifeifangxiang.contains("西")) {
                        Qifeifangxiang3 = "西";
                    }
                    if (Qifeifangxiang.contains("北")) {
                        Qifeifangxiang4 = "北";
                    }
                    if (!Qifeifangxiang1.isEmpty()) {
                        qifeifangxiang_list.add(Qifeifangxiang1);
                    }
                    if (!Qifeifangxiang2.isEmpty()) {
                        qifeifangxiang_list.add(Qifeifangxiang2);
                    }
                    if (!Qifeifangxiang3.isEmpty()) {
                        qifeifangxiang_list.add(Qifeifangxiang3);
                    }
                    if (!Qifeifangxiang4.isEmpty()) {
                        qifeifangxiang_list.add(Qifeifangxiang4);
                    }


                }
                cursor4.close();
                qifeifangxiangSpinner.setAdapter(adapter3);
                qifeifangxiangSpinner.setSelection(numberstoredQifeifangxiang);
                qifeifangxiangSpinner.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {
                    public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                        selected_qifeifangxiang = adapter3.getItem(arg2);
                        numberselectQifeifangxiang = arg2;
                        storedata();
                        biaogetianchong();
                        arg0.setVisibility(View.VISIBLE);
                    }

                    public void onNothingSelected(AdapterView<?> arg0) {
                        arg0.setVisibility(View.VISIBLE);
                    }
                });


                arg0.setVisibility(View.VISIBLE);
            }

            public void onNothingSelected(AdapterView<?> arg0) {
                arg0.setVisibility(View.VISIBLE);
            }
        });

        tujiSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String[] sftj = getResources().getStringArray(R.array.shifoutuji);
                selected_shifoutuji = sftj[position];
                //  numberselectshifoutuji = position;
                //   tujiSpinner.setSelection(2,true);
                switch (selected_shifoutuji) {
                    case "是":
                        bucangshijianButton.setVisibility(View.VISIBLE);
                        bucangshijianTextView.setVisibility(View.VISIBLE);
                        bucangshijianButton.setTextColor(Color.RED);
                        bucangshijianTextView.setTextColor(Color.RED);
                        mubiaoyudashikeTextView.setTextColor(Color.RED);
                        biaogetianchong();
                        break;
                    case "否":
                        bucangshijianButton.setVisibility(View.INVISIBLE);
                        bucangshijianTextView.setVisibility(View.INVISIBLE);
                        yudashikeTextView.setTextColor(Color.BLACK);
                        yingfeidisuTextView.setTextColor(Color.BLACK);
                        shijianwuchaTextView.setTextColor(Color.BLACK);
                        mubiaoyudashikeTextView.setTextColor(Color.BLACK);
                        biaogetianchong();
                        break;
                    default:
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    @SuppressLint("SetTextI18n")
    public void printGpsLocation(Location location) {                 //定义printGpsLocationg事件


        if (location != null) {
            mGpsInfo.setText("精度：" + location.getAccuracy() +
                    "\n海拔：" + location.getAltitude() +
                    "\n航向：" + location.getBearing() +
                    "\n速度：" + location.getSpeed() +
                    "\n纬度：" + location.getLatitude() +
                    "\n经度：" + location.getLongitude() +
                    "\n时间：" + location.getTime());

            JingweiduFormat jingweiduFormat = new JingweiduFormat();
            weiduTextView.setText(jingweiduFormat.WeiDu(location.getLatitude()));//显示纬度
            jingduTextView.setText(jingweiduFormat.JingDu(location.getLongitude()));  //显示经度


        }
    }

    private class GpsLocationListener implements LocationListener {
        public void onLocationChanged(Location location) {
            printGpsLocation(location);
        }

        public void onProviderDisabled(String provider) {
            Log.d(TAG, "ProvoderDisabled:" + provider);
        }

        public void onProviderEnabled(String provider) {
            Log.d(TAG, "ProviderEnable:" + provider);
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
            Log.d(TAG, "StatusChanged:" + provider + status);
        }
    }

    private void storedata() {//记录起飞时刻
        mSpSettings = getSharedPreferences(PREFS_QFSK, MODE_PRIVATE);
        SharedPreferences.Editor editor = mSpSettings.edit();
        editor.clear();
        editor.putBoolean("isKeep", true);
        editor.putString("storedqfsk", String.valueOf(QFSK));
        editor.putInt("numberJichang", numberselectJichang);
        editor.putInt("numberHangxian", numberselectHangxian);
        editor.putInt("numberHangdian", numberselectHangdian);
        editor.putInt("numberQifeifangxiang", numberselectQifeifangxiang);
        // editor.putInt("numberShifoutuji",numberselectshifoutuji);
        editor.apply();
    }

    private void getStoredDATA() {
        mSpSettings = getSharedPreferences(PREFS_QFSK, MODE_PRIVATE);
        if (mSpSettings.getBoolean("isKeep", false)) {
            QFSK = Long.parseLong(mSpSettings.getString("storedqfsk", ""));
            TimeFormat timeFormat = new TimeFormat();
            qifeishikeTextView.setText(timeFormat.BeiJingtime(QFSK - 28800000));
            numberstoredJichang = mSpSettings.getInt("numberJichang", 0);
            numberstoredHangxian = mSpSettings.getInt("numberHangxian", 0);
            numberstoredHangdian = mSpSettings.getInt("numberHangdian", 0);
            numberstoredQifeifangxiang = mSpSettings.getInt("numberQifeifangxiang", 0);
            //  numberstoredShifoutuji = mSpSettings.getInt("numberShifoutuji",0);
        }
    }


    private void openDialog() {//重置起飞计时按钮弹窗
        new AlertDialog.Builder(this)
                .setTitle("提示：")
                .setMessage("是否结束计时？")
                .setPositiveButton("确认",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                jishichongzhiButton.setVisibility(View.GONE);
                                jishiButton.setVisibility(View.VISIBLE);
                                shezhiButton.setVisibility(View.VISIBLE);
                                AAA = 0;
                            }
                        })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).show();
    }

    private void shezhibuchangshijian() {
        LayoutInflater factory = LayoutInflater.from(this);
        @SuppressLint("InflateParams") View textEntryView = factory.inflate(R.layout.bucangshijianedit, null);
        final EditText bucangshijianEditText = (EditText) textEntryView.findViewById(R.id.buchangshijian_edittext);

        new AlertDialog.Builder(this)
                .setTitle("请输入补偿时间(秒)：")
                .setView(textEntryView)
                .setPositiveButton("确定",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (!TextUtils.isEmpty(bucangshijianEditText.getText())) {
                                    bucangshijianButton.setText(bucangshijianEditText.getText().toString());
                                } else {
                                    bucangshijianButton.setText("0");
                                }

                            }
                        })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).show();
    }


    private void sheqifeishi() {
        LayoutInflater factory = LayoutInflater.from(this);
        @SuppressLint("InflateParams") View textEntryView = factory.inflate(R.layout.sheqifeishi, null);
        final EditText sheqifeishiEditText = (EditText) textEntryView.findViewById(R.id.sheqifeishi_edittext);

        new AlertDialog.Builder(this)
                .setTitle("请输入起飞时刻:（直接按确认可获得系统时间）")
                .setView(textEntryView)
                .setPositiveButton("确定",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                if (sheqifeishiEditText.getText().toString().length() != 0) {
                                    String a = sheqifeishiEditText.getText().toString();
                                    QFSK = ((Long.parseLong(a.substring(0, 2))) * 3600000 +
                                            Long.parseLong(a.substring(2, 4)) * 60000 +
                                            Long.parseLong(a.substring(4, 6)) * 1000);
                                } else {
                                    TimeFormat timeFormat = new TimeFormat();
                                    QFSK = timeToms(timeFormat.BeiJingtime(mGpsTime));
                                }
                                //storedata();

                                //AAA=1;

                                TimeFormat timeFormat = new TimeFormat();
                                qifeishikeTextView.setText(timeFormat.BeiJingtime(QFSK - 8 * 3600000));
                                biaogetianchong();
                                jishiButton.setVisibility(View.GONE);
                                jishichongzhiButton.setVisibility(View.VISIBLE);
                            }
                        })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        shezhiButton.setVisibility(View.VISIBLE);
                    }
                }).show();
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


    private class JingweiduFormat {
        private double wd;
        private double jd;

        @SuppressLint("DefaultLocale")
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

        @SuppressLint("DefaultLocale")
        String JingDu(double jd1) {
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


    private class LocationUtils {
        private double EARTH_RADIUS = 6378.137;

        private double rad(double d) {
            return d * Math.PI / 180.0;
        }


        double getDistance(double lat1, double lng1, double lat2,
                           double lng2) {
            double radLat1 = rad(lat1);
            double radLat2 = rad(lat2);
            double a = radLat1 - radLat2;
            double b = rad(lng1) - rad(lng2);
            double s = 2 * Math.asin(Math.sqrt(Math.pow(Math.sin(a / 2), 2)
                    + Math.cos(radLat1) * Math.cos(radLat2)
                    * Math.pow(Math.sin(b / 2), 2)));
            s = s * EARTH_RADIUS;
            s = Math.round(s * 10000d) / 10000d;
            s = s * 1000;
            return s;
        }
    }


    private void writeDB() {
        // f = DATABASE_PATH_NAME;
        FileOutputStream fout = null;
        InputStream inputStream = null;
        File dir = new File(DATABASE_PATH);
        if (!dir.exists()) {
            dir.mkdir();
        }
        String databasefilename = DATABASE_PATH + "/" + DATABASE_NAME;
        File filepath = new File(databasefilename);
        if (!filepath.exists()) {
            try {
                inputStream = getResources().openRawResource(R.raw.shuju);
                fout = new FileOutputStream(new File(databasefilename));
                byte[] buffer = new byte[128];
                int len = 0;
                while ((len = inputStream.read(buffer)) != -1) {
                    fout.write(buffer, 0, len);
                }
                fout.close();
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (fout != null) {
                    try {
                        fout.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

    }


    private class hangdian_info {
        final String hangdianname;
        Double hangdianweidu;
        Double hangdianjingdu;
        Long zhunshi;
        Long yudashike_biaozhun;
        Long zhunshishijiacha_long;


        hangdian_info(String hangdianname,
                      Double hangdianweidu,
                      Double hangdianjingdu,
                      Long zhunshi,
                      Long yudashike_biaozhun,
                      Long zhunshishijiacha_long) {
            this.hangdianname = hangdianname;
            this.hangdianweidu = hangdianweidu;
            this.hangdianjingdu = hangdianjingdu;
            this.zhunshi = zhunshi;
            this.yudashike_biaozhun = yudashike_biaozhun;
            this.zhunshishijiacha_long = zhunshishijiacha_long;


        }

        String getHangdianname() {
            return hangdianname;
        }

        Double getHangdianweidu() {
            return hangdianweidu;
        }

        Double getHangdianjingdu() {
            return hangdianjingdu;
        }

        Long getZhunshi() {
            return zhunshi;
        }

        Long getYudashike_biaozhun() {
            return yudashike_biaozhun;
        }

        long getZhunshishijiacha_long() {
            return zhunshishijiacha_long;
        }
    }


    private class ShijianchaTolong {
        String daichuli_str;
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


    private void biaogetianchong() {
        hangdian_info_list.clear();
        Cursor cursor = sqLiteDatabase.rawQuery("select * from HangDian where hangxian_ID = '" + selected_hangxianid + "' and used = '1' order by shunxu", null);

        while (cursor.moveToNext()) {
            String hangdianname = cursor.getString(cursor.getColumnIndex("hangdian_NAME"));
            Double hangdianweidu = cursor.getDouble(cursor.getColumnIndex("weidu"));
            Double hangdianjingdu = cursor.getDouble(cursor.getColumnIndex("jingdu"));
            Long hangdianzhunshi = cursor.getLong(cursor.getColumnIndex("zhunshishijian"));
            String zhunshishijiancha = cursor.getString(cursor.getColumnIndex("zhunshishijiancha"));
            Long tujizhunshi = cursor.getLong(cursor.getColumnIndex("tujishijian"));
            String tujishijiancha = cursor.getString(cursor.getColumnIndex("tujishijiancha"));
            Long zhunshi = 0L;
            String zhunshicha = null;


            switch (selected_shifoutuji) {
                case "是":
                    zhunshi = tujizhunshi;
                    zhunshicha = tujishijiancha;
                    break;
                case "否":
                    zhunshi = hangdianzhunshi;
                    zhunshicha = zhunshishijiancha;
                    break;
                default:
                    break;
            }


//根据选择的起飞方向，将zhunshicha中的差值赋值给zhunshishijiancha_long
            long zhunshishijiancha_long = 0;
            ShijianchaTolong shijianchaTolong = new ShijianchaTolong(zhunshicha);
            switch (selected_qifeifangxiang) {
                case "东":
                    zhunshishijiancha_long = shijianchaTolong.getDong();
                    break;
                case "南":
                    zhunshishijiancha_long = shijianchaTolong.getNan();
                    break;
                case "西":
                    zhunshishijiancha_long = shijianchaTolong.getXi();
                    break;
                case "北":
                    zhunshishijiancha_long = shijianchaTolong.getBei();
                    break;
                default:
                    break;
            }

            Long zhunshi_final = zhunshi + zhunshishijiancha_long;
            Long yudashike_biaozhun = zhunshi_final + QFSK;
            hangdian_info jc = new hangdian_info(hangdianname, hangdianweidu, hangdianjingdu, zhunshi_final, yudashike_biaozhun, zhunshishijiancha_long);
            hangdian_info_list.add(jc);
        }
        cursor.close();

        BaseAdapter mBaseAdapter = new BaseAdapter() {
            @Override
            public int getCount() {
                return hangdian_info_list.size();
            }

            @SuppressLint("SetTextI18n")
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view;

                if (convertView == null) {
                    view = View.inflate(getBaseContext(), R.layout.hangdiandetail_shouye, null);
                } else {
                    view = convertView;
                }

                TimeFormat timeFormat = new TimeFormat();
                JingweiduFormat jingweiduFormat = new JingweiduFormat();
                hangdian_info jc = hangdian_info_list.get(position);
                TextView hangdiannametv = (TextView) view.findViewById(R.id.hangdianname_textview);
                TextView hangdianweidutv = (TextView) view.findViewById(R.id.hangdianweidu_textview);
                TextView hangdianjingdutv = (TextView) view.findViewById(R.id.hangdianjingdu_textview);
                TextView zhunshi = (TextView) view.findViewById(R.id.hangdianzhunshi_textview);
                TextView yudashikebiaozhun = (TextView) view.findViewById(R.id.yudashikebiaozhun_textview);
                LinearLayout hangdianLinear = (LinearLayout) view.findViewById(R.id.hangdian_linear);
                hangdiannametv.setText(jc.getHangdianname());
                hangdianweidutv.setText("N " + jingweiduFormat.WeiDu(jc.getHangdianweidu()));
                hangdianjingdutv.setText("E " + jingweiduFormat.JingDu(jc.getHangdianjingdu()));
                zhunshi.setText(timeFormat.BeiJingtime((jc.getZhunshi() - 28800000)));
                /*if (QFSK == 0) {
                    yudashikebiaozhun.setText("无起飞时刻");
                } else {
                    yudashikebiaozhun.setText(timeFormat.BeiJingtime(jc.getYudashike_biaozhun() - 28800000));
                }*/
                zhunshi.setText("");
                yudashikebiaozhun.setText("");
                zhunshi.setText(timeFormat.BeiJingtime((jc.getZhunshi() - 28800000)));
                if (QFSK == 0) {
                    yudashikebiaozhun.setText("无起飞时刻");
                } else {
                    yudashikebiaozhun.setText(timeFormat.BeiJingtime(jc.getYudashike_biaozhun() - 28800000));
                }

                if (position == numberselectHangdian) {
                    hangdianLinear.setBackgroundColor(Color.parseColor("#cfdced"));
                    hangdianinfoListView.setSelection(numberselectHangdian);
                }else{
                    hangdianLinear.setBackgroundColor(Color.WHITE);
                }
                return view;
            }

            @Override
            public Object getItem(int position) {
                return null;
            }

            @Override
            public long getItemId(int position) {
                return 0;
            }
        };
        hangdianinfoListView.setAdapter(mBaseAdapter);
    }

    @NonNull
    private Long timeToms(String time) {
        String daichuli_str = time + "      ";
        int i = 0;
        int j = 1;
        String a;
        String xiaoshi = "0";
        String fen = "0";
        String miao = "0";

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

        return (Long.parseLong(xiaoshi) * 3600000) + (Long.parseLong(fen) * 60000) + (Long.parseLong(miao) * 1000);
    }


    private void findview() {
        mGpsInfo = (TextView) this.findViewById(R.id.TextView_GpsInfo);
        weiduTextView = (TextView) this.findViewById(R.id.weidu_text_view);
        jingduTextView = (TextView) this.findViewById(R.id.jingdu_text_view);
        shezhiButton = (Button) this.findViewById(R.id.shezhi_button);
        daifeijuTextView = (TextView) this.findViewById(R.id.daifeiju_text_view);
        daifeishiTextView = (TextView) this.findViewById(R.id.daifeishi_text_view);
        disuTextView = (TextView) this.findViewById(R.id.disu_text_view);

        daifeiju_lableTextView = (TextView) this.findViewById(R.id.daifeiju_lable_text_view);
        daifeishi_lableTextView = (TextView) this.findViewById(R.id.daifeishi_lable_text_view);
        yudashike_lableTextView = (TextView) this.findViewById(R.id.yudashike_lable_text_view);
        disu_lableTextView = (TextView) this.findViewById(R.id.disu_lable_text_view);
        weidu_lableTextView = (TextView) this.findViewById(R.id.weidu_lable_text_view);
        jingdu_lableTextView = (TextView) this.findViewById(R.id.jingdu_lable_text_view);

        jishiButton = (Button) this.findViewById(R.id.jishi_button);
        qifeishikeTextView = (TextView) this.findViewById(R.id.qifeishike_text_view);
        weixingshijianTextView = (TextView) this.findViewById(R.id.weixingshijian_text_view);
        shijianwuchaTextView = (TextView) this.findViewById(R.id.shijianwucha_text_view);
        jichangSpinner = (Spinner) this.findViewById(R.id.jichang_spinner);
        hangxianSpinner = (Spinner) this.findViewById(R.id.hangxian_spinner);
        hangdianSpinner = (Spinner) this.findViewById(R.id.hangdian_spinner);
        qifeifangxiangSpinner = (Spinner) this.findViewById(R.id.qifeifangxiang_spinner);
        yingfeidisuTextView = (TextView) this.findViewById(R.id.yingfeidisu_text_view);
        hangdianinfoListView = (ListView) this.findViewById(R.id.hangdianinfo_listview);
        yudashikeTextView = (TextView) this.findViewById(R.id.yudashike_text_view);
        mubiaoyudashikeTextView = (TextView) this.findViewById(R.id.mubiaoyudashike_text_view);
        jishichongzhiButton = (Button) this.findViewById(R.id.chongzhi_button);
        sheqifeishiButton = (Button) this.findViewById(R.id.sheqifeishi_button);
        bucangshijianButton = (Button) this.findViewById(R.id.buchangshijian_button);
        mubiaotuisuanButton = (Button) this.findViewById(R.id.mubiaotuisuan_button);
        yincangxiangxiButton = (Button) this.findViewById(R.id.yincangxiangxi_button);
        xianshixiangxiButton = (Button) this.findViewById(R.id.xianshixiangxi_button);
        tujiSpinner = (Spinner) this.findViewById(R.id.shifoutuji_spinner);
        bucangshijianTextView = (TextView) this.findViewById(R.id.buchangshijian_text_view);
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (!TextUtils.isEmpty(mProviderName)) {

            GpsManager.requestLocationUpdates(mProviderName, 1000, 1, locationListener);
            //

        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        if (AAA != 1 && GpsManager != null) {
            GpsManager.removeUpdates(locationListener);

        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        storedata();
        //AAA=1;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                AlertDialog.Builder build = new AlertDialog.Builder(this);
                build.setTitle("注意")
                        .setMessage("确定要退出么？")
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                finish();
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
        return false;
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // String result = data.getExtras().getString("result");
        //Toast.makeText("wo shoudaole ",)
        /*if (AAA == 1){
            jishiButton.setVisibility(View.GONE);
        }*/
        switch (resultCode) {
            case RESULT_OK:
                getStoredDATA();
                spinner_tianchong();
                AAA = 0;
                break;
            default:
                getStoredDATA();
                spinner_tianchong();
                AAA = 0;
                break;
        }
    }
}
