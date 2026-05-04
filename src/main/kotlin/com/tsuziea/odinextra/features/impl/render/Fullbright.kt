package com.tsuziea.odinextra.features.impl.render

import com.odtheking.odin.features.Module
import com.tsuziea.odinextra.mixin.OptionInstanceAccessor

object Fullbright : Module(
    name = "Fullbright",
    description = "Forces gamma to full brightness.") {

    override fun onEnable() {
        super.onEnable()
        setGamma(16.0)
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
