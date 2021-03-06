package cf.zandercraft.zceggify.nms;

import cf.zandercraft.zceggify.Main;
import cf.zandercraft.zceggify.NBTManager;
import cf.zandercraft.zceggify.items.CaptureEgg;
import cf.zandercraft.zceggify.jnbt.CompoundTag;
import cf.zandercraft.zceggify.jnbt.NBTInputStream;
import cf.zandercraft.zceggify.jnbt.NBTOutputStream;
import cf.zandercraft.zceggify.jnbt.Tag;
import net.minecraft.server.v1_15_R1.*;
import org.bukkit.*;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.craftbukkit.v1_15_R1.inventory.CraftItemStack;
import org.bukkit.entity.*;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MerchantRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class NMS_v1_15_R1 implements NMSHook {

    @Override
    public boolean isSpawnEgg(ItemStack itemStack) {
        if (itemStack.getType().name().contains("SPAWN_EGG")) {
            net.minecraft.server.v1_15_R1.ItemStack nmsStack = CraftItemStack.asNMSCopy(itemStack);
            if (nmsStack.hasTag()) {
                NBTTagCompound compound = nmsStack.getTag();
                NBTTagCompound entityDetails = compound.getCompound("tag");
                if (entityDetails != null) {
                    String entityType = entityDetails.getString("entity type");
                    return entityType != null;
                }
            }
        }

        return false;
    }

    @Override
    public LivingEntity spawnEntityFromNBTData(ItemStack spawnItem, Location target) {
        if (spawnItem != null) {
            net.minecraft.server.v1_15_R1.ItemStack nmsStack = CraftItemStack.asNMSCopy(spawnItem);
            if (nmsStack.hasTag()) {
                NBTTagCompound compound = nmsStack.getTag();
                NBTTagCompound entityDetails = compound.getCompound("tag");

                String entityType = entityDetails.getString("entity type");

                LivingEntity livingEntity = (LivingEntity) target.getWorld().spawnEntity(target.clone().add(0, 1f, 0), EntityType.valueOf(entityType));
                _applyNBTDataToEntity(livingEntity, entityDetails);

                return livingEntity;
            } else
                Main.logger.warning("Spawn Item does not have any NBT Tags.");
        } else
            Main.logger.warning("NULL spawn item passed to #spawnEntityFromNBTData().");

        return null;
    }

    @Override
    public void applyNBTDataToEntity(LivingEntity livingEntity, CompoundTag details) {
        _applyNBTDataToEntity(livingEntity, convertToNMS(details));
    }

    public void _applyNBTDataToEntity(LivingEntity livingEntity, NBTTagCompound entityDetails) {
        if (entityDetails.hasKey("custom name"))
            livingEntity.setCustomName(entityDetails.getString("custom name"));
        livingEntity.setAI(entityDetails.getBoolean("ai"));
        livingEntity.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(entityDetails.getDouble("max health"));
        livingEntity.setHealth(entityDetails.getDouble("health"));
        livingEntity.setGlowing(entityDetails.getBoolean("glowing"));

        NBTTagList potionEffectList = entityDetails.getList("potion effects", NBTManager.ListType.COMPOUND.ordinal());
        for (int i = 0; i < potionEffectList.size(); i++) {
            NBTTagCompound potionEffectCompound = potionEffectList.getCompound(i);

            int duration = potionEffectCompound.getInt("duration");
            int amplifier = potionEffectCompound.getInt("amplifier");
//            int colorRed = -1;
//            int colorGreen = -1;
//            int colorBlue = -1;
//            if (potionEffectCompound.hasKey("color red")) {
//                colorRed = potionEffectCompound.getInt("color red");
//                colorGreen = potionEffectCompound.getInt("color green");
//                colorBlue = potionEffectCompound.getInt("color blue");
//            }
            String type = potionEffectCompound.getString("type");
            boolean hasParticles = potionEffectCompound.getBoolean("particles");
            boolean isAmbient = potionEffectCompound.getBoolean("ambient");

            PotionEffectType potionEffectType = PotionEffectType.getByName(type);
//            Color color = null;
//            if (colorRed != -1 && colorBlue != -1 && colorGreen != -1)
//                color = Color.fromRGB(colorRed, colorGreen, colorBlue);

            PotionEffect potionEffect;
//            if (color != null)
//                potionEffect = new PotionEffect(potionEffectType, duration, amplifier, isAmbient, hasParticles, true);
//            else
            potionEffect = new PotionEffect(potionEffectType, duration, amplifier, isAmbient, hasParticles);
            livingEntity.addPotionEffect(potionEffect);
        }

        if (livingEntity instanceof Ageable) {
            Ageable ageable = (Ageable) livingEntity;
            ageable.setAge(entityDetails.getInt("age"));
        }

        if (livingEntity instanceof Tameable) {
            Tameable tameable = (Tameable) livingEntity;
            tameable.setTamed(entityDetails.getBoolean("tamed"));
            if (tameable.isTamed()) {
                try {
                    UUID ownerUUID = UUID.fromString(entityDetails.getString("owner"));
                    OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(ownerUUID);
                    tameable.setOwner(offlinePlayer);
                } catch (IllegalArgumentException ignored) {

                }
            }
        }

        if (livingEntity instanceof Sittable) {
            Sittable sittable = (Sittable) livingEntity;
            sittable.setSitting(entityDetails.getBoolean("sitting"));
        }

        switch (livingEntity.getType()) {
            case WOLF:
                Wolf wolf = (Wolf) livingEntity;
                wolf.setAngry(entityDetails.getBoolean("angry"));
                String color = entityDetails.getString("color");
                wolf.setCollarColor(DyeColor.valueOf(color));
                break;
            case PIG:
                Pig pig = (Pig) livingEntity;
                pig.setSaddle(entityDetails.getBoolean("saddled"));
                break;
            case SHEEP:
                Sheep sheep = (Sheep) livingEntity;
                sheep.setSheared(entityDetails.getBoolean("sheared"));
                sheep.setColor(DyeColor.valueOf(entityDetails.getString("color")));
                break;
            case CAT:
                Cat cat = (Cat) livingEntity;
                cat.setCatType(Cat.Type.valueOf(entityDetails.getString("cat type")));
                break;
            case RABBIT:
                Rabbit rabbit = (Rabbit) livingEntity;
                rabbit.setRabbitType(Rabbit.Type.valueOf(entityDetails.getString("rabbit type")));
                break;
            case DONKEY:
            case MULE:
            case SKELETON_HORSE:
            case ZOMBIE_HORSE:
            case HORSE:
                AbstractHorse abstractHorse = (AbstractHorse) livingEntity;
                abstractHorse.setJumpStrength(entityDetails.getDouble("jump strength"));
                abstractHorse.setTamed(entityDetails.getBoolean("tamed"));
                abstractHorse.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(entityDetails.getDouble("speed"));
                //TODO: INVENTORY CONTENTS
                break;
            case FOX:
                Fox fox = (Fox) livingEntity;
                fox.setFoxType(Fox.Type.valueOf(entityDetails.getString("fox type")));
                break;
            case VILLAGER:
                //1) Get basic villager data.
                Villager villager = (Villager) livingEntity;
                villager.setProfession(Villager.Profession.valueOf(entityDetails.getString("profession")));

                //2) Grab the recipe list.
                NBTTagList recipeList = entityDetails.getList("recipes", NBTManager.ListType.COMPOUND.ordinal());

                //3) Prepare an ArrayList for recipe list that will be stored in Villager.
                List<MerchantRecipe> merchantRecipeList = new ArrayList<>();
                for (int i = 0; i < recipeList.size(); i++) {
                    //4) Parse the recipe list.
                    NBTTagCompound recipeCompound = recipeList.getCompound(i);
                    int uses = recipeCompound.getInt("uses");
                    int maxUses = recipeCompound.getInt("max uses");
                    boolean experienceReward = recipeCompound.getBoolean("experience reward");
                    String[] resultString = recipeCompound.getString("result").split("\\.");
                    NBTTagCompound resultTags = recipeCompound.getCompound("result tags");
                    NBTTagList materialsAndAmount = recipeCompound.getList("materials", NBTManager.ListType.STRING.ordinal());
                    NBTTagList tagList = recipeCompound.getList("tags", NBTManager.ListType.COMPOUND.ordinal());

                    //5) Set the resulted item stack to its proper NBT tags.
                    ItemStack resultItemStack = new ItemStack(Material.valueOf(resultString[0]), Integer.parseInt(resultString[1]));
                    net.minecraft.server.v1_15_R1.ItemStack nmsStack = CraftItemStack.asNMSCopy(resultItemStack);
                    nmsStack.setTag(resultTags);
                    resultItemStack = CraftItemStack.asBukkitCopy(nmsStack);

                    //6) Register the recipes.
                    org.bukkit.inventory.MerchantRecipe merchantRecipe = new org.bukkit.inventory.MerchantRecipe(resultItemStack, maxUses);
                    merchantRecipe.setUses(uses);
                    merchantRecipe.setExperienceReward(experienceReward);
                    List<ItemStack> ingredients = new ArrayList<>();
                    for (int j = 0; j < materialsAndAmount.size(); j++) {
                        String[] ingredient = materialsAndAmount.getString(j).split("\\.");
                        NBTTagCompound tags = tagList.getCompound(j);
                        ItemStack itemStack = new ItemStack(Material.valueOf(ingredient[0]), Integer.parseInt(ingredient[1]));
                        net.minecraft.server.v1_15_R1.ItemStack nmsIngredientStack = CraftItemStack.asNMSCopy(itemStack);
                        nmsIngredientStack.setTag(tags);
                        itemStack = CraftItemStack.asBukkitCopy(nmsIngredientStack);
                        ingredients.add(itemStack);
                    }
                    merchantRecipe.setIngredients(ingredients);
                    merchantRecipeList.add(merchantRecipe);
                }

                //4) Input the recipe list into the villager
                villager.setRecipes(merchantRecipeList);
                break;
            case CREEPER:
                ((Creeper) livingEntity).setPowered(entityDetails.getBoolean("charged"));
                break;
            case SLIME:
                ((Slime) livingEntity).setSize(entityDetails.getInt("size"));
                break;
            case ZOMBIE_VILLAGER:
                ZombieVillager zombieVillager = (ZombieVillager) livingEntity;
                zombieVillager.setVillagerProfession(Villager.Profession.valueOf(entityDetails.getString("profession")));
                break;
            case PARROT:
                ((Parrot) livingEntity).setVariant(Parrot.Variant.valueOf(entityDetails.getString("variant")));
                break;
            case LLAMA:
                Integer strength = entityDetails.getInt("strength");
                Llama.Color lcolor = Llama.Color.valueOf(entityDetails.getString("color"));


                Llama llama = (Llama) livingEntity;
                llama.setStrength(strength);
                llama.setColor(lcolor);
                break;
        }

        if (livingEntity instanceof InventoryHolder) {
            InventoryHolder inventoryHolder = (InventoryHolder) livingEntity;

            //1) Declare storage for item stack material, slot, and NBT tags
            NBTTagList tagList = entityDetails.getList("inventory nbt tags", NBTManager.ListType.COMPOUND.ordinal());
            NBTTagList materialList = entityDetails.getList("inventory materials", NBTManager.ListType.STRING.ordinal());

            for (int i = 0; i < materialList.size(); i++) {
                //2) Load NBT tag data and material type
                String[] materialElements = materialList.getString(i).split("\\.");
                String materialName = materialElements[0];
                int slot = Integer.parseInt(materialElements[1]);
                NBTTagCompound tag = tagList.getCompound(i);

                //3) Create item stack and attach NBT tag to it.
                ItemStack itemStack = new ItemStack(Material.valueOf(materialName));
                if (!tag.equals(new NBTTagCompound())) {
                    net.minecraft.server.v1_15_R1.ItemStack nmsStack = CraftItemStack.asNMSCopy(itemStack);
                    nmsStack.setTag(tag);
                    itemStack = CraftItemStack.asBukkitCopy(nmsStack);
                }
                //4) Place in inventory at correct slot number.
                inventoryHolder.getInventory().setItem(slot, itemStack);
            }
        }

        // horse types
        if (livingEntity instanceof Llama) {
            Llama abstractHorse = (Llama) livingEntity;
            Llama.Color color = Llama.Color.valueOf(entityDetails.getString("color"));
            int strength = entityDetails.getInt("strength");
            abstractHorse.setColor(color);
            abstractHorse.setStrength(strength);
        } else if (livingEntity instanceof Horse) {
            Horse horse = (Horse) livingEntity;
            Horse.Color color = Horse.Color.valueOf(entityDetails.getString("color"));
            Horse.Style style = Horse.Style.valueOf(entityDetails.getString("style"));
            horse.setColor(color);
            horse.setStyle(style);
        }

    }

    public ItemStack castEntityDataToItemStackNBT(ItemStack itemStack, LivingEntity livingEntity) {
        //2) Figure entity name
        String entityName;
        if (livingEntity.getCustomName() == null) {
            entityName = livingEntity.getType().name().replace("_", " ").toLowerCase();
            entityName = entityName.substring(0, 1).toUpperCase() + entityName.substring(1);
        } else
            entityName = livingEntity.getCustomName();

        final ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.setDisplayName(CaptureEgg.TITLE_PREFIX + entityName);
        itemMeta.setLore(NBTManager.createItemLore(livingEntity));
        itemStack.setItemMeta(itemMeta);

        final net.minecraft.server.v1_15_R1.ItemStack nmsStack = CraftItemStack.asNMSCopy(itemStack);
        final NBTTagCompound tagCompound = (nmsStack.hasTag() && nmsStack.getTag() != null) ? nmsStack.getTag() : new NBTTagCompound();

        //2) Gather capture data

        final NBTTagCompound entityDetails = _generateNBTTagCompound(tagCompound, livingEntity);
        tagCompound.set("tag", entityDetails);

        //final NBTTagCompound display = tagCompound.hasKey("display") ? tagCompound.getCompound("display") : new NBTTagCompound();

        //3) What is the name of the item stack
        //TODO: Re-enable this when NBT Tags are finished in Spigot. SPIGOT BUG
        //display.set("Name", NBTTagString.a(CaptureEgg.TITLE_PREFIX + entityName));

        //display.set("Lore", createNMSItemLore(livingEntity));
        //tagCompound.set("display", display);

        //) Package and convert
        nmsStack.setTag(tagCompound);
        itemStack = CraftItemStack.asBukkitCopy(nmsStack);

        return itemStack;
    }

    public CompoundTag generateNBTTagCompound(CompoundTag compound, LivingEntity livingEntity) {
        return convertToAPI(_generateNBTTagCompound(convertToNMS(compound), livingEntity));
    }

    private NBTTagCompound _generateNBTTagCompound(NBTTagCompound compound, LivingEntity livingEntity) {
        NBTTagCompound entityDetails = compound.getCompound("tag");
        // General entity data
        if (livingEntity.getCustomName() != null)
            entityDetails.set("custom name", NBTTagString.a(livingEntity.getCustomName()));
        entityDetails.set("entity type", NBTTagString.a(livingEntity.getType().name()));
        entityDetails.setBoolean("ai", livingEntity.hasAI());
        entityDetails.set("health", NBTTagDouble.a(livingEntity.getHealth()));
        entityDetails.set("max health", NBTTagDouble.a(livingEntity.getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue()));
        entityDetails.setBoolean("glowing", livingEntity.isGlowing());

        NBTTagList potionEffectList = new NBTTagList();
        for (PotionEffect potionEffect : livingEntity.getActivePotionEffects()) {
            NBTTagCompound potionEffectCompound = new NBTTagCompound();
            potionEffectCompound.setInt("duration", potionEffect.getDuration());
            potionEffectCompound.setInt("amplifier", potionEffect.getAmplifier());
            potionEffect.getType().getColor();
            potionEffectCompound.setInt("color red", potionEffect.getType().getColor().getRed());
            potionEffectCompound.setInt("color green", potionEffect.getType().getColor().getGreen());
            potionEffectCompound.setInt("color blue", potionEffect.getType().getColor().getBlue());
            potionEffectCompound.setString("type", potionEffect.getType().getName());
            potionEffectCompound.setBoolean("ambient", potionEffect.isAmbient());
            potionEffectCompound.setBoolean("particles", potionEffect.hasParticles());

            potionEffectList.add(potionEffectCompound);
        }
        entityDetails.set("potion effects", potionEffectList);

        if (livingEntity instanceof Ageable) {
            Ageable ageable = (Ageable) livingEntity;
            entityDetails.setInt("age", ageable.getAge());
        }
        if (livingEntity instanceof InventoryHolder) {
            InventoryHolder inventoryHolder = (InventoryHolder) livingEntity;

            NBTTagList tagList = new NBTTagList();
            NBTTagList materialList = new NBTTagList();
            for (int i = 0; i < inventoryHolder.getInventory().getContents().length; i++) {
                ItemStack itemStack = inventoryHolder.getInventory().getContents()[i];
                if (itemStack != null) {
                    net.minecraft.server.v1_15_R1.ItemStack nmsStack = CraftItemStack.asNMSCopy(itemStack);
                    materialList.add(NBTTagString.a(itemStack.getType().name() + "." + i));
                    if (nmsStack.hasTag())
                        tagList.add(nmsStack.getTag());
                    else
                        tagList.add(new NBTTagCompound());
                }
            }

            entityDetails.set("inventory materials", materialList);
            entityDetails.set("inventory nbt tags", tagList);
        }

        if (livingEntity instanceof Tameable) {
            Tameable tameable = (Tameable) livingEntity;
            entityDetails.setBoolean("tamed", tameable.isTamed());
            if (tameable.isTamed()) {
                entityDetails.setString("owner", tameable.getOwner().getUniqueId().toString());
            }
        }

        if (livingEntity instanceof Sittable) {
            Sittable sittable = (Sittable) livingEntity;
            entityDetails.setBoolean("sitting", sittable.isSitting());
        }

        switch(livingEntity.getType()) {
            case WOLF:
                // Basic wolf data
                Wolf wolf = (Wolf) livingEntity;
                boolean isAngry = wolf.isAngry();
                boolean isSitting = wolf.isSitting();
                boolean isTamed = wolf.isTamed();

                entityDetails.setBoolean("is angry", isAngry);
                entityDetails.setBoolean("is sitting", isSitting);
                if (isTamed) {
                    // Tamed wolf data
                    String color = wolf.getCollarColor().name();
                    entityDetails.set("color", NBTTagString.a(color));
                }
                break;
            case FOX:
                Fox fox = (Fox) livingEntity;
                entityDetails.setString("fox type", fox.getFoxType().name());
            case SHEEP:
                Sheep sheep = (Sheep) livingEntity;
                boolean sheared = sheep.isSheared();
                String color = sheep.getColor().name();

                entityDetails.set("color", NBTTagString.a(color));
                entityDetails.setBoolean("sheared", sheared);
                break;
            case PIG:
                Pig pig = (Pig) livingEntity;

                boolean saddled = pig.hasSaddle();
                entityDetails.setBoolean("saddled", saddled);
                break;
            case CAT:
                Cat cat = (Cat) livingEntity;
                String catType = cat.getCatType().name();
                entityDetails.setString("cat type", catType);
                break;
            case RABBIT:
                entityDetails.setString("rabbit type", ((Rabbit) livingEntity).getRabbitType().name());
                break;
            case DONKEY:
            case MULE:
            case SKELETON_HORSE:
            case ZOMBIE_HORSE:
            case HORSE:
                AbstractHorse abstractHorse = (AbstractHorse) livingEntity;
                double jumpStrength = abstractHorse.getJumpStrength();
                double speed = livingEntity.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).getBaseValue();

                entityDetails.setDouble("jump strength", jumpStrength);
                entityDetails.setDouble("speed", speed);
                break;
            case VILLAGER:
                Villager villager = (Villager) livingEntity;
                String profession = villager.getProfession().name();

                NBTTagList recipeList = new NBTTagList();
                for (org.bukkit.inventory.MerchantRecipe recipe : villager.getRecipes()) {
                    List<ItemStack> ingredients = recipe.getIngredients();
                    // Store the materials and amounts
                    NBTTagList materialsAndAmount = new NBTTagList(); // Holds materials and amount separated by "."
                    // Store the tags
                    NBTTagList itemStackTags = new NBTTagList();
                    for (ItemStack itemStack : ingredients) {
                        materialsAndAmount.add(NBTTagString.a(itemStack.getType().name() + "." + itemStack.getAmount()));

                        net.minecraft.server.v1_15_R1.ItemStack nmsStack = CraftItemStack.asNMSCopy(itemStack);
                        NBTTagCompound itemStackCompound = (nmsStack.hasTag()) ? nmsStack.getTag() : new NBTTagCompound();
                        itemStackTags.add(itemStackCompound);
                    }
                    int uses = recipe.getUses();
                    int maxUses = recipe.getMaxUses();
                    boolean experienceReward = recipe.hasExperienceReward();

                    net.minecraft.server.v1_15_R1.ItemStack nmsStack = CraftItemStack.asNMSCopy(recipe.getResult());
                    NBTTagCompound resultTags = (nmsStack.hasTag()) ? nmsStack.getTag() : new NBTTagCompound();

                    NBTTagCompound recipeCompound = new NBTTagCompound();
                    recipeCompound.setInt("uses", uses);
                    recipeCompound.setInt("max uses", maxUses);
                    recipeCompound.setBoolean("experience reward", experienceReward);
                    recipeCompound.setString("result", recipe.getResult().getType().name() + "." + recipe.getResult().getAmount());
                    recipeCompound.set("result tags", resultTags);
                    recipeCompound.set("materials", materialsAndAmount);
                    recipeCompound.set("tags", itemStackTags);

                    recipeList.add(recipeCompound);
                }

                entityDetails.setString("profession", profession);
                entityDetails.set("recipes", recipeList);
                break;
            case CREEPER:
                Creeper creeper = (Creeper) livingEntity;
                entityDetails.setBoolean("charged", creeper.isPowered());
                break;
            case SLIME:
                Slime slime = (Slime) livingEntity;
                entityDetails.setInt("size", slime.getSize());
            case ZOMBIE_VILLAGER:
                ZombieVillager zombieVillager = (ZombieVillager) livingEntity;
                String zprofession = zombieVillager.getVillagerProfession().name();
                entityDetails.setString("profession", zprofession);
                break;
            case PARROT:
                Parrot parrot = (Parrot) livingEntity;
                Parrot.Variant pcolor = parrot.getVariant();
                entityDetails.setString("variant", pcolor.name());
        }

        if (livingEntity instanceof Tameable) {
            Tameable tameable = (Tameable) livingEntity;
            boolean tamed = tameable.isTamed();
            entityDetails.setBoolean("tamed", tamed);
            if (tamed && tameable.getOwner() != null) {
                String ownerUUID = tameable.getOwner().getUniqueId().toString();
                entityDetails.setString("owner", ownerUUID);
            }
        }

        // horse types
        if (livingEntity instanceof Llama) {
            Llama abstractHorse = (Llama) livingEntity;
            String color = abstractHorse.getColor().name();
            Integer strength = abstractHorse.getStrength();
            entityDetails.setString("color", color);
            entityDetails.setInt("strength", strength);
        } else if (livingEntity instanceof Horse) {
            Horse abstractHorse = (Horse) livingEntity;
            String color = abstractHorse.getColor().name();
            String style = abstractHorse.getStyle().name();
            entityDetails.setString("color", color);
            entityDetails.setString("style", style);
        }

        return entityDetails;
    }

    public static NBTTagCompound convertToNMS(CompoundTag compound) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            NBTOutputStream nbtOut = new NBTOutputStream(baos);
            nbtOut.writeNamedTag("", compound);
            baos.close();
            return NBTCompressedStreamTools.readNBT(new DataInputStream(new ByteArrayInputStream(baos.toByteArray())));
        } catch (IOException e) {
            Main.plugin.getLogger().severe("Failed to Convert CompoundTag to NBTTagCompound");
            e.printStackTrace();
        }
        return null;
    }

    public static CompoundTag convertToAPI(NBTTagCompound compound) {
        try {
            ByteArrayInputStream bin = new ByteArrayInputStream(compound.toString().getBytes(StandardCharsets.UTF_8));
            NBTInputStream nbtIn = new NBTInputStream(bin);
            Tag tag = nbtIn.readNamedTag().getTag();
            return (tag instanceof CompoundTag) ? (CompoundTag) tag : null;
        } catch (IOException e) {
            Main.plugin.getLogger().severe("Failed to Convert NBTTagCompound to CompoundTag");
            e.printStackTrace();
        }
        return null;
    }

}