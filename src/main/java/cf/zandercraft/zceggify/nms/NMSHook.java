package cf.zandercraft.zceggify.nms;

import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;

public interface NMSHook {
    boolean isSpawnEgg(ItemStack item);
    LivingEntity spawnEntityFromNBTData(ItemStack item, Location location);
    void applyNBTDataToEntity(LivingEntity entity, CompoundTag tag);
    ItemStack castEntityDataToItemStackNBT(ItemStack item, LivingEntity entity);
    CompoundTag generateNBTTagCompound(CompoundTag tag, LivingEntity entity);

    default double round(double value, int precision) {
        int scale = (int) Math.pow(10, precision);
        return (double) Math.round(value * scale) / scale;
    }
}