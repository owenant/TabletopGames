package games.dominion.players;

import core.AbstractGameState;
import core.AbstractPlayer;
import core.actions.AbstractAction;
import games.dominion.DominionConstants;
import games.dominion.DominionConstants.DeckType;
import games.dominion.DominionGameState;
import games.dominion.actions.BuyCard;
import games.dominion.actions.EndPhase;
import games.dominion.actions.Witch;
import games.dominion.cards.CardType;
import games.dominion.cards.DominionCard;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import core.components.Deck;

public class DoubleWitch extends AbstractPlayer {

    /**
     * Generate a valid action to play in the game. Valid actions can be found by accessing
     * AbstractGameState.getActions()
     *
     * @param gameState observation of the current game state
     */
    @Override
    public AbstractAction _getAction(AbstractGameState gameState, List<AbstractAction> possibleActions) {
        DominionGameState state = (DominionGameState) gameState;
        int player = gameState.getCurrentPlayer();
        //List<AbstractAction> actions = getForwardModel().computeAvailableActions(gameState, getParameters().actionSpace);

        //check cards in player's deck
        int totalDeckGoldCards = state.cardsOfType(CardType.GOLD, player, DeckType.ALL);
        int totalDeckWitchCards = state.cardsOfType(CardType.WITCH, player, DeckType.ALL);

        //check cards in supply
        int noOfProvinceInSupply = state.cardsOfType(CardType.PROVINCE, -1, DeckType.SUPPLY);
        int noOfWitchInSupply = state.cardsOfType(CardType.WITCH, -1, DeckType.SUPPLY);
        int noOfDuchyInSupply = state.cardsOfType(CardType.DUCHY, -1, DeckType.SUPPLY);
        int noOfEstateInSupply = state.cardsOfType(CardType.ESTATE, -1, DeckType.SUPPLY);
        int noOfGoldInSupply = state.cardsOfType(CardType.GOLD, -1, DeckType.SUPPLY);
        int noOfSilverInSupply = state.cardsOfType(CardType.SILVER, -1, DeckType.SUPPLY);

        //check that agent is in the buy phase
        if(gameState.getGamePhase() != DominionGameState.DominionGamePhase.Buy)
        {
            //play a witch card if possible
            int witchCardsInHand = state.cardsOfType(CardType.WITCH, player, DeckType.HAND);
            if (witchCardsInHand > 0) {
                return new Witch(player);
            }else {
                //TODO: check to see if all actions are discard and then throw away cheapest card
                return possibleActions.get(0);
            }
        }else{
            int cash = state.availableSpend(player);
            if(cash >= 8 && (noOfProvinceInSupply > 0) && (totalDeckGoldCards > 0)){
                return new BuyCard(CardType.PROVINCE, player);
            } else if(cash >= 5 && (noOfWitchInSupply > 0) && (totalDeckWitchCards < 2)) {
                return new BuyCard(CardType.WITCH, player);
            } else if (cash >= 5 && (noOfDuchyInSupply > 0) && (noOfProvinceInSupply < 4)) {
                return new BuyCard(CardType.DUCHY, player);
            } else if (cash >= 2 && (noOfEstateInSupply > 0) && (noOfProvinceInSupply < 2)) {
                return new BuyCard(CardType.ESTATE, player);
            } else if (cash >= 6 && (noOfGoldInSupply > 0)) {
                return new BuyCard(CardType.GOLD, player);
            } else if (cash >= 3 && (noOfSilverInSupply > 0)) {
                return new BuyCard(CardType.SILVER, player);
            } else{
                return new EndPhase();
            }
        }
    }

    @Override
    public String toString() {
        return "DoubleWitch";
    }

    @Override
    public DoubleWitch copy() {
        return this;
    }

    @Override
    public Map<AbstractAction, Map<String, Object>> getDecisionStats() {
        return Collections.emptyMap();
    }
}
