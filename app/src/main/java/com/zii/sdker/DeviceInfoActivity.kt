package com.zii.sdker

import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.StatFs
import android.telephony.TelephonyManager
import android.text.format.Formatter
import android.util.Log
import com.google.android.gms.ads.identifier.AdvertisingIdClient
import com.zii.sdker.base.BaseActivity
import com.zii.sdker.databinding.ActivityDeviceInfoBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import java.security.MessageDigest
import java.util.*

class DeviceInfoActivity : BaseActivity() {
    private lateinit var binding: ActivityDeviceInfoBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDeviceInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val sysApi = Build.VERSION.SDK_INT.toString()
        val sysRelease = Build.VERSION.RELEASE
        val board = Build.BOARD
        val device = Build.DEVICE
        val buildId = Build.ID
        val brand = Build.BRAND
        val model = Build.MODEL
        val time = Build.TIME //开机时间
        Build.USER

        // 系统版本
        val sysVersion = sysApi + sysRelease
        // 硬件型号
        val hardwareVersion = board + device + buildId + brand + model
        // 硬盘容量
        val storageSize = getStorageSize()
        // 开机时间
        val buildTime = time.toString()
        // 国家代码
        val countryCode = Locale.getDefault().country
        // 系统语言
        val language = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            resources.configuration.locales.get(0)
        } else {
            resources.configuration.locale
        }.language
        // 设备名称-无

        // 运营商信息
        val simOperator = getSimOperator()

        //client id
        val start = getMD5Hex(sysVersion + hardwareVersion + storageSize)
        val end = getMD5Hex(buildTime + countryCode + language + simOperator)
        val clientId = start.substring(8, 24) + end.substring(8, 24)

        //hex test
        val start2 = md5Hex(sysVersion + hardwareVersion + storageSize)
        val end2 = md5Hex(buildTime + countryCode + language + simOperator)
        val clientId2 = start.substring(8, 24) + end.substring(8, 24)

        val infoAll = "API版本：$sysApi"
            .plus("\n")
            .plus("系统版本：$sysRelease")
            .plus("\n")
            .plus("主板:$board")
            .plus("\n")
            .plus("设备:$device")
            .plus("\n")
            .plus("编译序列:$buildId")
            .plus("\n")
            .plus("制造商:$brand")
            .plus("\n")
            .plus("产品型号:$model")
            .plus("\n")
            .plus("编译时间:$time")
            .plus("\n\n\n")
            .plus("SDK设备信息：")
            .plus("\n")
            .plus("系统版本：$sysVersion")
            .plus("\n")
            .plus("硬件型号：$hardwareVersion")
            .plus("\n")
            .plus("硬盘容量：$storageSize")
            .plus("\n")
            .plus("开机时间：$buildTime")
            .plus("\n")
            .plus("国家代码：$countryCode")
            .plus("\n")
            .plus("系统语言：$language")
            .plus("\n")
            .plus("设备名称：无")
            .plus("\n")
            .plus("运营商信息：$simOperator")
            .plus("\n\n\n")
            .plus("client start：$start")
            .plus("\n")
            .plus("client end：$end")
            .plus("\n")
            .plus("client result：$clientId")
//                .plus("\n")
//                .plus("\n")
//                .plus("\n")
//                .plus("client2 start：$start2")
//                .plus("\n")
//                .plus("client2 end：$end2")
//                .plus("\n")
//                .plus("client2 result：$clientId2")

        binding.tvDeviceInfo.text = infoAll
        Log.d("zii-", infoAll)

        //Google 广告ID
        GlobalScope.launch {
            Log.d("zii-", "开始获取谷歌广告ID ...")
            val advertisingIdInfo = AdvertisingIdClient.getAdvertisingIdInfo(this@DeviceInfoActivity)
            val limitAdTrackingEnabled = advertisingIdInfo.isLimitAdTrackingEnabled
            val googleAdsId = advertisingIdInfo.id.toString()
            Log.d("zii-", "获取谷歌广告ID成功：$googleAdsId ")
            coroutineScope {
                launch(Dispatchers.Main) {
                    val infoAdsId = infoAll
                        .plus("\n\n\n")
                        .plus("限制个性化广告跟踪：$limitAdTrackingEnabled")
                        .plus("\n")
                        .plus("谷歌广告ID：$googleAdsId")
                    binding.tvDeviceInfo.text = infoAdsId
                }
            }
        }
    }

    private fun getStorageSize(): String {
        // 硬盘容量(内部存储，并不是整个硬盘容量)
        val path = Environment.getDataDirectory()
        val stat = StatFs(path.path)
        val size: Long =
            try {
                stat.totalBytes
            } catch (e: NoSuchMethodError) {
                (stat.blockSize * stat.blockCount).toLong()
            }
        return Formatter.formatFileSize(this, size)
    }

    private fun getSimOperator(): String {
        // 运营商信息：
        var simOperator = ""
        try {
            // 增加try-catch，防止没有权限闪退
            val telephonyManager = getSystemService(TELEPHONY_SERVICE) as TelephonyManager
            simOperator = telephonyManager.simOperator
        } catch (e: Exception) {
            Log.e("zii-", "运营商获取失败", e)
        }
        return simOperator
    }

    private fun getMD5Hex(inputString: String): String {
        val md = MessageDigest.getInstance("MD5")
        md.update(inputString.toByteArray())
        val digest = md.digest()
        return convertByteToHex(digest)
    }

    private fun convertByteToHex(byteData: ByteArray): String {
        val sb = StringBuilder()
        for (item in byteData) {
            sb.append(((item.toInt() and 0xff) + 0x100).toString(16).substring(1))
        }
        return sb.toString()
    }

    private val HEX_DIGITS_UPPER =
        charArrayOf('0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F')
    private val HEX_DIGITS_LOWER =
        charArrayOf('0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f')

    private fun md5Hex(input: String): String {
        val md = MessageDigest.getInstance("MD5")
        md.update(input.toByteArray())
        val digest = md.digest()
        return bytes2HexString(digest)
    }

    private fun bytes2HexString(bytes: ByteArray?, isUpperCase: Boolean = false): String {
        if (bytes == null) return ""
        val hexDigits: CharArray = if (isUpperCase) HEX_DIGITS_UPPER else HEX_DIGITS_LOWER
        val len = bytes.size
        if (len <= 0) return ""
        val ret = CharArray(len shl 1)
        var i = 0
        var j = 0
        while (i < len) {
            ret[j++] = hexDigits[bytes[i].toInt() shr 4 and 0x0f]
            ret[j++] = hexDigits[(bytes[i].toInt() and 0x0f)]
            i++
        }
        return String(ret)
    }
}