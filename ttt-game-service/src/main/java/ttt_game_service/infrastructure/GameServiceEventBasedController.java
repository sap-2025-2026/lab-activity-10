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

	/* static event channels names */
	 
	static final String CREATE_GAME_REQUESTS_EVC = "create-game-requests";
	static final String CREATE_GAME_REQUESTS_APPROVED_EVC = "create-game-requests-approved";
	static final String CREATE_GAME_REQUESTS_REJECTED_EVC = "create-game-requests-rejected";

	static final String NEW_GAME_CREATED_EVC = "new-game-created";

	/* static event channels for create game requests, approvals, rejection */
	
	private InputEventChannel createGameRequests;
	private OutputEventChannel createGameRequestsApproved, createGameRequestsRejected;
	
	/* static event channel about games created */
	
	private OutputEventChannel newGameCreated;
		
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

		/* creating static event channels */
		
		createGameRequests = new InputEventChannel(vertx, CREATE_GAME_REQUESTS_EVC, evChannelsLocation);		
		createGameRequestsApproved = new OutputEventChannel(vertx, CREATE_GAME_REQUESTS_APPROVED_EVC, evChannelsLocation);
		createGameRequestsRejected = new OutputEventChannel(vertx, CREATE_GAME_REQUESTS_REJECTED_EVC, evChannelsLocation);
		
		newGameCreated = new OutputEventChannel(vertx, NEW_GAME_CREATED_EVC, evChannelsLocation);

		/* configuring input channels with event handlers */

		createGameRequests.init(this::createNewGame);
		
		return prom.future();
	}


	/* Static event Handlers */
	

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
				
				/* dynamically creating channels for joining the game */

				var joinReqs = "game-" + gameId + "-join-requests";
				var joinReqsAccepted = "game-" + gameId + "-join-requests-approved";
				var joinReqsRejected = "game-" + gameId + "-join-requests-rejected";
				
				var joinGameRequests = new InputEventChannel(vertx, joinReqs, evChannelsLocation);

				var joinGameRequestsApproved = new OutputEventChannel(vertx, joinReqsAccepted, evChannelsLocation);
				var joinGameRequestsRejected = new OutputEventChannel(vertx, joinReqsRejected, evChannelsLocation);
				
				/* dynamically initializing join input channels with the handler */
				
				joinGameRequests.init(joinGameEv -> {
					logger.log(Level.INFO, "JoinGame request");
					var requestId2 = joinGameEv.getString("requestId");
					var userId = joinGameEv.getString("userId");
					var symbol = joinGameEv.getString("symbol");

					try {
						var playerSession = gameService.joinGame(new UserId(userId), 
								gameId, symbol.equals("X") ? TTTSymbol.X : TTTSymbol.O);
						
						var evRequestOK2 = new JsonObject();
						evRequestOK.put("requestId", requestId2);
						evRequestOK.put("gameId", gameId);
						evRequestOK.put("playerSessionId", playerSession.getId());
						joinGameRequestsApproved.postEvent(evRequestOK2)
						.onSuccess(v1 -> {
							logger.info("join game request approved");
							
							/* creating dynamically channels for specific players handling move events */
							
							var moveReqs = "session-" + playerSession.getId() + "-move-requests";
							var moveReqsAccepted = "session-" + playerSession.getId() + "-move-accepted";
							var moveReqsRejected = "session-" + playerSession.getId() + "-move-rejected";
							var newMoveRequests = new InputEventChannel(vertx, moveReqs, evChannelsLocation);
							var moveAccepted = new OutputEventChannel(vertx, moveReqsAccepted, evChannelsLocation);
							var moveRejected = new OutputEventChannel(vertx, moveReqsRejected, evChannelsLocation);
							
							/* dynamically initializing new move request input channels with the handler */
							
							newMoveRequests.init((JsonObject newMoveEv) -> {
								logger.log(Level.INFO, "NewMove");
								var requestId3 = newMoveEv.getString("requestId");
								int x = Integer.parseInt(newMoveEv.getString("x"));
								int y = Integer.parseInt(newMoveEv.getString("y"));
								try {
									var ps = gameService.getPlayerSession(playerSession.getId());
									ps.makeMove(x, y);				

									var evMoveAccepted = new JsonObject();
									evMoveAccepted.put("requestId", requestId3);
									moveAccepted.postEvent(evMoveAccepted)
									.onSuccess(v3 -> {
										logger.info("new move approved");
									})
									.onFailure(v3 -> {
										logger.info("new move approved but event generation failed");
									});
								} catch (InvalidMoveException ex) {
									var evMoveRejected = new JsonObject();
									evMoveRejected.put("requestId", requestId);
									moveRejected.postEvent(evMoveRejected)
									.onSuccess(v3 -> {
										logger.info("new move rejected");
									})
									.onFailure(v3 -> {
										logger.info("new move rejected and event generation failed");
									});
								}
							});
							
						})
						.onFailure(v1 -> {
							logger.info("join game request approved but event generation failed");
						});
										
					} catch (InvalidJoinException  ex) {
						var evRequestRejected = new JsonObject();
						evRequestRejected.put("requestId", requestId);
						evRequestRejected.put("gameId", gameId);
						evRequestRejected.put("reason", ex.getMessage());
						joinGameRequestsRejected.postEvent(evRequestRejected)
						.onSuccess(v1 -> {
							logger.info("join game failed");
						})
						.onFailure(v1 -> {
							logger.info("join game failed and event generation failed");
						});
					}						
				});
				
				
			})
			.onFailure(v -> {
				logger.info("create game request approved but event generation failed");
			});

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
	 * Get game info - TO BE IMPLEMENTED
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
