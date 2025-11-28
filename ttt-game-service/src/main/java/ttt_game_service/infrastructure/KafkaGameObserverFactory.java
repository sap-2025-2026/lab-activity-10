package ttt_game_service.infrastructure;

import io.vertx.core.Vertx;
import ttt_game_service.application.GameObserverFactory;
import ttt_game_service.domain.GameObserver;

public class KafkaGameObserverFactory implements GameObserverFactory {

	private String channelsLocation;
	private Vertx vertx;
	
	public KafkaGameObserverFactory(Vertx vertx, String channelsLocation) {
		this.channelsLocation = channelsLocation;
		this.vertx = vertx;
	}
	
	@Override
	public GameObserver makeNewGameObserver(String gameId) {
		return new KafkaGameObserver(vertx, "game-" + gameId + "-events", channelsLocation);
	}
	
}
