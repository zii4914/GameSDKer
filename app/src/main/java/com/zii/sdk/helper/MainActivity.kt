package com.zii.sdk.helper

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.PermissionUtils
import com.lxj.xpopup.XPopup
import com.zii.sdk.helper.base.BaseActivity
import com.zii.sdk.helper.const.CommonConst
import com.zii.sdk.helper.databinding.ActivityMainBinding
import com.zii.sdk.helper.topactivity.AppShortcutsActivity

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

        binding.btnQrcodeGenerate.setOnClickListener {
            startActivity(Intent(this, QrCodeActivity::class.java))
        }
        binding.btnQrcodeScan.setOnClickListener {
            startActivity(Intent(this, ScanQrCodeActivity::class.java))
        }
        binding.btnQrcodeIdentify.setOnClickListener {
            startActivity(Intent(this, ScanQrCodeActivity::class.java).apply {
                action = CommonConst.Action.QRCODE_IDENTIFY_FROM_IMAGE
            })
        }
        binding.btnAppSign.setOnClickListener {
            startActivity(Intent(this, AppSignaturesActivity::class.java))
        }
        binding.btnTopActivity.setOnClickListener {
            startActivity(Intent(this, AppShortcutsActivity::class.java))
        }
        binding.btnDevSettings.setOnClickListener {
            startActivity(Intent(android.provider.Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS))
        }
        binding.btnTransportText.setOnClickListener {
            startActivity(Intent(this, TransportActivity::class.java))
        }
        binding.btnTransportAll.setOnClickListener {
            val url = "https://cp.anyknew.com/" //拷贝兔
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        }
    }


}