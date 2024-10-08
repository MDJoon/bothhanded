package com.github.mdjoon.bothhanded

import io.papermc.paper.entity.LookAnchor
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Entity
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.player.PlayerInteractAtEntityEvent
import org.bukkit.plugin.java.JavaPlugin

class BothHandedPlugin : JavaPlugin(), Listener, Runnable {
    private val playerPair = mutableMapOf<Player, Int>()
    private val entityPair = mutableMapOf<Player, LivingEntity>()

    override fun onEnable() {
        server.pluginManager.registerEvents(this, this)
        server.scheduler.runTaskTimer(this, this, 0L, 1L)
    }

    @EventHandler
    fun onEntityDamagedByEntity(event: EntityDamageByEntityEvent) {
        val damager = event.damager;if(damager !is Player) return
        val entity = event.entity;if(entity !is LivingEntity) return

        val item = damager.inventory.itemInOffHand
        if(!playerPair.contains(damager) && (item.type == Material.NETHERITE_AXE || item.type == Material.NETHERITE_SWORD)) {
            playerPair[damager] = 0
            entityPair[damager] = entity
        }

    }

    override fun run() {
        playerPair.forEach { (player, time) ->
            if(time > 5) {
                val entity = entityPair[player]
                val offhandItem = player.inventory.itemInOffHand
                var damage = if(offhandItem.type == Material.NETHERITE_AXE) 9.0 else if(offhandItem.type == Material.NETHERITE_SWORD) 8.0 else 1.0

                player.swingOffHand()
                entity?.let {
                    it.noDamageTicks = 0

                    val enchantments = offhandItem.enchantments
                    if(enchantments.contains(Enchantment.FIRE_ASPECT)) {
                        it.fireTicks = enchantments.getOrDefault(Enchantment.FIRE_ASPECT, 0) * 80
                    }
                    if(enchantments.contains(Enchantment.SHARPNESS)) {
                        damage += 0.5 + enchantments.getOrDefault(Enchantment.SHARPNESS, 0) * 0.5
                    }
                    it.damage(damage, player)

                    it.noDamageTicks = 20
                }

                playerPair.remove(player)
                entityPair.remove(player)
                return@forEach
            }
            playerPair[player] = playerPair[player]!! + 1
        }

    }
}