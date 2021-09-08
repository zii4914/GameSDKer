package com.zii.sdker.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.blankj.utilcode.util.AppUtils
import com.blankj.utilcode.util.ClipboardUtils
import com.blankj.utilcode.util.ToastUtils

object MyUtils {

    fun copyAndToast(content: CharSequence?) {
        if (content.isNullOrEmpty()) return
        ClipboardUtils.copyText(content)
        ToastUtils.showShort("复制成功")
    }

    private fun intentOpenUrl(url: String): Intent {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        if (AppUtils.isAppInstalled("com.quark.browser")) {
            //优先用夸克浏览器
//            intent.setPackage("com.quark.browser")
        }
        return intent
    }

    fun openUrl(url: String, context: Context) {
        context.startActivity(intentOpenUrl(url))
    }
}