package de.lolsu.game.managers;

public interface IPhaseManager {

    /** This method is called on server startup */
    void initialize();

    /** This method is called 10 seconds before game-start when voting ends */
    void endVoting();

    /** This method is called when the game starts and all the players get teleported */
    void startGame();

    /** This method is called when there is only one team alive and the game has ended */
    void endGame();

    /** This method is basically an onDisable, called right before server restart */
    void restart();

}
