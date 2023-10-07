package com.zii.sdker

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.webkit.*
import android.widget.Toast
import androidx.core.content.FileProvider
import com.blankj.utilcode.util.*
import com.lxj.xpopup.XPopup
import com.zii.sdker.base.BaseActivity
import com.zii.sdker.const.CommonConst
import com.zii.sdker.databinding.ActivityWebViewBinding
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*


class WebViewActivity : BaseActivity() {

    private lateinit var binding: ActivityWebViewBinding

    private val rc_choose_files = 10

    private var filePathCallback: ValueCallback<Array<Uri>>? = null
    private var pathCameraPhoto: String? = null
    private var uriCameraPhoto: Uri? = null


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
                .asAttachList(arrayOf("粘贴", "复制", "清空", "清除缓存","设置代理"), intArrayOf()) { position, text ->
                    when (position) {
                        0 -> loadUrl(ClipboardUtils.getText().toString())
                        1 -> ClipboardUtils.copyText(binding.edtUrl.text)
                        2 -> binding.edtUrl.text.clear()
                        3 -> binding.webview.clearCache(true)
                        4 -> {binding.webview.settings.userAgentString = "GameSDKer";
                            Toast.makeText(this, "设置代理-GameSDKer", Toast.LENGTH_SHORT).show()
                        }
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

        //cookie配置
        CookieManager.getInstance().setAcceptThirdPartyCookies(binding.webview, true)

        //支持读取文件
        webSettings.allowFileAccess = true //设置可以访问文件

        //其他细节操作
        webSettings.cacheMode = WebSettings.LOAD_CACHE_ELSE_NETWORK //关闭webview中缓存
        webSettings.javaScriptCanOpenWindowsAutomatically = true //支持通过JS打开新窗口
        webSettings.loadsImagesAutomatically = true //支持自动加载图片
        webSettings.defaultTextEncodingName = "utf-8" //设置编码格式
    }

    private fun webviewClient(): WebViewClient {
        return object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                //给宿主应用一个机会去控制在当前webview即将加载的一个url，返回true会导致当前webview中止加载url，false则正常加载

                //触发时机：
                //1.在页面上使用a标签跳转一个链接时，包括远程连接和本地连接：
                //2.使用form表单，跳转action时，get方法和post方法都会触发：
                //3.不是通过webView.loadUrl来加载的 或者 是重定向
                //4.WebView的前进、后退、刷新、以及post请求都不会调用

                val url = request?.url.toString()
                LogUtils.d("shouldOverrideUrlLoading: $url")
                return false
            }

            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                LogUtils.d("onPageStarted: $url")
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                LogUtils.d("onPageFinished: $url")
                binding.edtUrl.setText(url)
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
                this@WebViewActivity.filePathCallback?.onReceiveValue(null)
                this@WebViewActivity.filePathCallback = null
                this@WebViewActivity.filePathCallback = filePathCallback
                //拍照Intent
                val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                if (takePictureIntent.resolveActivity(packageManager) != null) {
                    // Create the File where the photo should go
                    var photoFile: File? = null
                    try {
                        photoFile = createImageFile()
                    } catch (ex: IOException) {
                        LogUtils.e("Unable to create Image File", ex)
                    }

                    // Continue only if the File was successfully created
                    if (photoFile != null) {
                        val uri = FileProvider.getUriForFile(this@WebViewActivity, "com.zii.sdker.provider", photoFile)
                        pathCameraPhoto = photoFile.absolutePath
                        uriCameraPhoto = uri
                        LogUtils.d("set take photo path:" + photoFile.absolutePath + "   " + uriCameraPhoto)
                        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, uri)
                    }
                }

                //文件选择Intent
                val contentSelectionIntent = Intent(Intent.ACTION_GET_CONTENT)
                contentSelectionIntent.addCategory(Intent.CATEGORY_OPENABLE)
                contentSelectionIntent.type = "image/*"

                val chooserIntent = Intent(Intent.ACTION_CHOOSER)
                chooserIntent.putExtra(Intent.EXTRA_INTENT, contentSelectionIntent)
                chooserIntent.putExtra(Intent.EXTRA_TITLE, "图片选择")
                chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, arrayOf(takePictureIntent))
                chooserIntent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION

                startActivityForResult(chooserIntent, rc_choose_files)
                return true
            }

            override fun onJsAlert(view: WebView?, url: String?, message: String?, result: JsResult?): Boolean {
                return super.onJsAlert(view, url, message, result)
            }

            override fun onPermissionRequest(request: PermissionRequest?) {
                super.onPermissionRequest(request)
                request?.grant(request.resources);
            }

            private fun createImageFile(): File {
                // Create an image file name
                val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
                val imageFileName = "JPEG_" + timeStamp + "_"
                val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
                return File.createTempFile(
                    imageFileName,  /* prefix */
                    ".jpg",  /* suffix */
                    storageDir /* directory */
                )
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode != rc_choose_files || filePathCallback == null) {
            super.onActivityResult(requestCode, resultCode, data)
            return
        }
        var results: Array<Uri>? = null

        if (resultCode == Activity.RESULT_OK) {
            val isTakePicture = pathCameraPhoto != null && uriCameraPhoto != null
                    && FileUtils.isFileExists(pathCameraPhoto) && FileUtils.getFileLength(pathCameraPhoto) > 0
            if (isTakePicture) {
                results = arrayOf(uriCameraPhoto!!)
                LogUtils.d("得到拍照结果 $pathCameraPhoto")
            } else {
                val dataString: String? = data?.dataString
                if (dataString != null) {
                    results = arrayOf(Uri.parse(dataString))
                    LogUtils.d("得到图片选择结果 $dataString")
                }
            }
        }

        filePathCallback?.onReceiveValue(results)
        filePathCallback = null
        pathCameraPhoto = null
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.webview.destroy()
    }
}