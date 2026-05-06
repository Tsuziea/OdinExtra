package com.tsuziea.odinextra.features.impl.dungeon

import com.odtheking.odin.clickgui.settings.impl.BooleanSetting
import com.odtheking.odin.events.TickEvent
import com.odtheking.odin.events.WorldEvent
import com.odtheking.odin.events.core.on
import com.odtheking.odin.features.Module
import com.odtheking.odin.utils.equalsOneOf
import com.odtheking.odin.utils.itemId
import com.odtheking.odin.utils.noControlCodes
import com.odtheking.odin.utils.skyblock.dungeon.DungeonUtils
import com.odtheking.odin.utils.skyblock.dungeon.M7Phases
import com.tsuziea.odinextra.utils.leftClick
import com.tsuziea.odinextra.utils.rightClick
import net.minecraft.core.BlockPos
import net.minecraft.world.entity.EquipmentSlot
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.boss.enderdragon.EndCrystal
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.phys.BlockHitResult
import net.minecraft.world.phys.EntityHitResult
import net.minecraft.world.phys.Vec3
import kotlin.collections.get

object TriggerBot : Module(
    name = "Trigger Bot",
    description = "Automatically triggers when hit result is target."
) {

    private val secret by BooleanSetting("Secret", true, desc = "Right clicks on secrets.")
    private val crystal by BooleanSetting("Crystal", true, desc = "Right clicks on crystals in P1.")
    private val lever by BooleanSetting("Lever", true, desc = "Left clicks on levers in P3.")
    private val relic by BooleanSetting("Relic", true, desc = "Right clicks on relics and their respective pedestals in P5.")

    private var lastClick = 0L
    private val clickedSecrets = mutableSetOf<BlockPos>()

    init {
        on<WorldEvent.Load> {
            lastClick = 0L
            clickedSecrets.clear()
        }

        on<TickEvent.Start> {
            if (mc.screen != null) return@on
            if (!DungeonUtils.inDungeons) return@on

            val now = System.currentTimeMillis()
            if (now - lastClick < 200L) return@on

            if (secret && DungeonUtils.inClear) triggerSecret()
            if (crystal && DungeonUtils.getF7Phase() == M7Phases.P1) triggerCrystal()
            if (lever && DungeonUtils.getF7Phase() == M7Phases.P2) triggerLever()
            if (relic && DungeonUtils.getF7Phase() == M7Phases.P5) triggerRelic()
        }
    }

    private fun triggerSecret() {
        if (DungeonUtils.currentRoomName.equalsOneOf("Water Board", "Three Weirdos")) return

        val hit = mc.hitResult ?: return
        val hitBlock = (hit as? BlockHitResult)?.blockPos ?: return
        val state = mc.level?.getBlockState(hitBlock) ?: return

        if (DungeonUtils.isSecret(state, hitBlock) && !clickedSecrets.contains(hitBlock)) {
            lastClick = System.currentTimeMillis()
            rightClick()
            clickedSecrets.add(hitBlock)
        }
    }

    private fun triggerCrystal() {
        val hit = mc.hitResult ?: return
        val hitEntity = (hit as? EntityHitResult)?.entity ?: return

        if (hitEntity is EndCrystal) {
            lastClick = System.currentTimeMillis()
            rightClick()
        }
    }

    private fun triggerLever() {
        val player = mc.player ?: return
        if (player.position().distanceToSqr(lightPos) <= 25.0) return

        val hit = mc.hitResult ?: return
        val hitBlock = (hit as? BlockHitResult)?.blockPos ?: return
        val state = mc.level?.getBlockState(hitBlock) ?: return

        if (state.block == Blocks.LEVER) {
            lastClick = System.currentTimeMillis()
            leftClick()
        }
    }

    private fun triggerRelic() {
        val player = mc.player ?: return

        val hit = mc.hitResult ?: return
        val hitBlock = (hit as? BlockHitResult)?.blockPos
        val hitEntity = (hit as? EntityHitResult)?.entity

        val isRelic = (hitEntity as? LivingEntity)
            ?.let { e -> EquipmentSlot.entries.any { e.getItemBySlot(it).hoverName.string.noControlCodes.contains("relic", true) } }
            ?: false
        val isPedestal = relicPlacePos[player.mainHandItem.itemId.takeIf { it in relicIds }]?.contains(hitBlock) ?: false

        if (isRelic || isPedestal) {
            lastClick = System.currentTimeMillis()
            rightClick()
        }
    }

    private val lightPos = Vec3(60.5, 132.0, 140.5)

    private val relicPlacePos = mapOf(
        "GREEN_KING_RELIC" to setOf(BlockPos(49, 7, 44), BlockPos(49, 6, 44)),
        "PURPLE_KING_RELIC" to setOf(BlockPos(54, 7, 41), BlockPos(54, 6, 41)),
        "BLUE_KING_RELIC" to setOf(BlockPos(59, 7, 44), BlockPos(59, 6, 44)),
        "ORANGE_KING_RELIC" to setOf(BlockPos(57, 7, 42), BlockPos(57, 6, 42)),
        "RED_KING_RELIC" to setOf(BlockPos(51, 7, 42), BlockPos(51, 6, 42)),
    )
    private val relicIds = relicPlacePos.keys
}