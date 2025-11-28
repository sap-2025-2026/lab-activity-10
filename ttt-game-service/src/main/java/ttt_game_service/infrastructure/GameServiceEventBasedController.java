package ttt_game_service.infrastructure;

import java.util.logging.Level;
import java.util.logging.Logger;

import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.VerticleBase;
import io.vertx.core.json.*;
import sap.kafka.async.InputEventChannel;
import sap.kafka.async.OutputEventChannel;
import ttt_game_service.application.*;
import ttt_game_service.domain.*;

/**
*
* Game Service event-based controller
* 
* @author aricci
*
*/
public class GameServiceEventBasedController extends VerticleBase  {

	private String evChannelsLocation;
	
	static Logger logger = Logger.getLogger("[Game Service Event-Based Controller]");

	/* event channels names */
	 
	static final String CREATE_GAME_REQUESTS_EVC = "create-game-requests";
	static final String CREATE_GAME_REQUESTS_APPROVED_EVC = "create-game-requests-approved";
	static final String CREATE_GAME_REQUESTS_REJECTED_EVC = "create-game-requests-rejected";

	static final String NEW_GAME_CREATED_EVC = "new-game-created";

	static final String JOIN_GAME_REQUESTS_EVC = "join-game-requests";
	static final String JOIN_GAME_REQUESTS_APPROVED_EVC = "join-game-requests-approved";
	static final String JOIN_GAME_REQUESTS_REJECTED_EVC = "join-game-requests-rejected";

	static final String NEW_MOVE_REQUESTS_EVC = "new-move-requests";
	static final String MOVE_ACCEPTED_EVC = "move-accepted";
	static final String MOVE_REJECTED_EVC = "move-rejected";
	
	/* event channels for create game requests, approvals, rejection */
	private InputEventChannel createGameRequests;
	private OutputEventChannel createGameRequestsApproved, createGameRequestsRejected;

	/* event channel about games created */
	private OutputEventChannel newGameCreated;
	
	/* event channel for join game requests, approvals, rejections */
	private InputEventChannel joinGameRequests;
	private OutputEventChannel joinGameRequestsApproved, joinGameRequestsRejected;

	/* event channel for new move requests, approvals, rejections */	
	private InputEventChannel newMoveRequests;
	private OutputEventChannel moveAccepted, moveRejected;

	/* 
	 * besides this "static" channels, a dynamic event channel
	 * is created for each new game, with  name:
	 * 
	 *   "game-"<gameId>"-events"
	 *   
	 * including all events occurring to that game.   	
	 */
	
	/* Ref. to the application layer */
	private GameService gameService;
	
	public GameServiceEventBasedController(GameService service, String evChannelsLocation) {
		this.evChannelsLocation = evChannelsLocation;
		logger.setLevel(Level.INFO);
		this.gameService = service;

	}

	public Future<?> start() {
		logger.log(Level.INFO, "TTT Game Service initializing...");
		
		var prom = Promise.promise();

		/* creating event channels */
		
		createGameRequests = new InputEventChannel(vertx, CREATE_GAME_REQUESTS_EVC, evChannelsLocation);		
		createGameRequestsApproved = new OutputEventChannel(vertx, CREATE_GAME_REQUESTS_APPROVED_EVC, evChannelsLocation);
		createGameRequestsRejected = new OutputEventChannel(vertx, CREATE_GAME_REQUESTS_REJECTED_EVC, evChannelsLocation);
		
		newGameCreated = new OutputEventChannel(vertx, NEW_GAME_CREATED_EVC, evChannelsLocation);

		joinGameRequests = new InputEventChannel(vertx, JOIN_GAME_REQUESTS_EVC, evChannelsLocation);
		joinGameRequestsApproved = new OutputEventChannel(vertx, JOIN_GAME_REQUESTS_APPROVED_EVC, evChannelsLocation);
		joinGameRequestsRejected = new OutputEventChannel(vertx, JOIN_GAME_REQUESTS_REJECTED_EVC, evChannelsLocation);

		newMoveRequests = new InputEventChannel(vertx, NEW_MOVE_REQUESTS_EVC, evChannelsLocation);
		moveAccepted = new OutputEventChannel(vertx, MOVE_ACCEPTED_EVC, evChannelsLocation);
		moveRejected = new OutputEventChannel(vertx, MOVE_REJECTED_EVC, evChannelsLocation);
				
		/* configuring input channel with event handlers */

		createGameRequests.init(this::createNewGame);
		joinGameRequests.init(this::joinGame);
		newMoveRequests.init(this::newMove);
		
		return prom.future();
	}


	/* Event Handlers */
	

	/**
	 * 
	 * Create a New Game - by users logged in (with a UserSession)
	 * 
	 * @param context
	 */
	protected void createNewGame(JsonObject createEv) {
		logger.log(Level.INFO, "CreateNewGame request");
		var gameId = createEv.getString("gameId");
		var requestId = createEv.getString("requestId");
		try {
			gameService.createNewGame(gameId);

			var evGameCreated = new JsonObject();
			evGameCreated.put("gameId", gameId);
			newGameCreated.postEvent(evGameCreated)
			.onSuccess(v -> {
				logger.info("post event about new game succeeded");
			})
			.onFailure(v -> {
				logger.info("post event about new game failed");
			});
					
			var evRequestOK = new JsonObject();
			evRequestOK.put("requestId", requestId);
			evRequestOK.put("gameId", gameId);
			createGameRequestsApproved.postEvent(evRequestOK)
			.onSuccess(v -> {
				logger.info("create game request approved");
			})
			.onFailure(v -> {
				logger.info("create game request approved but event generation failed");
			});
			
			// gameStateUpdates = new OutputEventChannel(vertx, GAME_STATE_UPDATES_EVC, chAddress);

		} catch (GameAlreadyPresentException ex) {
			var evRequestRejected = new JsonObject();
			evRequestRejected.put("requestId", requestId);
			evRequestRejected.put("gameId", gameId);
			evRequestRejected.put("reason", "game-already-present");
			createGameRequestsRejected.postEvent(evRequestRejected)
			.onSuccess(v -> {
				logger.info("create new game failed");
			})
			.onFailure(v -> {
				logger.info("create new game failed and event generation failed");
			});
		}
	}


	/**
	 * 
	 * Join a Game - by user logged in (with a UserSession)
	 * 
	 * It creates a PlayerSession
	 * 
	 * @param context
	 */
	protected void joinGame(JsonObject joinGameEv) {
		logger.log(Level.INFO, "JoinGame request");
		var requestId = joinGameEv.getString("requestId");
		var gameId = joinGameEv.getString("gameId");
		var userId = joinGameEv.getString("userId");
		var symbol = joinGameEv.getString("symbol");

		try {
			var playerSession = gameService.joinGame(new UserId(userId), 
					gameId, symbol.equals("X") ? TTTSymbol.X : TTTSymbol.O);
			
			var evRequestOK = new JsonObject();
			evRequestOK.put("requestId", requestId);
			evRequestOK.put("gameId", gameId);
			evRequestOK.put("playerSessionId", playerSession.getId());
			joinGameRequestsApproved.postEvent(evRequestOK)
			.onSuccess(v -> {
				logger.info("join game request approved");
			})
			.onFailure(v -> {
				logger.info("join game request approved but event generation failed");
			});
							
		} catch (InvalidJoinException  ex) {
			var evRequestRejected = new JsonObject();
			evRequestRejected.put("requestId", requestId);
			evRequestRejected.put("gameId", gameId);
			evRequestRejected.put("reason", ex.getMessage());
			joinGameRequestsRejected.postEvent(evRequestRejected)
			.onSuccess(v -> {
				logger.info("join game failed");
			})
			.onFailure(v -> {
				logger.info("join game failed and event generation failed");
			});
		}
	}	
	
	/**
	 * 
	 * Make a move in a game - by players playing a game (with a PlayerSession)
	 * 
	 * @param context
	 */
	protected void newMove(JsonObject newMoveEv) {
		logger.log(Level.INFO, "NewMove");
		var playerSessionId = newMoveEv.getString("playerSessionId");
		var requestId = newMoveEv.getString("requestId");
		int x = Integer.parseInt(newMoveEv.getString("x"));
		int y = Integer.parseInt(newMoveEv.getString("y"));
		try {
			var ps = gameService.getPlayerSession(playerSessionId);
			ps.makeMove(x, y);				

			var evMoveAccepted = new JsonObject();
			evMoveAccepted.put("requestId", requestId);
			moveAccepted.postEvent(evMoveAccepted)
			.onSuccess(v -> {
				logger.info("new move approved");
			})
			.onFailure(v -> {
				logger.info("new move approved but event generation failed");
			});
		} catch (InvalidMoveException ex) {
			var evMoveRejected = new JsonObject();
			evMoveRejected.put("requestId", requestId);
			moveRejected.postEvent(evMoveRejected)
			.onSuccess(v -> {
				logger.info("new move rejected");
			})
			.onFailure(v -> {
				logger.info("new move rejected and event generation failed");
			});
		}
	}
	
	/**
	 * 
	 * Get game info
	 * 
	 * @param context
	 *//*
	protected void getGameInfo(RoutingContext context) {
		logger.log(Level.INFO, "get game info");
			var gameId = context.pathParam("gameId");
			var reply = new JsonObject();
			try {
				var game = gameService.getGameInfo(gameId);
				reply.put("result", "ok");
				var gameJson = new JsonObject();
				gameJson.put("gameId", game.getId());
				gameJson.put("gameState", game.getGameState());
				if (game.isStarted() || game.isFinished()) {
					var bs = game.getBoardState();
					JsonArray array = new JsonArray();
					for (var el: bs) {
						array.add(el);
					}
					gameJson.put("boardState", array);
				}
				if (game.isStarted()) {
					gameJson.put("turn", game.getCurrentTurn());
				}
				reply.put("gameInfo", gameJson);			
				sendReply(context.response(), reply);
			} catch (GameNotFoundException ex) {
				reply.put("result", "error");
				reply.put("error", "game-not-present");
				sendReply(context.response(), reply);
			} catch (Exception ex1) {
				sendError(context.response());
			}
	}*/
}
