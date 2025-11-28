package ttt_game_service.application;

import common.exagonal.InBoundPort;
import ttt_game_service.domain.*;

/**
 * 
 * Interface of the Game Service at the application layer
 * 
 */
@InBoundPort
public interface GameService  {


	/**
     * 
     * Get game info.
     * 
     * @param gameId
     * @return
     * @throws AccountNotFoundException
     */
	Game getGameInfo(String gameId) throws GameNotFoundException;
		
	/**
	 * 
	 * Retrieve an existing player session.
	 * 
	 * @param id
	 * @return
	 */
	PlayerSession getPlayerSession(String sessionId);
	
	/**
	 * 
	 * Create a game -- called by a UserSession (logged in user) 
     *	 *  
	 * @throws GameAlreadyPresentException
	 */
	void createNewGame(String gameId) throws GameAlreadyPresentException;
	
	/**
	 * 
	 * Join a game -- called by a UserSession (logged in user), creates a new PlayerSession
	 * 
	 * @param userId -- id of the user (player)
	 * @param gameId -- id of the game to be joined
	 * @param symbol -- symbol to be used (X, O)
	 * @return
	 * @throws InvalidJoinException
	 */
	PlayerSession joinGame(UserId userId, String gameId, TTTSymbol symbol) throws InvalidJoinException;

    
}
