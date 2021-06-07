package com.zii.sdker

import android.app.Activity
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.view.inputmethod.EditorInfo
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.ConvertUtils
import com.blankj.utilcode.util.LogUtils
import com.bumptech.glide.Glide
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.lxj.xpopup.XPopup
import com.zii.sdker.base.BaseActivity
import com.zii.sdker.databinding.ActivityApkListBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class ApkListActivity : BaseActivity() {

    private lateinit var binding: ActivityApkListBinding
    private lateinit var listData: MutableList<ApkInfo>
    private var listSearch: List<ApkInfo>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityApkListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initList()

        binding.radio.setOnCheckedChangeListener { _, checkedId ->
            LogUtils.d("checkId:$checkedId")
            when (checkedId) {
                R.id.rb_order_update_time -> {
                    getAdapterData()?.sortByDescending { it.updateTime }
                }
                R.id.rb_order_name -> {
                    getAdapterData()?.sortBy { it.name }
                }
                R.id.rb_order_package_name -> {
                    getAdapterData()?.sortBy { it.packageName }
                }
                R.id.rb_order_file_size -> {
                    getAdapterData()?.sortByDescending { it.fileSize }
                }
                else -> {
                    getAdapterData()?.sortByDescending { it.updateTime }
                }
            }
            getAdapter()?.notifyDataSetChanged()
        }

        binding.edtSearch.setOnEditorActionListener { view, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                search(view.text)
                return@setOnEditorActionListener true
            }
            return@setOnEditorActionListener false
        }
    }

    private fun search(text: CharSequence) {
        listSearch = getAdapterData()?.filter { it.name.contains(text) || it.packageName.contains(text) }
    }

    private data class ApkInfo(
        val packageName: String,
        val name: String,
        val icon: Drawable,
        val packagePath: String,
        val fileSize: String,
        val versionName: String,
        val versionCode: Int,
        val installedTime: String,
        val updateTime: String,
        val isSystem: Boolean
    ) {
        override fun toString(): String = "名称：$name\n" +
                "包名：$packageName\n" +
                "大小：$fileSize\n" +
                "版本名称：$versionName\n" +
                "版本代码：$versionCode\n" +
                "APK路径：$packagePath\n" +
                "安装时间：$installedTime\n" +
                "更新时间：$updateTime\n" +
                "系统应用：$isSystem"
    }

    private class ListAdapter(data: MutableList<ApkInfo>) :
        BaseQuickAdapter<ApkInfo, BaseViewHolder>(R.layout.list_apk, data) {

        override fun convert(holder: BaseViewHolder, item: ApkInfo) {
            val ivIcon = holder.getView<ImageView>(R.id.iv_icon)
            val tvName = holder.getView<TextView>(R.id.tv_name)
            val tvSize = holder.getView<TextView>(R.id.tv_size)
            val tvPackage = holder.getView<TextView>(R.id.tv_package)
            val tvInstallTime = holder.getView<TextView>(R.id.tv_install_time)
            val btnDetail = holder.getView<Button>(R.id.btn_detail)
            val btnShare = holder.getView<Button>(R.id.btn_share)

            Glide.with(ivIcon).load(item.icon).into(ivIcon)
            tvName.text = item.name
            tvSize.text = item.fileSize
            tvPackage.text = item.packageName
            tvInstallTime.text = "最后安装时间：${item.updateTime}"
            btnDetail.setOnClickListener {
                val dialog = XPopup.Builder(it.context)
                    .asConfirm("APK信息", item.toString(), null)
                dialog.isHideCancel = true
                dialog.contentTextView.gravity = Gravity.LEFT
                dialog.show()
            }
            btnShare.setOnClickListener {
                val intent = Intent(Intent.ACTION_SEND)
                intent.type = "*/*"
//                intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(File(item.packagePath)))
                val fileUri: Uri = FileProvider.getUriForFile(
                    context, "com.zii.sdker.provider",
                    File(item.packagePath)
                )
                intent.putExtra(Intent.EXTRA_STREAM, fileUri)
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                (context as Activity).startActivity(intent)
            }
        }
    }

    private fun initList() {
        val loading = XPopup.Builder(this)
            .asLoading("加载中...")
        loading.show()

        GlobalScope.launch {
            //get a list of installed apps.
            val packages = packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
            listData = packages
                .filterNot { ApplicationInfo.FLAG_SYSTEM and it.flags != 0 } //过滤系统应用
                .map { loadApkInfo(it) }
                .toMutableList()

            coroutineScope {
                launch(Dispatchers.Main) {
                    val context = this@ApkListActivity
                    val autoAdapter =
                        ArrayAdapter(context, R.layout.list_auto_complete_edittext, listData.map { it.packageName })

                    binding.edtSearch.setAdapter(autoAdapter)

                    binding.recv.layoutManager = LinearLayoutManager(context)
                    binding.recv.adapter = ListAdapter(listData)
                    binding.recv.addItemDecoration(DividerItemDecoration(context, RecyclerView.VERTICAL))
                    loading?.dismiss()
                }
            }
        }
    }

    private fun loadApkInfo(it: ApplicationInfo): ApkInfo {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        val packageName = it.packageName
        val packageInfo = packageManager.getPackageInfo(packageName, 0)
        val name = it.loadLabel(packageManager).toString()
        val icon = it.loadIcon(packageManager)
        val packagePath = it.sourceDir
        val fileSize = ConvertUtils.byte2FitMemorySize(File(packagePath).length(), 1)
        val versionName = packageInfo.versionName
        val versionCode = packageInfo.versionCode
        val installTime = dateFormat.format(Date(packageInfo.firstInstallTime))
        val updateTime = dateFormat.format(Date(packageInfo.lastUpdateTime))
        val isSystem = ApplicationInfo.FLAG_SYSTEM and it.flags != 0

        return ApkInfo(
            packageName,
            name,
            icon,
            packagePath,
            fileSize,
            versionName,
            versionCode,
            installTime,
            updateTime,
            isSystem
        )
    }

    private fun getAdapterData(): MutableList<ApkInfo>? {
        return getAdapter()?.data
    }

    private fun getAdapter(): ListAdapter? {
        val adapter = binding.recv.adapter
        return if (adapter == null) null else adapter as ListAdapter
    }

}