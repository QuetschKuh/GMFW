package de.lolsu.game.misc;

/**
 * A list of gamerule names and their default value. Useful since you may not know all gamerules by heart. Allows autocomplete essentially
 * */
public enum GameRule {

    MOB_GRIEF("mobGriefing", "true"),
    MOB_SPAWN("doMobSpawning", "true"),
    MOB_LOOT("doMobLoot", "true"),
    TILE_DROPS("doTileDrops", "true"),
    FIRE("doFireTick", "true"),
    KEEP_INV("keepInventory", "false"),
    CMD_OUTPUT("commandBlockOutput", "true"),
    REGEN("naturalRegeneration", "true"),
    DAYLIGHT_CYCLE("doDaylightCycle", "true"),
    CMD_LOG("logAdminCommands", "true"),
    SHOW_DEATH_MSG("showDeathMessages", "true"),
    TICK_SPEED("randomTickSpeed", "3"),
    CMD_FEEDBACK("sendCommandFeedback", "true"),
    REDUCED_DEBUG_INFO("reducedDebugInfo", "false"),
    ;

    public final String name;
    public final String defaultValue;
    GameRule(String name, String defaultValue) {
        this.name = name;
        this.defaultValue = defaultValue;
    }

}