package com.feiyunsoft.linghangcalculator

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.database.sqlite.SQLiteDatabase
import android.graphics.Color
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.text.TextUtils
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.BaseAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.Spinner
import android.widget.TextView
import kotlinx.android.synthetic.main.main_layout.*

import java.io.File
import java.io.FileOutputStream
import java.util.ArrayList
import java.util.HashMap
import java.util.Timer
import java.util.TimerTask


class MainActivity : AppCompatActivity() {

/*
    var DATABASE_PATH: String = ""
    var DATABASE_NAME: String = ""
*/
    private val PREFS_QFSK = "StoredQFSK"

    private val TAG = "LinghangCalculator"
/*
    private var TextView_GpsInfo: TextView? = null   //mGpsInfo用于在没有卫星信号的时候显示信息
    private var weidu_text_view: TextView? = null
    private var jingdu_text_view: TextView? = null   //weiduTextView是主页上的纬度显示区
    private var daifeiju_text_view: TextView? = null
    private var daifeishi_text_view: TextView? = null
    private var disu_text_view: TextView? = null
    private var buchangshijian_text_view: TextView? = null
    private var qifeishike_text_view: TextView? = null
    private var weixingshijian_text_view: TextView? = null
    private var shijianwucha_text_view: TextView? = null
    private var yudashike_text_view: TextView? = null
    private var mubiaoyudashike_text_view: TextView? = null
    private var yingfeidisu_text_view: TextView? = null
    private var daifeiju_lable_text_view: TextView? = null
    private var daifeishi_lable_text_view: TextView? = null
    private var yudashike_lable_text_view: TextView? = null
    private var disu_lable_text_view: TextView? = null
    private var weidu_lable_text_view: TextView? = null
    private var jingdu_lable_text_view: TextView? = null

    private var buchangshijian_button: Button? = null
    private var jishiButton: Button? = null
    private var chongzhi_button: Button? = null
    private var shezhiButton: Button? = null
    private var sheqifeishi_button: Button? = null
    private var mubiaotuisuan_button: Button? = null
    private var xianshixiangxi_button: Button? = null
    private var yincangxiangxi_button: Button? = null   //shezhiButton是主页上的设置按钮
*/

    private var QFSK: Long = 0

/*
    private var hangxian_spinner: Spinner? = null
    private var jichangSpinner: Spinner? = null
    private var hangdian_spinner: Spinner? = null
    private var tujiSpinner: Spinner? = null
    private var qifeifangxiang_spinner: Spinner? = null
*/

    private var sqLiteDatabase: SQLiteDatabase? = null
    private var adapter: ArrayAdapter<String>? = null
    private var adapter1: ArrayAdapter<String>? = null
    private var adapter2: ArrayAdapter<String>? = null
    private var adapter3: ArrayAdapter<String>? = null
    private val jichang_list = ArrayList<String>()
    private val hangxian_list = ArrayList<String>()
    private val hangdian_list = ArrayList<String>()
    private val qifeifangxiang_list = ArrayList<String>()
    private val hangdian_info_list = ArrayList<hangdian_info>()
/*
    private var hangdianinfo_listview: ListView? = null
*/
    private var selected_jichangname: String? = null
    private var selected_jichangid: String? = null
    private var selected_hangxianname: String? = null
    private var selected_hangdianname: String? = null
    private var selected_qifeifangxiang: String? = null
    private var selected_hangxianid: String? = null
    private val f: String? = null
    private var selected_shifoutuji: String? = null
    private var GpsManager: LocationManager? = null
    private var locationListener: LocationListener? = null
    private var mProviderName: String? = null
    private var mGpsTime: Long? = null
    private var numberselectJichang: Int = 0
    private var numberselectHangxian: Int = 0
    private var numberselectHangdian: Int = 0
    private var numberselectQifeifangxiang: Int = 0
    private val numberselectshifoutuji: Int = 0

    private var mSpSettings: SharedPreferences? = null

    private var AAA: Int = 0
    private var numberstoredJichang: Int = 0
    private var numberstoredHangxian: Int = 0
    private var numberstoredHangdian: Int = 0
    private var numberstoredQifeifangxiang: Int = 0
    private val numberstoredShifoutuji: Int = 0

    internal var task: TimerTask = object : TimerTask() {
        override fun run() {
            runOnUiThread {
                val mGpsManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

                val mlocation = mGpsManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                mGpsTime = 0L

                if (mlocation != null) {
                    val mlocationUtils = LocationUtils()


                    mGpsTime = mlocation.time
                }


                val timeFormat = TimeFormat()
                val t = timeFormat.BeiJingtime(mGpsTime!!)
                weixingshijian_text_view!!.text = t
            }
        }
    }

    internal var jisuantask: TimerTask = object : TimerTask() {
        override fun run() {
            runOnUiThread {
                try {


                    val GpsManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

                    val location = GpsManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                    val disu: Double

                    if (location != null) {
                        val locationUtils = LocationUtils()
                        val timeFormat = TimeFormat()

                        disu = location.speed.toDouble()//////米每秒
                        disu_text_view!!.setText(String.format("%.1f", disu * 3.6))


                        //                            disu=200;


                        var yudashike_biaozhun_final_jisuan = 0L
                        var yudashike_biaozhun_jisuan = 0L


                        val cursor = sqLiteDatabase!!.rawQuery("select * from HangDian where hangdian_NAME = '$selected_hangdianname'and hangxian_ID = '$selected_hangxianid' and used = '1' ", null)
                        cursor.moveToFirst()
                        val hangdianweidu_jisuan = cursor.getDouble(cursor.getColumnIndex("weidu"))
                        val hangdianjingdu_jisuan = cursor.getDouble(cursor.getColumnIndex("jingdu"))
                        val hangdianzhunshi_jisuan = cursor.getLong(cursor.getColumnIndex("zhunshishijian"))
                        val zhunshishijiancha_jisuan = cursor.getString(cursor.getColumnIndex("zhunshishijiancha"))
                        val tujizhunshi_jisuan = cursor.getLong(cursor.getColumnIndex("tujishijian"))
                        val tujishijiancha_jisuan = cursor.getString(cursor.getColumnIndex("tujishijiancha"))
                        cursor.close()


                        var zhunshijisuan: Long? = 0L
                        var zhunshichaJisuan = ""


                        when (selected_shifoutuji) {
                            "是" -> {
                                zhunshijisuan = tujizhunshi_jisuan
                                zhunshichaJisuan = tujishijiancha_jisuan
                            }
                            "否" -> {
                                zhunshijisuan = hangdianzhunshi_jisuan
                                zhunshichaJisuan = zhunshishijiancha_jisuan
                            }
                            else -> {
                            }
                        }


                        //根据选择的起飞方向，将zhunshicha中的差值赋值给zhunshishijiancha_long
                        var zhunshishijiancha_long_jisuan: Long = 0
                        val shijianchaTolong = ShijianchaTolong(zhunshichaJisuan)
                        when (selected_qifeifangxiang) {
                            "东" -> zhunshishijiancha_long_jisuan = shijianchaTolong.getDong()!!
                            "南" -> zhunshishijiancha_long_jisuan = shijianchaTolong.getNan()!!
                            "西" -> zhunshishijiancha_long_jisuan = shijianchaTolong.getXi()!!
                            "北" -> zhunshishijiancha_long_jisuan = shijianchaTolong.getBei()!!
                            else -> {
                            }
                        }

                        val zhunshi_final_jisuan = zhunshijisuan!! + zhunshishijiancha_long_jisuan
                        yudashike_biaozhun_jisuan = zhunshi_final_jisuan + QFSK


                        val daifeiju = locationUtils.getDistance(location.latitude, location.longitude, hangdianweidu_jisuan, hangdianjingdu_jisuan) / 1000   //得到待飞距 km
                        daifeiju_text_view!!.text = "" + String.format("%.2f", daifeiju)  //显示待飞距 km

                        val number_list = hangdian_list.size
                        var a = numberselectHangdian
                        if (number_list == numberselectHangdian + 1) {
                            a = -1
                        }
                        if (daifeiju < 5) {
                            hangdian_spinner!!.setSelection(a + 1)
                        }

                        if (disu != 0.0) {
                            val daifeishi = (daifeiju * 1000 / disu).toLong() * 1000  // long 待飞时  单位 毫秒
                            daifeishi_text_view!!.text = timeFormat.BeiJingtime(daifeishi - 28800000) //显示待飞时


                            val yudashike_shiji: Long

                            when (selected_shifoutuji) {
                                "是" -> if (disu < 237) {
                                    yudashike_biaozhun_final_jisuan = yudashike_biaozhun_jisuan + java.lang.Long.parseLong(buchangshijian_button!!.text.toString()) * 1000
                                    yingfeidisu_text_view!!.setTextColor(Color.RED)
                                    yudashike_text_view!!.setTextColor(Color.RED)
                                    shijianwucha_text_view!!.setTextColor(Color.RED)
                                    mubiaoyudashike_text_view!!.setTextColor(Color.RED)
                                } else {
                                    yudashike_biaozhun_final_jisuan = yudashike_biaozhun_jisuan
                                    yingfeidisu_text_view!!.setTextColor(Color.BLACK)
                                    yudashike_text_view!!.setTextColor(Color.BLACK)
                                    shijianwucha_text_view!!.setTextColor(Color.BLACK)
                                    mubiaoyudashike_text_view!!.setTextColor(Color.BLACK)
                                }
                                "否" -> yudashike_biaozhun_final_jisuan = yudashike_biaozhun_jisuan
                                else -> {
                                }
                            }

                            yudashike_shiji = daifeishi + timeToms(timeFormat.BeiJingtime(mGpsTime!!))


                            val wucha = yudashike_shiji - yudashike_biaozhun_final_jisuan
                            var kefeishijian: Long = 0
                            kefeishijian = yudashike_biaozhun_final_jisuan - timeToms(timeFormat.BeiJingtime(mGpsTime!!))
                            yudashike_text_view!!.text = timeFormat.BeiJingtime(yudashike_shiji - 28800000)
                            mubiaoyudashike_text_view!!.text = timeFormat.BeiJingtime(yudashike_biaozhun_final_jisuan - 28800000)


                            var zaowan = ""
                            if (wucha > 0) {
                                zaowan = "晚到 "
                            } else {
                                zaowan = "早到 "
                            }

                            val yingfeidisu = daifeiju / kefeishijian * 3600000
                            if (BuildConfig.DEBUG)

                                if (QFSK != 0L) {
                                    shijianwucha_text_view!!.text = "" + zaowan + timeFormat.BeiJingtime(Math.abs(wucha) - 28800000)
                                    yingfeidisu_text_view!!.setText(String.format("%.2f", yingfeidisu))
                                } else {
                                    shijianwucha_text_view!!.text = "无法计算"
                                    yingfeidisu_text_view!!.text = "无法计算"
                                }
                        } else {
                            daifeishi_text_view!!.text = "-- : -- : --"
                            yudashike_text_view!!.text = "-- : -- : --"
                            shijianwucha_text_view!!.text = "-- : -- : --"
                            yingfeidisu_text_view!!.text = "--- . --"
                            mubiaoyudashike_text_view!!.text = "-- : -- : --"
                        }
                    } else {
                        daifeishi_text_view!!.text = "-------"
                        yudashike_text_view!!.text = "-------"
                        shijianwucha_text_view!!.text = "-------"
                        yingfeidisu_text_view!!.text = "-------"
                        disu_text_view!!.text = "-------"
                        daifeiju_text_view!!.text = "-------"
                        weidu_text_view!!.text = "-------"
                        jingdu_text_view!!.text = "-------"
                        mubiaoyudashike_text_view!!.text = "无GPS信号"
                    }

                } catch (ignored: Exception) {
                }
            }
        }
    }

    /**
     * Kotlin
     * @param savedInstanceState
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_layout)
        DATABASE_PATH = filesDir.toString() + "/databases"
        DATABASE_NAME = "shuju.db"
        writeDB()
        sqLiteDatabase = this.openOrCreateDatabase(DATABASE_PATH + "/" + DATABASE_NAME, Activity.MODE_PRIVATE, null)
        val timer = Timer()
        locationListener = GpsLocationListener()
        GpsManager = this.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        mProviderName = LocationManager.GPS_PROVIDER
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        val location = GpsManager!!.getLastKnownLocation(LocationManager.GPS_PROVIDER)


/*
        findview()
*/
        getStoredDATA()
        spinner_tianchong()
        printGpsLocation(location)
        GpsManager!!.requestLocationUpdates(mProviderName, 1000, 5f, locationListener)
        timer.schedule(task, 2000, 1000) //用来更新时钟的
        timer.schedule(jisuantask, 100, 500)
        setlistener()


    }

    /**
     * 设置监听器，都是以匿名类的方式。
     */
    private fun setlistener() {
        shezhi_button.setOnClickListener {
            AAA = 1
            val intent = Intent(this@MainActivity, SetupActivity::class.java)
            startActivityForResult(intent, 1)
        }

        mubiaotuisuan_button.setOnClickListener {
            AAA = 1
            val intent = Intent(this@MainActivity, MubiaotuisuanActivity::class.java)
            startActivityForResult(intent, 1)
        }


        jishi_button.setOnClickListener {
            val timeFormat = TimeFormat()
            QFSK = timeToms(timeFormat.BeiJingtime(mGpsTime!!))
            storedata()
            qifeishike_text_view.text = timeFormat.BeiJingtime(QFSK - 8 * 3600000)
            biaogetianchong()
            jishi_button.visibility = View.GONE
            chongzhi_button.visibility = View.VISIBLE
            AAA = 1
        }

        chongzhi_button.setOnClickListener { openDialog() }

        buchangshijian_button.setOnClickListener { shezhibuchangshijian() }

        sheqifeishi_button.setOnClickListener { sheqifeishi() }

        xianshixiangxi_button.setOnClickListener {
            daifeiju_lable_text_view!!.visibility = View.VISIBLE
            daifeishi_lable_text_view!!.visibility = View.VISIBLE
            yudashike_lable_text_view!!.visibility = View.VISIBLE
            disu_lable_text_view!!.visibility = View.VISIBLE
            weidu_lable_text_view!!.visibility = View.VISIBLE
            jingdu_lable_text_view!!.visibility = View.VISIBLE
            daifeiju_text_view!!.visibility = View.VISIBLE
            daifeishi_text_view!!.visibility = View.VISIBLE
            yudashike_text_view!!.visibility = View.VISIBLE
            disu_text_view!!.visibility = View.VISIBLE
            weidu_text_view!!.visibility = View.VISIBLE
            jingdu_text_view!!.visibility = View.VISIBLE
            yincangxiangxi_button!!.visibility = View.VISIBLE

            xianshixiangxi_button!!.visibility = View.GONE
        }

        yincangxiangxi_button!!.setOnClickListener {
            daifeiju_lable_text_view!!.visibility = View.GONE
            daifeishi_lable_text_view!!.visibility = View.GONE
            yudashike_lable_text_view!!.visibility = View.GONE
            disu_lable_text_view!!.visibility = View.GONE
            weidu_lable_text_view!!.visibility = View.GONE
            jingdu_lable_text_view!!.visibility = View.GONE

            daifeiju_text_view!!.visibility = View.GONE
            daifeishi_text_view!!.visibility = View.GONE
            yudashike_text_view!!.visibility = View.GONE
            disu_text_view!!.visibility = View.GONE
            weidu_text_view!!.visibility = View.GONE
            jingdu_text_view!!.visibility = View.GONE

            yincangxiangxi_button!!.visibility = View.GONE
            xianshixiangxi_button!!.visibility = View.VISIBLE
        }

    }


    private fun spinner_tianchong() {

        adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, jichang_list)
        adapter1 = ArrayAdapter(this, android.R.layout.simple_spinner_item, hangxian_list)
        adapter2 = ArrayAdapter(this, android.R.layout.simple_spinner_item, hangdian_list)
        adapter3 = ArrayAdapter(this, android.R.layout.simple_spinner_item, qifeifangxiang_list)
        adapter!!.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        adapter1!!.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        adapter2!!.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        adapter3!!.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)


        val cursor = sqLiteDatabase!!.rawQuery("select * from JiChang where used = 1", null)//从机场表找出所有机场
        val jichang_spinnerlist = ArrayList<HashMap<String, String>>()
        jichang_list.clear()
        while (cursor.moveToNext()) {
            val JiChangId = cursor.getString(cursor.getColumnIndex("jichang_ID"))
            val JiChangName = cursor.getString(cursor.getColumnIndex("jichang_NAME"))
            val jichang_spinnermap = HashMap<String, String>()
            jichang_spinnermap["JiChangId"] = JiChangId
            jichang_spinnermap["JiChangName"] = JiChangName
            jichang_spinnerlist.add(jichang_spinnermap)
            jichang_list.add(JiChangName)
        }
        cursor.close()


        val number_list = jichang_list.size
        if (number_list <= numberstoredJichang) {
            numberstoredJichang = 0
        }

        jichang_spinner!!.adapter = adapter
        jichang_spinner!!.setSelection(numberstoredJichang)

        jichang_spinner!!.setOnItemClickListener { adapterView, view, i, l ->
            selected_jichangname = adapter!!.getItem(i)
            numberselectJichang = i
            storedata()

            var jichang_map_selected = HashMap<String, String>()
            jichang_map_selected = jichang_spinnerlist[numberselectJichang]
            selected_jichangid = jichang_map_selected["JiChangId"]

            hangxian_list.clear()
            val hangxian_spinnerlist = ArrayList<HashMap<String, String>>()
            val cursor2 = sqLiteDatabase!!.rawQuery("select * from HangXian where jichang_ID = '$selected_jichangid' and used = 1 ", null)
            while (cursor2.moveToNext()) {
                val HangXianId = cursor2.getString(cursor2.getColumnIndex("hangxian_ID"))
                val HangXianName = cursor2.getString(cursor2.getColumnIndex("hangxia_NAME"))
                val hangxian_spinnermap = HashMap<String, String>()
                hangxian_spinnermap["HangXianId"] = HangXianId
                hangxian_spinnermap["HangXianName"] = HangXianName
                hangxian_spinnerlist.add(hangxian_spinnermap)
                hangxian_list.add(HangXianName)
            }
            cursor2.close()

            val number_list = hangxian_list.size
            if (number_list <= numberstoredHangxian) {
                numberstoredHangxian = 0
            }
            hangxian_spinner!!.adapter = adapter1
            hangxian_spinner!!.setSelection(numberstoredHangxian)
            hangxian_spinner!!.setOnItemClickListener { adapterView, view, i, l ->
                selected_hangxianname = adapter1!!.getItem(i)
                numberselectHangxian = i
                storedata()

                var hangxian_map_selected = HashMap<String, String>()
                hangxian_map_selected = hangxian_spinnerlist[numberselectHangxian]
                selected_hangxianid = hangxian_map_selected["HangXianId"]
                biaogetianchong()
                hangdian_list.clear()
                val cursor4 = sqLiteDatabase!!.rawQuery("select * from HangDian where hangxian_ID = '$selected_hangxianid' and used = 1 order by shunxu", null)
                while (cursor4.moveToNext()) {
                    val HangDianNanme = cursor4.getString(cursor4.getColumnIndex("hangdian_NAME"))
                    hangdian_list.add(HangDianNanme)
                }
                cursor4.close()
                val number_list = hangdian_list.size
                if (number_list <= numberstoredHangdian) {
                    numberstoredHangdian = 0
                }
                hangdian_spinner!!.adapter = adapter2
                hangdian_spinner!!.setSelection(numberstoredHangdian)
                hangdian_spinner!!.setOnItemClickListener { adapterView, view, i, l ->
                    selected_hangdianname = adapter2!!.getItem(i)
                    numberselectHangdian = i
                    storedata()
                    adapterView.visibility = View.VISIBLE
                    biaogetianchong()
                }
                adapterView.visibility = View.VISIBLE
            }


            val cursor4 = sqLiteDatabase!!.rawQuery("select qifeifangxiang from JiChang where jichang_ID = '$selected_jichangid'", null)
            qifeifangxiang_list.clear()
            while (cursor4.moveToNext()) {
                val Qifeifangxiang = cursor4.getString(cursor4.getColumnIndex("qifeifangxiang"))
                var Qifeifangxiang1 = ""
                var Qifeifangxiang2 = ""
                var Qifeifangxiang3 = ""
                var Qifeifangxiang4 = ""
                if (Qifeifangxiang.contains("东")) {
                    Qifeifangxiang1 = "东"
                }
                if (Qifeifangxiang.contains("南")) {
                    Qifeifangxiang2 = "南"
                }
                if (Qifeifangxiang.contains("西")) {
                    Qifeifangxiang3 = "西"
                }
                if (Qifeifangxiang.contains("北")) {
                    Qifeifangxiang4 = "北"
                }
                if (!Qifeifangxiang1.isEmpty()) {
                    qifeifangxiang_list.add(Qifeifangxiang1)
                }
                if (!Qifeifangxiang2.isEmpty()) {
                    qifeifangxiang_list.add(Qifeifangxiang2)
                }
                if (!Qifeifangxiang3.isEmpty()) {
                    qifeifangxiang_list.add(Qifeifangxiang3)
                }
                if (!Qifeifangxiang4.isEmpty()) {
                    qifeifangxiang_list.add(Qifeifangxiang4)
                }
            }
            cursor4.close()
            qifeifangxiang_spinner!!.adapter = adapter3
            qifeifangxiang_spinner!!.setSelection(numberstoredQifeifangxiang)
            qifeifangxiang_spinner!!.setOnItemClickListener { adapterView, view, i, l ->
                selected_qifeifangxiang = adapter3!!.getItem(i)
                numberselectQifeifangxiang = i
                storedata()
                biaogetianchong()
                adapterView.visibility = View.VISIBLE
            }
            adapterView.visibility = View.VISIBLE
        }


        shifoutuji_spinner!!.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                val sftj = resources.getStringArray(R.array.shifoutuji)
                selected_shifoutuji = sftj[position]
                when (selected_shifoutuji) {
                    "是" -> {
                        buchangshijian_button!!.visibility = View.VISIBLE
                        buchangshijian_text_view!!.visibility = View.VISIBLE
                        buchangshijian_button!!.setTextColor(Color.RED)
                        buchangshijian_text_view!!.setTextColor(Color.RED)
                        mubiaoyudashike_text_view!!.setTextColor(Color.RED)
                        biaogetianchong()
                    }
                    "否" -> {
                        buchangshijian_button!!.visibility = View.INVISIBLE
                        buchangshijian_text_view!!.visibility = View.INVISIBLE
                        yudashike_text_view!!.setTextColor(Color.BLACK)
                        yingfeidisu_text_view!!.setTextColor(Color.BLACK)
                        shijianwucha_text_view!!.setTextColor(Color.BLACK)
                        mubiaoyudashike_text_view!!.setTextColor(Color.BLACK)
                        biaogetianchong()
                    }
                    else -> {
                    }
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {

            }
        }
    }

    @SuppressLint("SetTextI18n")
    fun printGpsLocation(location: Location?) {                 //定义printGpsLocationg事件


        if (location != null) {
            TextView_GpsInfo!!.text = "精度：" + location.accuracy +
                    "\n海拔：" + location.altitude +
                    "\n航向：" + location.bearing +
                    "\n速度：" + location.speed +
                    "\n纬度：" + location.latitude +
                    "\n经度：" + location.longitude +
                    "\n时间：" + location.time

            val jingweiduFormat = JingweiduFormat()
            weidu_text_view!!.text = jingweiduFormat.WeiDu(location.latitude)//显示纬度
            jingdu_text_view!!.text = jingweiduFormat.JingDu(location.longitude)  //显示经度


        }
    }

    private inner class GpsLocationListener : LocationListener {
        override fun onLocationChanged(location: Location) {
            printGpsLocation(location)
        }

        override fun onProviderDisabled(provider: String) {
            Log.d(TAG, "ProvoderDisabled:" + provider)
        }

        override fun onProviderEnabled(provider: String) {
            Log.d(TAG, "ProviderEnable:" + provider)
        }

        override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {
            Log.d(TAG, "StatusChanged:" + provider + status)
        }
    }

    private fun storedata() {//记录起飞时刻
        mSpSettings = getSharedPreferences(PREFS_QFSK, Context.MODE_PRIVATE)
        val editor = mSpSettings!!.edit()
        editor.clear()
        editor.putBoolean("isKeep", true)
        editor.putString("storedqfsk", QFSK.toString())
        editor.putInt("numberJichang", numberselectJichang)
        editor.putInt("numberHangxian", numberselectHangxian)
        editor.putInt("numberHangdian", numberselectHangdian)
        editor.putInt("numberQifeifangxiang", numberselectQifeifangxiang)
        // editor.putInt("numberShifoutuji",numberselectshifoutuji);
        editor.apply()
    }

    private fun getStoredDATA() {
        mSpSettings = getSharedPreferences(PREFS_QFSK, Context.MODE_PRIVATE)
        if (mSpSettings!!.getBoolean("isKeep", false)) {
            QFSK = java.lang.Long.parseLong(mSpSettings!!.getString("storedqfsk", ""))
            val timeFormat = TimeFormat()
            qifeishike_text_view!!.text = timeFormat.BeiJingtime(QFSK - 28800000)
            numberstoredJichang = mSpSettings!!.getInt("numberJichang", 0)
            numberstoredHangxian = mSpSettings!!.getInt("numberHangxian", 0)
            numberstoredHangdian = mSpSettings!!.getInt("numberHangdian", 0)
            numberstoredQifeifangxiang = mSpSettings!!.getInt("numberQifeifangxiang", 0)
            //  numberstoredShifoutuji = mSpSettings.getInt("numberShifoutuji",0);
        }
    }


    private fun openDialog() {//重置起飞计时按钮弹窗
        AlertDialog.Builder(this)
                .setTitle("提示：")
                .setMessage("是否结束计时？")
                .setPositiveButton("确认"
                ) { dialog, which ->
                    chongzhi_button.visibility = View.GONE
                    jishi_button.visibility = View.VISIBLE
                    shezhi_button.visibility = View.VISIBLE
                    AAA = 0
                }
                .setNegativeButton("取消") { dialog, which -> dialog.dismiss() }.show()
    }

    private fun shezhibuchangshijian() {
        val factory = LayoutInflater.from(this)
        @SuppressLint("InflateParams") val textEntryView = factory.inflate(R.layout.bucangshijianedit, null)
        val bucangshijianEditText = textEntryView.findViewById(R.id.buchangshijian_edittext) as EditText

        AlertDialog.Builder(this)
                .setTitle("请输入补偿时间(秒)：")
                .setView(textEntryView)
                .setPositiveButton("确定"
                ) { dialog, which ->
                    if (!TextUtils.isEmpty(bucangshijianEditText.text)) {
                        buchangshijian_button!!.text = bucangshijianEditText.text.toString()
                    } else {
                        buchangshijian_button!!.text = "0"
                    }
                }
                .setNegativeButton("取消") { dialog, which -> dialog.dismiss() }.show()
    }


    private fun sheqifeishi() {
        val factory = LayoutInflater.from(this)
        @SuppressLint("InflateParams") val textEntryView = factory.inflate(R.layout.sheqifeishi, null)
        val sheqifeishiEditText = textEntryView.findViewById(R.id.sheqifeishi_edittext) as EditText

        AlertDialog.Builder(this)
                .setTitle("请输入起飞时刻:（直接按确认可获得系统时间）")
                .setView(textEntryView)
                .setPositiveButton("确定"
                ) { dialog, which ->
                    if (sheqifeishiEditText.text.toString().length != 0) {
                        val a = sheqifeishiEditText.text.toString()
                        QFSK = java.lang.Long.parseLong(a.substring(0, 2)) * 3600000 +
                                java.lang.Long.parseLong(a.substring(2, 4)) * 60000 +
                                java.lang.Long.parseLong(a.substring(4, 6)) * 1000
                    } else {
                        val timeFormat = TimeFormat()
                        QFSK = timeToms(timeFormat.BeiJingtime(mGpsTime!!))
                    }
                    //storedata();

                    //AAA=1;

                    val timeFormat = TimeFormat()
                    qifeishike_text_view!!.text = timeFormat.BeiJingtime(QFSK - 8 * 3600000)
                    biaogetianchong()
                    jishi_button!!.visibility = View.GONE
                    chongzhi_button.visibility = View.VISIBLE
                }
                .setNegativeButton("取消") { dialog, which ->
                    dialog.dismiss()
                    shezhi_button.visibility = View.VISIBLE
                }.show()
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


    private inner class JingweiduFormat {
        private var wd: Double = 0.toDouble()
        private var jd: Double = 0.toDouble()

        @SuppressLint("DefaultLocale")
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

        @SuppressLint("DefaultLocale")
        internal fun JingDu(jd1: Double): String {
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


    private inner class LocationUtils {
        private val EARTH_RADIUS = 6378.137

        private fun rad(d: Double): Double {
            return d * Math.PI / 180.0
        }


        internal fun getDistance(lat1: Double, lng1: Double, lat2: Double,
                                 lng2: Double): Double {
            val radLat1 = rad(lat1)
            val radLat2 = rad(lat2)
            val a = radLat1 - radLat2
            val b = rad(lng1) - rad(lng2)
            var s = 2 * Math.asin(Math.sqrt(Math.pow(Math.sin(a / 2), 2.0) + (Math.cos(radLat1) * Math.cos(radLat2)
                    * Math.pow(Math.sin(b / 2), 2.0))))
            s = s * EARTH_RADIUS
            s = Math.round(s * 10000.0) / 10000.0
            s = s * 1000
            return s
        }
    }

    fun writeDB() {
        /**目标文件*/
        val databasefilename = DATABASE_PATH + "/" + DATABASE_NAME
        val file = File(databasefilename)
        //判断是否存在，如存在就不复制
        if (!file.exists()){
            //获取文件的InputStream对象
            val inputStream = resources.assets.open(DATABASE_NAME)
            val fos = FileOutputStream(databasefilename)
            val buffer = ByteArray(100)
            var count: Int
            while (true){
                count = inputStream.read(buffer)
                if (count<0){
                    break
                }
                fos.write(buffer,0,count)
            }
            fos.close()
            inputStream.close()
        }
    }
    /*private fun writeDB() {
        // f = DATABASE_PATH_NAME;
        var fout: FileOutputStream? = null
        var inputStream: InputStream? = null
        val dir = File(DATABASE_PATH)
        if (!dir.exists()) {
            dir.mkdir()
        }
        val databasefilename = DATABASE_PATH + "/" + DATABASE_NAME
        val filepath = File(databasefilename)
        if (!filepath.exists()) {
            try {
                inputStream = resources.openRawResource(R.raw.shuju)
                fout = FileOutputStream(File(databasefilename))
                val buffer = ByteArray(128)
                var len = 0
                while ((len = inputStream!!.read(buffer)) != -1) {
                    fout.write(buffer, 0, len)
                }
                fout.close()
                inputStream!!.close()
            } catch (e: IOException) {
                e.printStackTrace()
            } finally {
                if (fout != null) {
                    try {
                        fout.close()
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }

                }
                if (inputStream != null) {
                    try {
                        inputStream.close()
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }

                }
            }
        }

    }*/


    private inner class hangdian_info internal constructor(internal val hangdianname: String,
                                                           internal var hangdianweidu: Double?,
                                                           internal var hangdianjingdu: Double?,
                                                           internal var zhunshi: Long?,
                                                           internal var yudashike_biaozhun: Long?,
                                                           internal var zhunshishijiacha_long: Long?) {

        internal fun getZhunshishijiacha_long(): Long {
            return zhunshishijiacha_long!!
        }
    }


    private inner class ShijianchaTolong internal constructor(shijiancha_str: String?) {
        internal var daichuli_str: String
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


    private fun biaogetianchong() {
        hangdian_info_list.clear()
        val cursor = sqLiteDatabase!!.rawQuery("select * from HangDian where hangxian_ID = '$selected_hangxianid' and used = '1' order by shunxu", null)

        while (cursor.moveToNext()) {
            val hangdianname = cursor.getString(cursor.getColumnIndex("hangdian_NAME"))
            val hangdianweidu = cursor.getDouble(cursor.getColumnIndex("weidu"))
            val hangdianjingdu = cursor.getDouble(cursor.getColumnIndex("jingdu"))
            val hangdianzhunshi = cursor.getLong(cursor.getColumnIndex("zhunshishijian"))
            val zhunshishijiancha = cursor.getString(cursor.getColumnIndex("zhunshishijiancha"))
            val tujizhunshi = cursor.getLong(cursor.getColumnIndex("tujishijian"))
            val tujishijiancha = cursor.getString(cursor.getColumnIndex("tujishijiancha"))
            var zhunshi: Long? = 0L
            var zhunshicha: String? = null


            when (selected_shifoutuji) {
                "是" -> {
                    zhunshi = tujizhunshi
                    zhunshicha = tujishijiancha
                }
                "否" -> {
                    zhunshi = hangdianzhunshi
                    zhunshicha = zhunshishijiancha
                }
                else -> {
                }
            }


            //根据选择的起飞方向，将zhunshicha中的差值赋值给zhunshishijiancha_long
            var zhunshishijiancha_long: Long = 0
            val shijianchaTolong = ShijianchaTolong(zhunshicha)
            when (selected_qifeifangxiang) {
                "东" -> zhunshishijiancha_long = shijianchaTolong.getDong()!!
                "南" -> zhunshishijiancha_long = shijianchaTolong.getNan()!!
                "西" -> zhunshishijiancha_long = shijianchaTolong.getXi()!!
                "北" -> zhunshishijiancha_long = shijianchaTolong.getBei()!!
                else -> {
                }
            }

            val zhunshi_final = zhunshi!! + zhunshishijiancha_long
            val yudashike_biaozhun = zhunshi_final + QFSK
            val jc = hangdian_info(hangdianname, hangdianweidu, hangdianjingdu, zhunshi_final, yudashike_biaozhun, zhunshishijiancha_long)
            hangdian_info_list.add(jc)
        }
        cursor.close()

        val mBaseAdapter = object : BaseAdapter() {
            override fun getCount(): Int {
                return hangdian_info_list.size
            }

            @SuppressLint("SetTextI18n")
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view: View?

                if (convertView == null) {
                    view = View.inflate(baseContext, R.layout.hangdiandetail_shouye, null)
                } else {
                    view = convertView
                }

                val timeFormat = TimeFormat()
                val jingweiduFormat = JingweiduFormat()
                val jc = hangdian_info_list[position]
                val hangdiannametv = view!!.findViewById(R.id.hangdianname_textview) as TextView
                val hangdianweidutv = view.findViewById(R.id.hangdianweidu_textview) as TextView
                val hangdianjingdutv = view.findViewById(R.id.hangdianjingdu_textview) as TextView
                val zhunshi = view.findViewById(R.id.hangdianzhunshi_textview) as TextView
                val yudashikebiaozhun = view.findViewById(R.id.yudashikebiaozhun_textview) as TextView
                val hangdianLinear = view.findViewById(R.id.hangdian_linear) as LinearLayout
                hangdiannametv.text = jc.hangdianname
                hangdianweidutv.text = "N " + jingweiduFormat.WeiDu(jc.hangdianweidu!!)
                hangdianjingdutv.text = "E " + jingweiduFormat.JingDu(jc.hangdianjingdu!!)
                zhunshi.text = timeFormat.BeiJingtime(jc.zhunshi!! - 28800000)
                /*if (QFSK == 0) {
                    yudashikebiaozhun.setText("无起飞时刻");
                } else {
                    yudashikebiaozhun.setText(timeFormat.BeiJingtime(jc.getYudashike_biaozhun() - 28800000));
                }*/
                zhunshi.text = ""
                yudashikebiaozhun.text = ""
                zhunshi.text = timeFormat.BeiJingtime(jc.zhunshi!! - 28800000)
                if (QFSK == 0L) {
                    yudashikebiaozhun.text = "无起飞时刻"
                } else {
                    yudashikebiaozhun.text = timeFormat.BeiJingtime(jc.yudashike_biaozhun!! - 28800000)
                }

                if (position == numberselectHangdian) {
                    hangdianLinear.setBackgroundColor(Color.parseColor("#cfdced"))
                    hangdianinfo_listview!!.setSelection(numberselectHangdian)
                } else {
                    hangdianLinear.setBackgroundColor(Color.WHITE)
                }
                return view
            }

            override fun getItem(position: Int): Any? {
                return null
            }

            override fun getItemId(position: Int): Long {
                return 0
            }
        }
        hangdianinfo_listview!!.adapter = mBaseAdapter
    }

    private fun timeToms(time: String): Long {
        val daichuli_str = time + "      "
        var i = 0
        var j = 1
        var a: String
        var xiaoshi = "0"
        var fen = "0"
        var miao = "0"

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

        return java.lang.Long.parseLong(xiaoshi) * 3600000 + java.lang.Long.parseLong(fen) * 60000 + java.lang.Long.parseLong(miao) * 1000
    }


/*
    private fun findview() {
        TextView_GpsInfo = this.findViewById(R.id.TextView_GpsInfo) as TextView
        weidu_text_view = this.findViewById(R.id.weidu_text_view) as TextView
        jingdu_text_view = this.findViewById(R.id.jingdu_text_view) as TextView
        shezhiButton = this.findViewById(R.id.shezhi_button) as Button
        daifeiju_text_view = this.findViewById(R.id.daifeiju_text_view) as TextView
        daifeishi_text_view = this.findViewById(R.id.daifeishi_text_view) as TextView
        disu_text_view = this.findViewById(R.id.disu_text_view) as TextView

        daifeiju_lable_text_view = this.findViewById(R.id.daifeiju_lable_text_view) as TextView
        daifeishi_lable_text_view = this.findViewById(R.id.daifeishi_lable_text_view) as TextView
        yudashike_lable_text_view = this.findViewById(R.id.yudashike_lable_text_view) as TextView
        disu_lable_text_view = this.findViewById(R.id.disu_lable_text_view) as TextView
        weidu_lable_text_view = this.findViewById(R.id.weidu_lable_text_view) as TextView
        jingdu_lable_text_view = this.findViewById(R.id.jingdu_lable_text_view) as TextView

        jishiButton = this.findViewById(R.id.jishi_button) as Button
        qifeishike_text_view = this.findViewById(R.id.qifeishike_text_view) as TextView
        weixingshijian_text_view = this.findViewById(R.id.weixingshijian_text_view) as TextView
        shijianwucha_text_view = this.findViewById(R.id.shijianwucha_text_view) as TextView
        jichangSpinner = this.findViewById(R.id.jichang_spinner) as Spinner
        hangxian_spinner = this.findViewById(R.id.hangxian_spinner) as Spinner
        hangdian_spinner = this.findViewById(R.id.hangdian_spinner) as Spinner
        qifeifangxiang_spinner = this.findViewById(R.id.qifeifangxiang_spinner) as Spinner
        yingfeidisu_text_view = this.findViewById(R.id.yingfeidisu_text_view) as TextView
        hangdianinfo_listview = this.findViewById(R.id.hangdianinfo_listview) as ListView
        yudashike_text_view = this.findViewById(R.id.yudashike_text_view) as TextView
        mubiaoyudashike_text_view = this.findViewById(R.id.mubiaoyudashike_text_view) as TextView
        chongzhi_button = this.findViewById(R.id.chongzhi_button) as Button
        sheqifeishi_button = this.findViewById(R.id.sheqifeishi_button) as Button
        buchangshijian_button = this.findViewById(R.id.buchangshijian_button) as Button
        mubiaotuisuan_button = this.findViewById(R.id.mubiaotuisuan_button) as Button
        yincangxiangxi_button = this.findViewById(R.id.yincangxiangxi_button) as Button
        xianshixiangxi_button = this.findViewById(R.id.xianshixiangxi_button) as Button
        tujiSpinner = this.findViewById(R.id.shifoutuji_spinner) as Spinner
        buchangshijian_text_view = this.findViewById(R.id.buchangshijian_text_view) as TextView
    }
*/


    override fun onResume() {
        super.onResume()
        if (!TextUtils.isEmpty(mProviderName)) {

            GpsManager!!.requestLocationUpdates(mProviderName, 1000, 1f, locationListener)
            //

        }

    }

    override fun onPause() {
        super.onPause()
        if (AAA != 1 && GpsManager != null) {
            GpsManager!!.removeUpdates(locationListener)

        }
    }

    override fun onStop() {
        super.onStop()
        storedata()
        //AAA=1;
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        when (keyCode) {
            KeyEvent.KEYCODE_BACK -> {
                val build = AlertDialog.Builder(this)
                build.setTitle("注意")
                        .setMessage("确定要退出么？")
                        .setPositiveButton("确定") { dialogInterface, i -> finish() }
                        .setNegativeButton("取消") { dialogInterface, i -> }
                        .show()
            }

            else -> {
            }
        }
        return false
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        // String result = data.getExtras().getString("result");
        //Toast.makeText("wo shoudaole ",)
        /*if (AAA == 1){
            jishiButton.setVisibility(View.GONE);
        }*/
        when (resultCode) {
            Activity.RESULT_OK -> {
                getStoredDATA()
                spinner_tianchong()
                AAA = 0
            }
            else -> {
                getStoredDATA()
                spinner_tianchong()
                AAA = 0
            }
        }
    }

    companion object {
        var DATABASE_PATH: String = ""
        var DATABASE_NAME: String = ""
        private val PREFS_QFSK = "StoredQFSK"
    }
}
