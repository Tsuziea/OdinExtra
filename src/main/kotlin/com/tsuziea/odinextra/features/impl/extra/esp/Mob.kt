package com.tsuziea.odinextra.features.impl.extra.esp

import com.odtheking.odin.OdinMod.mc
import com.odtheking.odin.utils.skyblock.dungeon.DungeonUtils
import com.odtheking.odin.utils.skyblock.dungeon.M7Phases
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.ambient.Bat
import net.minecraft.world.entity.boss.wither.WitherBoss
import net.minecraft.world.entity.decoration.ArmorStand
import net.minecraft.world.entity.player.Player

object Mob {
    val entities = mutableSetOf<Entity>()

    fun updateEntities() {
        val level = mc.level ?: return

        level.entitiesForRendering().forEach { e ->
            if (!e.isAlive) return@forEach

            if (e is Bat) entities.add(e)
            if (e is WitherBoss && isWitherPhase()) entities.add(e)

            val stand = e as? ArmorStand ?: return@forEach
            val name = stand.name.string
            if (!dungeonMobSpawns.any { it in name }) return@forEach
            if (!starredRegex.matches(name)) return@forEach

            level.getEntities(
                stand,
                stand.boundingBox.move(0.0, -1.0, 0.0)
            ) { isValidEntity(it) }
                .firstOrNull()
                ?.let { entities.add(it) }
        }

        level.players().forEach { player ->
            if (player != mc.player && player.isAlive &&
                player.name.string.contains("Shadow Assassin")
            ) {
                entities.add(player)
            }
        }

        entities.removeIf { !it.isAlive || (it is Bat && hasNearbyBat(it)) }
    }

    private val starredRegex = Regex("^.*✯ .*\\d{1,3}(?:,\\d{3})*(?:\\.\\d+)?[kM]?❤$")

    private fun isValidEntity(entity: Entity): Boolean =
        when (entity) {
            is ArmorStand -> false
            is Player -> entity.uuid.version() == 2 && entity != mc.player
            else -> true
        }

    private fun hasNearbyBat(bat: Bat): Boolean {
        val level = mc.level ?: return false
        return level.getEntities(bat, bat.boundingBox.inflate(2.0)) {
            it is Bat && it.isAlive && it != bat
        }.isNotEmpty()
    }


    private fun isWitherPhase(): Boolean =
        when (DungeonUtils.getF7Phase()) {
            M7Phases.P1, M7Phases.P2, M7Phases.P3, M7Phases.P4 -> true
            else -> false
        }

    private val dungeonMobSpawns = hashSetOf(
        "Lurker", "Dreadlord", "Souleater", "Zombie", "Skeleton", "Skeletor",
        "Sniper", "Super Archer", "Spider", "Fels", "Withermancer",
        "Lost Adventurer", "Angry Archaeologist", "Frozen Adventurer"
    )
}
