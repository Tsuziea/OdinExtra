package com.tsuziea.odinextra.features.impl.extra

import com.odtheking.odin.clickgui.settings.Setting.Companion.withDependency
import com.odtheking.odin.clickgui.settings.impl.BooleanSetting
import com.odtheking.odin.clickgui.settings.impl.DropdownSetting
import com.odtheking.odin.clickgui.settings.impl.SelectorSetting
import com.odtheking.odin.events.MessageEvent
import com.odtheking.odin.events.LevelEvent
import com.odtheking.odin.events.core.on
import com.odtheking.odin.features.Module
import com.odtheking.odin.utils.alert
import com.odtheking.odin.utils.clickSlot
import com.odtheking.odin.utils.equalsOneOf
import com.odtheking.odin.utils.handlers.schedule
import com.odtheking.odin.utils.itemId
import com.odtheking.odin.utils.noControlCodes
import com.odtheking.odin.utils.skyblock.dungeon.DungeonClass
import com.odtheking.odin.utils.skyblock.dungeon.DungeonUtils
import com.odtheking.odin.utils.skyblock.dungeon.M7Phases
import com.tsuziea.odinextra.events.NewSectionEvent
import com.tsuziea.odinextra.events.TickStart
import com.tsuziea.odinextra.features.CustomCategory
import com.tsuziea.odinextra.utils.dungeon.Section
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
import kotlin.text.contains

object FastLeap : Module(
    name = "Fast Leap",
    description = "Class-based semi auto leap for F7/M7 boss phases.",
    category = CustomCategory.Extra
) {
    private val doorOpenEnabled by BooleanSetting("Door Open", true, desc = "-> Door Opener.")

    private val phase1Dropdown by DropdownSetting("Phase 1 Settings", false)
    private val crystalEnabled by BooleanSetting("Crystal", true, desc = "-> Tank.").withDependency { phase1Dropdown }

    private val phase2Dropdown by DropdownSetting("Phase 2 Settings", false)
    private val stormEnragedEnabled by BooleanSetting("Storm Enraged", true, desc = "-> Mage.").withDependency { phase2Dropdown }
    private val stormDieEnabled by BooleanSetting("Storm Die", true, desc = "-> Healer.").withDependency { phase2Dropdown }

    private val phase3Dropdown by DropdownSetting("Phase 3 Settings", false)
    private val s1ToS2Enabled by BooleanSetting("S1->S2", true, desc = "-> Mage/Archer.").withDependency { phase3Dropdown }
    private val s1ToS2Target by SelectorSetting("Target", "Mage", listOf("Mage", "Archer"), desc = "Leap target.").withDependency { phase3Dropdown && s1ToS2Enabled }
    private val s2ToS3Enabled by BooleanSetting("S2->S3", true, desc = "-> Healer.").withDependency { phase3Dropdown }
    private val s3ToS4Enabled by BooleanSetting("S3->S4", true, desc = "-> Mage.").withDependency { phase3Dropdown }
    private val coreOpenEnabled by BooleanSetting("Core Open", true, desc = "-> Mage.").withDependency { phase3Dropdown }

    private val phase4Dropdown by DropdownSetting("Phase 4 Settings", false)
    private val necronDieEnabled by BooleanSetting("Necron Die", true, desc = "-> Healer.").withDependency { phase4Dropdown }

    private val phase5Dropdown by DropdownSetting("Phase 5 Settings", false)
    private val relicEnabled by BooleanSetting("Relic", true, desc = "Green -> Archer; Purple/Blue -> Berserk").withDependency { phase5Dropdown }

    private enum class LeapState { IDLE, SELECT_ITEM, CLICK_TARGET }

    private var leapState = LeapState.IDLE
    private var leapSlot: Int? = null
    private var leapStartAt = 0L
    private var targetName: String? = null
    private var relicLept = false

    private val leapItemIds = setOf("SPIRIT_LEAP", "INFINITE_SPIRIT_LEAP")
    private val witherDoorRegex = Regex("^(.+) opened a WITHER door!$")

    init {
        on<LevelEvent.Load> {
            relicLept = false
            resetLeap()
        }

        on<TickStart> {
            handleLeap()
            handleCrystal()
            handleRelic()
        }

        on<MessageEvent.Chat> {
            handleMessage(message)
        }

        on<NewSectionEvent> {
            handleNewSection(previous)
        }
    }

    private fun handleCrystal() {

    }

    private fun handleRelic() {
        if (!relicEnabled || DungeonUtils.getF7Phase() != M7Phases.P5) return
        if (relicLept) return

        mc.player?.inventory?.getItem(8)?.itemId?.let { lastSlot ->
            val targetClass = when (lastSlot) {
                "GREEN_KING_RELIC" -> DungeonClass.ARCHER
                "PURPLE_KING_RELIC", "BLUE_KING_RELIC" -> DungeonClass.BERSERK
                else -> null
            }

            if (targetClass != null) {
                relicLept = true
                doLeap(targetClass)
            }
        }
    }

    private fun handleMessage(msg: String) {
        val clazz = DungeonUtils.currentDungeonPlayer.clazz
        val name = DungeonUtils.currentDungeonPlayer.name

        if (doorOpenEnabled && clazz in listOf(DungeonClass.ARCHER, DungeonClass.MAGE) && witherDoorRegex.matches(msg)) {
            val opener = DungeonUtils.doorOpener
            val target = DungeonUtils.dungeonTeammatesNoSelf.firstOrNull { it.name == opener }?.clazz ?: return

            doLeap(target)
            return
        }

        if (msg.noControlCodes.contains("$name picked up an Energy Crystal!") && crystalEnabled && DungeonUtils.getF7Phase() == M7Phases.P1) {
            schedule(2) { doLeap(DungeonClass.TANK) }
            return
        }

        if (msg.noControlCodes.contains("Storm is enraged") && stormEnragedEnabled && clazz == DungeonClass.ARCHER) {
            doLeap(DungeonClass.MAGE)
            return
        }

        if (msg.noControlCodes.contains("[BOSS] Storm: I should have known that I stood no chance.") && stormDieEnabled && clazz !in listOf(DungeonClass.HEALER, DungeonClass.BERSERK)) {
            doLeap(DungeonClass.HEALER)
            return
        }

        if (msg.noControlCodes.contains("[BOSS] Necron: Let's make some space!") && necronDieEnabled && clazz != DungeonClass.HEALER && DungeonUtils.getF7Phase() == M7Phases.P4){
            doLeap(DungeonClass.HEALER)
            return
        }
    }

    private fun handleNewSection(section: Section) {
        val clazz = DungeonUtils.currentDungeonPlayer.clazz

        when (section) {
            Section.S1 -> {
                if (s1ToS2Enabled) {
                    val target = when (s1ToS2Target) {
                        0 -> DungeonClass.MAGE
                        1 -> DungeonClass.ARCHER
                        else -> DungeonClass.ARCHER
                    }
                    if (clazz != target) doLeap(target)
                }
            }

            Section.S2 -> {
                if (s2ToS3Enabled&& clazz !in listOf(DungeonClass.HEALER, DungeonClass.MAGE)) doLeap(
                    DungeonClass.HEALER)
            }

            Section.S3 -> {
                if (s3ToS4Enabled&& clazz != DungeonClass.MAGE) doLeap(DungeonClass.MAGE)
            }

            Section.S4 -> {
                if (coreOpenEnabled&& clazz != DungeonClass.MAGE) doLeap(DungeonClass.MAGE)
            }

            else -> return
        }
    }

    private fun handleLeap() {
        val target = targetName ?: return
        val teammate = DungeonUtils.leapTeammates.firstOrNull { it.name.noControlCodes.equals(target, true) }
        val now = System.currentTimeMillis()

        if (teammate?.isDead == true || now - leapStartAt > 3000) {
            resetLeap()
            return
        }

        when (leapState) {
            LeapState.SELECT_ITEM -> {
                val player = mc.player ?: return
                val slot = leapSlot ?: (0..8).firstOrNull { idx -> player.inventory.getItem(idx).itemId in leapItemIds }?.also { leapSlot = it }

                if (slot == null) {
                    resetLeap()
                    return
                }

                if (player.mainHandItem.itemId !in leapItemIds) player.inventory.selectedSlot = slot
                leapState = LeapState.CLICK_TARGET
            }

            LeapState.CLICK_TARGET -> {
                val screen = mc.screen as? AbstractContainerScreen<*> ?: return
                if (!screen.title.string.equalsOneOf("Spirit Leap", "Teleport to Player")) return

                screen.menu.slots.subList(11, 16).firstOrNull {
                    it.item.hoverName.string.noControlCodes.substringAfter(' ').equals(target, true)
                }?.let { mc.player?.clickSlot(it.index) }

                resetLeap()
            }

            else -> Unit
        }
    }

    private fun doLeap(targetClass: DungeonClass) {
        val teammates = DungeonUtils.leapTeammates.filter { it.clazz == targetClass }
        if (teammates.isEmpty()) {
            resetLeap()
            return
        }

        val teammate = teammates.first()
        if (teammate.isDead) {
            resetLeap()
            return
        }

        schedule(2){ alert("§bLeap!") }

        targetName = teammate.name.noControlCodes
        leapState = LeapState.SELECT_ITEM
        leapStartAt = System.currentTimeMillis()
    }

    private fun resetLeap() {
        leapState = LeapState.IDLE
        leapSlot = null
        targetName = null
    }
}
