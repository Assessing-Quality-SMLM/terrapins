package com.coxphysics.terrapins.views

import com.coxphysics.terrapins.models.hawk.HAWK
import com.coxphysics.terrapins.models.hawk.Settings
import javax.swing.SwingWorker

class HAWKWorker private constructor(
    private val update_action_ : (Boolean) -> Unit,
    private val settings_: Settings
): SwingWorker<Unit, Boolean>()
{
    companion object
    {
        fun from(update_action : (Boolean) -> Unit, settings: Settings): HAWKWorker
        {
            return HAWKWorker(update_action, settings)
        }
    }

    override fun doInBackground(): Unit
    {
        val value = HAWK.from(settings_).save_to_disk()
        publish(value)
        return
    }

    override fun process(chunks: MutableList<Boolean>?)
    {
        if (chunks == null)
            return
        for (item in chunks)
        {
            update_action_(item);
        }
    }
}