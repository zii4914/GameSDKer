package com.zii.sdker

import android.content.pm.PackageManager
import android.content.pm.Signature
import android.os.Bundle
import android.util.Base64
import android.widget.ArrayAdapter
import com.blankj.utilcode.util.*
import com.zii.sdker.base.BaseActivity
import com.zii.sdker.databinding.ActivityAppSignaturesBinding
import com.zii.sdker.utils.MyUtils
import java.security.MessageDigest
import java.util.*


/**
 * 应用签名
 */
class AppSignaturesActivity : BaseActivity() {
    private val KEY_LAST_PKG_INPUT =  "LAST_PKG_INPUT";
    private lateinit var binding: ActivityAppSignaturesBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAppSignaturesBinding.inflate(layoutInflater)
        setContentView(binding.root)


        val lastInput = SPStaticUtils.getString(KEY_LAST_PKG_INPUT)
        if (!lastInput.isNullOrEmpty())
            binding.edtPackageName.setText(lastInput)

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

            SPStaticUtils.put(KEY_LAST_PKG_INPUT,appPackage);

            val keyHash = getAppSignaturesKeyHash(appPackage)?.get(0)
            val md5 = AppUtils.getAppSignaturesMD5(appPackage)[0]
            val sha1 = AppUtils.getAppSignaturesSHA1(appPackage)[0]
            val sha256 = AppUtils.getAppSignaturesSHA256(appPackage)[0]
            val wechat = md5.replace(":", "").lowercase(Locale.getDefault())

            binding.tvPackageName.text = "应用：$appPackage"
            binding.tvWechat.text = wechat
            binding.tvKeyHash.text = keyHash
            binding.tvMd5.text = md5
            binding.tvSHA1.text = sha1
            binding.tvSha256.text = sha256

            binding.edtPackageName.clearFocus()
            KeyboardUtils.hideSoftInput(binding.edtPackageName)
        }
        binding.btnCom.setOnClickListener {
            binding.edtPackageName.setText("com.")
            KeyboardUtils.showSoftInput(binding.edtPackageName)
            binding.edtPackageName.setSelection("com.".length)
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
        binding.tvKeyHash.setOnLongClickListener {
            val signature = binding.tvKeyHash.text.toString()
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
            val name = binding.tvPackageName.text.toString()
            val wechat = binding.tvWechat.text.toString()
            val hashKey = binding.tvKeyHash.text.toString()
            val md5 = binding.tvMd5.text.toString()
            val sha1 = binding.tvSHA1.text.toString()
            val sha256 = binding.tvSha256.text.toString()
            val text = "应用：$name"
                .plus("\n")
                .plus("应用签名:$wechat")
                .plus("\n")
                .plus("HashKey:$hashKey")
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

    private fun getAppSignaturesKeyHash(appPackage: String): List<String>? {
        val appSignatures = AppUtils.getAppSignatures(appPackage)
        return appSignatures?.map { signature: Signature ->
            try {
                val md = MessageDigest.getInstance("SHA")
                md.update(signature.toByteArray())
                return@map Base64.encodeToString(md.digest(), Base64.DEFAULT).trim()
            } catch (e: Exception) {
                return@map null
            }
        }?.filterNotNull()
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