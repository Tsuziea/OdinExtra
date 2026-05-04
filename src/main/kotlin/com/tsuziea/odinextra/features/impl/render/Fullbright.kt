package com.tsuziea.odinextra.features.impl.render

import com.odtheking.odin.events.TickEvent
import com.odtheking.odin.events.core.on
import com.odtheking.odin.features.Module
import com.tsuziea.odinextra.mixin.OptionInstanceAccessor
import kotlin.math.abs

object Fullbright : Module(
    name = "Fullbright",
    description = "Forces gamma to full brightness.") {

    init {
        on<TickEvent.End> {
            if (!enabled) return@on
            val currentGamma = runCatching { mc.options.gamma().get() }.getOrNull() ?: return@on
            if (abs(currentGamma - 16.0) > 0.1) {
                setGamma(16.0)
            }
        }
    }

    override fun onDisable() {
        super.onDisable()
        setGamma(1.0)
    }

    @Suppress("CAST_NEVER_SUCCEEDS")
    private fun setGamma(value: Double) {
        val result = runCatching {
            val gammaOption = mc.options.gamma()
            (gammaOption as OptionInstanceAccessor<Double>).`odinextra$setValue`(value)
        }
        if (result.isFailure) {
            runCatching {
                mc.options.gamma().set(value)
            }
        }
    }
}