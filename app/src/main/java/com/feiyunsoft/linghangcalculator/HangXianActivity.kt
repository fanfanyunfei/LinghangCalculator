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
import android.view.LayoutInflater
import android.view.View
import android.widget.AdapterView
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.SimpleAdapter

import java.util.ArrayList
import java.util.HashMap

import com.feiyunsoft.linghangcalculator.MainActivity.Companion.DATABASE_NAME
import com.feiyunsoft.linghangcalculator.MainActivity.Companion.DATABASE_PATH

class HangXianActivity : AppCompatActivity() {


    private var sqLiteDatabase: SQLiteDatabase? = null
    private var hangxianlistView: ListView? = null
    private var jichang_id: String? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.hang_xian_layout)

        val i = intent
        val b = i.getBundleExtra("jichangbd")
        jichang_id = b.getString("jichang_ID")

        //Intent intentresult = new Intent(HangXianActivity.this,MainActivity.class);
        //setResult(RESULT_OK,intentresult);

        val xinzengButton = findViewById(R.id.add_button) as Button
        hangxianlistView = findViewById(R.id.hangxian_listview) as ListView

        /*String DATABASE_PATH = getFilesDir() + "/databases";
        String DATABASE_NAME = "shuju.db";*/
        val f = MainActivity.DATABASE_PATH + "/" + MainActivity.DATABASE_NAME
        sqLiteDatabase = openOrCreateDatabase(f, Context.MODE_PRIVATE, null)

        tianchonglistview()
        itemLongClick_jichang()
        itemClick_jichang()
        xinzengButton.setOnClickListener { xinzenghangxian() }

    }


    private fun itemClick_jichang() {
        hangxianlistView!!.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
            val hangxianmap = hangxianlistView!!.getItemAtPosition(position) as HashMap<String, String>
            val hangxian_name = hangxianmap["hangxianname"]
            val hangxian_id = hangxianmap["hangxian_ID"]
            val intent = Intent(applicationContext, HangDianActivity::class.java)
            val bundle = Bundle()
            bundle.putString("hangxian_ID", hangxian_id)
            intent.putExtra("hangxianbd", bundle)
            startActivity(intent)
        }
    }

    private fun itemLongClick_jichang() {//对机场列表长按响应的方法
        hangxianlistView!!.onItemLongClickListener = AdapterView.OnItemLongClickListener { parent, view, position, id ->
            val jichangmap = hangxianlistView!!.getItemAtPosition(position) as HashMap<String, String>
            val hangxian_name = jichangmap["hangxianname"]
            val hangxian_id = jichangmap["hangxian_ID"]


            AlertDialog.Builder(this@HangXianActivity)
                    .setTitle("请选择：")
                    .setItems(R.array.bianji
                    ) { dialog, which ->
                        val PK = resources.getStringArray(R.array.bianji)
                        //Toast.makeText(JiChangGuanliActivity.this,PK[which],Toast.LENGTH_LONG).show();
                        when (PK[which]) {
                            "删除" -> {
                                val build = AlertDialog.Builder(this@HangXianActivity)
                                build.setTitle("注意")
                                        .setMessage("确定要删除吗？")
                                        .setPositiveButton("确定") { dialogInterface, i -> shanchu(hangxian_id) }
                                        .setNegativeButton("取消") { dialogInterface, i -> }
                                        .show()
                            }
                            "编辑" -> bianjijichang(hangxian_id, hangxian_name)
                            else -> {
                            }
                        }
                    }
                    .setNegativeButton("取消"
                    ) { dialog, which -> }.show()
            true
        }
    }

    private fun shanchu(hangxian_id: String?) {

        sqLiteDatabase!!.execSQL("update HangXian set used = '0' where hangxian_ID = '$hangxian_id'")
        tianchonglistview()
    }

    private fun xinzenghangxian() {
        val factory = LayoutInflater.from(this)
        @SuppressLint("InflateParams") val textEntryView = factory.inflate(R.layout.hangxiandetail, null)
        val hangxiannamedetailEditText = textEntryView.findViewById(R.id.hangxianname_detail_edittext) as EditText

        AlertDialog.Builder(this)
                .setTitle("航线名称：")
                .setView(textEntryView)
                .setPositiveButton("确定"
                ) { dialog, which ->
                    val hangxianname_insert = hangxiannamedetailEditText.text.toString()
                    val values = ContentValues()
                    values.put("hangxia_NAME", hangxianname_insert)
                    values.put("jichang_ID", jichang_id)
                    values.put("used", 1)
                    sqLiteDatabase!!.insert("HangXian", "hangxia_NAME", values)
                    tianchonglistview()
                }
                .setNegativeButton("取消") { dialog, which -> dialog.dismiss() }.show()
    }

    private fun bianjijichang(hangxian_id: String?, hangxian_name: String?) {
        val factory = LayoutInflater.from(this)
        @SuppressLint("InflateParams") val textEntryView = factory.inflate(R.layout.hangxiandetail, null)
        val hangxiannamedetailEditText = textEntryView.findViewById(R.id.hangxianname_detail_edittext) as EditText

        hangxiannamedetailEditText.setText(hangxian_name)

        val id = Integer.parseInt(hangxian_id)
        AlertDialog.Builder(this)
                .setTitle("航线名称：")
                .setView(textEntryView)
                .setPositiveButton("确定"
                ) { dialog, which ->
                    val hangxianname_insert = hangxiannamedetailEditText.text.toString()

                    sqLiteDatabase!!.execSQL("update HangXian set hangxia_NAME = '$hangxianname_insert' where hangxian_ID = $id")
                    tianchonglistview()
                }
                .setNegativeButton("取消") { dialog, which -> dialog.dismiss() }.show()
    }

    private fun tianchonglistview() {
        @SuppressLint("Recycle") val cursor = sqLiteDatabase!!.rawQuery("select * from HangXian where used = '1' and jichang_ID = '$jichang_id'", null)
        val hangxianlist = ArrayList<HashMap<String, String>>()
        //jichanglist.clear();
        while (cursor.moveToNext()) {
            val hangxian_ID = cursor.getString(cursor.getColumnIndex("hangxian_ID"))
            val hangxianName = cursor.getString(cursor.getColumnIndex("hangxia_NAME"))

            val jichangmap = HashMap<String, String>()
            jichangmap["hangxian_ID"] = hangxian_ID
            jichangmap["hangxianname"] = hangxianName
            hangxianlist.add(jichangmap)
        }

        val hangxiansimpleAdapter = SimpleAdapter(this,
                hangxianlist, R.layout.hangxianlist,
                arrayOf("hangxian_ID", "hangxianname"),
                intArrayOf(R.id.hangxian_id_textview, R.id.hangxian_textview))
        hangxianlistView!!.adapter = hangxiansimpleAdapter
    }

}
