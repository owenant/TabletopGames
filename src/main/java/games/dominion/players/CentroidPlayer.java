package games.dominion.players;

import games.dominion.DominionConstants.DeckType;
import games.dominion.DominionGameState;
import games.dominion.actions.BuyCard;
import games.dominion.actions.EndPhase;
import games.dominion.actions.TrashCard;
import games.dominion.actions.Witch;
import games.dominion.actions.MoveCard;
import games.dominion.cards.CardType;
import core.AbstractGameState;
import core.AbstractPlayer;
import core.actions.AbstractAction;
import utilities.Pair;
import java.util.Map;
import java.util.List;

public class CentroidPlayer extends AbstractPlayer {

    //centroid path gives for each round the target card amounts for each card type
    public List<Pair<CardType, Double>>[] centroidPath;

    public CentroidPlayer(String csv_file){
        //read data
        this.centroidPath = centroid;
    }

    public AbstractAction _getAction(AbstractGameState gameState, List<AbstractAction> possibleActions) {
        //Loop over all possible actions and find the one that is closest to the centroid for the current round
        double minDistance = Double.MAX_VALUE;
        AbstractAction bestAction = null;
        for (AbstractAction action : possibleActions) {
            double distance = 0;
            DominionGameState state = (DominionGameState) gameState;
            DominionGameState clone = (DominionGameState) state.clone();
            action.execute(clone);
            //get the new card amounts for the sum of hand, draw and discard
            Map<CardType, Double> newCardAmountsDiscard = clone.getDeck(DeckType.DISCARD).getCardTypeCount();
            Map<CardType, Double> newCardAmountsDraw = clone.getDeck(DeckType.DRAW).getCardTypeCount();
            Map<CardType, Double> newCardAmountsHand = clone.getDeck(DeckType.HAND).getCardTypeCount();
            

            
            //get the target card amounts
            Map<CardType, Double> targetCardAmounts = centroidPath[state.getRoundNumber()];
            //calculate the distance
            for (CardType cardType : CardType.values()) {
                distance += Math.pow(newCardAmounts.get(cardType) - targetCardAmounts.get(cardType), 2);
            }
            //update the best action
            if (distance < minDistance) {
                minDistance = distance;
                bestAction = action;
            }
        }


        return new EndPhase();
    }
}
