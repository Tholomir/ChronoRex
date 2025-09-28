package com.dino.chronorex.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.dino.chronorex.ChronoRexAppContainerHolder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val holder = context.applicationContext as? ChronoRexAppContainerHolder ?: return
        CoroutineScope(Dispatchers.Default).launch {
            holder.container.reminderManager.refreshSchedule()
        }
    }
}
