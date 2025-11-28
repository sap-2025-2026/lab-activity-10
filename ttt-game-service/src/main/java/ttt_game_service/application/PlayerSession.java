package ttt_game_service.application;

import java.util.logging.Logger;

import ttt_game_service.domain.*;
/**
 * 
 * Representing a player session.
 * 
 * - Created when a logged user joins a game.
 * - It includes the operations that a player can do.
 * - It acts as observer of events generated in the game. 
 * 
 */
public class PlayerSession /* implements GameObserver */ {

	static Logger logger = Logger.getLogger("[Player Session]");
	private UserId userId;
	private Game game;
	private TTTSymbol symbol;
	private String playerSessionId;
	
	public PlayerSession(String playerSessionId, UserId userId, Game game, TTTSymbol symbol) {
		this.userId = userId;
		this.game = game;
		this.symbol = symbol;
		this.playerSessionId = playerSessionId;
	}
		
	public void makeMove(int x, int y) throws InvalidMoveException {
		game.makeAmove(userId, x, y);
	}
	
	public TTTSymbol getSymbol() {
		return symbol;
	}
	
	public String getGameId() {
		return game.getId();
	}
	
	public String getId() {
		return playerSessionId;
	}

	private void log(String msg) {
		System.out.println("[ player " + userId.id() + " in game " + game.getId() + " ] " + msg);
	}
}
