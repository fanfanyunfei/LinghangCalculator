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
import android.support.v4.content.ContextCompat
import android.text.TextUtils
import android.util.Log
import android.view.*
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.BaseAdapter
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import kotlinx.android.synthetic.main.main_layout.*

import java.io.File
import java.io.FileOutputStream
import java.util.ArrayList
import java.util.HashMap
import java.util.Timer
import java.util.TimerTask


class MainActivity : AppCompatActivity() {

    private val mPREFSQFSK = "StoredQFSK"
    private val mTAG = "LinghangCalculator"
    private var QFSK: Long = 0
    private var sqLiteDatabase: SQLiteDatabase? = null
    private var adapter: ArrayAdapter<String>? = null
    private var adapter1: ArrayAdapter<String>? = null
    private var adapter2: ArrayAdapter<String>? = null
    private var adapter3: ArrayAdapter<String>? = null
    private val jichang_list = ArrayList<String>()
    private val hangxian_list = ArrayList<String>()
    private val hangdian_list = ArrayList<String>()
    private val qifeifangxiang_list = ArrayList<String>()
    private val hangdian_info_list = ArrayList<HangdianInfo>()
    private var selected_jichangname: String? = null
    private var selected_jichangid: String? = null
    private var selected_hangxianname: String? = null
    private var selected_hangdianname: String? = null
    private var selected_qifeifangxiang: String? = null
    private var selected_hangxianid: String? = null
    private var selected_shifoutuji: String? = null
    private var GpsManager: LocationManager? = null
    private var locationListener: LocationListener? = null
    private var mProviderName: String? = null
    private var mGpsTime: Long? = null
    private var numberselectJichang: Int = 0
    private var numberselectHangxian: Int = 0
    private var numberselectHangdian: Int = 0
    private var numberselectQifeifangxiang: Int = 0
    private var mSpSettings: SharedPreferences? = null
    private var AAA: Int = 0
    private var numberstoredJichang: Int = 0
    private var numberstoredHangxian: Int = 0
    private var numberstoredHangdian: Int = 0
    private var numberstoredQifeifangxiang: Int = 0

    private var task: TimerTask = object : TimerTask() {
        override fun run() {
            runOnUiThread {

                if(ContextCompat.checkSelfPermission(this@MainActivity,android.Manifest.permission.ACCESS_FINE_LOCATION)== PackageManager.PERMISSION_GRANTED){

                    val mGpsManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
                    val mlocation = mGpsManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                mGpsTime = 0L

                if (mlocation != null) {
//                    val mlocationUtils = LocationUtils()
                    mGpsTime = mlocation.time
                }


                val timeFormat = TimeFormat()
                val t = timeFormat.beiJingtime(mGpsTime!!,false)
                weixingshijian_text_view!!.text = t
            }
            }
        }
    }

    private var jisuantask: TimerTask = object : TimerTask() {
        override fun run() {
            runOnUiThread {
                try {

                    if(ContextCompat.checkSelfPermission(this@MainActivity,android.Manifest.permission.ACCESS_FINE_LOCATION)== PackageManager.PERMISSION_GRANTED){


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
                        daifeiju_text_view!!.text = String.format("%.2f", daifeiju)  //显示待飞距 km

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
                            daifeishi_text_view!!.text = timeFormat.beiJingtime(daifeishi,true) //显示待飞时


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

                            yudashike_shiji = daifeishi + timeToms(timeFormat.beiJingtime(mGpsTime!!,false))


                            val wucha = yudashike_shiji - yudashike_biaozhun_final_jisuan
                            var kefeishijian: Long = 0
                            kefeishijian = yudashike_biaozhun_final_jisuan - timeToms(timeFormat.beiJingtime(mGpsTime!!,false))
                            yudashike_text_view!!.text = timeFormat.beiJingtime(yudashike_shiji,true)
                            mubiaoyudashike_text_view!!.text = timeFormat.beiJingtime(yudashike_biaozhun_final_jisuan,true)


                            var zaowan = ""
                            if (wucha > 0) {
                                zaowan = "晚到 "
                            } else {
                                zaowan = "早到 "
                            }

                            val yingfeidisu = daifeiju / kefeishijian * 3600000
                            if (BuildConfig.DEBUG)

                                if (QFSK != 0L) {
                                    shijianwucha_text_view!!.text = zaowan + timeFormat.beiJingtime(Math.abs(wucha),true)
                                    yingfeidisu_text_view!!.text = String.format("%.2f", yingfeidisu)
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
                        mubiaoyudashike_text_view!!.text = "GPS信号"
                    }
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
        DATABASE_PATH = filesDir.toString()
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


        getStoredDATA()
        spinnerTianchong()
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
            QFSK = timeToms(timeFormat.beiJingtime(mGpsTime!!,false))
            storedata()
            qifeishike_text_view.text = timeFormat.beiJingtime(QFSK ,true)
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


    private fun spinnerTianchong() {

        adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, jichang_list)
        adapter1 = ArrayAdapter(this, android.R.layout.simple_spinner_item, hangxian_list)
        adapter2 = ArrayAdapter(this, android.R.layout.simple_spinner_item, hangdian_list)
        adapter3 = ArrayAdapter(this, android.R.layout.simple_spinner_item, qifeifangxiang_list)
        adapter!!.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        adapter1!!.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        adapter2!!.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        adapter3!!.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)


        val cursor = sqLiteDatabase!!.rawQuery("select * from JiChang where used = 1", null)//从机场表找出所有机场
        val jichangSpinnerlist = ArrayList<HashMap<String, String>>()
        jichang_list.clear()
        while (cursor.moveToNext()) {
            val jiChangId = cursor.getString(cursor.getColumnIndex("jichang_ID"))
            val jiChangName = cursor.getString(cursor.getColumnIndex("jichang_NAME"))
            val jichangSpinnermap = HashMap<String, String>()
            jichangSpinnermap["jiChangId"] = jiChangId
            jichangSpinnermap["jiChangName"] = jiChangName
            jichangSpinnerlist.add(jichangSpinnermap)
            jichang_list.add(jiChangName)
        }
        cursor.close()


        val numberList = jichang_list.size
        if (numberList <= numberstoredJichang) {
            numberstoredJichang = 0
        }

        jichang_spinner.adapter = adapter
        jichang_spinner.setSelection(numberstoredJichang)

        jichang_spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(arg0: AdapterView<*>, arg1: View, arg2: Int, arg3: Long) {
                selected_jichangname = adapter!!.getItem(arg2)
                numberselectJichang = arg2
                storedata()

                val jichangMapSelected: HashMap<String, String> = jichangSpinnerlist[numberselectJichang]
                selected_jichangid = jichangMapSelected["jiChangId"]

                hangxian_list.clear()
                val hangxianSpinnerlist = ArrayList<HashMap<String, String>>()
                val cursor2 = sqLiteDatabase!!.rawQuery("select * from HangXian where jichang_ID = '$selected_jichangid' and used = 1 ", null)
                while (cursor2.moveToNext()) {
                    val hangXianId = cursor2.getString(cursor2.getColumnIndex("hangxian_ID"))
                    val hangXianName = cursor2.getString(cursor2.getColumnIndex("hangxia_NAME"))
                    val hangxianSpinnermap = HashMap<String, String>()
                    hangxianSpinnermap["hangXianId"] = hangXianId
                    hangxianSpinnermap["hangXianName"] = hangXianName
                    hangxianSpinnerlist.add(hangxianSpinnermap)
                    hangxian_list.add(hangXianName)
                }
                cursor2.close()

                val mNumberList = hangxian_list.size
                if (mNumberList <= numberstoredHangxian) {
                    numberstoredHangxian = 0
                }
                hangxian_spinner!!.adapter = adapter1
                hangxian_spinner!!.setSelection(numberstoredHangxian)
                hangxian_spinner!!.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(arg0: AdapterView<*>, arg1: View, arg2: Int, arg3: Long) {


                        selected_hangxianname = adapter1!!.getItem(arg2)
                        numberselectHangxian = arg2
                        storedata()

                        val hangxianMapSelected: HashMap<String, String> = hangxianSpinnerlist[numberselectHangxian]
                        selected_hangxianid = hangxianMapSelected["hangXianId"]
                        biaogetianchong()
                        hangdian_list.clear()
                        val cursor4 = sqLiteDatabase!!.rawQuery("select * from HangDian where hangxian_ID = '$selected_hangxianid' and used = 1 order by shunxu", null)
                        while (cursor4.moveToNext()) {
                            val hangDianName = cursor4.getString(cursor4.getColumnIndex("hangdian_NAME"))
                            hangdian_list.add(hangDianName)
                        }
                        cursor4.close()
                        val numberList4 = hangdian_list.size
                        if (numberList4 <= numberstoredHangdian) {
                            numberstoredHangdian = 0
                        }
                        hangdian_spinner!!.adapter = adapter2
                        hangdian_spinner!!.setSelection(numberstoredHangdian)
                        hangdian_spinner!!.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                            override fun onItemSelected(arg0: AdapterView<*>, arg1: View, arg2: Int, arg3: Long) {
                                selected_hangdianname = adapter2!!.getItem(arg2)
                                numberselectHangdian = arg2
                                storedata()
                                arg0.visibility = View.VISIBLE
                                biaogetianchong()
                            }

                            override fun onNothingSelected(arg0: AdapterView<*>) {
                                arg0.visibility = View.VISIBLE
                            }
                        }






                        arg0.visibility = View.VISIBLE
                    }

                    override fun onNothingSelected(arg0: AdapterView<*>) {
                        arg0.visibility = View.VISIBLE
                    }
                }


                /**
                 * 从数据库中找出当前机场的起飞方向
                 * 填充到spinner中
                 * 监听选择
                 */
                val cursor4 = sqLiteDatabase!!.rawQuery("select qifeifangxiang from JiChang where jichang_ID = '$selected_jichangid'", null)
                qifeifangxiang_list.clear()
                while (cursor4.moveToNext()) {
                    val qifeifangxiang = cursor4.getString(cursor4.getColumnIndex("qifeifangxiang"))
                    var qifeifangxiang1 = ""
                    var qifeifangxiang2 = ""
                    var qifeifangxiang3 = ""
                    var qifeifangxiang4 = ""
                    if (qifeifangxiang.contains("东")) {
                        qifeifangxiang1 = "东"
                    }
                    if (qifeifangxiang.contains("南")) {
                        qifeifangxiang2 = "南"
                    }
                    if (qifeifangxiang.contains("西")) {
                        qifeifangxiang3 = "西"
                    }
                    if (qifeifangxiang.contains("北")) {
                        qifeifangxiang4 = "北"
                    }
                    if (!qifeifangxiang1.isEmpty()) {
                        qifeifangxiang_list.add(qifeifangxiang1)
                    }
                    if (!qifeifangxiang2.isEmpty()) {
                        qifeifangxiang_list.add(qifeifangxiang2)
                    }
                    if (!qifeifangxiang3.isEmpty()) {
                        qifeifangxiang_list.add(qifeifangxiang3)
                    }
                    if (!qifeifangxiang4.isEmpty()) {
                        qifeifangxiang_list.add(qifeifangxiang4)
                    }
                }
                cursor4.close()
                qifeifangxiang_spinner!!.adapter = adapter3
                qifeifangxiang_spinner!!.setSelection(numberstoredQifeifangxiang)
                qifeifangxiang_spinner!!.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(arg0: AdapterView<*>, arg1: View, arg2: Int, arg3: Long) {
                        selected_qifeifangxiang = adapter3!!.getItem(arg2)
                        numberselectQifeifangxiang = arg2
                        storedata()
                        biaogetianchong()
                        arg0.visibility = View.VISIBLE
                    }

                    override fun onNothingSelected(arg0: AdapterView<*>) {
                        arg0.visibility = View.VISIBLE
                    }
                }





                arg0.visibility = View.VISIBLE
            }

            override fun onNothingSelected(arg0: AdapterView<*>) {
                arg0.visibility = View.VISIBLE
            }
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
            weidu_text_view!!.text = jingweiduFormat.weiDu(location.latitude)//显示纬度
            jingdu_text_view!!.text = jingweiduFormat.jingDu(location.longitude)  //显示经度


        }
    }

    private inner class GpsLocationListener : LocationListener {
        override fun onLocationChanged(location: Location) {
            printGpsLocation(location)
        }

        override fun onProviderDisabled(provider: String) {
            Log.d(mTAG, "ProvoderDisabled:" + provider)
        }

        override fun onProviderEnabled(provider: String) {
            Log.d(mTAG, "ProviderEnable:" + provider)
        }

        override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {
            Log.d(mTAG, "StatusChanged:" + provider + status)
        }
    }

    private fun storedata() {//记录起飞时刻
        mSpSettings = getSharedPreferences(mPREFSQFSK, Context.MODE_PRIVATE)
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
        mSpSettings = getSharedPreferences(mPREFSQFSK, Context.MODE_PRIVATE)
        if (mSpSettings!!.getBoolean("isKeep", false)) {
            QFSK = java.lang.Long.parseLong(mSpSettings!!.getString("storedqfsk", ""))
            val timeFormat = TimeFormat()
            qifeishike_text_view!!.text = timeFormat.beiJingtime(QFSK,true)
            numberstoredJichang = mSpSettings!!.getInt("numberJichang", 0)
            numberstoredHangxian = mSpSettings!!.getInt("numberHangxian", 0)
            numberstoredHangdian = mSpSettings!!.getInt("numberHangdian", 0)
            numberstoredQifeifangxiang = mSpSettings!!.getInt("numberQifeifangxiang", 0)
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
                    if (sheqifeishiEditText.text.toString().isNotEmpty()) {
                        val a = sheqifeishiEditText.text.toString()
                        QFSK = java.lang.Long.parseLong(a.substring(0, 2)) * 3600000 +
                                java.lang.Long.parseLong(a.substring(2, 4)) * 60000 +
                                java.lang.Long.parseLong(a.substring(4, 6)) * 1000
                    } else {
                        val timeFormat = TimeFormat()
                        QFSK = timeToms(timeFormat.beiJingtime(mGpsTime!!,false))
                    }

                    val timeFormat = TimeFormat()
                    qifeishike_text_view!!.text = timeFormat.beiJingtime(QFSK,true)
                    biaogetianchong()
                    jishi_button!!.visibility = View.GONE
                    chongzhi_button.visibility = View.VISIBLE
                }
                .setNegativeButton("取消") { dialog, which ->
                    dialog.dismiss()
                    shezhi_button.visibility = View.VISIBLE
                }.show()
    }

    /**
     * 事件转换自定义类
     * beijingtime方法根据是否北京时间将ms格式事件转化为换hh：mm：ss
     *
     */
    private inner class TimeFormat {
        private var mGPStime: Double = 0.toDouble()

        internal fun beiJingtime(GPStime1: Long,beijingBoo:Boolean): String {
            if (beijingBoo){
                mGPStime = GPStime1.toDouble()
            }else{
                mGPStime = (GPStime1 + 28800000).toDouble()
            }
            val tian: Int = Math.floor(mGPStime / 86400000).toInt()
            val xiaoshi: Int
            val fen: Int
            val miao: Int
            val xiaoshiStr: String
            val fenStr: String
            val miaoStr: String
            xiaoshi = Math.floor(mGPStime / 3600000 - tian * 24).toInt()
            fen = Math.floor(mGPStime / 60000 - (tian * 24 + xiaoshi) * 60).toInt()
            miao = Math.floor(mGPStime / 1000 - ((tian * 24 + xiaoshi) * 60 + fen) * 60).toInt()
            if (xiaoshi < 10) {
                xiaoshiStr = "0" + xiaoshi
            } else {
                xiaoshiStr = "" + xiaoshi
            }
            if (fen < 10) {
                fenStr = "0" + fen
            } else {
                fenStr = "" + fen
            }
            if (miao < 10) {
                miaoStr = "0" + miao
            } else {
                miaoStr = "" + miao
            }
            return "$xiaoshiStr:$fenStr:$miaoStr"
        }
    }


    private inner class JingweiduFormat {
        private var wd: Double = 0.toDouble()
        private var jd: Double = 0.toDouble()

        @SuppressLint("DefaultLocale")
        internal fun weiDu(wd1: Double): String {
            val du: Int = Math.floor(wd).toInt()
            val duStr: String
            val fenStr: String
            wd = wd1
            if (du < 10) {
                duStr = "0" + du
            } else {
                duStr = "" + du
            }
            val fen = (wd - du) * 60
            if (fen < 10) {
                fenStr = "0" + String.format("%.1f", fen)
            } else {
                fenStr = "" + String.format("%.1f", fen)
            }
            return "$duStr°$fenStr'"
        }

        @SuppressLint("DefaultLocale")
        internal fun jingDu(jd1: Double): String {
            val du: Int = Math.floor(jd).toInt()
            val duStr: String
            val fenStr: String
            jd = jd1
            if (du < 10) {
                duStr = "0" + du
            } else {
                duStr = "" + du
            }
            val fen = (jd - du) * 60
            if (fen < 10) {
                fenStr = "0" + String.format("%.1f", fen)
            } else {
                fenStr = "" + String.format("%.1f", fen)
            }
            return "$duStr°$fenStr'"

        }
    }


    private inner class LocationUtils {
        private val earthRadius = 6378.137

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
            s *= earthRadius
            s = Math.round(s * 10000.0) / 10000.0
            s *= 1000
            return s
        }
    }

    private fun writeDB() {
        /**目标文件*/
        val databasefilename = DATABASE_PATH + "/" + DATABASE_NAME
        val file = File(databasefilename)
        //判断是否存在，如存在就不复制
        if (!file.exists()) {
            //获取文件的InputStream对象
            val inputStream = resources.assets.open(DATABASE_NAME)
            val fos = FileOutputStream(databasefilename)
            val buffer = ByteArray(100)
            var count: Int
            while (true) {
                count = inputStream.read(buffer)
                if (count < 0) {
                    break
                }
                fos.write(buffer, 0, count)
            }
            fos.close()
            inputStream.close()
        }
    }


    private inner class HangdianInfo internal constructor(internal val hangdianname: String,
                                                          internal var hangdianweidu: Double?,
                                                          internal var hangdianjingdu: Double?,
                                                          internal var zhunshi: Long?,
                                                          internal var yudashike_biaozhun: Long?)


    private inner class ShijianchaTolong internal constructor(shijiancha_str: String) {
        val shijianchaArr = shijiancha_str.split("|")
        var dong = shijianchaArr[0]
        var nan = shijianchaArr[1]
        var xi = shijianchaArr[2]
        var bei = shijianchaArr[3]
       /* internal var daichuliStr: String = shijiancha_str + "|||||"
        internal var i = 0
        internal var j = 1
        internal var a: String
        internal var dong = "0"
        internal var nan = "0"
        internal var xi = "0"
        internal var bei = "0"

        init {
            do {
                a = daichuliStr.substring(i, j)
                if (a != "|") {
                    dong += a
                }
                i++
                j++
            } while (a != "|")
            do {
                a = daichuliStr.substring(i, j)
                if (a != "|") {
                    nan += a
                }
                i++
                j++
            } while (a != "|")
            do {
                a = daichuliStr.substring(i, j)
                if (a != "|") {
                    xi += a
                }
                i++
                j++
            } while (a != "|")
            do {
                a = daichuliStr.substring(i, j)
                if (a != "|") {
                    bei += a
                }
                i++
                j++
            } while (a != "|")
        }*/

        internal fun getDong(): Long? {
            return dong.toLong()
        }

        internal fun getNan(): Long? {
            return nan.toLong()
        }

        internal fun getXi(): Long? {
            return xi.toLong()
        }

        internal fun getBei(): Long? {
            return bei.toLong()
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
            var zhunshicha = ""


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
            var zhunshishijianchaLong: Long = 0
            val shijianchaTolong = ShijianchaTolong(zhunshicha)
            when (selected_qifeifangxiang) {
                "东" -> zhunshishijianchaLong = shijianchaTolong.getDong()!!
                "南" -> zhunshishijianchaLong = shijianchaTolong.getNan()!!
                "西" -> zhunshishijianchaLong = shijianchaTolong.getXi()!!
                "北" -> zhunshishijianchaLong = shijianchaTolong.getBei()!!
                else -> {
                }
            }

            val zhunshiFinal = zhunshi!! + zhunshishijianchaLong
            val yudashikeBiaozhun = zhunshiFinal + QFSK
            val jc = HangdianInfo(hangdianname, hangdianweidu, hangdianjingdu, zhunshiFinal, yudashikeBiaozhun)
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
                hangdianweidutv.text = "N " + jingweiduFormat.weiDu(jc.hangdianweidu!!)
                hangdianjingdutv.text = "E " + jingweiduFormat.jingDu(jc.hangdianjingdu!!)
                zhunshi.text = timeFormat.beiJingtime(jc.zhunshi!!,true)
                zhunshi.text = ""
                yudashikebiaozhun.text = ""
                zhunshi.text = timeFormat.beiJingtime(jc.zhunshi!!,true)
                if (QFSK == 0L) {
                    yudashikebiaozhun.text = "无起飞时刻"
                } else {
                    yudashikebiaozhun.text = timeFormat.beiJingtime(jc.yudashike_biaozhun!!,true)
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
        val timeArr = time.split(":")
        return timeArr[0].toLong() * 3600000 + timeArr[1].toLong() * 60000 + timeArr[2].toLong() * 1000
        /*val daichuliStr = "$time : : : ::::"
        var i = 0
        var j = 1
        var a: String
        var xiaoshi = "0"
        var fen = "0"
        var miao = "0"

        do {
            a = daichuliStr.substring(i, j)
            if (a != " " && a != ":") {
                xiaoshi += a
            }
            i++
            j++
        } while (a != " " && a != ":")
        do {
            a = daichuliStr.substring(i, j)
            if (a != " " && a != ":") {
                fen += a
            }
            i++
            j++
        } while (a != " " && a != ":")
        do {
            a = daichuliStr.substring(i, j)
            if (a != " ") {
                miao += a
            }
            i++
            j++
        } while (a != " ")

        return xiaoshi.toLong() * 3600000 + fen.toLong() * 60000 + miao.toLong() * 1000
        */

    }



    override fun onResume() {
        super.onResume()
        if (!TextUtils.isEmpty(mProviderName)) {
            if(ContextCompat.checkSelfPermission(this,android.Manifest.permission.ACCESS_FINE_LOCATION)== PackageManager.PERMISSION_GRANTED){
                GpsManager!!.requestLocationUpdates(mProviderName, 1000, 1f, locationListener)
            }
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


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (resultCode) {
            Activity.RESULT_OK -> {
                getStoredDATA()
                spinnerTianchong()
                AAA = 0
            }
            else -> {
                getStoredDATA()
                spinnerTianchong()
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
