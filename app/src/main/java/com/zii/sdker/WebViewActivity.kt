package com.zii.sdker

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.RegexUtils
import com.blankj.utilcode.util.ToastUtils
import com.tencent.smtt.sdk.ValueCallback
import com.tencent.smtt.sdk.WebChromeClient
import com.tencent.smtt.sdk.WebView
import com.zii.sdker.base.BaseActivity
import com.zii.sdker.const.CommonConst
import com.zii.sdker.databinding.ActivityWebViewBinding

class WebViewActivity : BaseActivity() {

    private lateinit var binding: ActivityWebViewBinding

    private val rc_choose_files = 10

    private var uploadFiles: ValueCallback<Array<Uri>>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWebViewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.x5webview.webChromeClient = chromeClient()

        val url = intent.getStringExtra(CommonConst.Extra.URL)
        binding.x5webview.loadUrl(url)

        if (!RegexUtils.isURL(url)) ToastUtils.showLong("网址错误：$url")
    }

    private fun chromeClient(): WebChromeClient {
        return object : WebChromeClient() {

            // For Android  >= 5.0
            override fun onShowFileChooser(
                webView: WebView?,
                filePathCallback: ValueCallback<Array<Uri>>,
                fileChooserParams: FileChooserParams?
            ): Boolean {
                LogUtils.d("openFileChooser:$filePathCallback")
                this@WebViewActivity.uploadFiles = filePathCallback
                //openFileChooseProcess
                val i = Intent(Intent.ACTION_GET_CONTENT)
                i.addCategory(Intent.CATEGORY_OPENABLE)
                i.type = "*/*"
                startActivityForResult(Intent.createChooser(i, "选择文件"), rc_choose_files)
                return true
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            when (requestCode) {
                0 -> {
                    if (null != uploadFiles) {
                        if (data == null || resultCode != RESULT_OK) {
                            uploadFiles!!.onReceiveValue(null)
                        } else {
                            uploadFiles!!.onReceiveValue(arrayOf(data.data!!))
                        }
                        uploadFiles = null
                    }
                }
                else -> {
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.x5webview.destroy()
    }
}