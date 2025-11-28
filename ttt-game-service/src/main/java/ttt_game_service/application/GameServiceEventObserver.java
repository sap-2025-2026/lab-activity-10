package ttt_game_service.application;

import common.exagonal.OutBoundPort;
import ttt_game_service.domain.GameObserver;

@OutBoundPort
public interface GameServiceEventObserver extends GameObserver {

	void notifyNewGameCreated(String gameId);
}
