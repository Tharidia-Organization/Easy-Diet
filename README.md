# Tharidia: Easy Diet

> ⚠️ **ALPHA VERSION** - This mod is currently in active development and may contain bugs or incomplete features.

## About the Tharidia Project

**Tharidia** is an ambitious medieval fantasy roleplay server for Minecraft that completely transforms the vanilla experience. Set in a richly detailed medieval world with fantasy elements, Tharidia features complex mechanics that create an immersive roleplay environment far removed from standard Minecraft gameplay.

This mod, **Easy Diet**, is one of several specialized modules that make up the Tharidia ecosystem, each focusing on specific gameplay mechanics to enhance the roleplay experience.

## What is Easy Diet?

Easy Diet introduces a comprehensive nutrition and diet system that tracks what you eat and encourages balanced consumption. Instead of simply eating to fill your hunger bar, you must now pay attention to the variety and quality of your diet.

### Core Concept

Food items are categorized into different nutritional groups (proteins, vegetables, fruits, grains, dairy, etc.). As you consume food, your body builds up nutrition levels in each category. Maintaining a balanced diet across all categories provides powerful benefits, while neglecting certain food groups results in penalties.

### Key Features

- **Dynamic Food Categorization** - Every food item is assigned to one or more nutritional categories
- **Nutrition Tracking** - Your diet values are tracked per category and decay over time, simulating metabolism
- **Diet Profiles** - Configurable profiles define how much nutrition each food provides
- **Balanced Diet System** - Rewards for maintaining variety, penalties for imbalanced eating
- **Data-Driven Configuration** - Server administrators can customize food categories and diet profiles via JSON files
- **Client-Side Caching** - Efficient calculation system with background processing for smooth performance
- **Network Synchronization** - Diet data syncs between server and client for accurate display

### How It Works

1. **Eat Food** - When you consume food, it adds nutrition to specific diet categories
2. **Track Progress** - Your nutrition levels in each category are tracked and displayed
3. **Natural Decay** - Over time, nutrition values decay, simulating your body's metabolism
4. **Reap Rewards** - Maintain balanced nutrition across categories to gain beneficial effects
5. **Avoid Penalties** - Neglecting food groups or eating too much of one type results in negative effects

## Installation

### Requirements
- Minecraft 1.21.1
- NeoForge 21.1.215 or higher

### Steps
1. Download and install [NeoForge](https://neoforged.net/) for Minecraft 1.21.1
2. Download the latest release of Tharidia: Easy Diet
3. Place the JAR file in your `mods` folder
4. Launch Minecraft

## For Server Administrators

### Configuration

Diet profiles and food categories can be configured via JSON files in the `data/tharidia_easydiet/diet/` directory. This allows complete customization of:
- Which foods belong to which categories
- How much nutrition each food provides
- Decay rates for different food categories
- Thresholds for rewards and penalties

### Integration with Tharidia

This mod is designed to work seamlessly with other Tharidia modules:
- **Tharidia Tweaks** - Core gameplay modifications
- **Tharidia Features** - Additional roleplay mechanics

## Building from Source

```bash
./gradlew build
```

The compiled JAR will be located in `build/libs/Tharidia-EasyDiet.jar`

## Technical Information

- **Mod ID**: `tharidia_easydiet`
- **Version**: 0.1.0-alpha
- **Main Package**: `com.THproject.tharidia_easydiet`
- **Network Protocol**: Custom packet system for diet data synchronization
- **Data Storage**: Attachment-based player data storage

## Development Status

This mod is currently in **alpha** stage. Features are functional but may undergo significant changes. Expect:
- Possible bugs and edge cases
- Balance adjustments
- New features and improvements
- Configuration format changes

## Credits

**Developed by**: Tharidia Team  
**Part of**: The Tharidia Project  
**Extracted from**: Tharidia Things (parent mod)

## License

All Rights Reserved

---

*For more information about the Tharidia Project and its other modules, visit our community.*
