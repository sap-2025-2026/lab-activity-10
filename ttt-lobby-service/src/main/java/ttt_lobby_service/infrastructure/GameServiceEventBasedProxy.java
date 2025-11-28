package ttt_lobby_service.infrastructure;

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
import ttt_lobby_service.application.CreateGameFailedException;
import ttt_lobby_service.application.GameAlreadyPresentException;
import ttt_lobby_service.application.GameService;
import ttt_lobby_service.application.InvalidJoinGameException;
import ttt_lobby_service.application.JoinGameFailedException;
import ttt_lobby_service.application.ServiceNotAvailableException;
import ttt_lobby_service.domain.TTTSymbol;
import ttt_lobby_service.domain.UserId;

@Adapter
public class GameServiceEventBasedProxy  implements GameService {

	private String channelsLocation;
	static final String CREATE_GAME_REQUESTS_EVC = "create-game-requests";
	static final String CREATE_GAME_REQUESTS_APPROVED_EVC = "create-game-requests-approved";
	static final String CREATE_GAME_REQUESTS_REJECTED_EVC = "create-game-requests-rejected";

	static final String NEW_GAME_CREATED_EVC = "new-game-created";

	static final String JOIN_GAME_REQUESTS_EVC = "join-game-requests";
	static final String JOIN_GAME_REQUESTS_APPROVED_EVC = "join-game-requests-approved";
	static final String JOIN_GAME_REQUESTS_REJECTED_EVC = "join-game-requests-rejected";

	private OutputEventChannel createGameReq;
	private OutputEventChannel joinGameReq;
	private Vertx vertx;
	private int count;
	
	public GameServiceEventBasedProxy(Vertx vertx, String channelsLocation) {
		this.channelsLocation = channelsLocation;
		this.vertx = vertx;		
		createGameReq = new OutputEventChannel(vertx, CREATE_GAME_REQUESTS_EVC, channelsLocation);
		joinGameReq = new OutputEventChannel(vertx, JOIN_GAME_REQUESTS_EVC, channelsLocation);
		count = 0;
	}


	@Override
	public void createNewGame(String gameId)
			throws GameAlreadyPresentException, CreateGameFailedException, ServiceNotAvailableException {
		count++;
		var createGameReqEv = new JsonObject();
		createGameReqEv.put("requestId", "lobby-" + count);
		createGameReqEv.put("gameId", gameId);		
		createGameReq.postEvent(createGameReqEv);
	}

	@Override
	public String joinGame(UserId userId, String gameId, TTTSymbol symbol)
			throws InvalidJoinGameException, JoinGameFailedException, ServiceNotAvailableException {
		// TODO Auto-generated method stub
		count++;
		var joinGameReqEv = new JsonObject();
		joinGameReqEv.put("requestId", "lobby-" + count);
		joinGameReqEv.put("gameId", gameId);		
		joinGameReqEv.put("userId", userId);		
		joinGameReqEv.put("symbol", symbol.equals(TTTSymbol.X) ? "X" : "O");		
		joinGameReq.postEvent(joinGameReqEv);
		return null;
	}

	
}
