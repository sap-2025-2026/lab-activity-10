package ttt_game_service.infrastructure;

import io.vertx.core.Vertx;
import ttt_game_service.application.*;

/**
 * 
 * Game Service including support for observability.
 * 
 */
public class GameServiceMain {

	/* for testing with all services, deployed using docker compose .*/
	static final String EV_CHANNELS_LOCATION = "broker:9092";
	    
	public static void main(String[] args) {
		
		var service = new GameServiceImpl();
		service.bindGameRepository(new InMemoryGameRepository());

		var vertx = Vertx.vertx();

		var obsFactory = new KafkaGameObserverFactory(vertx, EV_CHANNELS_LOCATION);
		service.bindGameObserverFactory(obsFactory);
		
		var server = new GameServiceEventBasedController(service, EV_CHANNELS_LOCATION);
		vertx.deployVerticle(server);		
	}

}

