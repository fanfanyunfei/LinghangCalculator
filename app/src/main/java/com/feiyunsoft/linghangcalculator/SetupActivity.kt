package com.feiyunsoft.linghangcalculator

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView

class SetupActivity : AppCompatActivity() {

    private var mHangdianguanli_button: Button? = null


    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.setupactiviy_layout)

        val mVirsion_textview = this.findViewById(R.id.virsion_textview) as TextView
        mHangdianguanli_button = this.findViewById(R.id.hangdianguanli_button) as Button


        mVirsion_textview.text = "当前版本：" + versionString()
        setlistener()


    }

    /**
     * 获取版本号
     * @return 当前应用的版本号
     */
    private fun versionString(): String {
        try {
            val manager = this.packageManager
            val info = manager.getPackageInfo(this.packageName, 0)
            val version = info.versionName
            return this.getString(R.string.version_name) + version
        } catch (e: Exception) {
            e.printStackTrace()
            return this.getString(R.string.can_not_find_version_name)
        }

    }

    private fun setlistener() {
        mHangdianguanli_button!!.setOnClickListener {
            val intent = Intent(this@SetupActivity, JiChangGuanliActivity::class.java)
            startActivityForResult(intent, 1)
        }


    }
}
