package ttt_game_service.application;

public record NewGameCreatedEvent(long timestamp, String creatorId, long currentNumberOfGames) implements GameServiceEvent {
}
