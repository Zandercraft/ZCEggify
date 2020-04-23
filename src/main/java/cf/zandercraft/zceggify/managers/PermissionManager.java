package cf.zandercraft.zceggify.managers;

import cf.zandercraft.zceggify.Main;
import org.bukkit.entity.*;

public class PermissionManager {
    public final String NoCost = "ZCEggify.NoCost";
    public final String CatchPrefix = "ZCEggify.Catch.";
    public final String CatchPeaceful = "ZCEggify.Catch.Peaceful";
    public final String CatchHostile = "ZCEggify.Catch.Hostile";

    public boolean hasPermissionToCapture(Player player, LivingEntity livingEntity) {
        if (livingEntity instanceof Monster && player.hasPermission(CatchHostile)) {
            return true;
        } else if (livingEntity instanceof Mob && player.hasPermission(CatchPeaceful)) {
            return true;
        } else {
            return player.hasPermission(CatchPrefix + livingEntity.getType().name());
        }
    }
}