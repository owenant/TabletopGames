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
import games.battlecards.cards.BattleCardsBasicCard.CardType;
import games.loveletter.LoveLetterGameState;
import games.loveletter.LoveLetterParameters;
import games.loveletter.cards.LoveLetterCard;
import games.sushigo.SGGameState;
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
        Random rnd = new Random(bcgs.params.getRandomSeed());
        //initialise counters
        for(int p = 0; p < noPlayers; p++){
            bcgs.playerScore[p].setValue(0);
            bcgs.playerHealth[p].setValue(bcgs.params.MAX_HEALTH);
            bcgs.playerStamina[p].setValue(bcgs.params.MAX_STAMINA);
        }

        //create deck construction pile - using config from params object
        HashMap<BattleCardsBasicCard.CardType, Integer> counts = bcgs.params.initialCardCountsForConstructionPile;
        for (HashMap.Entry<BattleCardsBasicCard.CardType, Integer> entry : counts.entrySet()) {
            Integer noOfCards = entry.getValue();
            for (int i = 0; i < noOfCards; i++){
                BattleCardsBasicCard card = new BattleCardsBasicCard(entry.getKey());
                bcgs.deckConstructionPile.add(card);
            }
        }
        //give the construction pile a shuffle also
        bcgs.deckConstructionPile.shuffle(rnd);

        //create player draw piles
        counts = bcgs.params.initialCardCountsForPlayerDrawPiles;
        for (HashMap.Entry<BattleCardsBasicCard.CardType, Integer> entry : counts.entrySet()) {
            Integer noOfCards = entry.getValue();
            for(int p = 0; p < noPlayers; p++) {
                for (int i = 0; i < noOfCards; i++) {
                    BattleCardsBasicCard card = new BattleCardsBasicCard(entry.getKey());
                    bcgs.playerDrawPile.get(p).add(card);
                }
            }
        }

        //give all player draw piles a shuffle
        for(int p = 0; p < noPlayers; p++) {
            bcgs.playerDrawPile.get(p).shuffle(rnd);
        }
    }
    public void _startTurn(BattleCardsGameState bcgs) {
        //Draw new hands for players, need to manage the case when not enough cards are
        //in the player draw deck, and then the player discard deck is shuffled into
        //the player draw deck and then reset
        Random rnd = new Random(bcgs.params.getRandomSeed());
        for (int p = 0; p < bcgs.getNPlayers(); p++) {
            //first discard cards from hand into discard pile
            Deck<BattleCardsBasicCard> playerHand = bcgs.playerHandCards.get(p);
            for (int i = 0; i < playerHand.getSize(); i++) {
                bcgs.playerDiscardCards.get(p).add(playerHand.get(i));
            }
            playerHand.clear();

            //next draw cards from draw pile into hand
            for (int i = 0; i < bcgs.params.NO_CARDS_DRAWN_PER_TURN; i++){
                if (bcgs.playerDrawPile.get(p).getSize() == 0) {
                    //add discards to draw pile
                    for (int j = 0; j < bcgs.playerDiscardCards.get(p).getSize(); p++) {
                        bcgs.playerDrawPile.get(p).add(bcgs.playerDiscardCards.get(p).get(j));
                    }
                    //clear discard pile
                    bcgs.playerDiscardCards.get(p).clear();
                    //shuffle draw pile
                    bcgs.playerDrawPile.get(p).shuffle(rnd);
                }
                //draw card from draw pile into player hand
                bcgs.playerHandCards.get(p).add(bcgs.playerDrawPile.get(p).draw());
            }
        }
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
