package com.zii.sdker

import android.os.Bundle
import cn.bingoogolapple.qrcode.zxing.QRCodeEncoder
import com.blankj.utilcode.util.ConvertUtils
import com.blankj.utilcode.util.IntentUtils
import com.blankj.utilcode.util.KeyboardUtils
import com.blankj.utilcode.util.ToastUtils
import com.zii.sdker.base.BaseActivity
import com.zii.sdker.const.CommonConst
import com.zii.sdker.databinding.ActivityTransportBinding
import com.zii.sdker.model.Copy2Result
import com.zii.sdker.utils.MyUtils
import kotlinx.coroutines.*
import rxhttp.toClass
import rxhttp.wrapper.param.RxHttp

class TransportActivity : BaseActivity() {
    private lateinit var binding: ActivityTransportBinding

    var taskText: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTransportBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.tvLink.setOnClickListener {
            MyUtils.copyAndToast(binding.tvLink.text)
        }
        binding.btnShare.setOnClickListener {
            val text = binding.tvLink.text.toString()
            if (text.isEmpty()) {
                ToastUtils.showLong("内容为空~")
            } else {
                IntentUtils.getShareTextIntent(text)
            }
        }

        binding.layoutInput.getBtnDo().setOnClickListener {
            val content = binding.layoutInput.getEdtInput().text.toString()
            if (content.isEmpty()) {
                ToastUtils.showLong("没有内容~")
                return@setOnClickListener
            }

            KeyboardUtils.hideSoftInput(this)

            taskText?.cancel()
            taskText = GlobalScope.launch {
                postText(content)
            }
        }

        //处理直传内容
        val text = intent.getStringExtra(CommonConst.Extra.TEXT)
        if (!text.isNullOrEmpty()) {
            binding.layoutInput.getEdtInput().setText(text)
            binding.layoutInput.getBtnDo().callOnClick()
        }
    }

    private suspend fun postText(content: String) {
        val result = RxHttp.postForm(CommonConst.Url.COPY2_TEXT)
            .add("stype", "text")
            .add("content", content)
            .toClass<Copy2Result>()
            .await()

        if (result.status != 0) {
            ToastUtils.showLong("请求失败-> status:${result.status}  msg:${result.msg}")
            return
        }

        val url = result.data.url

        val qrcodeSize = ConvertUtils.dp2px(200f)
        val bitmap =
            if (content.isEmpty()) null else QRCodeEncoder.syncEncodeQRCode(url, qrcodeSize)

        coroutineScope {
            launch(Dispatchers.Main) {
                binding.tvLink.text = url

                if (bitmap == null || bitmap.width == 0 || bitmap.height == 0) {
                    binding.ivQrcode.setImageResource(R.drawable.loading_failure_iamge)
                } else binding.ivQrcode.setImageBitmap(bitmap)
            }
        }
    }
}