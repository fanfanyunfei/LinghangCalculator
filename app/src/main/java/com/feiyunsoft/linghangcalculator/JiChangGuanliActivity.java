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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.SimpleAdapter;

import java.util.ArrayList;
import java.util.HashMap;

import static com.feiyunsoft.linghangcalculator.MainActivity.DATABASE_NAME;
import static com.feiyunsoft.linghangcalculator.MainActivity.DATABASE_PATH;


public class JiChangGuanliActivity extends AppCompatActivity {
    private SQLiteDatabase sqLiteDatabase;
    private ListView jichanglistView;
    /*    public static String DATABASE_PATH;
    public static String DATABASE_NAME;*/





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.jichangguanli_layout);

        Button xinzengButton = (Button) findViewById(R.id.add_button);
        jichanglistView = (ListView) findViewById(R.id.jichang_listview);


        String f = DATABASE_PATH + "/" + DATABASE_NAME;
        sqLiteDatabase = openOrCreateDatabase(f,MODE_PRIVATE,null);

        tianchonglistview();
        itemLongClick_jichang();
        itemClick_jichang();

        xinzengButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                xinzengjichang();
            }
        });

    }



    private void itemClick_jichang(){
        jichanglistView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                HashMap<String,String> jichangmap = (HashMap<String,String>) jichanglistView.getItemAtPosition(position);
                /*final String jichang_name = jichangmap.get("jichangname");
                final String qifeifangxiang = jichangmap.get("qifeifangxiang");*/
                final String jichang_id = jichangmap.get("jichang_ID");
                Intent intent = new Intent(getApplicationContext(),HangXianActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("jichang_ID",jichang_id);
                intent.putExtra("jichangbd",bundle);
                startActivity(intent);
            }
        });
    }

    private void itemLongClick_jichang(){//对机场列表长按响应的方法
        jichanglistView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                HashMap<String,String> jichangmap = (HashMap<String, String>) jichanglistView.getItemAtPosition(position);
               final String jichang_name = jichangmap.get("jichangname");
                final String qifeifangxiang = jichangmap.get("qifeifangxiang");
                final String jichang_id = jichangmap.get("jichang_ID");


                new AlertDialog.Builder(JiChangGuanliActivity.this)
                        .setTitle("请选择：")
                        .setItems(R.array.bianji,
                                new DialogInterface.OnClickListener(){
                                    public void onClick(DialogInterface dialog,int which){
                                        String[] PK = getResources().getStringArray(R.array.bianji);
                                        //Toast.makeText(JiChangGuanliActivity.this,PK[which],Toast.LENGTH_LONG).show();
                                        switch (PK[which]){
                                            case "删除":
                                                AlertDialog.Builder build = new AlertDialog.Builder(JiChangGuanliActivity.this);
                                                build.setTitle("注意")
                                                        .setMessage("确定要删除吗？")
                                                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                                            @Override
                                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                                shanchujichang(jichang_name);
                                                            }
                                                        })
                                                        .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                                                            @Override
                                                            public void onClick(DialogInterface dialogInterface, int i) {

                                                            }
                                                        })
                                                        .show();


                                                break;
                                            default:break;
                                            case "编辑":
                                                bianjijichang(jichang_id,jichang_name,qifeifangxiang);
                                                break;
                                        }
                                    }
                                } )
                        .setNegativeButton("取消",
                                new DialogInterface.OnClickListener(){
                                    public void onClick(DialogInterface dialog,int which){

                                    }
                                }).show();
                return true;
            }
        });
    }

    private void shanchujichang(String jichang_name){

        sqLiteDatabase.execSQL("update JiChang set used = '0' where jichang_NAME = '"+jichang_name+"'");
        tianchonglistview();
    }

    private void xinzengjichang(){
        LayoutInflater factory = LayoutInflater.from(this);
        @SuppressLint("InflateParams") View textEntryView = factory.inflate(R.layout.jichangdetail,null);
        final EditText jichangnamedetailEditText = (EditText) textEntryView.findViewById(R.id.jichangname_detail_edittext);
        final RadioButton qifeifangxian_dongxi_checkbox = (RadioButton) textEntryView.findViewById(R.id.qifeifangxiang_dongxi_detail_checkbox);
        final RadioButton qifeifangxian_nanbei_checkbox = (RadioButton) textEntryView.findViewById(R.id.qifeifangxiang_nanbei_detail_checkbox);
        /*final CheckBox qifeifangxian_xi_checkbox = (CheckBox) textEntryView.findViewById(R.id.qifeifangxiang_xi_detail_checkbox);
        final CheckBox qifeifangxian_bei_checkbox = (CheckBox) textEntryView.findViewById(R.id.qifeifangxiang_bei_detail_checkbox);*/

        new AlertDialog.Builder(this)
                .setTitle("机场详细信息：")
                .setView(textEntryView)
                .setPositiveButton("确定",
                        new DialogInterface.OnClickListener(){
                            @Override
                            public void onClick(DialogInterface dialog,int which){
                                boolean dongxiChecked = qifeifangxian_dongxi_checkbox.isChecked();
                                boolean nanbeiChecked = qifeifangxian_nanbei_checkbox.isChecked();
                               /* boolean xiChecked = qifeifangxian_xi_checkbox.isChecked();
                                boolean beiChecked = qifeifangxian_bei_checkbox.isChecked();*/
                                String jichangname_insert = jichangnamedetailEditText.getText().toString();
                                String qifeifangxiang_insert = mgetQifeifangxiang_insert(dongxiChecked,nanbeiChecked);

                                ContentValues values = new ContentValues();
                                values.put("jichang_NAME",jichangname_insert);
                                values.put("qifeifangxiang",qifeifangxiang_insert);
                                values.put("used",1);
                                sqLiteDatabase.insert("JiChang","jichang_NAME",values);
                                tianchonglistview();

                            }
                        })
                .setNegativeButton("取消",new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialog,int which){
                        dialog.dismiss();
                    }
                }).show();
    }

    private void bianjijichang(String jichang_id,String jichangname,String qifeifangxiang){
        LayoutInflater factory = LayoutInflater.from(this);
        @SuppressLint("InflateParams") View textEntryView = factory.inflate(R.layout.jichangdetail,null);
        final EditText jichangnamedetailEditText = (EditText) textEntryView.findViewById(R.id.jichangname_detail_edittext);
        final RadioButton qifeifangxian_dongxi_checkbox = (RadioButton) textEntryView.findViewById(R.id.qifeifangxiang_dongxi_detail_checkbox);
        final RadioButton qifeifangxian_nanbei_checkbox = (RadioButton) textEntryView.findViewById(R.id.qifeifangxiang_nanbei_detail_checkbox);
        /*final CheckBox qifeifangxian_xi_checkbox = (CheckBox) textEntryView.findViewById(R.id.qifeifangxiang_xi_detail_checkbox);
        final CheckBox qifeifangxian_bei_checkbox = (CheckBox) textEntryView.findViewById(R.id.qifeifangxiang_bei_detail_checkbox);*/
        jichangnamedetailEditText.setText(jichangname);
        /*if (qifeifangxiang == null){
            qifeifangxiang = "";
        }*/

        if (qifeifangxiang.contains("东")){
            qifeifangxian_dongxi_checkbox.setChecked(true);
            qifeifangxian_nanbei_checkbox.setChecked(false);
        }else{
            qifeifangxian_dongxi_checkbox.setChecked(false);
            qifeifangxian_nanbei_checkbox.setChecked(true);
        }
        /*if (qifeifangxiang.contains("南")){
            qifeifangxian_nan_checkbox.setChecked(true);
        }else {
            qifeifangxian_nan_checkbox.setChecked(false);
        }
        if (qifeifangxiang.contains("西")){
            qifeifangxian_xi_checkbox.setChecked(true);
        }else {
            qifeifangxian_xi_checkbox.setChecked(false);
        }
        if (qifeifangxiang.contains("北")){
            qifeifangxian_bei_checkbox.setChecked(true);
        }else {
            final int id = Integer.parseInt(jichang_id);
        }*/
        final int id = Integer.parseInt(jichang_id);
        new AlertDialog.Builder(this)
                .setTitle("机场详细信息：")
                .setView(textEntryView)
                .setPositiveButton("确定",
                        new DialogInterface.OnClickListener(){
                            @Override
                            public void onClick(DialogInterface dialog,int which){
                                boolean dongxiChecked = qifeifangxian_dongxi_checkbox.isChecked();
                                boolean nanbeiChecked = qifeifangxian_nanbei_checkbox.isChecked();
                                /*boolean xiChecked = qifeifangxian_xi_checkbox.isChecked();
                                boolean beiChecked = qifeifangxian_bei_checkbox.isChecked();*/
                                String jichangname_insert = jichangnamedetailEditText.getText().toString();
                                String qifeifangxiang_insert = mgetQifeifangxiang_insert(dongxiChecked,nanbeiChecked);
                               /* String qifeifangxiang_insert = null;
                                if (qifeifangxian_dong_checkbox.isChecked()){
                                    qifeifangxiang_insert = qifeifangxian_dong_checkbox.getText().toString();
                                }
                                if (qifeifangxian_nan_checkbox.isChecked()){
                                    if (qifeifangxiang_insert == null){
                                        qifeifangxiang_insert = qifeifangxian_nan_checkbox.getText().toString();
                                    }else{
                                        qifeifangxiang_insert = qifeifangxiang_insert+"|"+qifeifangxian_nan_checkbox.getText().toString();
                                    }
                                }
                                if (qifeifangxian_xi_checkbox.isChecked()){
                                    if (qifeifangxiang_insert == null){
                                        qifeifangxiang_insert = qifeifangxian_xi_checkbox.getText().toString();
                                    }else{
                                        qifeifangxiang_insert = qifeifangxiang_insert+"|"+qifeifangxian_xi_checkbox.getText().toString();
                                    }
                                }
                                if (qifeifangxian_bei_checkbox.isChecked()){
                                    if (qifeifangxiang_insert == null){
                                        qifeifangxiang_insert = qifeifangxian_bei_checkbox.getText().toString();
                                    }else{
                                        qifeifangxiang_insert = qifeifangxiang_insert+"|"+qifeifangxian_bei_checkbox.getText().toString();
                                    }
                                }*/
                                sqLiteDatabase.execSQL("update JiChang set jichang_NAME = '"+jichangname_insert+"' where jichang_ID = "+id);
                                sqLiteDatabase.execSQL("update JiChang set qifeifangxiang = '"+qifeifangxiang_insert+"' where jichang_ID = "+id);
                                tianchonglistview();
                            }
                        })
                .setNegativeButton("取消",new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialog,int which){
                        dialog.dismiss();
                    }
                }).show();
    }

    private String mgetQifeifangxiang_insert(boolean dongxiChecked,boolean nanbeiChecked){
    String qifeifangxiang_insert = "";

        if (dongxiChecked){
            qifeifangxiang_insert = "东|西";
        }

        if (nanbeiChecked){
            qifeifangxiang_insert = "南|北";
        }
        if (!dongxiChecked & !nanbeiChecked ){
        qifeifangxiang_insert = "东|西";
        }
        if (BuildConfig.DEBUG) Log.d("ABC", "被选择的起飞方向是：" + qifeifangxiang_insert);
        return qifeifangxiang_insert;
    }

   /* public boolean nameexist(String name,String tablename,String father_Id){
        Cursor cursor = sqLiteDatabase.rawQuery("select * from "+tablename+" where hangxian_ID = '"+selected_hangxianid +"' and used = 1 order by shunxu", null);
        while (cursor4.moveToNext()) {
            String HangDianNanme = cursor4.getString(cursor4.getColumnIndex("hangdian_NAME"));
            hangdian_list.add(HangDianNanme);
        }
        cursor4.close();
    }*/

    private void tianchonglistview(){
        @SuppressLint("Recycle") Cursor cursor = sqLiteDatabase.rawQuery(getString(R.string.查询所有可用机场),null);
        ArrayList<HashMap<String, String>> jichanglist = new ArrayList<>();
        //jichanglist.clear();
        while (cursor.moveToNext()){
            String JiChang_ID = cursor.getString(cursor.getColumnIndex("jichang_ID"));
            String JiChangName = cursor.getString(cursor.getColumnIndex("jichang_NAME"));
            String QiFeiFangXiang = cursor.getString(cursor.getColumnIndex("qifeifangxiang"));
            //  jichang_info jc = new jichang_info (JiChangName,QiFeiFangXiang);
            HashMap<String,String> jichangmap = new HashMap<>();
            jichangmap.put("jichang_ID",JiChang_ID);
            jichangmap.put("jichangname",JiChangName);
            jichangmap.put("qifeifangxiang",QiFeiFangXiang);
            jichanglist.add(jichangmap);
            //jichanglist.add(jc);
        }

        SimpleAdapter jichangsimpleAdapter = new SimpleAdapter(this,
                jichanglist, R.layout.jichanglist,
                new String[]{"jichang_ID", "jichangname", "qifeifangxiang"},
                new int[]{R.id.jichang_id_textview, R.id.jichang_textview, R.id.qifeifangxiang_textview});
  /*      ListAdapter adapter = new BaseAdapter() {
            @Override
            public int getCount() {
                return jichanglist.size();
            }

            @Override
            public Object getItem(int position) {
                return null;
            }

            @Override
            public long getItemId(int position) {
                return 0;
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view;

                if (convertView==null){
                    view = View.inflate(getBaseContext(),R.layout.jichanglist,null);
                }
                else {
                    view = convertView;
                }

                jichang_info jc = jichanglist.get(position);
                TextView jichangTextView = (TextView)view.findViewById(R.id.jichang_textview);
                TextView qifeifangxiang1TextView= (TextView)view.findViewById(R.id.qifeifangxiang_textview);
                jichangTextView.setText(jc.getJiChangName());
                qifeifangxiang1TextView.setText(jc.getQiFeiFangXiang());
                return view;
            }
        };
        */
        jichanglistView.setAdapter(jichangsimpleAdapter);
    }















    /*    public class MyDatabaseHelper extends SQLiteOpenHelper {

    /*        public static final String CREATE_JICHANG = "create table JiChang(" +
                    "jichang_ID INTEGER primary key autoincrement," +
                    "jichang_NAME TEXT," +
                    "qifeifangxiang TEXT," +
                    "used INTEGER)";

            public static final String CREATE_HANGXIAN = "create table HangXian(" +
                    "hangxian_ID INTEGER primary key autoincrement," +
                    "hangxia_NAME TEXT," +
                    "jichang_ID TEXT," +
                    "used INTEGER," +
                    "FOREIGN KEY (jichang_ID) REFERENCES JiChang (jichang_ID))";

            public static final String CREATE_HANGDIAN= "create table HangDian(" +
                    "hangdian_ID INTEGER primary key autoincrement," +
                    "hangdian_NAME TEXT," +
                    "hangxian_ID INTEGER," +
                    "shunxu INTEGER,"+
                    "zhunshishijian REAL,"+
                    "zhunshishijiancha REAL,"+
                    "shifoutuji INTEGER,"+
                    "weidu REAL,"+
                    "jingdu REAL,"+
                    "used INTEGER," +
                    "FOREIGN KEY (hangxian_ID) REFERENCES HangXian (hangxian_ID))";

        private Context mContext;

        public MyDatabaseHelper(Context context, String name , SQLiteDatabase.CursorFactory factory,int version){
            super(context,name,factory,version);
            mContext = context;
        }

        @Override

        public void onCreate(SQLiteDatabase db){
        //    db.execSQL(CREATE_JICHANG);
         //   db.execSQL(CREATE_HANGXIAN);
         //   db.execSQL(CREATE_HANGDIAN);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db,int oldVersion,int newVersion){

        }



    }*/






  /*  public void writeDB(){
        f = getFilesDir()+"\\databases\\"+"shuju.db";
        FileOutputStream fout = null;
        InputStream inputStream = null;
        try {
            inputStream = getResources().openRawResource(R.raw.shuju);
            fout = new FileOutputStream(new File(f));
            byte[] buffer = new byte[128];
            int len = 0;
            while ((len = inputStream.read(buffer))!=-1){
                fout.write(buffer,0,len);
            }
            buffer = null;
        }catch (FileNotFoundException e){
            e.printStackTrace();
        }catch (IOException e){
            e.printStackTrace();
        }finally {
            if (fout != null){
                try {
                    fout.close();
                }catch (IOException e){
                    e.printStackTrace();
                }
            }
            if (inputStream !=null){
                try{
                    inputStream.close();
                }catch (IOException e){
                    e.printStackTrace();
                }
            }
        }
    }
*/

}
