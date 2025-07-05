package com.francobm.magicosmetics.nms.v1_18_R2.cache;

import com.francobm.magicosmetics.nms.spray.CustomSpray;
import it.unimi.dsi.fastutil.bytes.Byte2IntSortedMaps;
import net.minecraft.core.EnumDirection;
import net.minecraft.network.protocol.game.*;
import net.minecraft.network.syncher.DataWatcher;
import net.minecraft.network.syncher.DataWatcherObject;
import net.minecraft.network.syncher.DataWatcherRegistry;
import net.minecraft.server.level.WorldServer;
import net.minecraft.server.network.PlayerConnection;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.decoration.EntityItemFrame;
import net.minecraft.world.level.saveddata.maps.MapIcon;
import net.minecraft.world.level.saveddata.maps.WorldMap;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.craftbukkit.v1_18_R2.CraftWorld;
import org.bukkit.craftbukkit.v1_18_R2.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_18_R2.inventory.CraftItemStack;
import org.bukkit.craftbukkit.v1_18_R2.map.CraftMapView;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.map.MapView;

import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

public class CustomSprayHandler extends CustomSpray {
    private final EntityItemFrame itemFrame;
    private final Location location;
    private final net.minecraft.world.item.ItemStack itemStack;
    private final EnumDirection enumDirection;
    private final MapView mapView;
    private final int rotation;

    public CustomSprayHandler(Player player, Location location, BlockFace blockFace, ItemStack itemStack, MapView mapView, int rotation) {
        players = new CopyOnWriteArrayList<>(new ArrayList<>());
        this.uuid = player.getUniqueId();
        customSprays.put(uuid, this);
        WorldServer world = ((CraftWorld)player.getWorld()).getHandle();
        this.enumDirection = getEnumDirection(blockFace);
        itemFrame = new EntityItemFrame(EntityTypes.R, world);
        this.entity = (ItemFrame) itemFrame.getBukkitEntity();
        this.location = location;
        this.itemStack = CraftItemStack.asNMSCopy(itemStack);
        this.mapView = mapView;
        this.rotation = rotation;
        itemFrame.a(location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
    }

    @Override
    public void spawn(Player player) {
        if(players.contains(player.getUniqueId())) {
            if(!player.getWorld().equals(location.getWorld())) {
                remove(player);
            }
            return;
        }
        if(!player.getWorld().equals(location.getWorld())) return;
        itemFrame.j(true); //Invisible
        itemFrame.m(true); //Invulnerable
        itemFrame.setItem(itemStack, true, false);

        itemFrame.a(location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
        itemFrame.a(enumDirection);
        itemFrame.a(rotation);
        PlayerConnection connection = ((CraftPlayer)player).getHandle().b;
        connection.a(new PacketPlayOutSpawnEntity(itemFrame, enumDirection.b()));
        connection.a(new PacketPlayOutEntityMetadata(itemFrame.ae(), itemFrame.ai(), true));
        if(mapView != null) {
            player.sendMap(mapView);
        }
        players.add(player.getUniqueId());
    }

    @Override
    public void spawn(boolean exception) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if(exception && player.getUniqueId().equals(uuid)) continue;
            spawn(player);
        }
    }

    @Override
    public void remove() {
        for(UUID uuid : players){
            Player player = Bukkit.getPlayer(uuid);
            if(player == null) {
                players.remove(uuid);
                continue;
            }
            remove(player);
        }
        customSprays.remove(uuid);
    }

    @Override
    public void remove(Player player) {
        PlayerConnection connection = ((CraftPlayer)player).getHandle().b;
        connection.a(new PacketPlayOutEntityDestroy(itemFrame.ae()));
        players.remove(player.getUniqueId());
    }

    private EnumDirection getEnumDirection(BlockFace facing){
        switch (facing){
            case NORTH:
                return EnumDirection.c;
            case SOUTH:
                return EnumDirection.d;
            case WEST:
                return EnumDirection.e;
            case EAST:
                return EnumDirection.f;
            case DOWN:
                return EnumDirection.a;
            default:
                return EnumDirection.b;
        }
    }
}
