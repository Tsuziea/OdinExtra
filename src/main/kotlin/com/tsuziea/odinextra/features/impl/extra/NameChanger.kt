package com.tsuziea.odinextra.features.impl.extra

import com.odtheking.odin.clickgui.settings.impl.StringSetting
import com.odtheking.odin.features.Module
import com.tsuziea.odinextra.features.CustomCategory
import net.minecraft.client.Minecraft
import java.util.regex.Pattern

object NameChanger : Module(
    name = "Name Changer",
    description = "Replaces your name with the given nick, color codes work (&).",
    category = CustomCategory.Extra
) {
    private val nick by StringSetting(name = "Nick", default = "Odin", 32, desc = "The nick to replace your name with.")

    fun nickName(): String =
        nick.replace("&", "§")

    val pattern: Pattern by lazy {
        val name = Minecraft.getInstance().user.name
        Pattern.compile(Pattern.quote(name))
    }
}