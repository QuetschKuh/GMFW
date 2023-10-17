package de.lolsu.game.managers;

/** Implementing this class into your CustomManager will override all functions usually handled by the built-in PhaseManager, you can override them back by calling PhaseManager.function(); */
public interface IPhaseExtended {

    void teleportPlayers();

}
