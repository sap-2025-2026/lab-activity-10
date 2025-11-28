package ttt_api_gateway.infrastructure;

import io.prometheus.metrics.core.metrics.Counter;
import io.prometheus.metrics.exporter.httpserver.HTTPServer;
import io.prometheus.metrics.instrumentation.jvm.JvmMetrics;
import io.prometheus.metrics.model.snapshots.Unit;
import io.vertx.core.Vertx;
import ttt_api_gateway.application.*;

/**
 * 
 * Implementing a simple API Gateway for the TTT Game Server case study
 * 
 */
public class APIGatewayMain {

	static final int BACKEND_PORT = 8080;
	
	/* addresses to be used when using a manual deployment */
	
	// static final String ACCOUNT_SERVICE_ADDRESS = "http://localhost:9000";
	// static final String LOBBY_SERVICE_ADDRESS = "http://localhost:9001";
	// static final String GAME_SERVICE_ADDRESS = "http://localhost:9002";

	// static final String GAME_SERVICE_WS_ADDRESS = "localhost";
	// static final int GAME_SERVICE_WS_PORT = 9002;

	/* addresses to be used when deploying with Docker */
	
	static final String ACCOUNT_SERVICE_ADDRESS = "http://account-service:9000";
	static final String LOBBY_SERVICE_ADDRESS = "http://lobby-service:9001";
	static final String GAME_SERVICE_ADDRESS = "http://game-service:9002";
	
	static final String GAME_SERVICE_WS_ADDRESS = "game-service";
	static final int GAME_SERVICE_WS_PORT = 9002;

	static final int METRICS_SERVER_EXPOSED_PORT = 9401;

			
	public static void main(String[] args) {
				
		AccountService accountService = new AccountServiceProxy(ACCOUNT_SERVICE_ADDRESS);
		LobbyService lobbyService = new LobbyServiceProxy(LOBBY_SERVICE_ADDRESS);
		GameService gameService = new GameServiceProxy(GAME_SERVICE_ADDRESS, GAME_SERVICE_WS_ADDRESS, GAME_SERVICE_WS_PORT);

		PrometheusControllerObserver obs = null;
		try {
			obs = new PrometheusControllerObserver(METRICS_SERVER_EXPOSED_PORT);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
 		var vertx = Vertx.vertx();
		var server = new APIGatewayController(accountService, lobbyService, gameService, BACKEND_PORT);
		server.addControllerObserver(obs);
		vertx.deployVerticle(server);	
	}

}

