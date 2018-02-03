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
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.widget.AdapterView
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.SimpleAdapter
import android.widget.TextView

import java.util.ArrayList
import java.util.Collections
import java.util.HashMap

import com.feiyunsoft.linghangcalculator.MainActivity.Companion.DATABASE_NAME
import com.feiyunsoft.linghangcalculator.MainActivity.Companion.DATABASE_PATH

class HangDianActivity : AppCompatActivity() {

    private var sqLiteDatabase: SQLiteDatabase? = null
    private var hangdianlistView: ListView? = null
    private var hangxian_id: String? = null
    private var fangxiang: String? = null
    /*    private static String DATABASE_PATH;
    private static String DATABASE_NAME;*/


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.hang_dian_layout)

        val i = intent
        val b = i.getBundleExtra("hangxianbd")
        hangxian_id = b.getString("hangxian_ID")

        val xinzengButton = findViewById(R.id.add_button) as Button
        hangdianlistView = findViewById(R.id.hangdian_listview) as ListView
        val zhunshi1 = findViewById(R.id.zhunshi1) as TextView
        val zhunshi2 = findViewById(R.id.zhunshi2) as TextView

        Companion.setDATABASE_PATH(filesDir.toString() + "/databases")
        Companion.setDATABASE_NAME("shuju.db")
        val f = Companion.getDATABASE_PATH() + "/" + Companion.getDATABASE_NAME()
        sqLiteDatabase = openOrCreateDatabase(f, Context.MODE_PRIVATE, null)

        fangxiang = qifeifangxiang()
        if (fangxiang!!.contains("东")) {
            zhunshi1.text = "向东准时"
            zhunshi2.text = "向西准时"
        } else {
            zhunshi1.text = "向南准时"
            zhunshi2.text = "向北准时"
        }

        tianchonglistview()
        itemLongClick_hangdian()
        itemClick_hangdian()

        xinzengButton.setOnClickListener { xinzenghangdian() }

    }

    private fun itemClick_hangdian() {
        hangdianlistView!!.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
            val hangdianmap = hangdianlistView!!.getItemAtPosition(position) as HashMap<String, String>
            bianjihangdian(hangdianmap)
        }
    }

    private fun itemLongClick_hangdian() {//对机场列表长按响应的方法
        hangdianlistView!!.onItemLongClickListener = AdapterView.OnItemLongClickListener { parent, view, position, id ->
            val hangdianmap = hangdianlistView!!.getItemAtPosition(position) as HashMap<String, String>
            val hangdian_name = hangdianmap["hangdianname"]
            val hangdian_id = hangdianmap["hangdian_ID"]


            AlertDialog.Builder(this@HangDianActivity)
                    .setTitle("来吧来吧")
                    .setItems(R.array.bianji
                    ) { dialog, which ->
                        val PK = resources.getStringArray(R.array.bianji)
                        //Toast.makeText(JiChangGuanliActivity.this,PK[which],Toast.LENGTH_LONG).show();
                        when (PK[which]) {
                            "编辑" -> bianjihangdian(hangdianmap)
                            "删除" -> {

                                val build = AlertDialog.Builder(this@HangDianActivity)
                                build.setTitle("注意")
                                        .setMessage("确定要删除吗？")
                                        .setPositiveButton("确定") { dialogInterface, i -> shanchuhangdian(hangdian_id, hangdianmap) }
                                        .setNegativeButton("取消") { dialogInterface, i -> }
                                        .show()
                            }

                            else -> {
                            }
                        }
                    }
                    .setNegativeButton("取消"
                    ) { dialog, which -> }.show()
            true
        }
    }

    private fun shanchuhangdian(hangdian_id: String, hangdianmap: HashMap<String, String>) {

        sqLiteDatabase!!.execSQL("update HangDian set used = '0' where hangdian_ID = '$hangdian_id'")


        val i = Integer.parseInt(hangdianmap["hangdianshunxu"])//选中航点的顺序为i
        val cursor1 = sqLiteDatabase!!.rawQuery("select shunxu from HangDian where used = '1' and hangxian_ID = '$hangxian_id'", null)
        while (cursor1.moveToNext()) {
            val j = cursor1.getInt(cursor1.getColumnIndex("shunxu"))
            if (j > i) {
                val a = j - 1
                sqLiteDatabase!!.execSQL("update HangDian set shunxu = '$a' where shunxu = '$j'and hangxian_ID = '$hangxian_id' and used = '1'")
            }
        }
        cursor1.close()


        tianchonglistview()
    }

    @SuppressLint("SetTextI18n")
    private fun xinzenghangdian() {
        val factory = LayoutInflater.from(this)
        @SuppressLint("InflateParams") val textEntryView = factory.inflate(R.layout.hangdiandetail, null)
        val hangdiannamedetailEditText = textEntryView.findViewById(R.id.hangdianname_detail_edittext) as EditText
        val hangdianshunxudetailEditText = textEntryView.findViewById(R.id.hangdianshunxu_detail_edittext) as EditText
        val weidu_detailEditText = textEntryView.findViewById(R.id.weidu_detail_edittext) as EditText
        val jingdu_detailEditText = textEntryView.findViewById(R.id.jingdu_detail_edittext) as EditText
        val zhunshi_dong_detailEditText = textEntryView.findViewById(R.id.zhunshi_dong_detail_edittext) as EditText
        val zhunshi_dong_tuji_detailEditText = textEntryView.findViewById(R.id.tujizhunshi_dong_detail_edittext) as EditText
        val zhunshi_nan_detailEditText = textEntryView.findViewById(R.id.zhunshi_nan_detail_edittext) as EditText
        val zhunshi_nan_tuji_detailEditText = textEntryView.findViewById(R.id.tujizhunshi_nan_detail_edittext) as EditText
        val zhunshi_xi_detailEditText = textEntryView.findViewById(R.id.zhunshi_xi_detail_edittext) as EditText
        val zhunshi_xi_tuji_detailEditText = textEntryView.findViewById(R.id.tujizhunshi_xi_detail_edittext) as EditText
        val zhunshi_bei_detailEditText = textEntryView.findViewById(R.id.zhunshi_bei_detail_edittext) as EditText
        val zhunshi_bei_tuji_detailEditText = textEntryView.findViewById(R.id.tujizhunshi_bei_detail_edittext) as EditText
        /*final CheckBox dong_checkbox = (CheckBox) textEntryView.findViewById(R.id.tujizhunshi_dong_detail_checkbox);
        final CheckBox nan_checkbox = (CheckBox) textEntryView.findViewById(R.id.tujizhunshi_nan_detail_checkbox);
        final CheckBox xi_checkbox = (CheckBox) textEntryView.findViewById(R.id.tujizhunshi_xi_detail_checkbox);
        final CheckBox bei_checkbox = (CheckBox) textEntryView.findViewById(R.id.tujizhunshi_bei_detail_checkbox);*/
        val xiangdong = textEntryView.findViewById(R.id.qifeifangxiang_dong_detail_tv) as TextView
        val xiangnan = textEntryView.findViewById(R.id.qifeifangxiang_nan_detail_tv) as TextView
        val xiangxi = textEntryView.findViewById(R.id.qifeifangxiang_xi_detail_tv) as TextView
        val xiangbei = textEntryView.findViewById(R.id.qifeifangxiang_bei_detail_tv) as TextView
        val xiangdongli = textEntryView.findViewById(R.id.xiangdong_li) as LinearLayout
        val xiangnanli = textEntryView.findViewById(R.id.xiangnan_li) as LinearLayout
        val xiangxili = textEntryView.findViewById(R.id.xiangxi_li) as LinearLayout
        val xiangbeili = textEntryView.findViewById(R.id.xiangbei_li) as LinearLayout

        if (fangxiang!!.contains("东")) {
            xiangdong.visibility = View.VISIBLE
            xiangdongli.visibility = View.VISIBLE
            xiangxi.visibility = View.VISIBLE
            xiangxili.visibility = View.VISIBLE
        } else {
            xiangnan.visibility = View.VISIBLE
            xiangnanli.visibility = View.VISIBLE
            xiangbei.visibility = View.VISIBLE
            xiangbeili.visibility = View.VISIBLE
        }


        val shunxu = findzuidashunxu(hangxian_id)!! + 1
        hangdianshunxudetailEditText.setText("" + shunxu)

        AlertDialog.Builder(this)
                .setTitle("航点：")
                .setView(textEntryView)
                .setPositiveButton("确定"
                ) { dialog, which ->
                    val hangdianname_insert = hangdiannamedetailEditText.text.toString()
                    val shunxu_insert = hangdianshunxudetailEditText.text.toString()
                    val weidu_insert = jingweiToDouble(weidu_detailEditText.text.toString())
                    val jingdu_insert = jingweiToDouble(jingdu_detailEditText.text.toString())
                    val zhunshidong = zhunshi_dong_detailEditText.text.toString()
                    val zhunshinan = zhunshi_nan_detailEditText.text.toString()
                    val zhunshixi = zhunshi_xi_detailEditText.text.toString()
                    val zhunshibei = zhunshi_bei_detailEditText.text.toString()
                    val tujidong: String
                    val tujinan: String
                    val tujixi: String
                    val tujibei: String

                    if (!TextUtils.isEmpty(zhunshi_dong_tuji_detailEditText.text)) {
                        tujidong = zhunshi_dong_tuji_detailEditText.text.toString()
                    } else {
                        tujidong = zhunshidong
                    }

                    if (!TextUtils.isEmpty(zhunshi_nan_tuji_detailEditText.text)) {
                        tujinan = zhunshi_nan_tuji_detailEditText.text.toString()
                    } else {
                        tujinan = zhunshinan
                    }

                    if (!TextUtils.isEmpty(zhunshi_xi_tuji_detailEditText.text)) {
                        tujixi = zhunshi_xi_tuji_detailEditText.text.toString()
                    } else {
                        tujixi = zhunshixi
                    }
                    if (!TextUtils.isEmpty(zhunshi_bei_tuji_detailEditText.text)) {
                        tujibei = zhunshi_bei_tuji_detailEditText.text.toString()
                    } else {
                        tujibei = zhunshibei
                    }


                    val zhunshishijiancha = Zhunshishijiancha(zhunshidong, zhunshinan, zhunshixi, zhunshibei)
                    val hangdianzhunshi_insert = zhunshishijiancha.zuixiaozhunshi
                    val shijiancha_insert = zhunshishijiancha.zhunshicha

                    val tujishijiancha = Zhunshishijiancha(tujidong, tujinan, tujixi, tujibei)
                    val tujishijian_insert = tujishijiancha.zuixiaozhunshi
                    val tujishijiancha_insert = tujishijiancha.zhunshicha


                    val values = ContentValues()
                    values.put("hangdian_NAME", hangdianname_insert)
                    values.put("hangxian_ID", hangxian_id)
                    values.put("shunxu", shunxu_insert)
                    values.put("zhunshishijian", hangdianzhunshi_insert)
                    values.put("zhunshishijiancha", shijiancha_insert)
                    values.put("tujishijian", tujishijian_insert)
                    values.put("tujishijiancha", tujishijiancha_insert)
                    values.put("weidu", weidu_insert)
                    values.put("jingdu", jingdu_insert)
                    values.put("used", 1)
                    xiugaishunxu("" + shunxu, shunxu_insert)
                    sqLiteDatabase!!.insert("HangDian", "hangdian_NAME", values)
                    tianchonglistview()
                }
                .setNegativeButton("取消") { dialog, which -> dialog.dismiss() }.show()

    }

    private fun bianjihangdian(hangdianmap: HashMap<String, String>) {
        val factory = LayoutInflater.from(this)
        @SuppressLint("InflateParams") val textEntryView = factory.inflate(R.layout.hangdiandetail, null)
        val hangdiannamedetailEditText = textEntryView.findViewById(R.id.hangdianname_detail_edittext) as EditText
        val hangdianshunxudetailEditText = textEntryView.findViewById(R.id.hangdianshunxu_detail_edittext) as EditText
        val weidu_detailEditText = textEntryView.findViewById(R.id.weidu_detail_edittext) as EditText
        val jingdu_detailEditText = textEntryView.findViewById(R.id.jingdu_detail_edittext) as EditText
        val zhunshi_dong_detailEditText = textEntryView.findViewById(R.id.zhunshi_dong_detail_edittext) as EditText
        val zhunshi_dong_tuji_detailEditText = textEntryView.findViewById(R.id.tujizhunshi_dong_detail_edittext) as EditText
        val zhunshi_nan_detailEditText = textEntryView.findViewById(R.id.zhunshi_nan_detail_edittext) as EditText
        val zhunshi_nan_tuji_detailEditText = textEntryView.findViewById(R.id.tujizhunshi_nan_detail_edittext) as EditText
        val zhunshi_xi_detailEditText = textEntryView.findViewById(R.id.zhunshi_xi_detail_edittext) as EditText
        val zhunshi_xi_tuji_detailEditText = textEntryView.findViewById(R.id.tujizhunshi_xi_detail_edittext) as EditText
        val zhunshi_bei_detailEditText = textEntryView.findViewById(R.id.zhunshi_bei_detail_edittext) as EditText
        val zhunshi_bei_tuji_detailEditText = textEntryView.findViewById(R.id.tujizhunshi_bei_detail_edittext) as EditText
        /*final CheckBox dong_checkbox = (CheckBox) textEntryView.findViewById(R.id.tujizhunshi_dong_detail_checkbox);
        final CheckBox nan_checkbox = (CheckBox) textEntryView.findViewById(R.id.tujizhunshi_nan_detail_checkbox);
        final CheckBox xi_checkbox = (CheckBox) textEntryView.findViewById(R.id.tujizhunshi_xi_detail_checkbox);
        final CheckBox bei_checkbox = (CheckBox) textEntryView.findViewById(R.id.tujizhunshi_bei_detail_checkbox);*/
        val xiangdong = textEntryView.findViewById(R.id.qifeifangxiang_dong_detail_tv) as TextView
        val xiangnan = textEntryView.findViewById(R.id.qifeifangxiang_nan_detail_tv) as TextView
        val xiangxi = textEntryView.findViewById(R.id.qifeifangxiang_xi_detail_tv) as TextView
        val xiangbei = textEntryView.findViewById(R.id.qifeifangxiang_bei_detail_tv) as TextView
        val xiangdongli = textEntryView.findViewById(R.id.xiangdong_li) as LinearLayout
        val xiangnanli = textEntryView.findViewById(R.id.xiangnan_li) as LinearLayout
        val xiangxili = textEntryView.findViewById(R.id.xiangxi_li) as LinearLayout
        val xiangbeili = textEntryView.findViewById(R.id.xiangbei_li) as LinearLayout

        val hangdian_name = hangdianmap["hangdianname"]
        val hangdian_id = hangdianmap["hangdian_ID"]
        val hangdianshunxu = hangdianmap["hangdianshunxu"]
        val hangdianweidu = hangdianmap["hangdianweidu"]
        val hangdianjingdu = hangdianmap["hangdianjingdu"]
        val hangdianzhunshi1 = hangdianmap["hangdianzhunshi1"]
        val hangdianzhunshi1_tuji = hangdianmap["hangdianzhunshi1tuji"]
        val hangdianzhunshi2 = hangdianmap["hangdianzhunshi2"]
        val hangdianzhunshi2_tuji = hangdianmap["hangdianzhunshi2tuji"]


        hangdiannamedetailEditText.setText(hangdian_name)
        hangdianshunxudetailEditText.setText("0")
        weidu_detailEditText.setText(hangdianweidu)
        jingdu_detailEditText.setText(hangdianjingdu)
        hangdianshunxudetailEditText.setText(hangdianshunxu)


        if (fangxiang!!.contains("东")) {
            xiangdong.visibility = View.VISIBLE
            xiangdongli.visibility = View.VISIBLE
            zhunshi_dong_detailEditText.setText(hangdianzhunshi1)
            zhunshi_dong_tuji_detailEditText.setText(hangdianzhunshi1_tuji)
            xiangxi.visibility = View.VISIBLE
            xiangxili.visibility = View.VISIBLE
            zhunshi_xi_detailEditText.setText(hangdianzhunshi2)
            zhunshi_xi_tuji_detailEditText.setText(hangdianzhunshi2_tuji)
        } else {
            xiangnan.visibility = View.VISIBLE
            xiangnanli.visibility = View.VISIBLE
            zhunshi_nan_detailEditText.setText(hangdianzhunshi1)
            zhunshi_nan_tuji_detailEditText.setText(hangdianzhunshi1_tuji)
            xiangbei.visibility = View.VISIBLE
            xiangbeili.visibility = View.VISIBLE
            zhunshi_bei_detailEditText.setText(hangdianzhunshi2)
            zhunshi_bei_tuji_detailEditText.setText(hangdianzhunshi2_tuji)
        }



        AlertDialog.Builder(this)
                .setTitle("航点编辑：")
                .setView(textEntryView)
                .setPositiveButton("确定"
                ) { dialog, which ->
                    val hangdianname_insert = hangdiannamedetailEditText.text.toString()
                    val shunxu_insert = hangdianshunxudetailEditText.text.toString()
                    val weidu_insert = jingweiToDouble(weidu_detailEditText.text.toString())
                    val jingdu_insert = jingweiToDouble(jingdu_detailEditText.text.toString())
                    val zhunshidong = zhunshi_dong_detailEditText.text.toString()
                    val zhunshinan = zhunshi_nan_detailEditText.text.toString()
                    val zhunshixi = zhunshi_xi_detailEditText.text.toString()
                    val zhunshibei = zhunshi_bei_detailEditText.text.toString()
                    var tujidong = zhunshi_dong_tuji_detailEditText.text.toString()
                    var tujinan = zhunshi_nan_tuji_detailEditText.text.toString()
                    var tujixi = zhunshi_xi_tuji_detailEditText.text.toString()
                    var tujibei = zhunshi_bei_tuji_detailEditText.text.toString()
                    //      Integer shifoutuji_insert = 0 ;


                    if (!TextUtils.isEmpty(zhunshi_dong_tuji_detailEditText.text)) {
                        tujidong = zhunshi_dong_tuji_detailEditText.text.toString()
                    } else {
                        tujidong = zhunshidong
                    }

                    if (!TextUtils.isEmpty(zhunshi_nan_tuji_detailEditText.text)) {
                        tujinan = zhunshi_nan_tuji_detailEditText.text.toString()
                    } else {
                        tujinan = zhunshinan
                    }

                    if (!TextUtils.isEmpty(zhunshi_xi_tuji_detailEditText.text)) {
                        tujixi = zhunshi_xi_tuji_detailEditText.text.toString()
                    } else {
                        tujixi = zhunshixi
                    }

                    if (!TextUtils.isEmpty(zhunshi_bei_tuji_detailEditText.text)) {
                        tujibei = zhunshi_bei_tuji_detailEditText.text.toString()
                    } else {
                        tujibei = zhunshibei
                    }


                    val zhunshishijiancha = Zhunshishijiancha(zhunshidong, zhunshinan, zhunshixi, zhunshibei)
                    val hangdianzhunshi_insert = zhunshishijiancha.zuixiaozhunshi
                    val shijiancha_insert = zhunshishijiancha.zhunshicha

                    val tujishijiancha = Zhunshishijiancha(tujidong, tujinan, tujixi, tujibei)
                    val tujishijian_insert = tujishijiancha.zuixiaozhunshi
                    val tujishijiancha_insert = tujishijiancha.zhunshicha


                    val values = ContentValues()
                    values.put("hangdian_NAME", hangdianname_insert)
                    values.put("hangxian_ID", hangxian_id)
                    values.put("shunxu", shunxu_insert)
                    values.put("zhunshishijian", hangdianzhunshi_insert)
                    values.put("zhunshishijiancha", shijiancha_insert)
                    values.put("tujishijian", tujishijian_insert)
                    values.put("tujishijiancha", tujishijiancha_insert)
                    values.put("weidu", weidu_insert)
                    values.put("jingdu", jingdu_insert)
                    values.put("used", 1)
                    xiugaishunxu(hangdianshunxu, shunxu_insert)
                    sqLiteDatabase!!.update("HangDian", values, "hangdian_ID = '$hangdian_id'", null)

                    tianchonglistview()
                }
                .setNegativeButton("取消") { dialog, which -> dialog.dismiss() }.show()
    }

    private fun findzuidashunxu(hangxian_id: String?): Int? {
        val cursor = sqLiteDatabase!!.rawQuery("select * from HangDian where hangxian_ID = ? and used = ? ", arrayOf<String>(hangxian_id, "1"))
        val shunxu = ArrayList<Int>()
        shunxu.add(0)
        while (cursor.moveToNext()) {
            val a = cursor.getInt(cursor.getColumnIndex("shunxu"))
            shunxu.add(a)
        }
        cursor.close()
        return Collections.max(shunxu)
    }

    private fun xiugaishunxu(hangdianshunxu_befor: String, hangdianshunxu_after: String) {
        var i = Integer.parseInt(hangdianshunxu_befor)
        val j = Integer.parseInt(hangdianshunxu_after)
        if (i > j) {
            while (j < i) {
                val a = i - 1
                sqLiteDatabase!!.execSQL("update HangDian set shunxu = '$i' where shunxu = '$a'and hangxian_ID = '$hangxian_id' and used = '1'")
                i--
            }
        } else {
            while (j > i) {
                val a = i + 1
                sqLiteDatabase!!.execSQL("update HangDian set shunxu = '$i' where shunxu = '$a'and hangxian_ID = '$hangxian_id'and used = '1'")
                i++
            }

        }

    }

    private fun tianchonglistview() {
        val cursor = sqLiteDatabase!!.rawQuery("select * from HangDian where used = '1' and hangxian_ID = '$hangxian_id' order by shunxu", null)
        val hangdianlist = ArrayList<HashMap<String, String>>()
        //jichanglist.clear();
        while (cursor.moveToNext()) {
            val hangdian_ID = cursor.getString(cursor.getColumnIndex("hangdian_ID"))
            val hangdianshunxu = cursor.getString(cursor.getColumnIndex("shunxu"))
            val hangdianName = cursor.getString(cursor.getColumnIndex("hangdian_NAME"))
            val hangdianWeidu = cursor.getDouble(cursor.getColumnIndex("weidu"))
            val hangdianJingdu = cursor.getDouble(cursor.getColumnIndex("jingdu"))
            val hangdianZhunshi = cursor.getLong(cursor.getColumnIndex("zhunshishijian"))
            val hangdianZhunshicha = cursor.getString(cursor.getColumnIndex("zhunshishijiancha"))
            val tujishijian = cursor.getLong(cursor.getColumnIndex("tujishijian"))
            val tujishijiacha = cursor.getString(cursor.getColumnIndex("tujishijiancha"))
            val hangdianzhunshichaTolong = ShijianchaTolong(hangdianZhunshicha)
            val hangdianZhunshicha_dong = hangdianzhunshichaTolong.getDong()
            val hangdianZhunshicha_nan = hangdianzhunshichaTolong.getNan()
            val hangdianZhunshicha_xi = hangdianzhunshichaTolong.getXi()
            val hangdianZhunshicha_bei = hangdianzhunshichaTolong.getBei()

            val tujishijianchaTolong = ShijianchaTolong(tujishijiacha)
            val tujishijiancha_dong = tujishijianchaTolong.getDong()
            val tujishijiancha_nan = tujishijianchaTolong.getNan()
            val tujishijiancha_xi = tujishijianchaTolong.getXi()
            val tujishijiancha_bei = tujishijianchaTolong.getBei()

            var hangdianzhunshi1: Long? = hangdianZhunshi
            var hangdianzhunshi2: Long? = hangdianZhunshi
            var hangdianzhunshi1_tuji: Long? = 0L
            var hangdianzhunshi2_tuji: Long? = 0L

            if (fangxiang!!.contains("东")) {
                hangdianzhunshi1 = hangdianZhunshi + hangdianZhunshicha_dong!!
                hangdianzhunshi1_tuji = tujishijian + tujishijiancha_dong!!
                hangdianzhunshi2 = hangdianZhunshi + hangdianZhunshicha_xi!!
                hangdianzhunshi2_tuji = tujishijian + tujishijiancha_xi!!
            } else {
                hangdianzhunshi1 = hangdianZhunshi + hangdianZhunshicha_nan!!
                hangdianzhunshi1_tuji = tujishijian + tujishijiancha_nan!!
                hangdianzhunshi2 = hangdianZhunshi + hangdianZhunshicha_bei!!
                hangdianzhunshi2_tuji = tujishijian + tujishijiancha_bei!!
            }


            val timeFormat = TimeFormat()
            val hangdianzhunshi1_str = timeFormat.BeiJingtime(hangdianzhunshi1 - 28800000)
            val hangdianzhunshi2_str = timeFormat.BeiJingtime(hangdianzhunshi2 - 28800000)
            val hangdianzhunshi1_tuji_str = timeFormat.BeiJingtime(hangdianzhunshi1_tuji - 28800000)
            val hangdianzhunshi2_tuji_str = timeFormat.BeiJingtime(hangdianzhunshi2_tuji - 28800000)

            val jingweiduFormat = JingweiduFormat()


            val hangdianmap = HashMap<String, String>()
            hangdianmap["hangdian_ID"] = hangdian_ID
            hangdianmap["hangdianshunxu"] = hangdianshunxu
            hangdianmap["hangdianname"] = hangdianName
            hangdianmap["hangdianweidu"] = jingweiduFormat.WeiDu(hangdianWeidu)
            hangdianmap["hangdianjingdu"] = jingweiduFormat.WeiDu(hangdianJingdu)
            hangdianmap["hangdianzhunshi1"] = hangdianzhunshi1_str
            hangdianmap["hangdianzhunshi1tuji"] = hangdianzhunshi1_tuji_str
            hangdianmap["hangdianzhunshi2"] = hangdianzhunshi2_str
            hangdianmap["hangdianzhunshi2tuji"] = hangdianzhunshi2_tuji_str
            hangdianlist.add(hangdianmap)
        }

        val hangdiansimpleAdapter = SimpleAdapter(this,
                hangdianlist, R.layout.hangdianlist,
                arrayOf("hangdian_ID", "hangdianshunxu", "hangdianname", "hangdianweidu", "hangdianjingdu", "hangdianzhunshi1", "hangdianzhunshi1tuji", "hangdianzhunshi2", "hangdianzhunshi2tuji"),
                intArrayOf(R.id.hangdian_id_textview, R.id.shunxu_textview, R.id.hangdianname_textview, R.id.hangdianweidu_textview, R.id.hangdianjingdu_textview, R.id.hangdianzhunshi1_feituji_textview, R.id.hangdianzhunshi1_tuji_textview, R.id.hangdianzhunshi2_feituji_textview, R.id.hangdianzhunshi2_tuji_textview))
        hangdianlistView!!.adapter = hangdiansimpleAdapter
    }

    private fun qifeifangxiang(): String {
        var qifeifangxiang = ""
        var jichang_id = ""
        val cursor = sqLiteDatabase!!.rawQuery("select * from HangXian where hangxian_ID = '$hangxian_id'", null)
        cursor.moveToFirst()
        jichang_id = cursor.getString(cursor.getColumnIndex("jichang_ID"))
        cursor.close()
        val cursor1 = sqLiteDatabase!!.rawQuery("select * from JiChang where jichang_ID = '$jichang_id'", null)
        cursor1.moveToFirst()
        qifeifangxiang = cursor1.getString(cursor1.getColumnIndex("qifeifangxiang"))
        cursor1.close()
        return qifeifangxiang
    }

    private fun jingweiToDouble(jingweidu: String): Double? {
        val daichuli_str = jingweidu + "    "
        var i = 0
        var j = 1
        var a: String
        var du = "0"
        var fen = "0"
        var miao = "0"
        var jingweidu_double: Double? = 0.0

        do {
            a = daichuli_str.substring(i, j)
            if (a != " " && a != "°") {
                du += a
            }
            i++
            j++
        } while (a != " " && a != "°")
        do {
            a = daichuli_str.substring(i, j)
            if (a != " " && a != "'") {
                fen += a
            }
            i++
            j++
        } while (a != " " && a != "'")
        do {
            a = daichuli_str.substring(i, j)
            if (a != " " && a != "\"") {
                miao += a
            }
            i++
            j++
        } while (a != " " && a != "\"")

        jingweidu_double = java.lang.Double.parseDouble(du) + java.lang.Double.parseDouble(fen) / 60 + java.lang.Double.parseDouble(miao) / 3600
        return jingweidu_double
    }

    private fun timeToms(time: String): Long? {
        val daichuli_str = time + "      "
        var i = 0
        var j = 1
        var a: String
        var xiaoshi = "0"
        var fen = "0"
        var miao = "0"

        if (!TextUtils.isEmpty(time) && !time.contains(" ") && !time.contains(":")) {
            xiaoshi = time.substring(0, 2)
            fen = time.substring(2, 4)
            miao = time.substring(4, 6)
        } else {
            do {
                a = daichuli_str.substring(i, j)
                if (a != " " && a != ":") {
                    xiaoshi += a
                }
                i++
                j++
            } while (a != " " && a != ":")
            do {
                a = daichuli_str.substring(i, j)
                if (a != " " && a != ":") {
                    fen += a
                }
                i++
                j++
            } while (a != " " && a != ":")
            do {
                a = daichuli_str.substring(i, j)
                if (a != " ") {
                    miao += a
                }
                i++
                j++
            } while (a != " ")
        }


        return java.lang.Long.parseLong(xiaoshi) * 3600000 + java.lang.Long.parseLong(fen) * 60000 + java.lang.Long.parseLong(miao) * 1000
    }

    private inner class ShijianchaTolong internal constructor(shijiancha_str: String) {
        internal var daichuli_str: String
        internal var shijiancha_str: String? = null
        internal var i = 0
        internal var j = 1
        internal var a: String
        internal var dong = "0"
        internal var nan = "0"
        internal var xi = "0"
        internal var bei = "0"

        init {

            daichuli_str = shijiancha_str + "|||||"
            do {
                a = daichuli_str.substring(i, j)
                if (a != "|") {
                    dong += a
                }
                i++
                j++
            } while (a != "|")
            do {
                a = daichuli_str.substring(i, j)
                if (a != "|") {
                    nan += a
                }
                i++
                j++
            } while (a != "|")
            do {
                a = daichuli_str.substring(i, j)
                if (a != "|") {
                    xi += a
                }
                i++
                j++
            } while (a != "|")
            do {
                a = daichuli_str.substring(i, j)
                if (a != "|") {
                    bei += a
                }
                i++
                j++
            } while (a != "|")
        }

        internal fun getDong(): Long? {
            return java.lang.Long.parseLong(dong)
        }

        internal fun getNan(): Long? {
            return java.lang.Long.parseLong(nan)
        }

        internal fun getXi(): Long? {
            return java.lang.Long.parseLong(xi)
        }

        internal fun getBei(): Long? {
            return java.lang.Long.parseLong(bei)
        }
    }

    private inner class Zhunshishijiancha internal constructor(zhunshidong: String, zhunshinan: String, zhunshixi: String, zhunshibei: String) {
        internal var zuixiaozhunshi: Long? = 0L
        internal var shijiancha: Long? = null
        internal var zhunshicha = "0|0|0|0"
        internal var zuixiaozhunshifangxiang = ""

        init {
            val zhunshidong_long = timeToms(zhunshidong)
            val zhunshinan_long = timeToms(zhunshinan)
            val zhunshixi_long = timeToms(zhunshixi)
            val zhunshibei_long = timeToms(zhunshibei)


            if (zhunshidong_long <= zhunshinan_long) {

                zuixiaozhunshi = zhunshidong_long
                zuixiaozhunshifangxiang = "东"
            } else {
                zuixiaozhunshi = zhunshinan_long
                zuixiaozhunshifangxiang = "南"

            }
            if (zhunshixi_long < zuixiaozhunshi) {
                zuixiaozhunshi = zhunshixi_long
                zuixiaozhunshifangxiang = "西"
            }
            if (zhunshibei_long < zuixiaozhunshi) {
                zuixiaozhunshi = zhunshibei_long
                zuixiaozhunshifangxiang = "北"
            }

            val shijiancha_dong: Long
            val shijiancha_nan: Long
            val shijiancha_xi: Long
            val shijiancha_bei: Long

            when (zuixiaozhunshifangxiang) {
                "东" -> {

                    shijiancha_nan = zhunshinan_long!! - zuixiaozhunshi!!
                    shijiancha_xi = zhunshixi_long!! - zuixiaozhunshi!!
                    shijiancha_bei = zhunshibei_long!! - zuixiaozhunshi!!

                    zhunshicha = "0|$shijiancha_nan|$shijiancha_xi|$shijiancha_bei"
                }

                "南" -> {
                    shijiancha_dong = zhunshidong_long!! - zuixiaozhunshi!!
                    shijiancha_xi = zhunshixi_long!! - zuixiaozhunshi!!
                    shijiancha_bei = zhunshibei_long!! - zuixiaozhunshi!!

                    zhunshicha = shijiancha_dong.toString() + "|0|" + shijiancha_xi + "|" + shijiancha_bei
                }

                "西" -> {
                    shijiancha_dong = zhunshidong_long!! - zuixiaozhunshi!!
                    shijiancha_nan = zhunshinan_long!! - zuixiaozhunshi!!
                    shijiancha_bei = zhunshibei_long!! - zuixiaozhunshi!!

                    zhunshicha = (shijiancha_dong + shijiancha_nan).toString() + "|0|" + shijiancha_bei
                }

                "北" -> {
                    shijiancha_dong = zhunshidong_long!! - zuixiaozhunshi!!
                    shijiancha_nan = zhunshixi_long!! - zuixiaozhunshi!!
                    shijiancha_xi = zhunshibei_long!! - zuixiaozhunshi!!

                    zhunshicha = (shijiancha_dong + shijiancha_nan + shijiancha_xi).toString() + "|0"
                }
                else -> {
                }
            }
        }


    }

    private inner class JingweiduFormat {
        private var wd: Double = 0.toDouble()
        private var jd: Double = 0.toDouble()

        internal fun WeiDu(wd1: Double): String {
            val du: Int
            val du_str: String
            val fen_str: String
            wd = wd1
            du = Math.floor(wd).toInt()
            if (du < 10) {
                du_str = "0" + du
            } else {
                du_str = "" + du
            }
            val fen = (wd - du) * 60
            if (fen < 10) {
                fen_str = "0" + String.format("%.1f", fen)
            } else {
                fen_str = "" + String.format("%.1f", fen)
            }
            return "$du_str°$fen_str'"
        }

        fun JingDu(jd1: Double): String {
            val du: Int
            val du_str: String
            val fen_str: String
            jd = jd1
            du = Math.floor(jd).toInt()
            if (du < 10) {
                du_str = "0" + du
            } else {
                du_str = "" + du
            }
            val fen = (jd - du) * 60
            if (fen < 10) {
                fen_str = "0" + String.format("%.1f", fen)
            } else {
                fen_str = "" + String.format("%.1f", fen)
            }
            return "$du_str°$fen_str'"

        }
    }

    private inner class TimeFormat {
        private var GPStime: Double = 0.toDouble()

        internal fun BeiJingtime(GPStime1: Long): String {
            val tian: Int
            val xiaoshi: Int
            val fen: Int
            val miao: Int
            val xiaoshi_str: String
            val fen_str: String
            val miao_str: String
            GPStime = (GPStime1 + 28800000).toDouble()
            tian = Math.floor(GPStime / 86400000).toInt()
            xiaoshi = Math.floor(GPStime / 3600000 - tian * 24).toInt()
            fen = Math.floor(GPStime / 60000 - (tian * 24 + xiaoshi) * 60).toInt()
            miao = Math.floor(GPStime / 1000 - ((tian * 24 + xiaoshi) * 60 + fen) * 60).toInt()
            if (xiaoshi < 10) {
                xiaoshi_str = "0" + xiaoshi
            } else {
                xiaoshi_str = "" + xiaoshi
            }
            if (fen < 10) {
                fen_str = "0" + fen
            } else {
                fen_str = "" + fen
            }
            if (miao < 10) {
                miao_str = "0" + miao
            } else {
                miao_str = "" + miao
            }
            return "$xiaoshi_str:$fen_str:$miao_str"
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
