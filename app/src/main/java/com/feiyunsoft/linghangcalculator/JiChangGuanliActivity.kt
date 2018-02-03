package com.feiyunsoft.linghangcalculator

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.AdapterView
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.RadioButton
import android.widget.SimpleAdapter

import java.util.ArrayList
import java.util.HashMap

import com.feiyunsoft.linghangcalculator.MainActivity.Companion.DATABASE_NAME
import com.feiyunsoft.linghangcalculator.MainActivity.Companion.DATABASE_PATH


class JiChangGuanliActivity : AppCompatActivity() {
    private var sqLiteDatabase: SQLiteDatabase? = null
    private var jichanglistView: ListView? = null
    /*    public static String DATABASE_PATH;
    public static String DATABASE_NAME;*/


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.jichangguanli_layout)

        val xinzengButton = findViewById(R.id.add_button) as Button
        jichanglistView = findViewById(R.id.jichang_listview) as ListView


        val f = Companion.getDATABASE_PATH() + "/" + Companion.getDATABASE_NAME()
        sqLiteDatabase = openOrCreateDatabase(f, Context.MODE_PRIVATE, null)

        tianchonglistview()
        itemLongClick_jichang()
        itemClick_jichang()

        xinzengButton.setOnClickListener { xinzengjichang() }

    }


    private fun itemClick_jichang() {
        jichanglistView!!.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
            val jichangmap = jichanglistView!!.getItemAtPosition(position) as HashMap<String, String>
            /*final String jichang_name = jichangmap.get("jichangname");
                final String qifeifangxiang = jichangmap.get("qifeifangxiang");*/
            val jichang_id = jichangmap["jichang_ID"]
            val intent = Intent(applicationContext, HangXianActivity::class.java)
            val bundle = Bundle()
            bundle.putString("jichang_ID", jichang_id)
            intent.putExtra("jichangbd", bundle)
            startActivity(intent)
        }
    }

    private fun itemLongClick_jichang() {//对机场列表长按响应的方法
        jichanglistView!!.onItemLongClickListener = AdapterView.OnItemLongClickListener { parent, view, position, id ->
            val jichangmap = jichanglistView!!.getItemAtPosition(position) as HashMap<String, String>
            val jichang_name = jichangmap["jichangname"]
            val qifeifangxiang = jichangmap["qifeifangxiang"]
            val jichang_id = jichangmap["jichang_ID"]


            AlertDialog.Builder(this@JiChangGuanliActivity)
                    .setTitle("请选择：")
                    .setItems(R.array.bianji
                    ) { dialog, which ->
                        val PK = resources.getStringArray(R.array.bianji)
                        //Toast.makeText(JiChangGuanliActivity.this,PK[which],Toast.LENGTH_LONG).show();
                        when (PK[which]) {
                            "删除" -> {
                                val build = AlertDialog.Builder(this@JiChangGuanliActivity)
                                build.setTitle("注意")
                                        .setMessage("确定要删除吗？")
                                        .setPositiveButton("确定") { dialogInterface, i -> shanchujichang(jichang_name) }
                                        .setNegativeButton("取消") { dialogInterface, i -> }
                                        .show()
                            }
                            "编辑" -> bianjijichang(jichang_id, jichang_name, qifeifangxiang)
                            else -> {
                            }
                        }
                    }
                    .setNegativeButton("取消"
                    ) { dialog, which -> }.show()
            true
        }
    }

    private fun shanchujichang(jichang_name: String) {

        sqLiteDatabase!!.execSQL("update JiChang set used = '0' where jichang_NAME = '$jichang_name'")
        tianchonglistview()
    }

    private fun xinzengjichang() {
        val factory = LayoutInflater.from(this)
        @SuppressLint("InflateParams") val textEntryView = factory.inflate(R.layout.jichangdetail, null)
        val jichangnamedetailEditText = textEntryView.findViewById(R.id.jichangname_detail_edittext) as EditText
        val qifeifangxian_dongxi_checkbox = textEntryView.findViewById(R.id.qifeifangxiang_dongxi_detail_checkbox) as RadioButton
        val qifeifangxian_nanbei_checkbox = textEntryView.findViewById(R.id.qifeifangxiang_nanbei_detail_checkbox) as RadioButton
        /*final CheckBox qifeifangxian_xi_checkbox = (CheckBox) textEntryView.findViewById(R.id.qifeifangxiang_xi_detail_checkbox);
        final CheckBox qifeifangxian_bei_checkbox = (CheckBox) textEntryView.findViewById(R.id.qifeifangxiang_bei_detail_checkbox);*/

        AlertDialog.Builder(this)
                .setTitle("机场详细信息：")
                .setView(textEntryView)
                .setPositiveButton("确定"
                ) { dialog, which ->
                    val dongxiChecked = qifeifangxian_dongxi_checkbox.isChecked
                    val nanbeiChecked = qifeifangxian_nanbei_checkbox.isChecked
                    /* boolean xiChecked = qifeifangxian_xi_checkbox.isChecked();
                                boolean beiChecked = qifeifangxian_bei_checkbox.isChecked();*/
                    val jichangname_insert = jichangnamedetailEditText.text.toString()
                    val qifeifangxiang_insert = mgetQifeifangxiang_insert(dongxiChecked, nanbeiChecked)

                    val values = ContentValues()
                    values.put("jichang_NAME", jichangname_insert)
                    values.put("qifeifangxiang", qifeifangxiang_insert)
                    values.put("used", 1)
                    sqLiteDatabase!!.insert("JiChang", "jichang_NAME", values)
                    tianchonglistview()
                }
                .setNegativeButton("取消") { dialog, which -> dialog.dismiss() }.show()
    }

    private fun bianjijichang(jichang_id: String, jichangname: String, qifeifangxiang: String) {
        val factory = LayoutInflater.from(this)
        @SuppressLint("InflateParams") val textEntryView = factory.inflate(R.layout.jichangdetail, null)
        val jichangnamedetailEditText = textEntryView.findViewById(R.id.jichangname_detail_edittext) as EditText
        val qifeifangxian_dongxi_checkbox = textEntryView.findViewById(R.id.qifeifangxiang_dongxi_detail_checkbox) as RadioButton
        val qifeifangxian_nanbei_checkbox = textEntryView.findViewById(R.id.qifeifangxiang_nanbei_detail_checkbox) as RadioButton
        /*final CheckBox qifeifangxian_xi_checkbox = (CheckBox) textEntryView.findViewById(R.id.qifeifangxiang_xi_detail_checkbox);
        final CheckBox qifeifangxian_bei_checkbox = (CheckBox) textEntryView.findViewById(R.id.qifeifangxiang_bei_detail_checkbox);*/
        jichangnamedetailEditText.setText(jichangname)
        /*if (qifeifangxiang == null){
            qifeifangxiang = "";
        }*/

        if (qifeifangxiang.contains("东")) {
            qifeifangxian_dongxi_checkbox.isChecked = true
            qifeifangxian_nanbei_checkbox.isChecked = false
        } else {
            qifeifangxian_dongxi_checkbox.isChecked = false
            qifeifangxian_nanbei_checkbox.isChecked = true
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
        val id = Integer.parseInt(jichang_id)
        AlertDialog.Builder(this)
                .setTitle("机场详细信息：")
                .setView(textEntryView)
                .setPositiveButton("确定"
                ) { dialog, which ->
                    val dongxiChecked = qifeifangxian_dongxi_checkbox.isChecked
                    val nanbeiChecked = qifeifangxian_nanbei_checkbox.isChecked
                    /*boolean xiChecked = qifeifangxian_xi_checkbox.isChecked();
                                boolean beiChecked = qifeifangxian_bei_checkbox.isChecked();*/
                    val jichangname_insert = jichangnamedetailEditText.text.toString()
                    val qifeifangxiang_insert = mgetQifeifangxiang_insert(dongxiChecked, nanbeiChecked)
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
                    sqLiteDatabase!!.execSQL("update JiChang set jichang_NAME = '$jichangname_insert' where jichang_ID = $id")
                    sqLiteDatabase!!.execSQL("update JiChang set qifeifangxiang = '$qifeifangxiang_insert' where jichang_ID = $id")
                    tianchonglistview()
                }
                .setNegativeButton("取消") { dialog, which -> dialog.dismiss() }.show()
    }

    private fun mgetQifeifangxiang_insert(dongxiChecked: Boolean, nanbeiChecked: Boolean): String {
        var qifeifangxiang_insert = ""

        if (dongxiChecked) {
            qifeifangxiang_insert = "东|西"
        }

        if (nanbeiChecked) {
            qifeifangxiang_insert = "南|北"
        }
        if (!dongxiChecked and !nanbeiChecked) {
            qifeifangxiang_insert = "东|西"
        }
        if (BuildConfig.DEBUG) Log.d("ABC", "被选择的起飞方向是：" + qifeifangxiang_insert)
        return qifeifangxiang_insert
    }

    /* public boolean nameexist(String name,String tablename,String father_Id){
        Cursor cursor = sqLiteDatabase.rawQuery("select * from "+tablename+" where hangxian_ID = '"+selected_hangxianid +"' and used = 1 order by shunxu", null);
        while (cursor4.moveToNext()) {
            String HangDianNanme = cursor4.getString(cursor4.getColumnIndex("hangdian_NAME"));
            hangdian_list.add(HangDianNanme);
        }
        cursor4.close();
    }*/

    private fun tianchonglistview() {
        @SuppressLint("Recycle") val cursor = sqLiteDatabase!!.rawQuery(getString(R.string.查询所有可用机场), null)
        val jichanglist = ArrayList<HashMap<String, String>>()
        //jichanglist.clear();
        while (cursor.moveToNext()) {
            val JiChang_ID = cursor.getString(cursor.getColumnIndex("jichang_ID"))
            val JiChangName = cursor.getString(cursor.getColumnIndex("jichang_NAME"))
            val QiFeiFangXiang = cursor.getString(cursor.getColumnIndex("qifeifangxiang"))
            //  jichang_info jc = new jichang_info (JiChangName,QiFeiFangXiang);
            val jichangmap = HashMap<String, String>()
            jichangmap["jichang_ID"] = JiChang_ID
            jichangmap["jichangname"] = JiChangName
            jichangmap["qifeifangxiang"] = QiFeiFangXiang
            jichanglist.add(jichangmap)
            //jichanglist.add(jc);
        }

        val jichangsimpleAdapter = SimpleAdapter(this,
                jichanglist, R.layout.jichanglist,
                arrayOf("jichang_ID", "jichangname", "qifeifangxiang"),
                intArrayOf(R.id.jichang_id_textview, R.id.jichang_textview, R.id.qifeifangxiang_textview))
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
        jichanglistView!!.adapter = jichangsimpleAdapter
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
