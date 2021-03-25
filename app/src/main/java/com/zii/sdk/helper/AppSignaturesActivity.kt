package com.zii.sdk.helper

import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.ArrayAdapter
import com.blankj.utilcode.util.AppUtils
import com.blankj.utilcode.util.ClipboardUtils
import com.blankj.utilcode.util.KeyboardUtils
import com.blankj.utilcode.util.ToastUtils
import com.zii.sdk.helper.base.BaseActivity
import com.zii.sdk.helper.databinding.ActivityAppSignaturesBinding
import com.zii.sdk.helper.utils.MyUtils
import java.util.*


/**
 * 应用签名
 */
class AppSignaturesActivity : BaseActivity() {
    private lateinit var binding: ActivityAppSignaturesBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAppSignaturesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        listInstalledApplication()

        binding.btnGetSignatures.setOnClickListener {
            val appPackage = binding.edtPackageName.text.toString()
            if (appPackage.isEmpty()) {
                ToastUtils.showLong("APP包名不能为空")
                return@setOnClickListener
            }

            if (!AppUtils.isAppInstalled(appPackage)) {
                ToastUtils.showLong("该应用不存在")
                return@setOnClickListener
            }

            val md5 = AppUtils.getAppSignaturesMD5(appPackage)[0]
            val sha1 = AppUtils.getAppSignaturesSHA1(appPackage)[0]
            val sha256 = AppUtils.getAppSignaturesSHA256(appPackage)[0]
            val wechat = md5.replace(":", "").toLowerCase(Locale.getDefault())

            binding.tvWechat.text = wechat
            binding.tvMd5.text = md5
            binding.tvSHA1.text = sha1
            binding.tvSha256.text = sha256

            binding.edtPackageName.clearFocus()
            KeyboardUtils.hideSoftInput(binding.edtPackageName)
        }
        binding.btnCopy.setOnClickListener {
            val appPackage = binding.edtPackageName.text.toString()
            ClipboardUtils.copyText(appPackage)
        }
        binding.btnPast.setOnClickListener {
            binding.edtPackageName.setText(ClipboardUtils.getText().toString())
        }
        binding.btnClear.setOnClickListener {
            binding.edtPackageName.text.clear()
        }

        binding.tvWechat.setOnLongClickListener {
            val signature = binding.tvWechat.text.toString()
            MyUtils.copyAndToast(signature)
            return@setOnLongClickListener true
        }
        binding.tvMd5.setOnLongClickListener {
            val signature = binding.tvMd5.text.toString()
            MyUtils.copyAndToast(signature)
            return@setOnLongClickListener true
        }
        binding.tvSHA1.setOnLongClickListener {
            val signature = binding.tvSHA1.text.toString()
            MyUtils.copyAndToast(signature)
            return@setOnLongClickListener true
        }
        binding.tvSha256.setOnLongClickListener {
            val signature = binding.tvSha256.text.toString()
            MyUtils.copyAndToast(signature)
            return@setOnLongClickListener true
        }
        binding.btnCopyAll.setOnClickListener {
            val wechat = binding.tvWechat.text.toString()
            val md5 = binding.tvMd5.text.toString()
            val sha1 = binding.tvSHA1.text.toString()
            val sha256 = binding.tvSha256.text.toString()
            val text = "应用签名:$wechat"
                .plus("\n")
                .plus("MD5:$md5")
                .plus("\n")
                .plus("SHA1:$sha1")
                .plus("\n")
                .plus("SHA256:$sha256")
                .plus("\n")
            MyUtils.copyAndToast(text)
        }
    }

    private fun listInstalledApplication() {
        val pm = packageManager
        //get a list of installed apps.
        val packages = pm.getInstalledApplications(PackageManager.GET_META_DATA)
        val list = packages
            .filterNot { AppUtils.isAppSystem(it.packageName) }
            .map { it.packageName }

        binding.edtPackageName.setAdapter(
            ArrayAdapter(
                this,
                R.layout.list_auto_complete_edittext,
                list
            )
        )
    }

}