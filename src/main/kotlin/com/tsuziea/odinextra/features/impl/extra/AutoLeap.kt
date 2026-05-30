package com.tsuziea.odinextra.features.impl.extra

import com.odtheking.odin.clickgui.settings.Setting.Companion.withDependency
import com.odtheking.odin.clickgui.settings.impl.BooleanSetting
import com.odtheking.odin.clickgui.settings.impl.DropdownSetting
import com.odtheking.odin.clickgui.settings.impl.SelectorSetting
import com.odtheking.odin.events.ChatPacketEvent
import com.odtheking.odin.events.ScreenEvent
import com.odtheking.odin.events.TickEvent
import com.odtheking.odin.events.WorldEvent
import com.odtheking.odin.events.core.on
import com.odtheking.odin.features.Module
import com.odtheking.odin.utils.clickSlot
import com.odtheking.odin.utils.equalsOneOf
import com.odtheking.odin.utils.handlers.schedule
import com.odtheking.odin.utils.itemId
import com.odtheking.odin.utils.modMessage
import com.odtheking.odin.utils.noControlCodes
import com.odtheking.odin.utils.skyblock.dungeon.DungeonClass
import com.odtheking.odin.utils.skyblock.dungeon.DungeonUtils
import com.odtheking.odin.utils.skyblock.dungeon.M7Phases
import com.tsuziea.odinextra.events.NewSectionEvent
import com.tsuziea.odinextra.features.CustomCategory
import com.tsuziea.odinextra.utils.dungeon.ExtraDungeonUtils
import com.tsuziea.odinextra.utils.dungeon.Section
import com.tsuziea.odinextra.utils.rightClick
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
import kotlin.collections.contains

object AutoLeap : Module(
    name = "Auto Leap",
    description = "Auto leap during F7/M7 boss phases.",
    category = CustomCategory.Extra
) {
    private val phase2Dropdown by DropdownSetting("Phase 2 Settings", false)
    private val stormEnragedEnabled by BooleanSetting("Storm Enraged", true, desc = "-> Mage.").withDependency { phase2Dropdown }
    private val stormDieEnabled by BooleanSetting("Storm Die", true, desc = "-> Healer.").withDependency { phase2Dropdown }

    private val phase3Dropdown by DropdownSetting("Phase 3 Settings", false)
    private val i4CompleteEnabled by BooleanSetting("I4 Complete", true, desc = "Berserk -> Tank.").withDependency { phase3Dropdown }
    private val s1ToS2Enabled by BooleanSetting("S1->S2", true, desc = "-> Mage/Archer.").withDependency { phase3Dropdown }
    private val s1ToS2Target by SelectorSetting("Target", "Mage", listOf("Mage", "Archer"), desc = "Leap target.").withDependency { phase3Dropdown && s1ToS2Enabled }
    private val s2ToS3Enabled by BooleanSetting("S2->S3", true, desc = "-> Healer.").withDependency { phase3Dropdown }
    private val s3ToS4Enabled by BooleanSetting("S3->S4", true, desc = "-> Mage.").withDependency { phase3Dropdown }
    private val coreOpenEnabled by BooleanSetting("Core Open", true, desc = "-> Mage.").withDependency { phase3Dropdown }

    private val phase4Dropdown by DropdownSetting("Phase 4 Settings", false)
    private val necronDieEnabled by BooleanSetting("Necron Die", true, desc = "-> Healer.").withDependency { phase4Dropdown }

    private val phase5Dropdown by DropdownSetting("Phase 5 Settings", false)
    private val relicEnabled by BooleanSetting("Relic", true, desc = "Green -> Archer; Purple/Blue -> Berserk").withDependency { phase5Dropdown }

    private enum class LeapState { IDLE, SELECT_ITEM, OPEN_MENU, PENDING_MENU, CLICK_TARGET }
    private var leapState = LeapState.IDLE
    private var leapSlot: Int? = null
    private var targetName: String? = null
    private var leapMenu: AbstractContainerScreen<*>? = null

    private var archCount = 0
    private var relicLept = false

    private val leapItemIds = setOf("SPIRIT_LEAP", "INFINITE_SPIRIT_LEAP")
    private val completedRegex = Regex("^(.{1,16}) (activated|completed) a (terminal|lever|device)! \\((\\d)/(\\d)\\)$")

    init {
        on<WorldEvent.Load> {
            archCount = 0
            relicLept = false
            resetLeap()
        }

        on<ScreenEvent.Open> {
            if (leapState == LeapState.PENDING_MENU) {
                val screen = screen as? AbstractContainerScreen<*> ?: return@on
                val title = screen.title.string.noControlCodes
                if (!title.equalsOneOf("Spirit Leap", "Teleport to Player")) return@on

                leapMenu = screen
                leapState = LeapState.CLICK_TARGET
            }
        }

        on<TickEvent.Start> {
            handleLeap()
            handleRelic()
        }

        on<ChatPacketEvent> {
            if (DungeonUtils.inBoss) handleMessage(value.noControlCodes)
        }

        on<NewSectionEvent> {
            handleNewSection(previous)
        }
    }

    private fun handleRelic() {
        if (!relicEnabled || DungeonUtils.getF7Phase() != M7Phases.P5) return
        if (relicLept) return

        mc.player?.inventory?.getItem(8)?.itemId?.let { lastSlot ->
            val targetClass = when (lastSlot) {
                "GREEN_KING_RELIC" -> DungeonClass.Archer
                "PURPLE_KING_RELIC", "BLUE_KING_RELIC" -> DungeonClass.Berserk
                else -> null
            }

            if (targetClass != null) {
                relicLept = true
                doLeap(targetClass)
            }
        }
    }

    private fun handleMessage(msg: String) {
        val clazz = selfClass()

        if (msg.contains("ARGH!")) {
            if (necronDieEnabled && clazz != DungeonClass.Healer && DungeonUtils.getF7Phase() != M7Phases.P5 && ++archCount == 2){
                doLeap(DungeonClass.Healer)
                archCount = 0
                return
            }
        }

        if (stormEnragedEnabled && clazz == DungeonClass.Archer && msg.contains("Storm is enraged")) {
            doLeap(DungeonClass.Mage)
            return
        }

        if (stormDieEnabled && clazz !in listOf(DungeonClass.Healer, DungeonClass.Berserk) && msg.contains("I should have known that I stood no chance")) {
            doLeap(DungeonClass.Healer)
            return
        }

        if (i4CompleteEnabled && clazz == DungeonClass.Berserk && ExtraDungeonUtils.section == Section.S1) {
            val name = mc.player?.name?.string?.noControlCodes ?: return
            val match = completedRegex.find(msg) ?: return
            val actor = match.groupValues[1].noControlCodes
            val type = match.groupValues[3]

            if (actor.equals(name, true) && type == "device") doLeap(DungeonClass.Tank)
            return
        }
    }

    private fun handleNewSection(section: Section) {
        when (section) {
            Section.S1 -> {
                if (s1ToS2Enabled) {
                    val target = when (s1ToS2Target) {
                        0 -> DungeonClass.Mage
                        1 -> DungeonClass.Archer
                        else -> DungeonClass.Archer
                    }
                    if (selfClass() != target) doLeap(target)
                }
            }

            Section.S2 -> {
                if (s2ToS3Enabled && selfClass() !in listOf(DungeonClass.Healer, DungeonClass.Mage)) doLeap(
                    DungeonClass.Healer)
            }

            Section.S3 -> {
                if (s3ToS4Enabled && selfClass() != DungeonClass.Mage) doLeap(DungeonClass.Mage)
            }

            Section.S4 -> {
                if (coreOpenEnabled && selfClass() != DungeonClass.Mage) doLeap(DungeonClass.Mage)
            }

            else -> return
        }
    }

    private fun selfClass(): DungeonClass? {
        val name = mc.player?.name?.string?.noControlCodes?.lowercase() ?: return null
        return DungeonUtils.dungeonTeammates.firstOrNull { it.name.noControlCodes.lowercase() == name }?.clazz
    }

    private fun handleLeap() {
        val target = targetName ?: return
        val teammate = DungeonUtils.leapTeammates.firstOrNull { it.name.noControlCodes.equals(target, true) }

        if (teammate?.isDead == true) {
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
                leapState = LeapState.OPEN_MENU
            }

            LeapState.OPEN_MENU -> {
                rightClick()
                leapState = LeapState.PENDING_MENU
            }

            LeapState.CLICK_TARGET -> {
                val menu = leapMenu ?: return
                schedule(2) {
                    menu.menu.slots.subList(11, 16)
                        .firstOrNull {
                            it.item.hoverName.string.noControlCodes.substringAfter(' ').equals(target, true)
                        }
                        ?.let { mc.player?.clickSlot(menu.menu.containerId, it.index) }
                    resetLeap()
                }
            }

            else -> Unit
        }
    }

    private fun doLeap(targetClass: DungeonClass) {
        val teammates = DungeonUtils.leapTeammates.filter { it.clazz == targetClass }
        if (teammates.isEmpty()) resetLeap()

        val teammate = teammates.first()
        if (teammate.isDead) resetLeap()

        targetName = teammate.name.noControlCodes
        leapState = LeapState.SELECT_ITEM
        modMessage("§8[§bAutoLeap§8] §aLeaping to $targetName.§r")
    }

    private fun resetLeap() {
        leapState = LeapState.IDLE
        leapSlot = null
        targetName = null
        leapMenu = null
    }
}