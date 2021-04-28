package com.zii.sdk.helper.const

object CommonConst {
    object Shared {
        const val SHARED_NAME = "default_shared"

        /** 二维码扫描：自动复制开关 */
        const val KEY_SCAN_COPY_TOGGLE = "key_scan_copy_toggle";

    }

    object Action {
        /** 二维码：使用粘贴板内容生成二维码 */
        const val QRCODE_GENERATE_FROM_CLIPBOARD = "sdker.qrcode.ACTION_GENERATE_FROM_CLIPBOARD"

        /** 二维码：从图片识别 */
        const val QRCODE_IDENTIFY_FROM_IMAGE = "sdker.qrcode.ACTION_IDENTIFY_FROM_IMAGE"

        /** 传输文字：使用粘贴板内容 */
        const val TRANSPORT_TEXT_FROM_CLIPBOARD = "sdker.transport.ACTION_TEXT_FROM_CLIPBOARD"
    }

    object Extra {
        const val URL = "url"
        const val TEXT = "text"
    }

    object Url {
        const val COPY2_TEXT = "https://cp.anyknew.com/stag"
    }
}