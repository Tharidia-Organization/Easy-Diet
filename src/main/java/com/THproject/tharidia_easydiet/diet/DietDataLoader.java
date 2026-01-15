package com.THproject.tharidia_easydiet.diet;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.neoforged.fml.loading.FMLPaths;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

/**
 * Loads diet configuration from datapacks.
 * Files located at data/diet_config.json
 */
public class DietDataLoader extends SimpleJsonResourceReloadListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(DietDataLoader.class);
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String DIRECTORY = "diet_config";

    public DietDataLoader() {
        super(GSON, DIRECTORY);
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> data, ResourceManager resourceManager, ProfilerFiller profiler) {
        LOGGER.info("Loading diet configuration...");
        LOGGER.debug("Found {} diet config files", data.size());
        DietRegistry.reset();

        // Always generate default config as a reference for users
        generateDefaultConfig();

        if (data.isEmpty()) {
            LOGGER.warn("No diet_config data found; using defaults.");
            DietRegistry.loadConfig(DietPackConfig.DEFAULT);
            return;
        }

        DietPackConfig selectedConfig = null;
        ResourceLocation selectedId = null;

        for (Map.Entry<ResourceLocation, JsonElement> entry : data.entrySet()) {
            try {
                DietPackConfig packConfig = DietPackConfig.CODEC
                        .parse(JsonOps.INSTANCE, entry.getValue())
                        .resultOrPartial(error -> LOGGER.error("Failed to parse diet config {}: {}", entry.getKey(), error))
                        .orElse(null);

                if (packConfig != null) {
                    selectedConfig = packConfig;
                    selectedId = entry.getKey();
                }
            } catch (Exception ex) {
                LOGGER.error("Error loading diet config {}: {}", entry.getKey(), ex.getMessage(), ex);
            }
        }

        if (selectedConfig != null) {
            DietRegistry.loadConfig(selectedConfig);
            LOGGER.info("Loaded diet config from {}", selectedId);
        } else {
            LOGGER.warn("All diet configs failed to load; falling back to defaults.");
            DietRegistry.loadConfig(DietPackConfig.DEFAULT);
        }
    }

    /**
     * Generates a default config file in the config directory if one doesn't exist.
     * This provides users with a template they can customize.
     */
    private void generateDefaultConfig() {
        LOGGER.info("[DIET CONFIG] Attempting to generate default config file...");

        try {
            // Try FMLPaths first, fall back to working directory
            Path configDir;
            try {
                configDir = FMLPaths.CONFIGDIR.get().resolve("tharidia_easydiet");
            } catch (Exception e) {
                LOGGER.warn("[DIET CONFIG] FMLPaths not available, using working directory");
                configDir = Path.of("config", "tharidia_easydiet");
            }

            Path configFile = configDir.resolve("diet_config.json");

            LOGGER.info("[DIET CONFIG] Config directory: {}", configDir.toAbsolutePath());
            LOGGER.info("[DIET CONFIG] Config file path: {}", configFile.toAbsolutePath());

            if (Files.exists(configFile)) {
                LOGGER.info("[DIET CONFIG] Default config already exists at {}", configFile);
                return;
            }

            Files.createDirectories(configDir);
            LOGGER.info("[DIET CONFIG] Created config directory");

            // Manually build JSON to ensure all default values are included for reference
            String jsonContent = buildDefaultConfigJson();
            Files.writeString(configFile, jsonContent);
            LOGGER.info("[DIET CONFIG] Generated default diet config at: {}", configFile.toAbsolutePath());
            LOGGER.info("[DIET CONFIG] To use custom settings, create a datapack with diet_config JSON files.");
        } catch (Exception e) {
            LOGGER.error("[DIET CONFIG] Failed to generate default config file: {}", e.getMessage(), e);
        }
    }

    /**
     * Builds the default config JSON manually to ensure all values are included.
     * The codec's optionalFieldOf skips default values, so we build it manually.
     */
    private String buildDefaultConfigJson() {
        DietPackConfig config = DietPackConfig.DEFAULT;
        DietSystemSettings settings = config.settings();

        return """
                {
                  "_comment": "This is a reference config file. To customize, create a datapack with diet_config JSON files.",
                  "_datapack_location": "data/<namespace>/diet_config/<name>.json",
                  "items": {
                    "minecraft:bread": {
                      "grain": 3.0,
                      "protein": 0.0,
                      "vegetable": 0.0,
                      "fruit": 0.0,
                      "sugar": 0.0,
                      "water": 0.0
                    },
                    "minecraft:cooked_beef": {
                      "grain": 0.0,
                      "protein": 8.0,
                      "vegetable": 0.0,
                      "fruit": 0.0,
                      "sugar": 0.0,
                      "water": 0.0
                    },
                    "minecraft:apple": {
                      "grain": 0.0,
                      "protein": 0.0,
                      "vegetable": 0.0,
                      "fruit": 4.0,
                      "sugar": 1.0,
                      "water": 1.0
                    },
                    "minecraft:carrot": {
                      "grain": 0.0,
                      "protein": 0.0,
                      "vegetable": 3.0,
                      "fruit": 0.0,
                      "sugar": 0.5,
                      "water": 0.5
                    }
                  },
                  "decay_rates": {
                    "grain": %s,
                    "protein": %s,
                    "vegetable": %s,
                    "fruit": %s,
                    "sugar": %s,
                    "water": %s
                  },
                  "max_values": {
                    "grain": %s,
                    "protein": %s,
                    "vegetable": %s,
                    "fruit": %s,
                    "sugar": %s,
                    "water": %s
                  },
                  "settings": {
                    "decay_interval_seconds": %s,
                    "heuristics": {
                      "saturation_scale": %s,
                      "fast_food_saturation_threshold": %s,
                      "grain_nutrition_multiplier": %s,
                      "fast_food_grain_bonus": %s,
                      "protein_meat_multiplier": %s,
                      "protein_base_multiplier": %s,
                      "vegetable_hint_multiplier": %s,
                      "vegetable_base_multiplier": %s,
                      "fruit_hint_multiplier": %s,
                      "fruit_base_multiplier": %s,
                      "sugar_base_multiplier": %s,
                      "fast_sugar_flat_bonus": %s,
                      "fast_sugar_saturation_multiplier": %s,
                      "water_always_eat_bonus": %s,
                      "water_default_bonus": %s,
                      "drink_water_bonus": %s
                    }
                  }
                }
                """.formatted(
                // decay_rates
                config.decayRates().get(DietCategory.GRAIN),
                config.decayRates().get(DietCategory.PROTEIN),
                config.decayRates().get(DietCategory.VEGETABLE),
                config.decayRates().get(DietCategory.FRUIT),
                config.decayRates().get(DietCategory.SUGAR),
                config.decayRates().get(DietCategory.WATER),
                // max_values
                config.maxValues().get(DietCategory.GRAIN),
                config.maxValues().get(DietCategory.PROTEIN),
                config.maxValues().get(DietCategory.VEGETABLE),
                config.maxValues().get(DietCategory.FRUIT),
                config.maxValues().get(DietCategory.SUGAR),
                config.maxValues().get(DietCategory.WATER),
                // settings
                settings.decayIntervalSeconds(),
                settings.saturationScale(),
                settings.fastFoodSaturationThreshold(),
                settings.grainNutritionMultiplier(),
                settings.fastFoodGrainBonus(),
                settings.proteinMeatMultiplier(),
                settings.proteinBaseMultiplier(),
                settings.vegetableHintMultiplier(),
                settings.vegetableBaseMultiplier(),
                settings.fruitHintMultiplier(),
                settings.fruitBaseMultiplier(),
                settings.sugarBaseMultiplier(),
                settings.fastSugarFlatBonus(),
                settings.fastSugarSaturationMultiplier(),
                settings.waterAlwaysEatBonus(),
                settings.waterDefaultBonus(),
                settings.drinkWaterBonus()
        );
    }
}
