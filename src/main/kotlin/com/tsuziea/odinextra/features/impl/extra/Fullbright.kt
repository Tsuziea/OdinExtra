package com.tsuziea.odinextra.features.impl.extra

import com.odtheking.odin.events.WorldEvent
import com.odtheking.odin.events.core.on
import com.odtheking.odin.features.Module
import com.tsuziea.odinextra.features.CustomCategory
import com.tsuziea.odinextra.mixin.OptionInstanceAccessor

object Fullbright : Module(
    name = "Fullbright",
    description = "Forces gamma to full brightness.",
    category = CustomCategory.Extra
) {
    init {
        on< WorldEvent.Load> {
            if (mc.options.gamma().get() < 16.0)
            setGamma(16.0)
        }
    }

    override fun onEnable() {
        super.onEnable()
        if (mc.player != null) setGamma(16.0)
    }

    override fun onDisable() {
        super.onDisable()
        setGamma(1.0)
    }

    @Suppress("CAST_NEVER_SUCCEEDS")
    private fun setGamma(value: Double) {
        val gammaOption = mc.options.gamma()
        (gammaOption as OptionInstanceAccessor<Double>).`odinextra$setValue`(value)
    }
}