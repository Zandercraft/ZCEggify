package cf.zandercraft.zceggify.nms;

import cf.zandercraft.zceggify.jnbt.CompoundTag;
import cf.zandercraft.zceggify.jnbt.NBTOutputStream;
import net.minecraft.server.v1_15_R1.NBTCompressedStreamTools;
import net.minecraft.server.v1_15_R1.NBTTagCompound;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;

public class NMS_v1_15_R1 implements NMSHook {

    @Override
    public boolean isSpawnEgg(ItemStack item) {
        return false;
    }

    @Override
    public LivingEntity spawnEntityFromNBTData(ItemStack item, Location location) {
        return null;
    }

    @Override
    public void applyNBTDataToEntity(LivingEntity entity, CompoundTag tag) {

    }

    @Override
    public ItemStack castEntityDataToItemStackNBT(ItemStack item, LivingEntity entity) {
        return null;
    }

    @Override
    public CompoundTag generateNBTTagCompound(CompoundTag tag, LivingEntity entity) {
        return null;
    }
    public static NBTTagCompound convertToNMS(CompoundTag compound) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            NBTOutputStream nbtOut = new NBTOutputStream(baos);
            nbtOut.writeNamedTag("", compound);
            baos.close();
            return NBTCompressedStreamTools.readNBT(new DataInputStream(new ByteArrayInputStream(baos.toByteArray())));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}