package games.dominion.players;

import core.AbstractGameState;
import core.AbstractPlayer;
import core.actions.AbstractAction;
import games.dominion.DominionGameState;
import games.dominion.actions.BuyCard;
import games.dominion.actions.EndPhase;
import games.dominion.cards.CardType;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class BigMoney extends AbstractPlayer {

    public BigMoney() {
        super(null, "BigMoney");
    }

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

        //check that agent is in the buy phase
        if(gameState.getGamePhase() != DominionGameState.DominionGamePhase.Buy)
        {
            //TODO: check to see if all actions are discard and then throw away
            //the cheapest card
            return possibleActions.get(0);
        }else{
            int cash = state.availableSpend(player);
            int provinces = state.getCardsIncludedInGame().getOrDefault(CardType.PROVINCE, 0);

            switch (cash) {
                case 0:
                case 1:
                    return new EndPhase();
                case 2:
                    if (provinces < 4 && possibleActions.contains(new BuyCard(CardType.ESTATE, player)))
                        return new BuyCard(CardType.ESTATE, player);
                    return new EndPhase();
                case 3:
                case 4:
                    return new BuyCard(CardType.SILVER, player);
                case 5:
                    if (provinces < 6 && possibleActions.contains(new BuyCard(CardType.DUCHY, player)))
                        return new BuyCard(CardType.DUCHY, player);
                    else
                        return new BuyCard(CardType.SILVER, player);
                case 6:
                case 7:
                    return new BuyCard(CardType.GOLD, player);
                default:
                    return new BuyCard(CardType.PROVINCE, player);

            }
        }
    }

    @Override
    public String toString() {
        return "BigMoney";
    }

    @Override
    public BigMoney copy() {
        return this;
    }

    @Override
    public Map<AbstractAction, Map<String, Object>> getDecisionStats() {
        return Collections.emptyMap();
    }
}
