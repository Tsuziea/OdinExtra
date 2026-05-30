package com.tsuziea.odinextra.features.impl.extra

import com.odtheking.odin.clickgui.settings.Setting.Companion.withDependency
import com.odtheking.odin.clickgui.settings.impl.KeybindSetting
import com.odtheking.odin.clickgui.settings.impl.KeybindSetting.Companion.isDown
import com.odtheking.odin.clickgui.settings.impl.NumberSetting
import com.odtheking.odin.clickgui.settings.impl.SelectorSetting
import com.odtheking.odin.events.TickEvent
import com.odtheking.odin.events.core.on
import com.odtheking.odin.features.Module
import com.tsuziea.odinextra.features.CustomCategory
import com.tsuziea.odinextra.utils.isHolding
import com.tsuziea.odinextra.utils.leftClick
import com.tsuziea.odinextra.utils.rightClick
import net.minecraft.client.KeyMapping
import net.minecraft.world.item.BlockItem
import net.minecraft.world.phys.HitResult
import org.lwjgl.glfw.GLFW

object AutoClicker : Module(
    name = "Auto Clicker",
    description = "Automatically clicks.",
    category = CustomCategory.Extra
) {
    private val mode by SelectorSetting("Mode", "Normal", listOf("Normal", "Toggle"), "Mode to trigger Auto Clicker")
    private val leftCps by NumberSetting(
        "Left CPS",
        15.0f,
        1.0,
        15.0,
        1,
        desc = "The amount of left clicks per second to perform."
    )
    private val rightCps by NumberSetting(
        "Right CPS",
        15.0f,
        1.0,
        15.0,
        1,
        desc = "The amount of right clicks per second to perform."
    ).withDependency { mode == 0 }
    private val leftClickKeybind = KeybindSetting(
        "Left Toggle", GLFW.GLFW_KEY_UNKNOWN, desc = "The keybind to toggle Auto Clicker."
    ).onPress {
        if (mode == 1) leftToggleState = !leftToggleState
    }.withDependency { mode == 1 }

    private var nextLeftClick = .0
    private var nextRightClick = .0

    private var leftToggleState = false
    private var leftPressTime: Long = 0
    private var rightPressTime: Long = 0

    init {
        this.registerSetting(leftClickKeybind)

        on<TickEvent.Start> {
            if (mc.player == null) return@on
            if (mc.screen != null) return@on
            val now = System.currentTimeMillis()

            if (mc.options.keyUse.defaultKey.isDown() && isHolding("TERMINATOR")) {
                if (now < nextRightClick) return@on
                nextRightClick = now + ((1000.0 / leftCps) + ((Math.random() - .5) * 60.0))
                leftClick()
            } else {
                if (leftActive() && now >= nextLeftClick) {
                    if (mc.hitResult?.type == HitResult.Type.BLOCK) {
                        KeyMapping.set(mc.options.keyAttack.defaultKey, true)
                        return@on
                    }

                    nextLeftClick = now + ((1000.0 / leftCps) + ((Math.random() - .5) * 60.0))
                    leftClick()
                }

                if (rightActive() && now >= nextRightClick) {
                    if (mc.player?.mainHandItem?.item !is BlockItem) {
                        KeyMapping.set(mc.options.keyUse.defaultKey, true)
                        return@on
                    }

                    nextRightClick = now + ((1000.0 / rightCps) + ((Math.random() - .5) * 60.0))
                    rightClick()
                }
            }
        }
    }

    private fun leftActive(): Boolean {
        return when (mode) {
            0 -> {
                if (GLFW.glfwGetMouseButton(mc.window.handle(), GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS) {
                    if (leftPressTime == 0L) {
                        leftPressTime = System.currentTimeMillis()
                    }
                    System.currentTimeMillis() - leftPressTime >= 1000.0 / leftCps
                } else {
                    leftPressTime = 0L
                    false
                }
            }

            1 -> leftToggleState
            else -> false
        }
    }

    private fun rightActive(): Boolean {
        if (GLFW.glfwGetMouseButton(mc.window.handle(), GLFW.GLFW_MOUSE_BUTTON_RIGHT) == GLFW.GLFW_PRESS) {
            if (rightPressTime == 0L) {
                rightPressTime = System.currentTimeMillis()
            }
            return System.currentTimeMillis() - rightPressTime >= 1000.0 / rightCps
        } else {
            rightPressTime = 0L
            return false
        }
    }
}