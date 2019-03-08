package com.mmxb.helper

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.support.annotation.RequiresApi
import android.support.v7.app.AppCompatActivity
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.CompoundButton.OnCheckedChangeListener
import android.widget.Toast
import com.mmxb.helper.floatwindow.FloatWindowService
import com.mmxb.helper.service.MyAccessibilityService
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

    val REQUSET_CODE_FLOAT_WINDOW = 1
    val REQUEST_CODE_ACCESS_SERVICE = 2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initData()
    }

    override fun onResume() {
        super.onResume()
    }

    private fun initData() {

        val sp = this.getSharedPreferences("helper", Context.MODE_PRIVATE)
        val isOpenSwitchWindow = sp.getBoolean("switchFloatWindow", false)
        switchFloatWindow.setChecked(isOpenSwitchWindow)

        if (isOpenSwitchWindow) {
            applyFloatWindowPermission()
        }

        // todo 版本兼容
        switchFloatWindow.setOnCheckedChangeListener(OnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                applyFloatWindowPermission()
            } else {
                switchFloatWindow.setChecked(true)
                Toast.makeText(this, getString(R.string.open_float_window_permission), Toast.LENGTH_LONG).show()
            }
        })

        switchAccessibilityService.setOnCheckedChangeListener(OnCheckedChangeListener { _, _ ->
            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            startActivityForResult(intent, REQUEST_CODE_ACCESS_SERVICE)
        })
    }

    private fun applyFloatWindowPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (Settings.canDrawOverlays(this)) {
                startFloatWindowService()
            } else {
                val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName"))
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                startActivityForResult(intent, REQUSET_CODE_FLOAT_WINDOW)
            }
        } else {
            startFloatWindowService()
        }
    }

    private fun startFloatWindowService() {
        startService(Intent(this@MainActivity, FloatWindowService::class.java))
    }


    @RequiresApi(api = Build.VERSION_CODES.M)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUSET_CODE_FLOAT_WINDOW) {
            if (Settings.canDrawOverlays(this)) {
                startFloatWindowService()
                val edit = getSharedPreferences("helper", Context.MODE_PRIVATE).edit()
                edit.putBoolean("switchFloatWindow", true)
                edit.apply()
                switchFloatWindow.setChecked(true)
            } else {
                switchFloatWindow.setChecked(false)
                Toast.makeText(this, getString(R.string.please_open_f_w_permission), Toast.LENGTH_LONG).show()
            }
        } else if (requestCode == REQUEST_CODE_ACCESS_SERVICE) {
            Toast.makeText(this, "aaa", Toast.LENGTH_LONG).show()
        }
    }

    fun close(view: View) {
        stopService(Intent(this@MainActivity, FloatWindowService::class.java))
    }


}

