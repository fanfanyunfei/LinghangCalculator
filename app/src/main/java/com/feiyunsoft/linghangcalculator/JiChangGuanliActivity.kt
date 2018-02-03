package com.feiyunsoft.linghangcalculator

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.database.sqlite.SQLiteDatabase
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.AdapterView
import android.widget.EditText
import android.widget.RadioButton
import android.widget.SimpleAdapter
import kotlinx.android.synthetic.main.jichangguanli_layout.*

import java.util.ArrayList
import java.util.HashMap


class JiChangGuanliActivity : AppCompatActivity() {
    private var sqLiteDatabase: SQLiteDatabase? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.jichangguanli_layout)



        val f = MainActivity.DATABASE_PATH + "/" + MainActivity.DATABASE_NAME
        sqLiteDatabase = openOrCreateDatabase(f, Context.MODE_PRIVATE, null)

        tianchonglistview()
        itemLongClick_jichang()
        itemClick_jichang()

        add_button.setOnClickListener { xinzengjichang() }

    }


    private fun itemClick_jichang() {
        jichang_listview!!.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
            val jichangmap = jichang_listview!!.getItemAtPosition(position) as HashMap<String, String>
            val jichang_id = jichangmap["jichang_ID"]
            val intent = Intent(applicationContext, HangXianActivity::class.java)
            val bundle = Bundle()
            bundle.putString("jichang_ID", jichang_id)
            intent.putExtra("jichangbd", bundle)
            startActivity(intent)
        }
    }

    private fun itemLongClick_jichang() {//对机场列表长按响应的方法
        jichang_listview!!.onItemLongClickListener = AdapterView.OnItemLongClickListener { parent, view, position, id ->
            val jichangmap = jichang_listview!!.getItemAtPosition(position) as HashMap<String, String>
            val jichang_name = jichangmap["jichangname"]
            val qifeifangxiang = jichangmap["qifeifangxiang"]
            val jichang_id = jichangmap["jichang_ID"]


            AlertDialog.Builder(this@JiChangGuanliActivity)
                    .setTitle("请选择：")
                    .setItems(R.array.bianji
                    ) { dialog, which ->
                        val PK = resources.getStringArray(R.array.bianji)
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

    private fun shanchujichang(jichang_name: String?) {

        sqLiteDatabase!!.execSQL("update JiChang set used = '0' where jichang_NAME = '$jichang_name'")
        tianchonglistview()
    }

    private fun xinzengjichang() {
        val factory = LayoutInflater.from(this)
        @SuppressLint("InflateParams") val textEntryView = factory.inflate(R.layout.jichangdetail, null)
        val jichangnamedetailEditText = textEntryView.findViewById(R.id.jichangname_detail_edittext) as EditText
        val qifeifangxian_dongxi_checkbox = textEntryView.findViewById(R.id.qifeifangxiang_dongxi_detail_checkbox) as RadioButton
        val qifeifangxian_nanbei_checkbox = textEntryView.findViewById(R.id.qifeifangxiang_nanbei_detail_checkbox) as RadioButton

        AlertDialog.Builder(this)
                .setTitle("机场详细信息：")
                .setView(textEntryView)
                .setPositiveButton("确定"
                ) { dialog, which ->
                    val dongxiChecked = qifeifangxian_dongxi_checkbox.isChecked
                    val nanbeiChecked = qifeifangxian_nanbei_checkbox.isChecked
                    val jichangname_insert = jichangnamedetailEditText.text.toString()
                    val qifeifangxiang_insert = mgetQifeifangxiangInsert(dongxiChecked, nanbeiChecked)

                    val values = ContentValues()
                    values.put("jichang_NAME", jichangname_insert)
                    values.put("qifeifangxiang", qifeifangxiang_insert)
                    values.put("used", 1)
                    sqLiteDatabase!!.insert("JiChang", "jichang_NAME", values)
                    tianchonglistview()
                }
                .setNegativeButton("取消") { dialog, which -> dialog.dismiss() }.show()
    }

    private fun bianjijichang(jichang_id: String?, jichangname: String?, qifeifangxiang: String?) {
        val factory = LayoutInflater.from(this)
        @SuppressLint("InflateParams") val textEntryView = factory.inflate(R.layout.jichangdetail, null)
        val jichangnamedetailEditText = textEntryView.findViewById(R.id.jichangname_detail_edittext) as EditText
        val qifeifangxianDongxiCheckbox = textEntryView.findViewById(R.id.qifeifangxiang_dongxi_detail_checkbox) as RadioButton
        val qifeifangxianNanbeiCheckbox = textEntryView.findViewById(R.id.qifeifangxiang_nanbei_detail_checkbox) as RadioButton
        jichangnamedetailEditText.setText(jichangname)

        if (qifeifangxiang!!.contains("东")) {
            qifeifangxianDongxiCheckbox.isChecked = true
            qifeifangxianNanbeiCheckbox.isChecked = false
        } else {
            qifeifangxianDongxiCheckbox.isChecked = false
            qifeifangxianNanbeiCheckbox.isChecked = true
        }
        val id = Integer.parseInt(jichang_id)
        AlertDialog.Builder(this)
                .setTitle("机场详细信息：")
                .setView(textEntryView)
                .setPositiveButton("确定"
                ) { dialog, which ->
                    val dongxiChecked = qifeifangxianDongxiCheckbox.isChecked
                    val nanbeiChecked = qifeifangxianNanbeiCheckbox.isChecked
                    val jichangnameInsert = jichangnamedetailEditText.text.toString()
                    val qifeifangxiangInsert = mgetQifeifangxiangInsert(dongxiChecked, nanbeiChecked)

                    sqLiteDatabase!!.execSQL("update JiChang set jichang_NAME = '$jichangnameInsert' where jichang_ID = $id")
                    sqLiteDatabase!!.execSQL("update JiChang set qifeifangxiang = '$qifeifangxiangInsert' where jichang_ID = $id")
                    tianchonglistview()
                }
                .setNegativeButton("取消") { dialog, which -> dialog.dismiss() }.show()
    }

    private fun mgetQifeifangxiangInsert(dongxiChecked: Boolean, nanbeiChecked: Boolean): String {
        var qifeifangxiangInsert = ""

        if (dongxiChecked) {
            qifeifangxiangInsert = "东|西"
        }

        if (nanbeiChecked) {
            qifeifangxiangInsert = "南|北"
        }
        if (!dongxiChecked and !nanbeiChecked) {
            qifeifangxiangInsert = "东|西"
        }
        if (BuildConfig.DEBUG) Log.d("ABC", "被选择的起飞方向是：" + qifeifangxiangInsert)
        return qifeifangxiangInsert
    }


    private fun tianchonglistview() {
        @SuppressLint("Recycle") val cursor = sqLiteDatabase!!.rawQuery(getString(R.string.查询所有可用机场), null)
        val jichanglist = ArrayList<HashMap<String, String>>()
        while (cursor.moveToNext()) {
            val jiChangID = cursor.getString(cursor.getColumnIndex("jichang_ID"))
            val jiChangName = cursor.getString(cursor.getColumnIndex("jichang_NAME"))
            val qiFeiFangXiang = cursor.getString(cursor.getColumnIndex("qifeifangxiang"))
            val jichangmap = HashMap<String, String>()
            jichangmap["jichang_ID"] = jiChangID
            jichangmap["jichangname"] = jiChangName
            jichangmap["qifeifangxiang"] = qiFeiFangXiang
            jichanglist.add(jichangmap)
        }
        cursor.close()
        val jichangsimpleAdapter = SimpleAdapter(this,
                jichanglist, R.layout.jichanglist,
                arrayOf("jichang_ID", "jichangname", "qifeifangxiang"),
                intArrayOf(R.id.jichang_id_textview, R.id.jichang_textview, R.id.qifeifangxiang_textview))
        jichang_listview!!.adapter = jichangsimpleAdapter
    }
}