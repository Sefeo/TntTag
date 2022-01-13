package TntTag.listener;

import TntTag.manager.Manager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.PlayerDropItemEvent;

public class SubListeners implements Listener {

    private Manager manager;

    public SubListeners(Manager manager) {
        this.manager = manager;
    }

    @EventHandler
    private void onItemDrop(PlayerDropItemEvent e) {
        e.setCancelled(true);
    }

    @EventHandler
    private void onDamageTaken(EntityDamageEvent e) {
        if (e.getCause().equals(EntityDamageEvent.DamageCause.ENTITY_ATTACK)) e.setDamage(0F);
        else e.setCancelled(true);
    }

    @EventHandler
    private void onMobSpawn(CreatureSpawnEvent e) {
        e.setCancelled(true);
    }

    @EventHandler
    private void onBlockPlace(BlockPlaceEvent e) {
        e.setCancelled(true);
    }

    @EventHandler
    private void onBlockBreak(BlockBreakEvent e) {
        e.setCancelled(true);
    }
}
