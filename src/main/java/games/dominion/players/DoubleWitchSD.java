package games.dominion.players;

import core.AbstractGameState;
import core.AbstractPlayer;
import core.actions.AbstractAction;
import games.dominion.DominionConstants.DeckType;
import games.dominion.DominionGameState;
import games.dominion.actions.BuyCard;
import games.dominion.actions.EndPhase;
import games.dominion.actions.TrashCard;
import games.dominion.actions.Witch;
import games.dominion.actions.MoveCard;
import games.dominion.cards.CardType;
import java.util.Collections;
import java.util.List;
import java.util.Map;

//Warning: this DoubleWitch player is designed for use with Size Distortion Kingdom cards and shouldnt
//be used with other Kingdom card selections
public class DoubleWitchSD extends AbstractPlayer {

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
            //possible actions in action phase for Double Witch in SD kingdom set, are play witch, do nothing,
            //or react to bureaucrat or bandit.
            for (AbstractAction action : possibleActions) {
                if (action instanceof MoveCard) {
                    //First we check to see if we need to deal with opponent playing Bureaucrat
                    //This is signaled in the case of the SD card set by having to move a victory
                    //card ot the draw pile
                    //note there maybe other possible actions corresponding to moving either a province,
                    //duchy or estate back ontop of the player's draw pile
                    //for a DW strategy it makes no difference which of these we choose, so for simplicity
                    //we just perform this first found move card action
                    return action;
                }else if (action instanceof TrashCard) {
                    //Next we check to see if we need to deal with opponent playing a Bandit card
                    //see if our top two cards are silver or gold and if so trash the cheapest
                    //then discard the rest of the card

                    //if action is to trash silver then go ahead as this is the least expensive option,
                    // if it is to trash gold first check to see if we can trash a silver first
                    TrashCard trashAction = (TrashCard) action;
                    if (trashAction.getTrashedCard() == CardType.SILVER) {
                        return action;
                    }else if (trashAction.getTrashedCard() == CardType.GOLD) {
                        //check for possibility to trash a silver first
                        for (AbstractAction actionCheckForSilver : possibleActions){
                            TrashCard trashActionCheckForSilver = (TrashCard) actionCheckForSilver;
                            if (trashActionCheckForSilver.getTrashedCard() == CardType.SILVER) {
                                return trashActionCheckForSilver;
                            }
                        }
                        //if no silver found, go ahead and trash gold
                        return action;
                    }
                } else if (action instanceof Witch) {
                    //in this case we can play a witch card
                    return action;
                }
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
            }
        }
        //if we get here we just need to do nothing
        return new EndPhase();
    }

    @Override
    public String toString() {
        return "DoubleWitch";
    }

    @Override
    public DoubleWitchSD copy() {
        return this;
    }

    @Override
    public Map<AbstractAction, Map<String, Object>> getDecisionStats() {
        return Collections.emptyMap();
    }
}
