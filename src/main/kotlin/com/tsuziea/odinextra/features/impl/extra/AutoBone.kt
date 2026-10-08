package com.tsuziea.odinextra.features.impl.extra

import com.mojang.blaze3d.platform.InputConstants
import com.odtheking.odin.clickgui.settings.impl.NumberSetting
import com.odtheking.odin.events.InputEvent
import com.odtheking.odin.events.LevelEvent
import com.odtheking.odin.events.core.on
import com.odtheking.odin.features.Module
import com.odtheking.odin.utils.clickSlot
import com.odtheking.odin.utils.handlers.schedule
import com.odtheking.odin.utils.sendCommand
import com.tsuziea.odinextra.features.CustomCategory
import com.tsuziea.odinextra.events.TickStart
import com.tsuziea.odinextra.utils.isHolding
import com.tsuziea.odinextra.utils.rightClick
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
import net.minecraft.world.inventory.ContainerInput

object AutoBone : Module(
    name = "Auto Bone",
    description = "Left click while holding bonemerang to proceed.",
    category = CustomCategory.Extra
) {
    private val loadoutSlots = intArrayOf(14, 15, 16, 23, 24, 25, 32, 33, 34, 41, 42, 43)
    private val swordSlot by NumberSetting("Sword Slot", 1.0, 1.0, 8.0, 1, desc = "Sword's hotbar slot.")
    private val fbSlot by NumberSetting("Frozen Blaze Slot", 1.0, 1.0, 12.0, 1, desc = "Frozen Blaze loadout number.")
    private val mrSlot by NumberSetting("Monster Raider Slot", 1.0, 1.0, 12.0, 1, desc = "Monster Raider loadout number.")

    private var state = State.IDLE

    init {
        on<LevelEvent.Load> {
            state = State.IDLE
        }

        on<InputEvent> {
            if (state != State.IDLE || mc.screen != null || key.value != InputConstants.MOUSE_BUTTON_LEFT || !isHolding(boneIds)) return@on

            state = State.PREPARE
            cancel()
        }

        on<TickStart> {
            val player = mc.player ?: return@on

            when (state) {
                State.PREPARE -> {
                    sendCommand("loadout")

                    state = State.WTFFB
                }

                State.WTFFB -> {
                    val screen = mc.screen as? AbstractContainerScreen<*> ?: return@on
                    if (!screen.title.string.contains("Loadouts")) return@on

                    player.clickSlot(loadoutSlots[(fbSlot - 1).toInt()], 0, ContainerInput.PICKUP)
                    player.closeContainer()

                    state = State.THROW
                }

                State.THROW -> {
                    rightClick()
                    state = State.SWAP_SWORD
                }

                State.SWAP_SWORD -> {
                    schedule(2){
                        player.inventory.selectedSlot = (swordSlot - 1).toInt()
                        sendCommand("loadout")
                    }

                    state = State.SWAP_ARMOR
                }

                State.SWAP_ARMOR -> {
                    val screen = mc.screen as? AbstractContainerScreen<*> ?: return@on
                    if (!screen.title.string.contains("Loadouts")) return@on

                    player.clickSlot(loadoutSlots[(mrSlot - 1).toInt()], 0, ContainerInput.PICKUP)
                    player.closeContainer()

                    state = State.IDLE
                }

                else -> Unit
            }
        }
    }

    private enum class State { IDLE, PREPARE, WTFFB, THROW, SWAP_SWORD, SWAP_ARMOR }
    private val boneIds = setOf("BONE_BOOMERANG", "STARRED_BONE_BOOMERANG")
}
