package com.zii.sdk.helper.utils

import com.blankj.utilcode.util.ClipboardUtils
import com.blankj.utilcode.util.ToastUtils

object MyUtils {

    fun copyAndToast(content: CharSequence?) {
        if (content.isNullOrEmpty()) return
        ClipboardUtils.copyText(content)
        ToastUtils.showLong("复制成功")
    }
}