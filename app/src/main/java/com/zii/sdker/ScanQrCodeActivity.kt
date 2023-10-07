package com.zii.sdker

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.View.OnClickListener
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.Group
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.LinearLayoutManager
import cn.bingoogolapple.qrcode.core.BarcodeType
import cn.bingoogolapple.qrcode.core.QRCodeView
import com.blankj.utilcode.util.*
import com.bumptech.glide.Glide
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.listener.OnItemClickListener
import com.chad.library.adapter.base.listener.OnItemLongClickListener
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.luck.picture.lib.PictureSelector
import com.luck.picture.lib.config.PictureMimeType
import com.lxj.xpopup.XPopup
import com.lxj.xpopup.core.BasePopupView
import com.zii.sdker.base.BaseActivity
import com.zii.sdker.const.CommonConst
import com.zii.sdker.databinding.ActivityScanQrCodeBinding
import com.zii.sdker.utils.MyUtils
import java.io.File
import java.net.URL

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
    private var listAdapter: ListAdapter = ListAdapter(mutableListOf())
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

        val spHistorySet = SPUtils.getInstance().getStringSet(CommonConst.Shared.KEY_SCAN_HISTORY)
        listAdapter.data = spHistorySet.toMutableList()
        Log.d("zii-", "onCreate: $spHistorySet")
        listAdapter.setOnItemClickListener { adapter, _, position ->
            val url = adapter.data[position] as String
            val isUrl = url.startsWith("http://") || url.startsWith("https://")
            if (isUrl)
                MyUtils.openUrl(url, this@ScanQrCodeActivity)
            else
                ToastUtils.showShort("不是链接，无法打开")
        }
        listAdapter.setOnItemLongClickListener(OnItemLongClickListener { adapter, view, position ->
            MyUtils.copyAndToast(adapter.data[position] as String)
            return@OnItemLongClickListener true
        })
        binding.recv.layoutManager = LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false)
        binding.recv.adapter = listAdapter
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
        LogUtils.d("扫描结果：$result  $dialogResult")
        dialogResult?.dismiss()
        dialogResult = null
        createResultDialog(result)
        val spHistorySet = SPUtils.getInstance().getStringSet(CommonConst.Shared.KEY_SCAN_HISTORY)
        val toMutableList = spHistorySet.toMutableList()
        toMutableList.remove(result)
        toMutableList.add(0,result)
        val toMutableSet = toMutableList.toMutableSet()
        SPUtils.getInstance().put(CommonConst.Shared.KEY_SCAN_HISTORY, toMutableSet)
        LogUtils.d("保存结果: $spHistorySet ........... $toMutableSet")
        listAdapter.data = toMutableList
        listAdapter.notifyDataSetChanged()
    }

    private fun createResultDialog(result: String?) {
        dialogResult = XPopup.Builder(this)
            .asCustom(object : BasePopupView(this) {
                override fun onCreate() {
                    val tvResult = findViewById<TextView>(R.id.tv_result)
                    val tvTitle = findViewById<TextView>(R.id.tv_title)
                    val btnBack = findViewById<Button>(R.id.btn_back)
                    val btnOpenUrl = findViewById<Button>(R.id.btn_open_url)
                    val btnShare = findViewById<Button>(R.id.btn_share)
                    val btnLinkChrome = findViewById<Button>(R.id.btn_link_chrome)
                    val btnLinkQuark = findViewById<Button>(R.id.btn_link_quark)
                    val btnLinkWebview = findViewById<Button>(R.id.btn_link_webview)
                    val btnLinkSystem = findViewById<Button>(R.id.btn_link_system)
                    val groupLinkOpen = findViewById<Group>(R.id.group_link_open)

                    val isResultEmpty = result.isNullOrEmpty()
                    val displayResult = result.orEmpty().ifEmpty { "扫描内容为空~" }

                    tvResult.text = displayResult

                    //点击事件
                    val clickCopy = OnClickListener {
                        ClipboardUtils.copyText(displayResult)
                        ToastUtils.showShort("复制成功!")
                    }
                    tvResult.setOnClickListener(clickCopy)
                    tvTitle.setOnClickListener(clickCopy)
                    btnBack.setOnClickListener { dismissWith { finish() } }
                    btnShare.setOnClickListener {
                        dismissWith {
                            startActivity(IntentUtils.getShareTextIntent(displayResult))
                        }
                    }
                    btnOpenUrl.setOnClickListener {
                        MyUtils.openUrl(displayResult, this@ScanQrCodeActivity)
                    }
                    btnLinkChrome.setOnClickListener { MyUtils.openUrlChrome(displayResult, this@ScanQrCodeActivity) }
                    btnLinkQuark.setOnClickListener { MyUtils.openUrlQuark(displayResult, this@ScanQrCodeActivity) }
                    btnLinkWebview.setOnClickListener { MyUtils.openUrlWebview(displayResult, this@ScanQrCodeActivity) }
                    btnLinkSystem.setOnClickListener { MyUtils.openUrlSystem(displayResult, this@ScanQrCodeActivity) }

                    //内容显示
                    tvResult.isEnabled = !isResultEmpty
                    tvTitle.isEnabled = !isResultEmpty
                    tvTitle.visibility = if (isResultEmpty) View.GONE else View.VISIBLE
                    btnShare.isEnabled = !isResultEmpty
                    val isUrl = !isResultEmpty
                            && (displayResult.startsWith("http://") || displayResult.startsWith("https://"))
                    btnOpenUrl.visibility = if (isUrl) View.VISIBLE else View.GONE
                    groupLinkOpen.visibility = if (isUrl) View.VISIBLE else View.GONE

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

                override fun doAfterDismiss() {
                    super.doAfterDismiss()
                    dialogResult = null
                    binding.zxingView.startSpot()
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

    private class ListAdapter(data: MutableList<String>) :
        BaseQuickAdapter<String, BaseViewHolder>(R.layout.item_scan_history, data) {

        override fun convert(holder: BaseViewHolder, item: String) {
            val tv = holder.getView<TextView>(R.id.item)
            tv.text = formatText(item)
        }

        private fun formatText(item: String):String {
            // https://play.google.com/store/apps/details?id=com.proficientcity.ddtankbrazil
            if (item.startsWith("https://play.google.com/store/apps/details")){
                val uri = Uri.parse(item)
                val appId = uri.getQueryParameter("id")
                return "Play : $appId"
            }
            return item
        }
    }
}