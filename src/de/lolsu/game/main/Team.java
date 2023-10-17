package de.lolsu.game.main;

import de.lolsu.game.managers.PlayerManager;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class Team {

	public final int id;
	private List<Player> players;
	private List<Player> playersAlive;
	
	/** Creates a new team */
	public Team() {
		this.players = new ArrayList<>();
		id = PlayerManager.teamList.size();
	}

	/*
	 * Getters
	 */

	public int size() {
		return players.size();
	}

	public int alive() {
		return playersAlive.size();
	}

	public boolean isFull() { return players.size() >= Gamemode.singleton.maxTeamSize; }

	public boolean isEmpty() { return players.isEmpty(); }

	public boolean isDead() { return playersAlive.isEmpty(); }

	public boolean contains(Player p) {
		return players.contains(p);
	}

	public List<String> names() {
		return players.stream().map(Player::getDisplayName).collect(Collectors.toList());
	}

	public List<String> names(String prepend) {
		return players.stream().map(p -> prepend + p.getDisplayName()).collect(Collectors.toList());
	}

	public List<Player> getPlayers() {
		return new ArrayList<>(players); // Send a copy because a reference would be deadly
	}

	/*
	 * Modifiers
	 */

	public void addPlayers(Player... players) {
		this.players.addAll(Arrays.asList(players));
		for(Player p : players) PlayerManager.playerTeamMap.put(p, this);
	}

	/**
	 * Adds the given player to the team and updates all relevant variables
	 * @return False if the team is full, true if the player was successfully added
	 * */
	public boolean addPlayer(Player p) {
		if(isFull()) return false;
		players.add(p);
		PlayerManager.playerTeamMap.put(p, this);
		return true;
	}

	public void removePlayer(Player p) {
		players.remove(p);
		PlayerManager.playerTeamMap.remove(p);
	}

	public void killPlayer(Player p) {
		playersAlive.remove(p);
	}

	public void initialize() {
		playersAlive = new ArrayList<>(players);
	}
	
}
