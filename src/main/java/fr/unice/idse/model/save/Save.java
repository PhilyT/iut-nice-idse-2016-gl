package fr.unice.idse.model.save;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.unice.idse.db.*;
import fr.unice.idse.model.Game;
import fr.unice.idse.model.card.Card;
import fr.unice.idse.model.player.Player;

public class Save implements Observer {
	private Logger logger = LoggerFactory.getLogger(Save.class);
	private static Save instance;
	
	protected DataBaseGame gameDAO;
	protected DataBaseUser userDAO;
	protected DataBaseCard cardDAO;	

	protected Save() {
		gameDAO = new DataBaseGame();
		userDAO = new DataBaseUser();
		cardDAO = new DataBaseCard();
	}

	protected Save(String connector) {
		gameDAO = new DataBaseGame(connector);
		userDAO = new DataBaseUser(connector);
		cardDAO = new DataBaseCard(connector);
	}

	public static Save getInstance() {
		if(instance == null) {
			instance = new Save();
		}
		return instance;
	}
	
	@Override
	public void update(Observable o, Object arg) {
		try {
			if(!(arg instanceof SaveListEnum)) {
				throw new Exception("ERROR : Expecting a SaveListEnum");
			}
			SaveListEnum sle = (SaveListEnum)arg;
			
			switch (sle) {
				case NewGameSave:
					this.saveNewGame((Game) o);				
					break;
				case SaveTurn:
					this.saveTurn((Game) o);
					break;
				default:
					break;
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e.getCause());	
		}
	}

	/*
	 * Sauvegarde d'une nouvelle partie
	 * 
	 * @param Game game
	 */
	private void saveNewGame(Game game) throws Exception {
		if(gameDAO.addGame(game)) {
			throw new Exception("An error occured - The game could not be added");
		}

		List<Player> arrayPlayer = game.getPlayers();

		for (int i = 0; i < arrayPlayer.size(); i++) {
			BusinessQuery.addPlayerToGame(game, arrayPlayer.get(i), i);
		}

		Integer matchId = BusinessQuery.newMatch(game);
		Integer turnId = BusinessQuery.newTurn(game.getActualPlayer(), matchId, false);

		ArrayList<Card> topCard = game.getStack().getStack();

		BusinessQuery.addCardToStack(topCard.get(0), matchId, turnId);

		for (int i = 0; i < arrayPlayer.size(); i++) {
			for (int j = 0; j < arrayPlayer.get(i).getCards().size(); j++) {
				BusinessQuery.addCardToPlayerHand(arrayPlayer.get(i),
						arrayPlayer.get(i).getCards().get(j), matchId, turnId);
			}
		}
	}

	private void saveTurn(Game game) throws Exception {
		/*
		 * Game id a partir du gameName
		 */
		String gameName = game.getGameName();
		int gameId = gameDAO.getIdgameWithName(gameName);
		
		/*
		 * MatchId a partir du GameId
		 */
		int matchId = gameDAO.getIdMatchWithGameId(gameId);
		boolean inversed = game.getDirection();

		int turnId = BusinessQuery.newTurn(game.getActualPlayer(),
				matchId, inversed);

		ArrayList<Card> topCard = game.getStack().getStack();
		BusinessQuery.addCardToStack(topCard.get(0), matchId, turnId);

		List<Player> arrayPlayer = game.getPlayers();

		for (int i = 0; i < arrayPlayer.size(); i++) {
			for (int j = 0; j < arrayPlayer.get(i).getCards().size(); j++) {
				BusinessQuery.addCardToPlayerHand(arrayPlayer.get(i),
						arrayPlayer.get(i).getCards().get(j), matchId, turnId);
			}
		}
	}

}
