package com.THproject.tharidia_easydiet.client;

import com.THproject.tharidia_easydiet.TharidiaEasyDiet;
import com.THproject.tharidia_easydiet.TharidiaEasyDietClient;
import com.THproject.tharidia_easydiet.diet.ClientDietProfileCache;
import com.THproject.tharidia_easydiet.diet.DietCategory;
import com.THproject.tharidia_easydiet.diet.DietProfile;
import com.THproject.tharidia_easydiet.diet.DietRegistry;
import com.THproject.tharidia_easydiet.diet.DietSystemSettings;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Adds diet contribution lines to food tooltips.
 */
@EventBusSubscriber(modid = TharidiaEasyDiet.MODID, value = Dist.CLIENT)
public class DietTooltipHandler {
    private static final Map<ResourceLocation, List<Component>> CACHE = new HashMap<>();
    private static final EnumMap<DietCategory, TextColor> CATEGORY_COLORS = new EnumMap<>(DietCategory.class);

    static {
        CATEGORY_COLORS.put(DietCategory.GRAIN, TextColor.fromRgb(0xDAA520));
        CATEGORY_COLORS.put(DietCategory.PROTEIN, TextColor.fromRgb(0xCD5C5C));
        CATEGORY_COLORS.put(DietCategory.VEGETABLE, TextColor.fromRgb(0x228B22));
        CATEGORY_COLORS.put(DietCategory.FRUIT, TextColor.fromRgb(0xFF6347));
        CATEGORY_COLORS.put(DietCategory.SUGAR, TextColor.fromRgb(0xFFB6C1));
        CATEGORY_COLORS.put(DietCategory.WATER, TextColor.fromRgb(0x1E90FF));
    }

    @SubscribeEvent
    public static void onTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        if (stack.isEmpty()) {
            return;
        }

        // Check if this is a food item OR a water bottle
        boolean isFood = stack.getItem().getFoodProperties(stack, event.getEntity()) != null;
        boolean isWaterBottle = isWaterBottle(stack);

        if (!isFood && !isWaterBottle) {
            return;
        }

        ResourceLocation id = net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(stack.getItem());
        // Use a special cache key for water bottles
        ResourceLocation cacheKey = isWaterBottle ? ResourceLocation.parse("tharidia_easydiet:water_bottle") : id;

        List<Component> cached = CACHE.get(cacheKey);
        if (cached == null) {
            cached = isWaterBottle ? buildWaterBottleTooltip() : buildTooltipLines(stack);
            CACHE.put(cacheKey, cached);
        }

        if (!cached.isEmpty()) {
            event.getToolTip().add(Component.literal(" "));
            event.getToolTip().add(Component.translatable("diet.tooltip.header").withStyle(style -> style.withColor(0xFFAA00)));
            event.getToolTip().addAll(cached);
        }
    }

    /**
     * Checks if the item is a water bottle (potion with water)
     */
    private static boolean isWaterBottle(ItemStack stack) {
        if (!stack.is(Items.POTION)) {
            return false;
        }
        PotionContents contents = stack.get(DataComponents.POTION_CONTENTS);
        if (contents == null) {
            return false;
        }
        return contents.potion()
                .map(holder -> holder.value() == Potions.WATER.value())
                .orElse(contents.customEffects().isEmpty() && contents.customColor().isEmpty());
    }

    /**
     * Builds tooltip lines for water bottles
     */
    private static List<Component> buildWaterBottleTooltip() {
        DietSystemSettings settings = DietRegistry.getSettings();
        float waterValue = settings.waterAlwaysEatBonus() + settings.drinkWaterBonus();

        List<Component> lines = new java.util.ArrayList<>();
        TextColor color = CATEGORY_COLORS.getOrDefault(DietCategory.WATER, TextColor.fromRgb(0x1E90FF));
        lines.add(createDietLine("diet.category.water", waterValue, color));
        return lines;
    }

    private static List<Component> buildTooltipLines(ItemStack stack) {
        // Try to get from client cache first (pre-calculated, no lag)
        ClientDietProfileCache clientCache = TharidiaEasyDietClient.getClientDietCache();
        DietProfile profile = null;
        
        if (clientCache != null) {
            ResourceLocation itemId = net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(stack.getItem());
            profile = clientCache.getProfile(itemId);
        }
        
        // Fallback to DietRegistry if not in client cache
        if (profile == null) {
            profile = DietRegistry.getProfile(stack);
        }
        
        if (profile.isEmpty()) {
            return List.of();
        }

        List<Component> lines = new java.util.ArrayList<>();
        for (DietCategory category : DietCategory.VALUES) {
            float value = profile.get(category);
            if (value <= 0.0f) {
                continue;
            }
            String translationKey = getCategoryTranslationKey(category);
            TextColor color = CATEGORY_COLORS.getOrDefault(category, TextColor.fromRgb(0xFFFFFF));
            lines.add(createDietLine(translationKey, value, color));
        }
        return lines;
    }

    private static Component createDietLine(String translationKey, float value, TextColor color) {
        MutableComponent line = Component.literal("");
        line.append(Component.literal(" - ").withStyle(ChatFormatting.GRAY));
        line.append(Component.translatable(translationKey).append(": ").withStyle(Style.EMPTY.withColor(color)));
        line.append(Component.literal(String.format("+%.1f", value)).withStyle(ChatFormatting.WHITE));
        return line;
    }
    
    private static String getCategoryTranslationKey(DietCategory category) {
        return switch (category) {
            case GRAIN -> "diet.category.grain";
            case PROTEIN -> "diet.category.protein";
            case VEGETABLE -> "diet.category.vegetable";
            case FRUIT -> "diet.category.fruit";
            case SUGAR -> "diet.category.sugar";
            case WATER -> "diet.category.water";
        };
    }
}
