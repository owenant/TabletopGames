package games.battlecards;

import core.AbstractGameState;
import core.CoreConstants.GameResult;
import core.CoreConstants.VisibilityMode;
import core.StandardForwardModel;
import core.actions.AbstractAction;
import core.components.Counter;
import core.components.Deck;
import core.components.PartialObservableDeck;
import games.GameType;
import games.battlecards.BattleCardsGameState;
import games.battlecards.cards.BattleCardsBasicCard;
import games.loveletter.LoveLetterGameState;
import games.loveletter.LoveLetterParameters;
import games.loveletter.cards.LoveLetterCard;
import gametemplate.actions.GTAction;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.Set;

/**
 * <p>The forward model contains all the game rules and logic. It is mainly responsible for declaring rules for:</p>
 * <ol>
 *     <li>Game setup</li>
 *     <li>Actions available to players in a given game state</li>
 *     <li>Game events or rules applied after a player's action</li>
 *     <li>Game end</li>
 * </ol>
 */
public class BattleCardsForwardModel extends StandardForwardModel {

    /**
     * Initializes all variables in the given game state. Performs initial game setup according to game rules, e.g.:
     * <ul>
     *     <li>Sets up decks of cards and shuffles them</li>
     *     <li>Gives player cards</li>
     *     <li>Places tokens on boards</li>
     *     <li>...</li>
     * </ul>
     *
     * @param firstState - the state to be modified to the initial game state.
     */
    @Override
    protected void _setup(AbstractGameState firstState) {
        BattleCardsGameState bcgs = (BattleCardsGameState) firstState;
        int noPlayers = bcgs.getNPlayers();
        // perform initialization of variables
        bcgs.playerHandCards = new ArrayList<>(noPlayers);
        bcgs.playerDiscardCards = new ArrayList<>(noPlayers);
        bcgs.playerDrawPile = new ArrayList<>(noPlayers);
        bcgs.deckConstructionPile = new Deck<>("Deck Construction Pile", VisibilityMode.HIDDEN_TO_ALL);
        //Indicates the card chosen by the players for this turn, saved for simultaneous execution
        bcgs.playedCards = new Deck<>("Played Cards", VisibilityMode.VISIBLE_TO_ALL);
        bcgs.playerScore = new Counter[noPlayers];
        bcgs.playerHealth = new Counter[noPlayers];
        bcgs.playerStamina = new Counter[noPlayers];
        bcgs.playerTarget = new Integer[noPlayers];

        //initialise counters
        for(int p = 0; p < noPlayers; p++){
            bcgs.playerScore[p].setValue(0);
            bcgs.playerHealth[p].setValue(bcgs.params.MAX_HEALTH);
            bcgs.playerStamina[p].setValue(bcgs.params.MAX_STAMINA);
        }

        //create deck construction pile


        //create player draw piles
    }

    /**
     * Calculates the list of currently available actions, possibly depending on the game phase.
     * @return - List of AbstractAction objects.
     */
    @Override
    protected List<AbstractAction> _computeAvailableActions(AbstractGameState gameState) {
        List<AbstractAction> actions = new ArrayList<>();
        // TODO: create action classes for the current player in the given game state and add them to the list. Below just an example that does nothing, remove.
        actions.add(new GTAction());
        return actions;
    }
}
