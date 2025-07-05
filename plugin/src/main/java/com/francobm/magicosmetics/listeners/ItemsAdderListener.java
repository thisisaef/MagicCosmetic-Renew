package com.francobm.magicosmetics.listeners;

import com.francobm.magicosmetics.MagicCosmetics;
import com.francobm.magicosmetics.api.Cosmetic;
import com.francobm.magicosmetics.cache.*;
import com.francobm.magicosmetics.cache.inventories.Menu;
import com.francobm.magicosmetics.cache.items.Items;
import dev.lone.itemsadder.api.Events.CustomBlockInteractEvent;
import dev.lone.itemsadder.api.Events.CustomBlockPlaceEvent;
import dev.lone.itemsadder.api.Events.ItemsAdderLoadDataEvent;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.EquipmentSlot;

public class ItemsAdderListener implements Listener {
    private final MagicCosmetics plugin = MagicCosmetics.getInstance();

    @EventHandler
    public void onIALoadEvent(ItemsAdderLoadDataEvent event){
        if(event.getCause() != ItemsAdderLoadDataEvent.Cause.FIRST_LOAD) return;
        plugin.getServer().getScheduler().runTask(plugin, () -> {
            plugin.ava = plugin.getItemsAdder().replaceFontImages(plugin.ava);
            plugin.unAva = plugin.getItemsAdder().replaceFontImages(plugin.unAva);
            plugin.equip = plugin.getItemsAdder().replaceFontImages(plugin.equip);
            for(String lines : plugin.getMessages().getStringList("bossbar")){
                lines = plugin.getItemsAdder().replaceFontImages(lines);
                BossBar boss = plugin.getServer().createBossBar(lines, plugin.bossBarColor, BarStyle.SOLID);
                boss.setVisible(true);
                plugin.getBossBar().add(boss);
            }
            Cosmetic.loadCosmetics();
            Color.loadColors();
            Items.loadItems();
            Zone.loadZones();
            Token.loadTokens();
            Sound.loadSounds();
            Menu.loadMenus();
        });
    }

    @EventHandler
    public void onPlaceBlocks(CustomBlockPlaceEvent event) {
        Player player = event.getPlayer();
        PlayerData playerData = PlayerData.getPlayer(player);
        if(playerData.getWStick() == null) return;
        if(!playerData.getWStick().isCosmetic(event.getItemInHand())) return;
        event.setCancelled(true);
    }

    @EventHandler
    public void onPlaceBlocks(CustomBlockInteractEvent event) {

    }
}
