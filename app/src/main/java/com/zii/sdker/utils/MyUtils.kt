package com.zii.sdker.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.blankj.utilcode.util.ClipboardUtils
import com.blankj.utilcode.util.ToastUtils
import com.blankj.utilcode.util.Utils
import com.zii.sdker.WebViewActivity
import com.zii.sdker.const.CommonConst

object MyUtils {

    fun copyAndToast(content: CharSequence?) {
        if (content.isNullOrEmpty()) return
        ClipboardUtils.copyText(content)
        ToastUtils.showShort("复制成功")
    }

    private fun intentOpenUrl(url: String, target: String): Intent {
        if ("webview" == target) {
            return Intent(Utils.getApp(), WebViewActivity::class.java).putExtra(CommonConst.Extra.URL, url)
        }
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        var packageName = ""
        when (target) {
            "quark" ->
                packageName = "com.quark.browser"
            "chrome" ->
                packageName = "com.android.chrome"
            "system" ->
                packageName = "com.android.browser"
        }
        if (packageName.isNotEmpty()) intent.setPackage(packageName)
        return intent
    }

    fun openUrl(url: String, context: Context) {
        context.startActivity(intentOpenUrl(url, ""))
    }

    fun openUrlQuark(url: String, context: Context) {
        context.startActivity(intentOpenUrl(url, "quark"))
    }

    fun openUrlChrome(url: String, context: Context) {
        context.startActivity(intentOpenUrl(url, "chrome"))
    }

    fun openUrlSystem(url: String, context: Context) {
        context.startActivity(intentOpenUrl(url, "system"))
    }

    fun openUrlWebview(url: String, context: Context) {
        context.startActivity(intentOpenUrl(url, "webview"))
    }
}