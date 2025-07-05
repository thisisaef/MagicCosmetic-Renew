package com.francobm.magicosmetics.nms.v1_17_R1.cache;

import com.francobm.magicosmetics.cache.RotationType;
import com.francobm.magicosmetics.nms.balloon.PlayerBalloon;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.Vector3f;
import net.minecraft.network.protocol.game.*;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.server.level.WorldServer;
import net.minecraft.server.network.PlayerConnection;
import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.EnumItemSlot;
import net.minecraft.world.entity.animal.EntityPufferFish;
import net.minecraft.world.entity.decoration.EntityArmorStand;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_17_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_17_R1.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

public class PlayerBalloonHandler extends PlayerBalloon {
    private final EntityArmorStand armorStand;
    private final EntityLiving leashed;
    private final double distance;
    private final double SQUARED_WALKING;
    private final double SQUARED_DISTANCE;

    public PlayerBalloonHandler(Player p, double space, double distance, boolean bigHead, boolean invisibleLeash) {
        viewers = new CopyOnWriteArrayList<>(new ArrayList<>());
        hideViewers = new CopyOnWriteArrayList<>(new ArrayList<>());
        this.uuid = p.getUniqueId();
        this.distance = distance;
        this.invisibleLeash = invisibleLeash;
        playerBalloons.put(uuid, this);
        Player player = getPlayer();
        WorldServer world = ((CraftWorld)player.getWorld()).getHandle();

        Location location = player.getLocation().clone().add(0, space, 0);
        location = location.clone().add(player.getLocation().getDirection().multiply(-1));

        armorStand = new EntityArmorStand(EntityTypes.c, world);
        armorStand.setInvulnerable(true); //invulnerable true
        armorStand.setInvisible(true); //Invisible true
        armorStand.setMarker(true); //Marker true
        armorStand.setPositionRotation(location.getX(), location.getY() - 1.3, location.getZ(), location.getYaw(), location.getPitch());
        this.bigHead = bigHead;
        if(isBigHead()){
            armorStand.setRightArmPose(new Vector3f(armorStand.cj.getX(), 0, 0));
        }

        leashed = new EntityPufferFish(EntityTypes.at, world);
        leashed.setInvulnerable(true); //invulnerable true
        leashed.setInvisible(true); //Invisible true
        leashed.setSilent(true); //Marker
        leashed.collides = false;
        leashed.setPositionRotation(location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
        this.space = space;
        this.SQUARED_WALKING = 5.5 * space;
        this.SQUARED_DISTANCE = 10 * space;
    }

    @Override
    public void spawn(Player player) {
        if(hideViewers.contains(player.getUniqueId())) return;
        Player owner = getPlayer();
        if(owner == null) return;
        if(viewers.contains(player.getUniqueId())) {
            if(!owner.getWorld().equals(player.getWorld())) {
                remove(player);
                return;
            }
            if(owner.getLocation().distanceSquared(player.getLocation()) > distance) {
                remove(player);
            }
            return;
        }
        if(!owner.getWorld().equals(player.getWorld())) return;
        if(owner.getLocation().distanceSquared(player.getLocation()) > distance) return;
        PlayerConnection connection = ((CraftPlayer)player).getHandle().b;
        connection.sendPacket(new PacketPlayOutSpawnEntityLiving(armorStand));
        connection.sendPacket(new PacketPlayOutEntityMetadata(armorStand.getId(), armorStand.getDataWatcher(), true));
        //connection.a(new PacketPlayOutEntityMetadata(armorStand.ae(), armorStand.ai(), true));
        //client settings
        connection.sendPacket(new PacketPlayOutSpawnEntityLiving(leashed));
        connection.sendPacket(new PacketPlayOutEntityMetadata(leashed.getId(), leashed.getDataWatcher(), true));
        if(!invisibleLeash) {
            connection.sendPacket(new PacketPlayOutAttachEntity(leashed, lendEntity == null ? ((CraftPlayer) owner).getHandle() : ((CraftLivingEntity)lendEntity).getHandle()));
        }
        //connection.a(new PacketPlayOutEntityMetadata(leashed.ae(), leashed.ai(), true));
        //client settings
        viewers.add(player.getUniqueId());
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
        for(UUID uuid : viewers){
            Player player = Bukkit.getPlayer(uuid);
            if(player == null) {
                viewers.remove(uuid);
                continue;
            }
            if(!viewers.contains(player.getUniqueId())) continue;
            remove(player);
        }
        playerBalloons.remove(uuid);
    }

    @Override
    public void remove(Player player) {
        PlayerConnection connection = ((CraftPlayer)player).getHandle().b;
        connection.sendPacket(new PacketPlayOutEntityDestroy(armorStand.getId()));
        connection.sendPacket(new PacketPlayOutEntityDestroy(leashed.getId()));
        viewers.remove(player.getUniqueId());
    }

    @Override
    public void setItem(ItemStack itemStack) {
        if(isBigHead()) {
            setItemBigHead(itemStack);
            return;
        }
        ArrayList<Pair<EnumItemSlot, net.minecraft.world.item.ItemStack>> list = new ArrayList<>();
        list.add(new Pair<>(EnumItemSlot.f, CraftItemStack.asNMSCopy(itemStack)));
        for (UUID uuid : viewers) {
            Player player = Bukkit.getPlayer(uuid);
            if(player == null) {
                viewers.remove(uuid);
                continue;
            }
            PlayerConnection connection = ((CraftPlayer)player).getHandle().b;
            connection.sendPacket(new PacketPlayOutEntityEquipment(armorStand.getId(), list));
        }
    }

    public void setItemBigHead(ItemStack itemStack) {
        ArrayList<Pair<EnumItemSlot, net.minecraft.world.item.ItemStack>> list = new ArrayList<>();
        list.add(new Pair<>(EnumItemSlot.a, CraftItemStack.asNMSCopy(itemStack)));
        for (UUID uuid : viewers) {
            Player player = Bukkit.getPlayer(uuid);
            if(player == null) {
                viewers.remove(uuid);
                continue;
            }
            PlayerConnection connection = ((CraftPlayer)player).getHandle().b;
            connection.sendPacket(new PacketPlayOutEntityEquipment(armorStand.getId(), list));
        }
    }

    @Override
    public void lookEntity(float yaw, float pitch) {
        for (UUID uuid : viewers) {
            Player player = Bukkit.getPlayer(uuid);
            if(player == null) {
                viewers.remove(uuid);
                continue;
            }
            PlayerConnection connection = ((CraftPlayer) player).getHandle().b;
            connection.sendPacket(new PacketPlayOutEntityHeadRotation(armorStand, (byte) (yaw * 256 / 360)));
            connection.sendPacket(new PacketPlayOutEntity.PacketPlayOutEntityLook(armorStand.getId(), (byte) (yaw * 256 / 360), /*(byte) (pitch * 256 / 360)*/(byte)0, true));
            connection.sendPacket(new PacketPlayOutEntityHeadRotation(leashed, (byte) (yaw * 256 / 360)));
            connection.sendPacket(new PacketPlayOutEntity.PacketPlayOutEntityLook(leashed.getId(), (byte) (yaw * 256 / 360), /*(byte) (pitch * 256 / 360)*/(byte)0, true));
        }
    }

    protected void teleport(Location location) {
        Location newLocation = location.add(0, space, 0);
        leashed.setLocation(newLocation.getX(), newLocation.getY(), newLocation.getZ(), newLocation.getYaw(), newLocation.getPitch());
        armorStand.setLocation(newLocation.getX(), newLocation.getY() - 1.3, newLocation.getZ(), newLocation.getYaw(), newLocation.getPitch());
    }

    protected void instantUpdate() { //implement this method to others versions
        Player owner = getPlayer();
        if(owner == null) return;
        if(armorStand == null) return;
        if(leashed == null) return;
        if(!owner.getWorld().equals(leashed.getBukkitEntity().getWorld())) {
            spawn(false);
            return;
        }
        Location playerLoc = owner.getLocation().clone();
        Location stand = leashed.getBukkitEntity().getLocation().clone();
        Vector standDir = owner.getEyeLocation().clone().subtract(stand).toVector();
        if (!standDir.equals(new Vector())) {
            standDir.normalize();
        }
        Location standToLoc = playerLoc.setDirection(standDir.setY(0));
        //Location standToLoc = owner.getLocation().clone().setDirection(standDir.setY(0));
        if (!floatLoop) {
            y += 0.01;
            standToLoc.add(0, 0.01, 0);
            //standToLoc.setYaw(standToLoc.getYaw() - 3F);
            if (y > 0.10) {
                floatLoop = true;
            }
        } else {
            y -= 0.01;
            standToLoc.subtract(0, 0.01, 0);
            //standToLoc.setYaw(standToLoc.getYaw() + 3F);
            if (y < (-0.11 + 0)) {
                floatLoop = false;
                rotate *= -1;
            }
        }
        teleport(standToLoc);
        if (!rotateLoop) {
            rot += 0.02;
            armorStand.setHeadPose(new Vector3f(armorStand.v().getX() - 0.5f, armorStand.v().getY(), armorStand.v().getZ() + rotate));
            //armorStand.setHeadPose(armorStand.getHeadPose().add(0, 0, rotate).subtract(0.008, 0, 0));
            if (rot > 0.20) {
                rotateLoop = true;
            }
        } else {
            rot -= 0.02;
            armorStand.setHeadPose(new Vector3f(armorStand.v().getX() + 0.5f, armorStand.v().getY(), armorStand.v().getZ() + rotate));
            //armorStand.setHeadPose(armorStand.getHeadPose().add(0.008, 0, rotate));//.subtract(0.006, 0, 0));
            if (rot < -0.20) {
                rotateLoop = false;
            }
        }
        if (heightLoop) {
            height -= 0.01;
            armorStand.setHeadPose(new Vector3f(armorStand.v().getX() + 0.8f, armorStand.v().getY(), armorStand.v().getZ()));
            //((ArmorStand)armorStand.getBukkitEntity()).setHeadPose(((ArmorStand)armorStand.getBukkitEntity()).getHeadPose().add(0.022, 0, 0));
            if (height < (-0.10 + 0)) heightLoop = false;
            return;
        }
        for(UUID uuid : viewers){
            Player player = Bukkit.getPlayer(uuid);
            if(player == null) {
                viewers.remove(uuid);
                continue;
            }
            EntityPlayer p = ((CraftPlayer)player).getHandle();
            if(!invisibleLeash) {
                p.b.sendPacket(new PacketPlayOutAttachEntity(leashed, lendEntity == null ? ((CraftPlayer) owner).getHandle() : ((CraftLivingEntity)lendEntity).getHandle()));
            }
            p.b.sendPacket(new PacketPlayOutEntityMetadata(armorStand.getId(), armorStand.getDataWatcher(), true));
            p.b.sendPacket(new PacketPlayOutEntityTeleport(leashed));
            p.b.sendPacket(new PacketPlayOutEntityTeleport(armorStand));
        }
    }

    private final double CATCH_UP_INCREMENTS = .27; //.25
    private double CATCH_UP_INCREMENTS_DISTANCE = CATCH_UP_INCREMENTS;
    @Override
    public void update(boolean instantFollow){
        if(isBigHead()) {
            updateBigHead();
            return;
        }
        if(instantFollow){
            instantUpdate();
            return;
        }
        Player owner = getPlayer();
        if(owner == null) return;
        if(armorStand == null) return;
        if(leashed == null) return;
        if(!owner.getWorld().equals(leashed.getBukkitEntity().getWorld())) {
            spawn(false);
            return;
        }
        Location playerLoc = owner.getLocation().clone().add(0, space, 0);
        Location stand = leashed.getBukkitEntity().getLocation();
        Vector standDir = owner.getEyeLocation().clone().subtract(stand).toVector();
        Location distance2 = stand.clone();
        Location distance1 = owner.getLocation().clone();

        if(distance1.distanceSquared(distance2) > SQUARED_WALKING){
            Vector lineBetween = playerLoc.clone().subtract(stand).toVector();
            if (!standDir.equals(new Vector())) {
                standDir.normalize();
            }
            Vector distVec = lineBetween.clone().normalize().multiply(CATCH_UP_INCREMENTS_DISTANCE);
            Location standTo = stand.clone().setDirection(standDir.setY(0)).add(distVec.clone());
            Location newLocation = standTo.clone();
            leashed.setLocation(newLocation.getX(), newLocation.getY(), newLocation.getZ(), newLocation.getYaw(), newLocation.getPitch());
            armorStand.setLocation(newLocation.getX(), newLocation.getY() - 1.3, newLocation.getZ(), newLocation.getYaw(), newLocation.getPitch());
        }else {
            Vector lineBetween = playerLoc.clone().subtract(stand).toVector();
            if (!standDir.equals(new Vector())) {
                standDir.normalize();
            }
            Vector distVec = lineBetween.clone().normalize().multiply(CATCH_UP_INCREMENTS_DISTANCE);
            double distY = distVec.getY();
            if(owner.isSneaking()){
                distY -= 0.13;
            }
            Location standToLoc = stand.clone().setDirection(standDir.setY(0)).add(0, distY, 0);
            if (!floatLoop) {
                y += 0.01;
                standToLoc.add(0, 0.01, 0);
                //standToLoc.setYaw(standToLoc.getYaw() - 3F);
                if (y > 0.10) {
                    floatLoop = true;
                }
            } else {
                y -= 0.01;
                standToLoc.subtract(0, 0.01, 0);
                //standToLoc.setYaw(standToLoc.getYaw() + 3F);
                if (y < (-0.11 + 0)) {
                    floatLoop = false;
                    rotate *= -1;
                }
            }

            if (!rotateLoop) {
                rot += 0.01;
                armorStand.setHeadPose(new Vector3f(armorStand.v().getX() - 0.5f, armorStand.v().getY(), armorStand.v().getZ() + rotate));
                //armorStand.setHeadPose(armorStand.getHeadPose().add(0, 0, rotate).subtract(0.008, 0, 0));
                if (rot > 0.20) {
                    rotateLoop = true;
                }
            } else {
                rot -= 0.01;
                armorStand.setHeadPose(new Vector3f(armorStand.v().getX() + 0.5f, armorStand.v().getY(), armorStand.v().getZ() + rotate));
                //armorStand.setHeadPose(armorStand.getHeadPose().add(0.008, 0, rotate));//.subtract(0.006, 0, 0));
                if (rot < -0.20) {
                    rotateLoop = false;
                }
            }
            Location newLocation = standToLoc.clone();
            leashed.setLocation(newLocation.getX(), newLocation.getY(), newLocation.getZ(), newLocation.getYaw(), newLocation.getPitch());
            armorStand.setLocation(newLocation.getX(), newLocation.getY() - 1.3, newLocation.getZ(), newLocation.getYaw(), newLocation.getPitch());
        }
        for(UUID uuid : viewers){
            Player player = Bukkit.getPlayer(uuid);
            if(player == null) {
                viewers.remove(uuid);
                continue;
            }
            EntityPlayer p = ((CraftPlayer)player).getHandle();
            if(!invisibleLeash) {
                p.b.sendPacket(new PacketPlayOutAttachEntity(leashed, lendEntity == null ? ((CraftPlayer) owner).getHandle() : ((CraftLivingEntity)lendEntity).getHandle()));
            }
            p.b.sendPacket(new PacketPlayOutEntityMetadata(armorStand.getId(), armorStand.getDataWatcher(), true));
            p.b.sendPacket(new PacketPlayOutEntityTeleport(leashed));
            p.b.sendPacket(new PacketPlayOutEntityTeleport(armorStand));
        }

        if(distance1.distanceSquared(distance2) > SQUARED_WALKING){
            if(!heightLoop){
                height += 0.01;
                armorStand.setHeadPose(new Vector3f(armorStand.v().getX() - 0.8f, armorStand.v().getY(), armorStand.v().getZ()));
                //((ArmorStand)armorStand.getBukkitEntity()).setHeadPose(((ArmorStand)armorStand.getBukkitEntity()).getHeadPose().subtract(0.022, 0, 0));
                if(height > 0.10) heightLoop = true;
            }
        }else{
            if (heightLoop) {
                height -= 0.01;
                armorStand.setHeadPose(new Vector3f(armorStand.v().getX() + 0.8f, armorStand.v().getY(), armorStand.v().getZ()));
                //((ArmorStand)armorStand.getBukkitEntity()).setHeadPose(((ArmorStand)armorStand.getBukkitEntity()).getHeadPose().add(0.022, 0, 0));
                if (height < (-0.10 + 0)) heightLoop = false;
                return;
            }

        }
        if(distance1.distanceSquared(distance2) > SQUARED_DISTANCE){
            CATCH_UP_INCREMENTS_DISTANCE += 0.01;
        }else{
            CATCH_UP_INCREMENTS_DISTANCE = CATCH_UP_INCREMENTS;
        }
    }

    public void updateBigHead(){
        Player owner = getPlayer();
        if(owner == null) return;
        if(armorStand == null) return;
        if(leashed == null) return;
        if(!owner.getWorld().equals(leashed.getBukkitEntity().getWorld())) {
            spawn(false);
            return;
        }
        Location playerLoc = owner.getLocation().clone().add(0, space, 0);
        Location stand = leashed.getBukkitEntity().getLocation();
        Vector standDir = owner.getEyeLocation().clone().subtract(stand).toVector();
        Location distance2 = stand.clone();
        Location distance1 = owner.getLocation().clone();

        if(distance1.distanceSquared(distance2) > SQUARED_WALKING){
            Vector lineBetween = playerLoc.clone().subtract(stand).toVector();
            if (!standDir.equals(new Vector())) {
                standDir.normalize();
            }
            Vector distVec = lineBetween.clone().normalize().multiply(CATCH_UP_INCREMENTS_DISTANCE);
            Location standTo = stand.clone().setDirection(standDir.setY(0)).add(distVec.clone());
            Location newLocation = standTo.clone();
            leashed.setLocation(newLocation.getX(), newLocation.getY(), newLocation.getZ(), newLocation.getYaw(), newLocation.getPitch());
            armorStand.setLocation(newLocation.getX(), newLocation.getY() - 1.3, newLocation.getZ(), newLocation.getYaw(), newLocation.getPitch());
        }else {
            if (!standDir.equals(new Vector())) {
                standDir.normalize();
            }
            Location standToLoc = stand.clone().setDirection(standDir.setY(0));
            if (!floatLoop) {
                y += 0.01;
                standToLoc.add(0, 0.01, 0);
                //standToLoc.setYaw(standToLoc.getYaw() - 3F);
                if (y > 0.10) {
                    floatLoop = true;
                }
            } else {
                y -= 0.01;
                standToLoc.subtract(0, 0.01, 0);
                //standToLoc.setYaw(standToLoc.getYaw() + 3F);
                if (y < (-0.11 + 0)) {
                    floatLoop = false;
                    rotate *= -1;
                }
            }

            if (!rotateLoop) {
                rot += 0.01;
                armorStand.setRightArmPose(new Vector3f(armorStand.cj.getX() - 0.5f, armorStand.cj.getY(), armorStand.cj.getZ() + rotate));
                //armorStand.setHeadPose(armorStand.getHeadPose().add(0, 0, rotate).subtract(0.008, 0, 0));
                if (rot > 0.20) {
                    rotateLoop = true;
                }
            } else {
                rot -= 0.01;
                armorStand.setRightArmPose(new Vector3f(armorStand.cj.getX() + 0.5f, armorStand.cj.getY(), armorStand.cj.getZ() + rotate));
                //armorStand.setHeadPose(armorStand.getHeadPose().add(0.008, 0, rotate));//.subtract(0.006, 0, 0));
                if (rot < -0.20) {
                    rotateLoop = false;
                }
            }
            Location newLocation = standToLoc.clone();
            leashed.setLocation(newLocation.getX(), newLocation.getY(), newLocation.getZ(), newLocation.getYaw(), newLocation.getPitch());
            armorStand.setLocation(newLocation.getX(), newLocation.getY() - 1.3, newLocation.getZ(), newLocation.getYaw(), newLocation.getPitch());
        }
        for(UUID uuid : viewers){
            Player player = Bukkit.getPlayer(uuid);
            if(player == null) {
                viewers.remove(uuid);
                continue;
            }
            EntityPlayer p = ((CraftPlayer)player).getHandle();
            if(!invisibleLeash) {
                p.b.sendPacket(new PacketPlayOutAttachEntity(leashed, lendEntity == null ? ((CraftPlayer) owner).getHandle() : ((CraftLivingEntity)lendEntity).getHandle()));
            }
            p.b.sendPacket(new PacketPlayOutEntityMetadata(armorStand.getId(), armorStand.getDataWatcher(), true));
            p.b.sendPacket(new PacketPlayOutEntityTeleport(leashed));
            p.b.sendPacket(new PacketPlayOutEntityTeleport(armorStand));
        }

        if(distance1.distanceSquared(distance2) > SQUARED_WALKING){
            if(!heightLoop){
                height += 0.01;
                armorStand.setRightArmPose(new Vector3f(armorStand.cj.getX() - 0.8f, armorStand.cj.getY(), armorStand.cj.getZ()));
                //((ArmorStand)armorStand.getBukkitEntity()).setHeadPose(((ArmorStand)armorStand.getBukkitEntity()).getHeadPose().subtract(0.022, 0, 0));
                if(height > 0.10) heightLoop = true;
            }
        }else{
            if (heightLoop) {
                height -= 0.01;
                armorStand.setRightArmPose(new Vector3f(armorStand.cj.getX() + 0.8f, armorStand.cj.getY(), armorStand.cj.getZ()));
                //((ArmorStand)armorStand.getBukkitEntity()).setHeadPose(((ArmorStand)armorStand.getBukkitEntity()).getHeadPose().add(0.022, 0, 0));
                if (height < (-0.10 + 0)) heightLoop = false;
                return;
            }

        }
        if(distance1.distanceSquared(distance2) > SQUARED_DISTANCE){
            CATCH_UP_INCREMENTS_DISTANCE += 0.01;
        }else{
            CATCH_UP_INCREMENTS_DISTANCE = CATCH_UP_INCREMENTS;
        }
    }


    @Override
    public void rotate(boolean rotation, RotationType rotationType, float rotate) {
        if(isBigHead()){
            rotateBigHead(rotation, rotationType, rotate);
            return;
        }
        if(!rotation) return;
        switch (rotationType){
            case RIGHT:
                armorStand.setHeadPose(new Vector3f(armorStand.v().getX(), armorStand.v().getY() + rotate, armorStand.v().getZ()));
                break;
            case UP:
                armorStand.setHeadPose(new Vector3f(armorStand.v().getX() + rotate, armorStand.v().getY(), armorStand.v().getZ()));
                break;
            case ALL:
                armorStand.setHeadPose(new Vector3f(armorStand.v().getX() + rotate, armorStand.v().getY() + rotate, armorStand.v().getZ()));
                break;
        }
        for(UUID uuid : viewers){
            Player player = Bukkit.getPlayer(uuid);
            if(player == null) {
                viewers.remove(uuid);
                continue;
            }
            ((CraftPlayer)player).getHandle().b.sendPacket(new PacketPlayOutEntityMetadata(armorStand.getId(), armorStand.getDataWatcher(), true));
        }
    }

    public void rotateBigHead(boolean rotation, RotationType rotationType, float rotate) {
        if(!rotation) return;
        switch (rotationType){
            case RIGHT:
                armorStand.setRightArmPose(new Vector3f(armorStand.cj.getX(), armorStand.cj.getY() + rotate, armorStand.cj.getZ()));
                break;
            case UP:
                armorStand.setRightArmPose(new Vector3f(armorStand.cj.getX() + rotate, armorStand.cj.getY(), armorStand.cj.getZ()));
                break;
            case ALL:
                armorStand.setRightArmPose(new Vector3f(armorStand.cj.getX() + rotate, armorStand.cj.getY() + rotate, armorStand.cj.getZ()));
                break;
        }
        for(UUID uuid : viewers){
            Player player = Bukkit.getPlayer(uuid);
            if(player == null) {
                viewers.remove(uuid);
                continue;
            }
            ((CraftPlayer)player).getHandle().b.sendPacket(new PacketPlayOutEntityMetadata(armorStand.getId(), armorStand.getDataWatcher(), true));
        }
    }
}
