package com.tsuziea.odinextra.features.impl.dungeon

import com.odtheking.odin.clickgui.settings.impl.KeybindSetting
import com.odtheking.odin.features.Module
import com.odtheking.odin.utils.skyblock.dungeon.DungeonUtils
import org.lwjgl.glfw.GLFW

object DungeonAbilities : Module(
    name = "Dungeon Abilities",
    description = "Uses your ability when keybind is pressed while in dungeons."
) {
    private val abilityKeybind by KeybindSetting(
        "Ability Keybind",
        GLFW.GLFW_KEY_UNKNOWN,
        desc = "Keybind to use your ability."
    ).onPress {
        if (enabled && DungeonUtils.inDungeons) mc.player?.drop(true)
    }
}