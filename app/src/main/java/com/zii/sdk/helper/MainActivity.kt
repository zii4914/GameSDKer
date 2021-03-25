package com.zii.sdk.helper

import android.Manifest
import android.content.Intent
import android.os.Bundle
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.PermissionUtils
import com.lxj.xpopup.XPopup
import com.zii.sdk.helper.base.BaseActivity
import com.zii.sdk.helper.databinding.ActivityMainBinding

class MainActivity : BaseActivity() {
    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        PermissionUtils.permission(Manifest.permission.CAMERA)
            .callback { isAllGranted, granted, deniedForever, denied ->
                LogUtils.d(granted, deniedForever, denied)
                if (!isAllGranted) {
                    XPopup.Builder(this)
                        .asConfirm(
                            "缺少权限",
                            "请开启相关权限->$denied"
                        ) { PermissionUtils.launchAppDetailsSettings() }
                        .show()
                }
            }
            .request()

        binding.btnQrcode.setOnClickListener {
            startActivity(Intent(this, QrCodeActivity::class.java))
        }
        binding.btnAppSign.setOnClickListener {
            startActivity(Intent(this, AppSignaturesActivity::class.java))
        }
    }


}