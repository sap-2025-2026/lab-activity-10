package ttt_game_service.infrastructure;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Semaphore;
import java.util.logging.Logger;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import sap.kafka.async.InputEventChannel;
import sap.kafka.async.OutputEventChannel;

public class SimpleInteractionExample {

	static Logger logger = Logger.getLogger("[Example]");
		
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
	
	public static void main(String[] args) throws Exception {
		var vertx = Vertx.vertx();

		var location = "localhost:9092";
		
		// var admin = new EventChannelsAdmin(vertx, location);
		// admin.purgeChannel(CREATE_GAME_REQUESTS_EVC);

		var gameCreated = new Semaphore(0);
		
		OutputEventChannel createGameReq = new OutputEventChannel(vertx, CREATE_GAME_REQUESTS_EVC, location);
				
		InputEventChannel createGameReqApproved = new InputEventChannel(vertx, CREATE_GAME_REQUESTS_APPROVED_EVC, location);
		createGameReqApproved.init((JsonObject ev) -> {			
			logger.info("created game request approved - " + (ev.encodePrettily()));
		});
				
		InputEventChannel createGameReqRejected = new InputEventChannel(vertx, CREATE_GAME_REQUESTS_REJECTED_EVC, location);
		createGameReqRejected.init((JsonObject ev) -> {
			logger.info("created game request rejected - " + (ev.encodePrettily()));
		});

		InputEventChannel newGames = new InputEventChannel(vertx, NEW_GAME_CREATED_EVC, location);
		newGames.init((JsonObject ev) -> {
			logger.info("game created - " + (ev.encodePrettily()));
			gameCreated.release();
		});

		/* create a game */
		
		var createGameReqEv = new JsonObject();
		createGameReqEv.put("requestId", 1);
		createGameReqEv.put("gameId", "supergame");		
		createGameReq
		.postEvent(createGameReqEv)
		.onSuccess(v -> {
			logger.info("event creation posted");
		});
		
		gameCreated.acquire();
		
		// Thread.sleep(1000);
		
		/* observing game events */
		
		InputEventChannel gameEvents = new InputEventChannel(vertx, "game-supergame-events", location);
		gameEvents.init((JsonObject ev) -> {
		}).onSuccess(r -> {					
			logger.info("registered to game events");
		});

		/* join the game - user-1 */
		
		OutputEventChannel joinGameReq = new OutputEventChannel(vertx, JOIN_GAME_REQUESTS_EVC, location);
		
		var joinGameReqEv = new JsonObject();
		joinGameReqEv.put("requestId", 2);
		joinGameReqEv.put("gameId", "supergame");		
		joinGameReqEv.put("userId", "user-1");		
		joinGameReqEv.put("symbol", "X");		
		joinGameReq
		.postEvent(joinGameReqEv)
		.onSuccess(v -> {
			logger.info("join game user-1 posted");
		});

		
		/* join the game - user-2*/
		
		var joinGameReqEv2 = new JsonObject();
		joinGameReqEv2.put("requestId", 3);
		joinGameReqEv2.put("gameId", "supergame");		
		joinGameReqEv2.put("userId", "user-2");		
		joinGameReqEv2.put("symbol", "O");		
		joinGameReq
		.postEvent(joinGameReqEv2)
		.onSuccess(v -> {
			logger.info("join game user-2 posted");
		});
		
		var myLatch = new CountDownLatch(2);
		
		InputEventChannel joinGameReqApproved = new InputEventChannel(vertx, JOIN_GAME_REQUESTS_APPROVED_EVC, location);
		joinGameReqApproved.init((JsonObject ev) -> {			
			logger.info("join game request approved - " + (ev.encodePrettily()));
			myLatch.countDown();
		});

		myLatch.await();
		
		logger.info("both joined");
		
		/* users move  */

		OutputEventChannel newMoveReq = new OutputEventChannel(vertx, NEW_MOVE_REQUESTS_EVC, location);

		var newMoveReqEv = new JsonObject();
		newMoveReqEv.put("requestId", 4);
		newMoveReqEv.put("playerSessionId", "player-session-1");		
		newMoveReqEv.put("x", "1");		
		newMoveReqEv.put("y", "1");		
		newMoveReq
		.postEvent(newMoveReqEv)
		.onSuccess(v -> {
			logger.info("new move for user-1 posted");
		});

		var newMoveReqEv2 = new JsonObject();
		newMoveReqEv2.put("requestId", 5);
		newMoveReqEv2.put("playerSessionId", "player-session-2");		
		newMoveReqEv2.put("x", "0");		
		newMoveReqEv2.put("y", "0");		
		newMoveReq
		.postEvent(newMoveReqEv2)
		.onSuccess(v -> {
			logger.info("new move for user-2 posted");
		});
		
		Thread.sleep(100000);
			
	}
}
