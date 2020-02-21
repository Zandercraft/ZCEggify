package cf.zandercraft.zceggify.items;

import cf.zandercraft.zceggify.NBTManager;
import org.bukkit.*;
import org.bukkit.Material;
import org.bukkit.entity.*;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Vector;

public class CaptureEgg {
    public static final String TITLE_PREFIX = ChatColor.GOLD + "Captured ";

    public static void captureLivingEntity(LivingEntity livingEntity) {
        ItemStack eggItemStack = CaptureEgg.get(livingEntity);
        Item drop = livingEntity.getLocation().getWorld().dropItem(livingEntity.getLocation(), eggItemStack);
        drop.setItemStack(eggItemStack);
        drop.setVelocity(new Vector(0, 0.3f, 0));
    }

    public static LivingEntity useSpawnItem(ItemStack spawnItem, Location target) {
        return NBTManager.spawnEntityFromNBTData(spawnItem, target);
    }

    private static ItemStack get(LivingEntity livingEntity) {
        return convertEntityToEntitySpawnEgg(livingEntity);
    }

    private static ItemStack convertEntityToEntitySpawnEgg(LivingEntity livingEntity) {
        Material material = Material.getMaterial(livingEntity.getType().name().toUpperCase() + "_SPAWN_EGG");
        if (material == null) {
            switch (livingEntity.getType()) {
                case PIG_ZOMBIE:
                    material = Material.ZOMBIE_PIGMAN_SPAWN_EGG;
                    break;
                case MUSHROOM_COW:
                    material = Material.MOOSHROOM_SPAWN_EGG;
                    break;
                default:
                    material = Material.GHAST_SPAWN_EGG;
                    break;
            }
        }
        ItemStack spawnEgg = new ItemStack(material, 1);
        return NBTManager.castEntityDataToItemStackNBT(spawnEgg, livingEntity);
    }
}