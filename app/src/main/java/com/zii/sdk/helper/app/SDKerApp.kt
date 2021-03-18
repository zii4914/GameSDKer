package com.zii.sdk.helper.app

import android.app.Application
import android.content.Context
import com.blankj.utilcode.util.LogUtils
import com.luck.picture.lib.app.IApp
import com.luck.picture.lib.app.PictureAppMaster
import com.luck.picture.lib.engine.ImageEngine
import com.luck.picture.lib.engine.PictureSelectorEngine
import com.luck.picture.lib.entity.LocalMedia
import com.luck.picture.lib.listener.OnResultCallbackListener

class SDKerApp : Application(), IApp {
    override fun onCreate() {
        super.onCreate()
        LogUtils.getConfig()
            .setGlobalTag("zii-")
            .setBorderSwitch(false)

        PictureAppMaster.getInstance().app = this
    }

    override fun getAppContext(): Context {
        return this
    }

    override fun getPictureSelectorEngine(): PictureSelectorEngine {
        return object : PictureSelectorEngine {
            override fun createEngine(): ImageEngine {
                return GlideEngine.get()
            }

            override fun getResultCallbackListener(): OnResultCallbackListener<LocalMedia> {
                return object : OnResultCallbackListener<LocalMedia> {
                    override fun onResult(result: MutableList<LocalMedia>?) {
                        //这种情况是内存极度不足的情况下，比如开启开发者选项中的不保留活动或后台进程限制，导致OnResultCallbackListener被回收
                        // 可以在这里进行一些补救措施，通过广播或其他方式将结果推送到相应页面，防止结果丢失的情况
                    }

                    override fun onCancel() {
                    }
                }
            }
        }
    }
}