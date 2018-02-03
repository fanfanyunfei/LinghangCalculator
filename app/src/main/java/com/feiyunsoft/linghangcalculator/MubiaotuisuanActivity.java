package com.feiyunsoft.linghangcalculator;

import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

public class MubiaotuisuanActivity extends AppCompatActivity {

    private TextView xianzaiweiduTextView,xianzaijingduTextView;
    private TextView fenzhonghouEditText,fenzhonghouweiduTextView,fenzhonghoujingduTextView;
    private TextView shikeTextView,shikeweiduTextView,shikejingduTextView;
    private EditText mubiaoweiduEditText,mubiaojingduEditText;
    private EditText mubiaohangxiangEditText,mubiaosuduEditText;
    private EditText huoqushijianEditText;
    private Button jisuanButton;
    private RadioButton jieRadio,kmRadio;
    private SharedPreferences mSpSettings = null;
    private static final String PREFS_info = "Storedinfo";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mubiaotuisuan_layout);

        findview();
        //  getStoredQFSK();
        anniujiankong();

    }
    public void findview(){
        xianzaiweiduTextView = (TextView) findViewById(R.id.xianzaiweidu_textview);
        xianzaijingduTextView = (TextView) findViewById(R.id.xianzaijingdu_textview);
        fenzhonghouweiduTextView = (TextView) findViewById(R.id.fenzhonghouweidu_textview);
        fenzhonghoujingduTextView = (TextView) findViewById(R.id.fenzhonghoujingdu_textview);
        fenzhonghouEditText = (TextView) findViewById(R.id.fenzhonghou_edittext);
        shikeweiduTextView = (TextView) findViewById(R.id.shikeweidu_textview);
        shikejingduTextView = (TextView) findViewById(R.id.shikejingdu_textview);
        shikeTextView = (TextView) findViewById(R.id.shike_edittext);
        mubiaoweiduEditText = (EditText) findViewById(R.id.mubiaoweidu_edittext);
        mubiaojingduEditText = (EditText) findViewById(R.id.mubiaojingdu_edittext);
        mubiaohangxiangEditText = (EditText) findViewById(R.id.mubiaohangxiang_edittext);
        mubiaosuduEditText = (EditText) findViewById(R.id.mubiaosudu_edittext);
        huoqushijianEditText = (EditText) findViewById(R.id.huoqushijian_edittext);
        jisuanButton = (Button) findViewById(R.id.jisuan_button);
        jieRadio = (RadioButton) findViewById(R.id.jie_radio);
        kmRadio = (RadioButton) findViewById(R.id.km_radio);
    }

    public void anniujiankong(){
        jisuanButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                double mubiaoweidu ;
                double mubiaojingdu ;
                Integer mubiaohangxiang ;
                double mubiaosudu=0 ;//单位：km/h
                long time_huoqu ;
                if (mubiaoweiduEditText.getText().toString().length()!=0 &
                        mubiaojingduEditText.getText().toString().length()!=0 &
                        mubiaohangxiangEditText.getText().toString().length()!=0 &
                        mubiaosuduEditText.getText().toString().length() !=0 &
                        huoqushijianEditText.getText().toString().length() !=0){
                    mubiaoweidu = jingweiToDouble(mubiaoweiduEditText.getText().toString());
                    mubiaojingdu = jingweiToDouble(mubiaojingduEditText.getText().toString());
                    mubiaohangxiang = Integer.parseInt(mubiaohangxiangEditText.getText().toString());
                    if (jieRadio.isChecked()){
                        mubiaosudu = Double.parseDouble(mubiaosuduEditText.getText().toString())*1.852;//单位：km/h
                    }else if (kmRadio.isChecked()){
                        mubiaosudu = Double.parseDouble(mubiaosuduEditText.getText().toString());
                    }else{
                        Toast.makeText(MubiaotuisuanActivity.this,"请选择速度单位！",Toast.LENGTH_LONG).show();
                    }

                    String a = huoqushijianEditText.getText().toString();
                    time_huoqu = ((Long.parseLong(a.substring(0,2)))*3600000+
                            Long.parseLong(a.substring(2,4))*60000+
                            Long.parseLong(a.substring(4,6))*1000);

                    long timenow = System.currentTimeMillis();
                    double timefenzhong;
                    if (fenzhonghouEditText.getText().toString().length()!=0)
                    {
                        timefenzhong = Double.parseDouble(fenzhonghouEditText.getText().toString());
                    }else{
                        timefenzhong = 0;
                    }
                    long timeshike;
                    if (shikeTextView.getText().toString().length()!=0){
                        String b = shikeTextView.getText().toString();
                        timeshike = ((Long.parseLong(b.substring(0,2)))*3600000+
                                Long.parseLong(b.substring(2,4))*60000+
                                Long.parseLong(b.substring(4,6))*1000);
                    }else{
                        timeshike = 0L;
                    }



                    JiSuanJingweidu jiSuanJingweidu = new JiSuanJingweidu();
                    JingweiduFormat jingweiduFormat = new JingweiduFormat();
                    TimeFormat timeFormat = new TimeFormat();
                    double nowjuli = (timeToms(timeFormat.BeiJingtime(timenow)) - time_huoqu)*mubiaosudu/3600000;//单位：km
                    double nowweidu = jiSuanJingweidu.jisuanWeiDu(mubiaoweidu,mubiaojingdu,nowjuli,mubiaohangxiang);
                    String nowweidu_str = jingweiduFormat.WeiDu(nowweidu);
                    double nowjingdu = jiSuanJingweidu.jisuanJingDu(mubiaoweidu,mubiaojingdu,nowjuli,mubiaohangxiang);
                    String nowjingdu_str = jingweiduFormat.JingDu(nowjingdu);
                    xianzaiweiduTextView.setText(nowweidu_str);
                    xianzaijingduTextView.setText(nowjingdu_str);

                    if (timefenzhong !=0){
                        double fenzhonghoujuli = timefenzhong/60*mubiaosudu;//单位：km
                        double fzhweidu = jiSuanJingweidu.jisuanWeiDu(mubiaoweidu,mubiaojingdu,fenzhonghoujuli,mubiaohangxiang);
                        String fzhweidu_str = jingweiduFormat.WeiDu(fzhweidu);
                        double fzhjingdu = jiSuanJingweidu.jisuanJingDu(mubiaoweidu,mubiaojingdu,fenzhonghoujuli,mubiaohangxiang);
                        String fzhjingdu_str = jingweiduFormat.JingDu(fzhjingdu);
                        fenzhonghouweiduTextView.setText(fzhweidu_str);
                        fenzhonghoujingduTextView.setText(fzhjingdu_str);
                    }


                    if (timeshike!=0L){
                        double skjuli = (timeshike - time_huoqu) * mubiaosudu/3600000;//单位：km
                        double skweidu = jiSuanJingweidu.jisuanWeiDu(mubiaoweidu,mubiaojingdu,skjuli,mubiaohangxiang);
                        String skweidu_str = jingweiduFormat.WeiDu(skweidu);
                        double skjingdu = jiSuanJingweidu.jisuanJingDu(mubiaoweidu,mubiaojingdu,skjuli,mubiaohangxiang);
                        String skjingdu_str = jingweiduFormat.JingDu(skjingdu);
                        shikeweiduTextView.setText(skweidu_str);
                        shikejingduTextView.setText(skjingdu_str);
                    }



                }else{
                    Toast.makeText(MubiaotuisuanActivity.this,"请输入目标参数！",Toast.LENGTH_LONG).show();
                }








            }
        });





    }

    public class JiSuanJingweidu{
        private double wd;
        private double jd;
        private double jl;
        private int hangxiang;
        Double jisuanWeiDu(double wd1,double jd1,double juli1,int hangxiang1){
            wd=wd1;
            jd=jd1;
            jl=juli1;
            hangxiang=hangxiang1;

            double lon = jd + (jl * Math.sin(hangxiang* Math.PI / 180)) / (111 * Math.cos(wd * Math.PI / 180));//将距离转换成经度的计算公式
            double lat = wd + (jl * Math.cos(hangxiang* Math.PI / 180)) / 111;//将距离转换成纬度的计算公式
            return lat;
        }
        Double jisuanJingDu(double wd1,double jd1,double juli1,int hangxiang1){
            wd=wd1;
            jd=jd1;
            jl=juli1;
            hangxiang=hangxiang1;
            double lon = jd + (jl * Math.sin(hangxiang* Math.PI / 180)) / (111 * Math.cos(wd * Math.PI / 180));//将距离转换成经度的计算公式
            double lat = wd + (jl * Math.cos(hangxiang* Math.PI / 180)) / 111;//将距离转换成纬度的计算公式
            return lon;
        }
    }

    private class JingweiduFormat {
        private double wd;
        private double jd;
        String WeiDu(double wd1){
            int du;
            String du_str,fen_str;
            wd =wd1;
            du = (int) Math.floor(wd);
            if (du<10){
                du_str = "0"+du;
            }else{
                du_str = ""+du;
            }
            double fen =  (wd - du)*60;
            if (fen<10){
                fen_str = "0"+String.format("%.1f",fen);
            }else{
                fen_str = ""+String.format("%.1f",fen);
            }
            return du_str+"°"+fen_str+"'";
        }
        String JingDu(double jd1){
            int du;
            String du_str,fen_str;
            jd =jd1;
            du = (int) Math.floor(jd);
            if (du<10){
                du_str = "0"+du;
            }else{
                du_str = ""+du;
            }
            double fen =  (jd - du)*60;
            if (fen<10){
                fen_str = "0"+String.format("%.1f",fen);
            }else{
                fen_str = ""+String.format("%.1f",fen);
            }
            return du_str+"°"+fen_str+"'";

        }
    }

    public Double jingweiToDouble(String jingweidu){
        String daichuli_str = jingweidu + "    ";
        int i = 0;
        int j = 1;
        String  a;
        String du = "0";
        String fen="0";
        String miao="0";
        Double jingweidu_double = 0D;

        do {
            a = daichuli_str.substring(i,j);
            if (!a.equals(" ")&&!a.equals("°")&&!a.equals("#")){
                du += a;
            }
            i++;
            j++;
        }while (!a.equals(" ")&&!a.equals("°")&&!a.equals("#"));
        do {
            a = daichuli_str.substring(i,j);
            if (!a.equals(" ")&&!a.equals("'")&&!a.equals("#")){
                fen += a;
            }
            i++;
            j++;
        }while (!a.equals(" ")&&!a.equals("'")&&!a.equals("#"));
        do {
            a = daichuli_str.substring(i,j);
            if (!a.equals(" ")&&!a.equals("\"")&&!a.equals("#")){
                miao += a;
            }
            i++;
            j++;
        }while (!a.equals(" ")&&!a.equals("\"")&&!a.equals("#"));

        jingweidu_double = Double.parseDouble(du)+Double.parseDouble(fen)/60+Double.parseDouble(miao)/3600;
        return jingweidu_double;
    }

    public Long timeToms(String time){
        String daichuli_str = time + "      ";
        int i = 0;
        int j = 1;
        String  a;
        String xiaoshi = "0";
        String fen="0";
        String miao="0";

        do {
            a = daichuli_str.substring(i,j);
            if (!a.equals(" ")&&!a.equals(":")&&!a.equals("#")){
                xiaoshi += a;
            }
            i++;
            j++;
        }while (!a.equals(" ")&&!a.equals(":")&&!a.equals("#"));
        do {
            a = daichuli_str.substring(i,j);
            if (!a.equals(" ")&&!a.equals(":")&&!a.equals("#")){
                fen += a;
            }
            i++;
            j++;
        }while (!a.equals(" ")&&!a.equals(":")&&!a.equals("#"));
        do {
            a = daichuli_str.substring(i,j);
            if (!a.equals(" ")&&!a.equals("#")){
                miao += a;
            }
            i++;
            j++;
        }while (!a.equals(" ")&&!a.equals("#"));

        Long time_long = Long.parseLong(xiaoshi)*3600000+Long.parseLong(fen)*60000+Long.parseLong(miao)*1000;
        return time_long;
    }

    /*  private void getStoredQFSK(){
          mSpSettings = getSharedPreferences(PREFS_info,MODE_PRIVATE);
          if (mSpSettings.getBoolean("isKeep",false)){
              QFSK = Long.parseLong(mSpSettings.getString("storedinfo",""));
              TimeFormat timeFormat = new TimeFormat();
              qifeishikeTextView.setText(timeFormat.BeiJingtime(QFSK));
          }
      }
      private void storeinfo(){
          mSpSettings = getSharedPreferences(PREFS_info,MODE_PRIVATE);
          SharedPreferences.Editor editor = mSpSettings.edit();
          editor.clear();
          editor.putBoolean("isKeep",true);
          editor.putString("storedinfo",String.valueOf(mubiaoweiduEditText));
          editor.apply();
      }*/
    private class TimeFormat {
        private double GPStime;

        String BeiJingtime(long GPStime1) {
            int tian;
            int xiaoshi;
            int fen;
            int miao;
            String xiaoshi_str,fen_str,miao_str;
            GPStime = GPStime1+28800000;
            tian = (int) Math.floor(GPStime / 86400000);
            xiaoshi = (int) Math.floor(GPStime / 3600000 - tian*24);
            fen = (int) Math.floor(GPStime/60000-(tian*24+xiaoshi)*60);
            miao = (int) Math.floor(GPStime/1000-((tian*24+xiaoshi)*60+fen)*60);
            if (xiaoshi<10){
                xiaoshi_str = "0"+xiaoshi;
            }else{
                xiaoshi_str = ""+xiaoshi;
            }
            if (fen<10){
                fen_str = "0"+fen;
            }else{
                fen_str = ""+fen;
            }
            if (miao<10){
                miao_str = "0"+miao;
            }else{
                miao_str = ""+miao;
            }
            return xiaoshi_str + ":" + fen_str + ":" + miao_str;
        }
    }


}
