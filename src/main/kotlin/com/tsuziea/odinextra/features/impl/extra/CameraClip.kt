package com.tsuziea.odinextra.features.impl.extra

import com.odtheking.odin.clickgui.settings.impl.NumberSetting
import com.odtheking.odin.features.Module
import com.tsuziea.odinextra.features.CustomCategory

object CameraClip : Module(
    name = "Camera Clip",
    description = "Disables camera clip.",
    category = CustomCategory.Extra
){
    val cameraDist by NumberSetting("Distance", 4f, 3.0, 10.0, 0.1, "The distance of the camera from the player.")
}