package ttt_game_service.application;

import ttt_game_service.domain.GameObserver;

public interface GameObserverFactory {

	GameObserver makeNewGameObserver(String gameId);
	
}
