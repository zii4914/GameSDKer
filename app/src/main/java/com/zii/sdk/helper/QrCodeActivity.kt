package com.zii.sdk.helper

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import cn.bingoogolapple.qrcode.zxing.QRCodeEncoder
import com.blankj.utilcode.util.ClipboardUtils
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.ScreenUtils
import com.zii.sdk.helper.base.BaseActivity
import com.zii.sdk.helper.const.CommonConst
import com.zii.sdk.helper.databinding.ActivityQrcodeBinding
import kotlinx.coroutines.*


class QrCodeActivity : BaseActivity(), TextWatcher {
    private lateinit var binding: ActivityQrcodeBinding

    private val qrcodeSize = (ScreenUtils.getScreenWidth() * 0.6).toInt()

    private var generateTask: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityQrcodeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.edtContent.addTextChangedListener(this)

        binding.btnCopy.setOnClickListener { ClipboardUtils.copyText(binding.edtContent.text) }
        binding.btnPast.setOnClickListener {
            binding.edtContent.setText(ClipboardUtils.getText())
            binding.edtContent.clearFocus()
        }
        binding.btnQrcodeScan.setOnClickListener {
            startActivity(Intent(this, ScanQrCodeActivity::class.java))
        }
        binding.btnClear.setOnClickListener { binding.edtContent.text.clear() }

        binding.root.post(Runnable {
            handleAction() //必须等view绘制完成，粘贴板才能获取到内容
        })
    }

    private fun handleAction() {
        if (CommonConst.Action.QRCODE_GENERATE_FROM_CLIPBOARD == intent.action) {
            val text = ClipboardUtils.getText()
            if (text.isNotEmpty()) {
                binding.edtContent.setText(text)
            }
        }
    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

    }

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        val text = s?.toString().orEmpty()

        generateTask?.cancel()
        generateTask = GlobalScope.launch {
            generateQrCode(text)
        }
    }

    override fun afterTextChanged(s: Editable?) {

    }

    private suspend fun generateQrCode(content: String) {
        delay(1000)
        LogUtils.d("generateQrCode: $content")
        val bitmap =
            if (content.isEmpty()) null else QRCodeEncoder.syncEncodeQRCode(content, qrcodeSize)
        coroutineScope {
            launch(Dispatchers.Main) {
                LogUtils.d(Thread.currentThread().name, Thread.currentThread().id)
                if (bitmap == null || bitmap.width == 0 || bitmap.height == 0) {
//                    val msg = "图片生成失败：${if (bitmap == null) "无内容" else content}"
//                    Toast.makeText(this@QrCodeActivity, msg, Toast.LENGTH_SHORT).show()
                    binding.ivQrcode.setImageResource(R.drawable.loading_failure_iamge)
                } else
                    binding.ivQrcode.setImageBitmap(bitmap)
            }
        }
    }

}