package com.francobm.magicosmetics.nms.v1_16_R3;

import com.francobm.magicosmetics.nms.NPC.ItemSlot;
import com.francobm.magicosmetics.nms.NPC.NPC;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.datafixers.util.Pair;
import io.netty.buffer.Unpooled;
import net.minecraft.server.v1_16_R3.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_16_R3.CraftServer;
import org.bukkit.craftbukkit.v1_16_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftArmorStand;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_16_R3.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.UUID;

public class NPCHandler extends NPC {

    private EntityArmorStand balloon;
    private EntityLiving leashed;

    @Override
    public void spawnPunch(Player player, Location location) {
        EntityPlayer entityPlayer = ((CraftPlayer)player).getHandle();
        EntityLiving entityPunch = ((CraftLivingEntity)this.punch).getHandle();
        entityPunch.setPositionRotation(location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
        float yaw = location.getYaw() * 256.0F / 360.0F;
        entityPlayer.playerConnection.sendPacket(new PacketPlayOutSpawnEntityLiving(entityPunch));
        entityPlayer.playerConnection.sendPacket(new PacketPlayOutEntityHeadRotation(entityPunch, (byte) yaw));
        entityPlayer.playerConnection.sendPacket(new PacketPlayOutEntityMetadata(entityPunch.getId(), entityPunch.getDataWatcher(), true));
        entityPlayer.playerConnection.sendPacket(new PacketPlayOutCamera(entityPunch));
    }

    @Override
    public void addNPC(Player player) {
        MinecraftServer server = ((CraftServer) Bukkit.getServer()).getServer();
        WorldServer world = ((CraftWorld) player.getWorld()).getHandle();
        GameProfile gameProfile = new GameProfile(UUID.randomUUID(), player.getName());
        EntityPlayer npc = new EntityPlayer(server, world, gameProfile, new PlayerInteractManager(world));
        EntityArmorStand armorStand = new EntityArmorStand(EntityTypes.ARMOR_STAND, world);
        armorStand.setPositionRotation(player.getLocation().getX(), player.getLocation().getY(), player.getLocation().getZ(), player.getLocation().getYaw(), 0);
        armorStand.setInvisible(true); //Invisible
        armorStand.setInvulnerable(true); //Invulnerable
        npc.setPositionRotation(player.getLocation().getX(), player.getLocation().getY(), player.getLocation().getZ(), player.getLocation().getYaw(), 0);
        //npc.b(player.getLocation().getX(), player.getLocation().getY(), player.getLocation().getZ(), player.getLocation().getYaw(), player.getLocation().getPitch());
        //balloon
        balloon = new EntityArmorStand(EntityTypes.ARMOR_STAND, world);
        balloon.setInvulnerable(true); //invulnerable true
        balloon.setInvisible(true); //Invisible true
        EntityArmorStand entityPunch = new EntityArmorStand(EntityTypes.ARMOR_STAND, world);
        entityPunch.setInvulnerable(true);
        entityPunch.setInvisible(true);
        entityPunch.setPositionRotation(player.getLocation().getX(), player.getLocation().getY(), player.getLocation().getZ(), player.getLocation().getYaw(), player.getLocation().getPitch());
        leashed = new EntityPufferFish(EntityTypes.PUFFERFISH, world);
        ((EntityPufferFish)leashed).setLeashHolder(npc, true);
        leashed.setInvisible(true);
        leashed.setInvulnerable(true);
        leashed.setSilent(true); //silent true
        //balloon
        //skin
        try {
            String[] skin = getFromPlayer(player);
            npc.getProfile().getProperties().put("textures", new Property("textures", skin[0], skin[1]));
        }catch (NoSuchElementException ignored){

        }

        //skin
        // The client settings.
        DataWatcher watcher = armorStand.getDataWatcher();
        watcher.set(new DataWatcherObject<>(0, DataWatcherRegistry.a), (byte)0x20);
        PacketPlayOutEntityMetadata packet = new PacketPlayOutEntityMetadata(armorStand.getId(), watcher, true);
        ((CraftPlayer)player).getHandle().playerConnection.sendPacket(packet);

        //
        this.entity = npc.getBukkitEntity();
        this.punch = entityPunch.getBukkitEntity();
        this.armorStand = armorStand.getBukkitEntity();
        addNPC(this, player);
    }

    @Override
    public void addNPC(Player player, Location location) {
        MinecraftServer server = ((CraftServer) Bukkit.getServer()).getServer();
        WorldServer world = ((CraftWorld) player.getWorld()).getHandle();
        GameProfile gameProfile = new GameProfile(UUID.randomUUID(), player.getName());
        EntityPlayer npc = new EntityPlayer(server, world, gameProfile, new PlayerInteractManager(world));

        EntityArmorStand armorStand = new EntityArmorStand(EntityTypes.ARMOR_STAND, world);
        armorStand.setPositionRotation(player.getLocation().getX(), player.getLocation().getY(), player.getLocation().getZ(), player.getLocation().getYaw(), 0);
        npc.setPositionRotation(location.getX(), location.getY(), location.getZ(), location.getYaw(), 0);
        //balloon
        balloon = new EntityArmorStand(EntityTypes.ARMOR_STAND, world);
        balloon.setInvulnerable(true); //invulnerable true
        balloon.setInvisible(true); //Invisible true
        EntityArmorStand entityPunch = new EntityArmorStand(EntityTypes.ARMOR_STAND, world);
        entityPunch.setInvulnerable(true);
        entityPunch.setInvisible(true);
        entityPunch.setPositionRotation(player.getLocation().getX(), player.getLocation().getY(), player.getLocation().getZ(), player.getLocation().getYaw(), player.getLocation().getPitch());
        leashed = new EntityPufferFish(EntityTypes.PUFFERFISH, world);
        ((EntityPufferFish)leashed).setLeashHolder(npc, true);
        leashed.setInvulnerable(true);
        leashed.setInvisible(true);
        leashed.setSilent(true); //silent true
        //balloon
        //skin
        try {
            String[] skin = getFromPlayer(player);
            npc.getProfile().getProperties().put("textures", new Property("textures", skin[0], skin[1]));
        }catch (NoSuchElementException ignored){

        }
        //skin

        // The client settings.

        //
        this.entity = npc.getBukkitEntity();
        this.punch = entityPunch.getBukkitEntity();
        this.armorStand = armorStand.getBukkitEntity();

        addNPC(this, player);
    }

    @Override
    public void removeNPC(Player player) {
        EntityPlayer entityPlayer = ((CraftPlayer)player).getHandle();
        entityPlayer.playerConnection.sendPacket(new PacketPlayOutEntityDestroy(armorStand.getEntityId(), entity.getEntityId(), punch.getEntityId(), balloon.getId(), leashed.getId()));
    }

    @Override
    public void removeBalloon(Player player) {
        PlayerConnection connection = ((CraftPlayer)player).getHandle().playerConnection;
        connection.sendPacket(new PacketPlayOutEntityDestroy(balloon.getId(), leashed.getId()));
    }

    @Override
    public void spawnNPC(Player player) {
        EntityPlayer entityPlayer = ((CraftPlayer)this.entity).getHandle();
        EntityArmorStand armorStand = ((CraftArmorStand)this.armorStand).getHandle();
        armorStand.setInvulnerable(true); //invulnerable true
        armorStand.setMarker(true);
        //armorStand.setInvisible(true); //Invisible true
        //armorStand.setCustomNameVisible(true); //setCustomNameVisible true
        armorStand.setCustomName(new ChatMessage(player.getPlayerListName())); //setCustomName
        PlayerConnection connection = ((CraftPlayer)player).getHandle().playerConnection;
        connection.sendPacket(new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER, entityPlayer));
        connection.sendPacket(new PacketPlayOutNamedEntitySpawn(entityPlayer));
        connection.sendPacket(new PacketPlayOutEntityHeadRotation(entityPlayer, (byte) (player.getLocation().getYaw() * 256 / 360)));
        connection.sendPacket(new PacketPlayOutSpawnEntityLiving(armorStand));
        //client settings
        DataWatcher watcher = armorStand.getDataWatcher();
        watcher.set(new DataWatcherObject<>(0, DataWatcherRegistry.a), (byte)0x20);
        connection.sendPacket(new PacketPlayOutEntityMetadata(armorStand.getId(), watcher, true));
        //
        watcher = entityPlayer.getDataWatcher();
        byte bitmask = ((CraftPlayer)player).getHandle().getDataWatcher().get(new DataWatcherObject<>(16, DataWatcherRegistry.a));
        watcher.set(new DataWatcherObject<>(16, DataWatcherRegistry.a), bitmask);
        connection.sendPacket(new PacketPlayOutEntityMetadata(entityPlayer.getId(), watcher, true));
        new BukkitRunnable() {
            @Override
            public void run() {
                connection.sendPacket(new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.REMOVE_PLAYER, entityPlayer));
            }
        }.runTaskLater(Objects.requireNonNull(Bukkit.getPluginManager().getPlugin("MagicCosmetics")), 20L);
        addPassenger(player);
    }

    @Override
    public void lookNPC(Player player, float yaw) {
        EntityPlayer entityPlayer = ((CraftPlayer)this.entity).getHandle();
        EntityArmorStand armorStand = ((CraftArmorStand)this.armorStand).getHandle();
        armorStand.setInvulnerable(true); //invulnerable true
        armorStand.setInvisible(true); //Invisible true
        PlayerConnection connection = ((CraftPlayer)player).getHandle().playerConnection;
        connection.sendPacket(new PacketPlayOutEntityHeadRotation(armorStand, (byte)(yaw * 256 / 360)));
        connection.sendPacket(new PacketPlayOutEntity.PacketPlayOutEntityLook(armorStand.getId(), (byte)(yaw * 256 / 360), (byte)0, true));

        connection.sendPacket(new PacketPlayOutEntityHeadRotation(entityPlayer, (byte)(yaw * 256 / 360)));
        connection.sendPacket(new PacketPlayOutEntity.PacketPlayOutEntityLook(entityPlayer.getId(), (byte)(yaw * 256 / 360), (byte)0, true));
        //connection.sendPacket();(new PacketPlayOutEntityTeleport(entityPlayer));
    }

    @Override
    public void armorStandSetItem(Player player, ItemStack itemStack) {
        EntityArmorStand entityPlayer = ((CraftArmorStand)this.armorStand).getHandle();
        PlayerConnection connection = ((CraftPlayer)player).getHandle().playerConnection;
        ArrayList<Pair<EnumItemSlot, net.minecraft.server.v1_16_R3.ItemStack>> list = new ArrayList<>();
        list.add(new Pair<>(EnumItemSlot.HEAD, CraftItemStack.asNMSCopy(itemStack)));
        connection.sendPacket(new PacketPlayOutEntityEquipment(entityPlayer.getId(), list));
    }

    @Override
    public void balloonSetItem(Player player, ItemStack itemStack) {
        PlayerConnection connection = ((CraftPlayer)player).getHandle().playerConnection;
        ArrayList<Pair<EnumItemSlot, net.minecraft.server.v1_16_R3.ItemStack>> list = new ArrayList<>();
        if(isBigHead()){
            list.add(new Pair<>(EnumItemSlot.MAINHAND, CraftItemStack.asNMSCopy(itemStack)));
        }else {
            list.add(new Pair<>(EnumItemSlot.HEAD, CraftItemStack.asNMSCopy(itemStack)));
        }
        connection.sendPacket(new PacketPlayOutEntityEquipment(balloon.getId(), list));
    }

    @Override
    public void balloonNPC(Player player, Location location, ItemStack itemStack, boolean bigHead){
        removeBalloon(player);
        //balloon
        EntityPlayer entityPlayer = ((CraftPlayer)this.entity).getHandle();
        PlayerConnection connection = ((CraftPlayer)player).getHandle().playerConnection;
        balloonPosition = location.clone();
        balloon.setPositionRotation(location.getX(), location.getY()-1.2, location.getZ(), location.getYaw(), location.getPitch());

        leashed.setPositionRotation(location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
        this.bigHead = bigHead;
        if(isBigHead()){
            balloon.setRightArmPose(new Vector3f(balloon.rightArmPose.getX(), 0, 0));
        }
        connection.sendPacket(new PacketPlayOutSpawnEntityLiving(balloon));
        connection.sendPacket(new PacketPlayOutSpawnEntityLiving(leashed));
        DataWatcher watcher1 = balloon.getDataWatcher();
        watcher1.set(new DataWatcherObject<>(0, DataWatcherRegistry.a), (byte)0x20);
        connection.sendPacket(new PacketPlayOutEntityMetadata(balloon.getId(), watcher1, true));
        DataWatcher watcher2 = leashed.getDataWatcher();
        watcher2.set(new DataWatcherObject<>(4, DataWatcherRegistry.i), leashed.isSilent());
        connection.sendPacket(new PacketPlayOutEntityMetadata(leashed.getId(), watcher2, true));
        connection.sendPacket(new PacketPlayOutAttachEntity(leashed, entityPlayer));
        balloonSetItem(player, itemStack);
    }

    @Override
    public void equipNPC(Player player, ItemSlot itemSlot, ItemStack itemStack) {
        EntityPlayer entityPlayer = ((CraftPlayer)this.entity).getHandle();
        PlayerConnection connection = ((CraftPlayer)player).getHandle().playerConnection;
        ArrayList<Pair<EnumItemSlot, net.minecraft.server.v1_16_R3.ItemStack>> list = new ArrayList<>();
        switch (itemSlot){
            case MAIN_HAND:
                list.add(new Pair<>(EnumItemSlot.MAINHAND, CraftItemStack.asNMSCopy(itemStack)));
                connection.sendPacket(new PacketPlayOutEntityEquipment(entityPlayer.getId(), list));
                break;
            case OFF_HAND:
                list.add(new Pair<>(EnumItemSlot.OFFHAND, CraftItemStack.asNMSCopy(itemStack)));
                connection.sendPacket(new PacketPlayOutEntityEquipment(entityPlayer.getId(), list));
                break;
            case BOOTS:
                list.add(new Pair<>(EnumItemSlot.FEET, CraftItemStack.asNMSCopy(itemStack)));
                connection.sendPacket(new PacketPlayOutEntityEquipment(entityPlayer.getId(), list));
                break;
            case LEGGINGS:
                list.add(new Pair<>(EnumItemSlot.LEGS, CraftItemStack.asNMSCopy(itemStack)));
                connection.sendPacket(new PacketPlayOutEntityEquipment(entityPlayer.getId(), list));
                break;
            case CHESTPLATE:
                list.add(new Pair<>(EnumItemSlot.CHEST, CraftItemStack.asNMSCopy(itemStack)));
                connection.sendPacket(new PacketPlayOutEntityEquipment(entityPlayer.getId(), list));
                break;
            case HELMET:
                list.add(new Pair<>(EnumItemSlot.HEAD, CraftItemStack.asNMSCopy(itemStack)));
                connection.sendPacket(new PacketPlayOutEntityEquipment(entityPlayer.getId(), list));
                break;
        }
    }

    @Override
    public void addPassenger(Player player) {
        if(entity == null) return;
        EntityArmorStand armorStand = ((CraftArmorStand)this.armorStand).getHandle();
        EntityPlayer p = ((CraftPlayer)player).getHandle();
        EntityPlayer entityPlayer = ((CraftPlayer)this.entity).getHandle();
        PacketPlayOutMount packetPlayOutMount = new PacketPlayOutMount();
        this.createDataSerializer(packetDataSerializer -> {
            packetDataSerializer.d(entityPlayer.getId());
            packetDataSerializer.a(new int[]{armorStand.getId()});
            packetPlayOutMount.a(packetDataSerializer);
            return null;
        });
        p.playerConnection.sendPacket(packetPlayOutMount);
    }

    public void addPassenger(Player player, net.minecraft.server.v1_16_R3.Entity entity1, net.minecraft.server.v1_16_R3.Entity entity2) {
        if(entity1 == null) return;
        if(entity2 == null) return;
        EntityPlayer p = ((CraftPlayer)player).getHandle();
        PacketPlayOutMount packetPlayOutMount = new PacketPlayOutMount();
        this.createDataSerializer(packetDataSerializer -> {
            packetDataSerializer.d(entity1.getId());
            packetDataSerializer.a(new int[]{entity2.getId()});
            packetPlayOutMount.a(packetDataSerializer);
            return null;
        });
        p.playerConnection.sendPacket(packetPlayOutMount);
    }

    @Override
    public void animation(Player player){
        if(isBigHead()){
            animationBigHead(player);
            return;
        }
        EntityPlayer p = ((CraftPlayer)player).getHandle();
        //
        if(balloonPosition == null) return;
        if (!floatLoop) {
            y += 0.01;
            balloonPosition.add(0, 0.01, 0);
            //standToLoc.setYaw(standToLoc.getYaw() - 3F);
            if (y > 0.10) {
                floatLoop = true;
            }
        } else {
            y -= 0.01;
            balloonPosition.subtract(0, 0.01, 0);
            //standToLoc.setYaw(standToLoc.getYaw() + 3F);
            if (y < (-0.11 + 0)) {
                floatLoop = false;
                rotate *= -1;
            }
        }
        if (!rotateLoop) {
            rot += 0.01;
            balloon.setHeadPose(new Vector3f(balloon.r().getX() - 0.5f, balloon.r().getY(), balloon.r().getZ() + rotate));
            //armorStand.setHeadPose(armorStand.getHeadPose().add(0, 0, rotate).subtract(0.008, 0, 0));
            if (rot > 0.20) {
                rotateLoop = true;
            }
        } else {
            rot -= 0.01;
            balloon.setHeadPose(new Vector3f(balloon.r().getX() + 0.5f, balloon.r().getY(), balloon.r().getZ() + rotate));
            //armorStand.setHeadPose(armorStand.getHeadPose().add(0.008, 0, rotate));//.subtract(0.006, 0, 0));
            if (rot < -0.20) {
                rotateLoop = false;
            }
        }
        leashed.setLocation(balloonPosition.getX(), balloonPosition.getY(), balloonPosition.getZ(), balloonPosition.getYaw(), balloonPosition.getPitch());
        balloon.setLocation(balloonPosition.getX(), balloonPosition.getY() - 1.3, balloonPosition.getZ(), balloonPosition.getYaw(), balloonPosition.getPitch());
        p.playerConnection.sendPacket(new PacketPlayOutEntityMetadata(balloon.getId(), balloon.getDataWatcher(), true));
        p.playerConnection.sendPacket(new PacketPlayOutEntityTeleport(leashed));
        p.playerConnection.sendPacket(new PacketPlayOutEntityTeleport(balloon));
    }

    public void animationBigHead(Player player){
        EntityPlayer p = ((CraftPlayer)player).getHandle();
        //
        if(balloonPosition == null) return;
        if (!floatLoop) {
            y += 0.01;
            balloonPosition.add(0, 0.01, 0);
            //standToLoc.setYaw(standToLoc.getYaw() - 3F);
            if (y > 0.10) {
                floatLoop = true;
            }
        } else {
            y -= 0.01;
            balloonPosition.subtract(0, 0.01, 0);
            //standToLoc.setYaw(standToLoc.getYaw() + 3F);
            if (y < (-0.11 + 0)) {
                floatLoop = false;
                rotate *= -1;
            }
        }
        if (!rotateLoop) {
            rot += 0.01;
            balloon.setRightArmPose(new Vector3f(balloon.rightArmPose.getX() - 0.5f, balloon.rightArmPose.getY(), balloon.rightArmPose.getZ() + rotate));
            //armorStand.setHeadPose(armorStand.getHeadPose().add(0, 0, rotate).subtract(0.008, 0, 0));
            if (rot > 0.20) {
                rotateLoop = true;
            }
        } else {
            rot -= 0.01;
            balloon.setRightArmPose(new Vector3f(balloon.rightArmPose.getX() + 0.5f, balloon.rightArmPose.getY(), balloon.rightArmPose.getZ() + rotate));
            //armorStand.setHeadPose(armorStand.getHeadPose().add(0.008, 0, rotate));//.subtract(0.006, 0, 0));
            if (rot < -0.20) {
                rotateLoop = false;
            }
        }
        leashed.setLocation(balloonPosition.getX(), balloonPosition.getY(), balloonPosition.getZ(), balloonPosition.getYaw(), balloonPosition.getPitch());
        balloon.setLocation(balloonPosition.getX(), balloonPosition.getY() - 1.3, balloonPosition.getZ(), balloonPosition.getYaw(), balloonPosition.getPitch());
        p.playerConnection.sendPacket(new PacketPlayOutEntityMetadata(balloon.getId(), balloon.getDataWatcher(), true));
        p.playerConnection.sendPacket(new PacketPlayOutEntityTeleport(leashed));
        p.playerConnection.sendPacket(new PacketPlayOutEntityTeleport(balloon));
    }

    @Override
    public NPC getNPC(Player player) {
        return npcs.get(player.getUniqueId());
    }

    private <T> T createDataSerializer(UnsafeFunction<PacketDataSerializer, T> callback) {
        PacketDataSerializer data = new PacketDataSerializer(Unpooled.buffer());
        T result = null;
        try {
            result = callback.apply(data);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            data.release();
        }
        return result;
    }

    @FunctionalInterface
    private interface UnsafeFunction<K, T> {
        T apply(K k) throws Exception;
    }

    public String[] getFromPlayer(Player playerBukkit) throws NoSuchElementException {
        EntityPlayer playerNMS = ((CraftPlayer) playerBukkit).getHandle();
        GameProfile profile = playerNMS.getProfile();
        Property property = profile.getProperties().get("textures").iterator().next();
        String texture = property.getValue();
        String signature = property.getSignature();
        return new String[] {texture, signature};
    }

    public String[] getFromName(String name) throws IOException {
        URL url_0 = new URL("https://api.mojang.com/users/profiles/minecraft/" + name);
        InputStreamReader reader_0 = new InputStreamReader(url_0.openStream());
        String uuid = new JsonParser().parse(reader_0).getAsJsonObject().get("id").getAsString();

        URL url_1 = new URL("https://sessionserver.mojang.com/session/minecraft/profile/" + uuid + "?unsigned=false");
        InputStreamReader reader_1 = new InputStreamReader(url_1.openStream());
        JsonObject textureProperty = new JsonParser().parse(reader_1).getAsJsonObject().get("properties").getAsJsonArray().get(0).getAsJsonObject();
        String texture = textureProperty.get("value").getAsString();
        String signature = textureProperty.get("signature").getAsString();

        return new String[] {texture, signature};
    }
}
