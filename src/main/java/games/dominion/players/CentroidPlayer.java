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
        
        return new EndPhase();
    }
}
