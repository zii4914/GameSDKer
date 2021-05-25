package com.zii.sdk.helper

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.View.OnClickListener
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import cn.bingoogolapple.qrcode.core.BarcodeType
import cn.bingoogolapple.qrcode.core.QRCodeView
import com.blankj.utilcode.util.ClipboardUtils
import com.blankj.utilcode.util.IntentUtils
import com.blankj.utilcode.util.SPUtils
import com.blankj.utilcode.util.ToastUtils
import com.luck.picture.lib.PictureSelector
import com.luck.picture.lib.config.PictureMimeType
import com.lxj.xpopup.XPopup
import com.lxj.xpopup.core.BasePopupView
import com.lxj.xpopup.interfaces.SimpleCallback
import com.zii.sdk.helper.base.BaseActivity
import com.zii.sdk.helper.const.CommonConst
import com.zii.sdk.helper.databinding.ActivityScanQrCodeBinding
import com.zii.sdk.helper.utils.MyUtils

/**
 * 扫描二维码
 * 复制
 * 打开链接
 *
 */
class ScanQrCodeActivity : BaseActivity(), QRCodeView.Delegate {

    private val rc_select_image = 10

    private lateinit var binding: ActivityScanQrCodeBinding
    private var isActionFinish = false
    private var dialogResult: BasePopupView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityScanQrCodeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.zxingView.setDelegate(this)
        binding.zxingView.setType(BarcodeType.ONLY_QR_CODE, null)

        binding.tvDecodeImage.setOnClickListener {
            PictureSelector.create(this)
                .openGallery(PictureMimeType.ofImage())
                .maxSelectNum(1)
                .forResult(rc_select_image)
        }
        binding.tvCopyAuto.setOnClickListener {
            binding.tvCopyAuto.isSelected = toggleCopyNot()
        }

        //自动复制开关
        binding.tvCopyAuto.isSelected = toggleCopy()

        handleAction()
    }

    private fun handleAction() {
        if (CommonConst.Action.QRCODE_IDENTIFY_FROM_IMAGE == intent.action) {
            isActionFinish = true
            binding.tvDecodeImage.callOnClick()
        }
    }

    private fun toggleCopy(): Boolean {
        return SPUtils.getInstance().getBoolean(CommonConst.Shared.KEY_SCAN_COPY_TOGGLE, true)
    }

    private fun toggleCopyNot(): Boolean {
        val key = CommonConst.Shared.KEY_SCAN_COPY_TOGGLE
        val not = !SPUtils.getInstance().getBoolean(key)
        SPUtils.getInstance().put(key, not)
        return not
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == rc_select_image) {
            if (resultCode == RESULT_OK) {
                data?.also {
                    val path = PictureSelector.obtainMultipleResult(it)?.get(0)?.realPath
                    binding.zxingView.decodeQRCode(path)
                }
                isActionFinish = false
            } else if (isActionFinish) {
                finish()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        binding.zxingView.startCamera() // 打开后置摄像头开始预览，但是并未开始识别
        //binding.zxingView.startCamera(Camera.CameraInfo.CAMERA_FACING_FRONT); // 打开前置摄像头开始预览，但是并未开始识别
        binding.zxingView.startSpotAndShowRect()// 显示扫描框，并开始识别
    }

    override fun onStop() {
        binding.zxingView.stopCamera()// 关闭摄像头预览，并且隐藏扫描框
        super.onStop()
    }

    override fun onDestroy() {
        binding.zxingView.onDestroy() // 销毁二维码扫描控件
        super.onDestroy()
    }

    override fun onScanQRCodeSuccess(result: String?) {
        if (dialogResult == null) {
            createResultDialog(result)
        } else {
            dialogResult?.dismissWith {
                dialogResult = null
                createResultDialog(result)
            }
        }
    }

    private fun createResultDialog(result: String?) {
        dialogResult = XPopup.Builder(this)
            .setPopupCallback(object : SimpleCallback() {
                override fun onDismiss(popupView: BasePopupView?) {
                    binding.zxingView.startSpot()
                }
            }).asCustom(object : BasePopupView(this) {
                override fun onCreate() {
                    val tvResult = findViewById<TextView>(R.id.tv_result)
                    val tvTitle = findViewById<TextView>(R.id.tv_title)
                    val btnBack = findViewById<Button>(R.id.btn_back)
                    val btnOpenUrl = findViewById<Button>(R.id.btn_open_url)
                    val btnShare = findViewById<Button>(R.id.btn_share)

                    val isResultEmpty = result.isNullOrEmpty()
                    val displayResult = result.orEmpty().ifEmpty { "扫描内容为空~" }

                    tvResult.text = displayResult

                    //点击事件
                    val clickCopy = OnClickListener {
                        ClipboardUtils.copyText(displayResult)
                        ToastUtils.showLong("复制成功!")
                    }
                    tvResult.setOnClickListener(clickCopy)
                    tvTitle.setOnClickListener(clickCopy)
                    btnBack.setOnClickListener { dismissWith { finish() } }
                    btnShare.setOnClickListener {
                        dismissWith {
                            binding.zxingView.startSpot()
                            startActivity(IntentUtils.getShareTextIntent(displayResult))
                        }
                    }
                    btnOpenUrl.setOnClickListener {
                        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(displayResult))
                        startActivity(browserIntent)
                    }

                    //内容显示
                    tvResult.isEnabled = !isResultEmpty
                    tvTitle.isEnabled = !isResultEmpty
                    tvTitle.visibility = if (isResultEmpty) View.GONE else View.VISIBLE
                    btnShare.isEnabled = !isResultEmpty
                    val isUrl = !isResultEmpty
                            && (displayResult.startsWith("http://") || displayResult.startsWith("https://"))
                    btnOpenUrl.visibility = if (isUrl) View.VISIBLE else View.GONE

                    //自动复制开关
                    if (toggleCopy()) {
                        tvResult.isEnabled = false
                        tvTitle.visibility = GONE

                        if (isResultEmpty.not()) MyUtils.copyAndToast(displayResult)
                    }
                }

                override fun getPopupLayoutId(): Int {
                    return R.layout.dialog_scan_result
                }
            }).also { it.show() }
    }

    override fun onCameraAmbientBrightnessChanged(isDark: Boolean) {
        // 这里是通过修改提示文案来展示环境是否过暗的状态，接入方也可以根据 isDark 的值来实现其他交互效果
        var tipText: String = binding.zxingView.scanBoxView.tipText
        val ambientBrightnessTip = "\n环境过暗，请打开闪光灯"
        if (isDark) {
            if (!tipText.contains(ambientBrightnessTip)) {
                binding.zxingView.scanBoxView.tipText = tipText + ambientBrightnessTip
            }
        } else {
            if (tipText.contains(ambientBrightnessTip)) {
                tipText = tipText.substring(0, tipText.indexOf(ambientBrightnessTip))
                binding.zxingView.scanBoxView.tipText = tipText
            }
        }
    }

    override fun onScanQRCodeOpenCameraError() {
        Toast.makeText(this, "打开相机出错", Toast.LENGTH_SHORT).show()
    }

}