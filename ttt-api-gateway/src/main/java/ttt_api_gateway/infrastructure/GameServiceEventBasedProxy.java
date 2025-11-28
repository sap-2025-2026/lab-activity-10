package ttt_api_gateway.infrastructure;

import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

import common.exagonal.Adapter;
import io.vertx.core.Vertx;
import io.vertx.core.http.WebSocketClient;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import sap.kafka.async.InputEventChannel;
import sap.kafka.async.OutputEventChannel;
import ttt_api_gateway.application.*;
import ttt_api_gateway.domain.Game;

@Adapter
public class GameServiceEventBasedProxy extends HTTPSyncBaseProxy implements GameService  {

	private String channelsLocation;
	static final String NEW_MOVE_REQUESTS_EVC = "new-move-requests";
	static final String MOVE_ACCEPTED_EVC = "move-accepted";
	static final String MOVE_REJECTED_EVC = "move-rejected";

	private OutputEventChannel newMoveReq;
	private Vertx vertx;
	private int count;
	
	public GameServiceEventBasedProxy(Vertx vertx, String channelsLocation) {
		this.channelsLocation = channelsLocation;
		this.vertx = vertx;		
		newMoveReq = new OutputEventChannel(vertx, NEW_MOVE_REQUESTS_EVC, channelsLocation);
		count = 0;
	}

	@Override
	public void makeAMove(String gameId, String playerSessionId, int x, int y) throws InvalidMoveException, ServiceNotAvailableException {
		try {
			var newMoveReqEv = new JsonObject();
			count++;
			newMoveReqEv.put("requestId", playerSessionId + "-" + count);
			newMoveReqEv.put("playerSessionId", playerSessionId);		
			newMoveReqEv.put("x", x);		
			newMoveReqEv.put("y", y);		
			newMoveReq.postEvent(newMoveReqEv);
		} catch (Exception ex) {
			throw new ServiceNotAvailableException();
		}
	}

	@Override
	public Game getGameInfo(String gameId) throws GameNotFoundException, ServiceNotAvailableException {
		throw new ServiceNotAvailableException();
	}
	
	
	@Override
	public void createAnEventChannel(String gameId, String playerSessionId, Vertx vertx) {		
		var eb = vertx.eventBus();
		InputEventChannel gameEvents = new InputEventChannel(vertx, "game-" + gameId + "-events", channelsLocation);
		gameEvents.init((JsonObject ev) -> {
			eb.publish(playerSessionId, ev); 
		});
		
	}

	
}
