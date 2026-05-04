package com.tsuziea.odinextra.features.impl.render

import com.odtheking.odin.clickgui.settings.impl.NumberSetting
import com.odtheking.odin.features.Module

object CameraClip : Module(
    name = "Camera Clip",
    description = "Disables camera clip."
){
    val cameraDist by NumberSetting("Distance", 4f, 3.0, 10.0, 0.1, "The distance of the camera from the player.")
}