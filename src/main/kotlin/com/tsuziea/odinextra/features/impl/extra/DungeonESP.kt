package com.tsuziea.odinextra.features.impl.extra

import com.odtheking.odin.clickgui.settings.Setting.Companion.withDependency
import com.odtheking.odin.clickgui.settings.impl.BooleanSetting
import com.odtheking.odin.clickgui.settings.impl.ColorSetting
import com.odtheking.odin.clickgui.settings.impl.DropdownSetting
import com.odtheking.odin.clickgui.settings.impl.SelectorSetting
import com.odtheking.odin.events.RenderEvent
import com.odtheking.odin.events.TickEvent
import com.odtheking.odin.events.WorldEvent
import com.odtheking.odin.events.core.on
import com.odtheking.odin.features.Module
import com.odtheking.odin.utils.Colors
import com.odtheking.odin.utils.render.drawStyledBox
import com.odtheking.odin.utils.skyblock.dungeon.DungeonUtils
import com.tsuziea.odinextra.features.CustomCategory
import com.tsuziea.odinextra.features.impl.extra.esp.Door
import com.tsuziea.odinextra.features.impl.extra.esp.Mob
import com.tsuziea.odinextra.utils.GlowUtils.setGlow
import com.tsuziea.odinextra.utils.dungeon.ExtraDungeonUtils
import net.minecraft.world.phys.AABB

object DungeonESP : Module(
    name = "Dungeon ESP",
    description = "Dungeon mob, boss, and door ESP.",
    category = CustomCategory.Extra
) {
    private val mobDropdown by DropdownSetting("Mob Dropdown", false)
    private val mobEnabled by BooleanSetting("Mob", true, desc = "Highlight starred mobs, bats and withers").withDependency { mobDropdown }
    private val mobColor by ColorSetting("Mob Color", Colors.MINECRAFT_AQUA, true, "Color of the outline.").withDependency { mobDropdown && mobEnabled }

    private val doorDropdown by DropdownSetting("Door Dropdown", false)
    private val doorEnabled by BooleanSetting("Door", true, desc = "Highlight doors.").withDependency { doorDropdown }
    private val doorColor by ColorSetting("Door Color", Colors.WHITE, true, "Color of the outline.").withDependency { doorDropdown && doorEnabled }
    private val doorStyle by SelectorSetting("Door Style", "Filled Outline", listOf("Filled", "Outline", "Filled Outline"), desc = "Style of the box.").withDependency { doorDropdown && doorEnabled }

    init {
        on<WorldEvent.Load> {
            Mob.entities.clear()
            Door.doors.clear()
        }

        on<TickEvent.End> {
            if (!DungeonUtils.inDungeons) return@on
            if (mobEnabled) Mob.updateEntities()
            if (doorEnabled) Door.scanDoors()
        }

        on<RenderEvent.Extract> {
            if (doorEnabled) {
                Door.doors.forEach { door ->
                    val color = when (door.type) {
                        com.odtheking.odin.features.impl.dungeon.map.Door.Type.NORMAL -> doorColor
                        com.odtheking.odin.features.impl.dungeon.map.Door.Type.WITHER, com.odtheking.odin.features.impl.dungeon.map.Door.Type.BLOOD -> if (ExtraDungeonUtils.keyPicked) Colors.MINECRAFT_GREEN else Colors.MINECRAFT_RED
                    }

                    val aabb = AABB(
                        door.pos.x - 1.0, 69.0, door.pos.z - 1.0,
                        door.pos.x + 2.0, 73.0, door.pos.z + 2.0
                    )

                    drawStyledBox(aabb, color, doorStyle, false)
                }
            }

            if (mobEnabled) {
                Mob.entities.forEach { entity ->
                    entity.setGlow(true, mobColor)
                }
            }
        }
    }
}