package de.lolsu.game.managers;

public interface IWarmupManager {

    /** This method is called when the watch-period ends and players gain full movement */
    void endWatch();

    /** This method is called if pvp is enabled, the warmup-period ends and players are allowed to do pvp */
    void endWarmup();

}
