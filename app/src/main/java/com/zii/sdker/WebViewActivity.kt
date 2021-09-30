package com.zii.sdker

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.webkit.*
import com.blankj.utilcode.util.ClipboardUtils
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.RegexUtils
import com.blankj.utilcode.util.ToastUtils
import com.lxj.xpopup.XPopup
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

        WebView.setWebContentsDebuggingEnabled(true)//允许chrome inspect 调试

        binding.webview.webChromeClient = chromeClient()
        binding.webview.webViewClient = webviewClient()
        webviewSetting()

        val url = intent.getStringExtra(CommonConst.Extra.URL).orEmpty()
        if (url.isNotEmpty()) {
            binding.webview.loadUrl(url)
            binding.edtUrl.setText(url)
            if (!RegexUtils.isURL(url)) {
                ToastUtils.showLong("网址错误：$url")
            }
        }
        binding.btnGo.setOnClickListener {
            val loadUrl = binding.edtUrl.text.toString()
            binding.webview.loadUrl(loadUrl)
        }
        binding.btnMore.setOnClickListener {
            XPopup.Builder(this)
                .atView(it)
                .asAttachList(arrayOf("粘贴", "复制", "清空", "清除缓存"), intArrayOf()) { position, text ->
                    when (position) {
                        0 -> loadUrl(ClipboardUtils.getText().toString())
                        1 -> ClipboardUtils.copyText(binding.edtUrl.text)
                        2 -> binding.edtUrl.text.clear()
                        3 -> binding.webview.clearCache(true)
                    }
                }.show()
        }

    }

    private fun loadUrl(url: String) {
        binding.edtUrl.setText(url)
        binding.webview.loadUrl(url)
    }

    private fun webviewSetting() {
        val webSettings = binding.webview.settings


        webSettings.javaScriptEnabled = true //如果访问的页面中要与Javascript交互，则webview必须设置支持Javascript
        // 若加载的 html 里有JS 在执行动画等操作，会造成资源浪费（CPU、电量）
        // 在 onStop 和 onResume 里分别把 setJavaScriptEnabled() 给设置成 false 和 true 即可
        webSettings.setDomStorageEnabled(true);// 打开本地缓存提供JS调用,至关重要

        //设置自适应屏幕，两者合用
        webSettings.useWideViewPort = true //将图片调整到适合webview的大小
        webSettings.loadWithOverviewMode = true // 缩放至屏幕的大小

        //缩放操作
        webSettings.setSupportZoom(true) //支持缩放，默认为true。是下面那个的前提。
        webSettings.builtInZoomControls = true //设置内置的缩放控件。若为false，则该WebView不可缩放
        webSettings.displayZoomControls = false //隐藏原生的缩放控件

        //其他细节操作
        webSettings.cacheMode = WebSettings.LOAD_CACHE_ELSE_NETWORK //关闭webview中缓存
        webSettings.allowFileAccess = true //设置可以访问文件
        webSettings.javaScriptCanOpenWindowsAutomatically = true //支持通过JS打开新窗口
        webSettings.loadsImagesAutomatically = true //支持自动加载图片
        webSettings.defaultTextEncodingName = "utf-8" //设置编码格式
    }

    private fun webviewClient(): WebViewClient {
        return object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                val url = request?.url.toString()
                Log.i("zii-", "shouldOverrideUrlLoading: " + url)
                view?.loadUrl(url)
                binding.edtUrl.setText(url)
                return true
            }

            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                Log.i("zii-", "onPageStarted: $url")
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                Log.i("zii-", "onPageFinished: $url")
            }
        }
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

            override fun onJsAlert(view: WebView?, url: String?, message: String?, result: JsResult?): Boolean {
                return super.onJsAlert(view, url, message, result)
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
        binding.webview.destroy()
    }
}