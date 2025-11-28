package ttt_api_gateway.application;

import common.exagonal.OutBoundPort;

/**
 * 
 * Interface for interacting with the LobbyService 
 * 
 */
@OutBoundPort
public interface LobbyService  {

	String login(String userName, String password) throws LoginFailedException, ServiceNotAvailableException;
	
	void createNewGame(String sessionId, String gameId) throws CreateGameFailedException, ServiceNotAvailableException;
	
	String joinGame(String sessionId, String gameId, TTTSymbol symbol) throws JoinGameFailedException, ServiceNotAvailableException;
	
	
    
}
