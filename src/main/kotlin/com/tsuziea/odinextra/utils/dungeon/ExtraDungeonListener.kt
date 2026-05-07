package com.tsuziea.odinextra.utils.dungeon

import com.odtheking.odin.OdinMod.mc
import com.odtheking.odin.events.ChatPacketEvent
import com.odtheking.odin.events.TickEvent
import com.odtheking.odin.events.WorldEvent
import com.odtheking.odin.events.core.EventBus
import com.odtheking.odin.events.core.on
import com.odtheking.odin.events.core.onReceive
import com.tsuziea.odinextra.events.NewSectionEvent
import com.odtheking.odin.features.impl.dungeon.DungeonMap
import com.odtheking.odin.features.impl.dungeon.map.DungMap
import com.odtheking.odin.features.impl.dungeon.map.MapScanner
import com.odtheking.odin.features.impl.dungeon.map.SpecialColumn
import com.odtheking.odin.utils.noControlCodes
import com.odtheking.odin.utils.skyblock.dungeon.DungeonUtils
import com.tsuziea.odinextra.utils.dungeon.Section.*
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientChunkEvents
import net.minecraft.network.protocol.game.ClientboundMapItemDataPacket

object ExtraDungeonListener {
    var dungeonStates = ExtraDungeonStates()
    private var firstInSection = false
    private var isComplete = false
    private var lastCompleted = 0
    private var device = false
    private var terminals = 0
    private var gate = false
    private var levers = 0

    init {
        on<WorldEvent.Load> {
            dungeonStates = ExtraDungeonStates()
            resetSectionState()

            if (DungeonMap.enabled) return@on
            SpecialColumn.unload()
            MapScanner.unload()
            DungMap.unload()
        }

        ClientChunkEvents.CHUNK_LOAD.register { _, _ ->
            if (DungeonUtils.inClear && !DungeonMap.enabled) DungMap.onChunkLoad()
        }

        on<TickEvent.End> {
            if (DungeonUtils.inClear && !DungeonMap.enabled) MapScanner.scan(world)
        }

        onReceive<ClientboundMapItemDataPacket> {
            if (DungeonUtils.inClear && !DungeonMap.enabled) mc.execute { DungMap.rescanMapItem(this) }
        }

        on<ChatPacketEvent> {
            if (!DungeonUtils.inDungeons) return@on
            val message = value.noControlCodes

            if (witherKeyRegex.containsMatchIn(message)) {
                val matched = witherKeyRegex.find(message)
                dungeonStates.keyPicked = true
                dungeonStates.keyPicker = matched?.groupValues?.getOrNull(1)
            }
            if (witherDoorRegex.containsMatchIn(message)) {
                dungeonStates.keyPicked = false
                dungeonStates.keyPicker = null
            }
            if (bloodKeyRegex.containsMatchIn(message)) {
                val matched = bloodKeyRegex.find(message)
                dungeonStates.keyPicked = true
                dungeonStates.keyPicker = matched?.groupValues?.getOrNull(1)
            }
            if (bloodDoorRegex.containsMatchIn(message)) {
                dungeonStates.keyPicked = false
                dungeonStates.keyPicker = null
            }

            when {
                completedRegex.matches(value) -> {
                    val it = completedRegex.find(value) ?: return@on
                    val completed = (it.groupValues[4].toIntOrNull() ?: 0).apply { if (this == 1) firstInSection = true }

                    if (completed == (it.groupValues[5].toIntOrNull() ?: 0)) {
                        if (gate) newSection() else isComplete = true
                        return@on
                    }

                    when (it.groupValues[3]) {
                        "lever" -> levers++
                        "terminal" -> terminals++
                        "device" -> if (!firstInSection || lastCompleted != completed) device = true
                    }
                    lastCompleted = completed
                }

                gateRegex.matches(message) -> {
                    gate = true
                    if (isComplete) newSection()
                }

                goldorRegex.matches(message) -> {
                    dungeonStates.section = S1
                }

                coreOpeningRegex.matches(message) -> {
                    newSection()
                    resetSectionState()
                }
            }
        }
    }

    private val witherKeyRegex = Regex("(.+) has obtained Wither Key!$")
    private val witherDoorRegex = Regex("^(.+) opened a WITHER door!$")
    private val bloodKeyRegex = Regex("(.+) has obtained Blood Key!$")
    private val bloodDoorRegex = Regex("^The BLOOD DOOR has been opened!\n$")
    private val completedRegex = Regex("^(.{1,16}) (activated|completed) a (terminal|lever|device)! \\((\\d)/(\\d)\\)$")
    private val goldorRegex = Regex("^\\[BOSS] Goldor: Who dares trespass into my domain\\?$")
    private val gateRegex = Regex("^The gate has been destroyed!$")
    private val coreOpeningRegex = Regex("^The Core entrance is opening!$")

    private fun newSection() {
        val previous = dungeonStates.section
        val current = when (previous) {
            S1 -> S2
            S2 -> S3
            S3 -> S4
            S4 -> CORE
            CORE -> CORE
        }
        dungeonStates.section = current
        resetSectionState()
        EventBus.post(NewSectionEvent(previous))
    }

    private fun resetSectionState() {
        firstInSection = false
        lastCompleted = 0
        isComplete = false
        device = false
        terminals = 0
        gate = false
        levers = 0
    }

    data class ExtraDungeonStates(
        var keyPicked: Boolean = false,
        var keyPicker: String? = null,
        var section: Section = S1,
    )
}
