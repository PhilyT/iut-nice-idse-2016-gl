package fr.unice.idse.model.regle;

import fr.unice.idse.model.*;
import fr.unice.idse.model.card.*;
import fr.unice.idse.model.player.Player;

public class EffectCard
{
	private Value value;
	private Game game;
	private boolean isColorChangingCard;
	
	public EffectCard(Game game, Value value)
	{
		this.value = value;
		this.game = game;
		this.isColorChangingCard=false;
	}

	public Value getValue()
	{
		return value;
	}

	public Game getGame()
	{
		return game;
	}

	public boolean isColorChangingCard() {
		return isColorChangingCard;
	}

	public void setColorChangingCard(boolean isColorChangingCard) {
		this.isColorChangingCard = isColorChangingCard;
	}

	/**
	 * Pour savoir si la carte jouée correspond à la règle.
	 * @param card
	 * @return
	 */
	public boolean isEffect(Card card)
	{
		return card.getValue() == getValue();
	}
	
	/**
	 * Pour savoir si on a affaire à une regle contrable
	 * @return
	 */
	public boolean getEffect()
	{
		return game.getCptDrawCard() > 1;
	}
	
	/***
	 * Methode à override pour les changement de couleur
	 * @param : Couleur demandé par le Joueur.
	 */
	public void changeColor(Color color)
	{
		
	}
	
	/***
	 * Methode à override pour l'échange des jeux de cartes du joueur actuel avec un autre
	 * @param : joueur souhaité pour réaliser l'échange.
	 */
	public boolean tradeDecks(String playerName)
	{
		return false;
	}
	
	/***
	 * Methode à override pour les effets immediats
	 */
	public void action()
	{

	}
	
	/***
	 * Methode à override pour les effets contrables
	 */
	public void effect()
	{
		
	}
}
