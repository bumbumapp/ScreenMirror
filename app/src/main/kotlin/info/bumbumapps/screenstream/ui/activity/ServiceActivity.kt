package info.bumbumapps.screenstream.ui.activity

import android.content.ComponentName
import android.content.Context
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import androidx.annotation.CallSuper
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.coroutineScope
import androidx.lifecycle.lifecycleScope
import com.elvishew.xlog.XLog
import info.bumbumapps.screenstream.data.other.getLog
import info.bumbumapps.screenstream.service.AppService
import info.bumbumapps.screenstream.service.ServiceMessage
import info.bumbumapps.screenstream.service.helper.IntentAction
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

abstract class ServiceActivity(@LayoutRes contentLayoutId: Int) : AppUpdateActivity(contentLayoutId) {

    private val serviceMessageLiveData = MutableLiveData<ServiceMessage>()
    private var serviceMessageFlowJob: Job? = null
    private var isBound: Boolean = false

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder) {
            XLog.d(this@ServiceActivity.getLog("onServiceConnected"))
            serviceMessageFlowJob =
                lifecycle.coroutineScope.launch(CoroutineName("ServiceActivity.ServiceMessageFlow")) {
                    (service as AppService.AppServiceBinder).getServiceMessageFlow()
                        .onEach { serviceMessage ->
                            XLog.v(this@ServiceActivity.getLog("onServiceMessage", "$serviceMessage"))
                            serviceMessageLiveData.value = serviceMessage
                        }
                        .catch { cause -> XLog.e(this@ServiceActivity.getLog("onServiceMessage"), cause) }
                        .collect()
                }

            isBound = true
            IntentAction.GetServiceState.sendToAppService(this@ServiceActivity)
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            XLog.w(this@ServiceActivity.getLog("onServiceDisconnected"))
            serviceMessageFlowJob?.cancel()
            serviceMessageFlowJob = null
            isBound = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        lifecycleScope.launchWhenCreated {
            settings.nightModeFlow.onEach { AppCompatDelegate.setDefaultNightMode(it) }.launchIn(this)
        }
        super.onCreate(savedInstanceState)
    }

    override fun onStart() {
        super.onStart()
        serviceMessageLiveData.observe(this) { message -> message?.let { onServiceMessage(it) } }
        bindService(AppService.getAppServiceIntent(this), serviceConnection, Context.BIND_AUTO_CREATE)
    }

    override fun onResume() {
        super.onResume()
        IntentAction.GetServiceState.sendToAppService(this)
    }

    override fun onStop() {
        if (isBound) {
            serviceMessageFlowJob?.cancel()
            serviceMessageFlowJob = null
            unbindService(serviceConnection)
            isBound = false
        }

        super.onStop()
    }

    @CallSuper
    open fun onServiceMessage(serviceMessage: ServiceMessage) {
        if (serviceMessage is ServiceMessage.FinishActivity) finishAndRemoveTask()
    }

    fun getServiceMessageLiveData(): LiveData<ServiceMessage> = serviceMessageLiveData
}