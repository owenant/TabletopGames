package games.battlecards;

import core.AbstractGameState;
import core.AbstractParameters;
import games.battlecards.cards.BattleCardsBasicCard;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * <p>This class should hold a series of variables representing game parameters (e.g. number of cards dealt to players,
 * maximum number of rounds in the game etc.). These parameters should be used everywhere in the code instead of
 * local variables or hard-coded numbers, by accessing these parameters from the game state via {@link AbstractGameState#getGameParameters()}.</p>
 *
 * <p>It should then implement appropriate {@link #_copy()}, {@link #_equals(Object)} and {@link #hashCode()} functions.</p>
 *
 * <p>The class can optionally extend from {@link evaluation.TunableParameters} instead, which allows to use
 * automatic game parameter optimisation tools in the framework.</p>
 */
public class BattleCardsParameters extends AbstractParameters {

    /*
    This is modelled off cant stop example,
     */

    public int INITIAL_PLAYER_DECK_SIZE = 10;
    public int NO_CARDS_DRAWN_PER_TURN = 3;
    public int NO_CARDS_DRAWN_FROM_CONSTRUCTION_DECK = 5;
    public int NO_CARDS_KEPT_FROM_CONSTRUCTION_DECK_DRAW = 2;
    public int MAX_HEALTH = 10;
    public int MAX_STAMINA = 10;
    public int NO_PTS_FOR_KO = 2;
    public int NO_PTS_FOR_HAVING_MOST_HEALTH_AT_ROUND_END = 2;
    public int NO_PTS_TO_WIN = 16;

    // Occurrence counts for each card in the initial construction deck pile
    public HashMap<BattleCardsBasicCard.CardType, Integer> initialCardCountsForConstructionPile = new HashMap<BattleCardsBasicCard.CardType, Integer>() {{
        put(BattleCardsBasicCard.CardType.LightAttack, 10);
        put(BattleCardsBasicCard.CardType.MediumAttack, 10);
        put(BattleCardsBasicCard.CardType.StrongAttack, 10);
        put(BattleCardsBasicCard.CardType.Bulldoze, 10);
        put(BattleCardsBasicCard.CardType.Block, 10);
    }};

    // Occurrence counts for each card in the starting draw pile for each player
    public HashMap<BattleCardsBasicCard.CardType, Integer> initialCardCountsForPlayerDrawPiles = new HashMap<BattleCardsBasicCard.CardType, Integer>() {{
        put(BattleCardsBasicCard.CardType.LightAttack, 2);
        put(BattleCardsBasicCard.CardType.MediumAttack, 2);
        put(BattleCardsBasicCard.CardType.StrongAttack, 2);
        put(BattleCardsBasicCard.CardType.Bulldoze, 2);
        put(BattleCardsBasicCard.CardType.Block, 2);
    }};

    public BattleCardsParameters(long seed) {
        super(seed);
    }

    @Override
    protected AbstractParameters _copy() {
        // TODO: deep copy of all variables.
        return this;
    }

    @Override
    protected boolean _equals(Object o) {
        // TODO: compare all variables.
        return o instanceof BattleCardsParameters;
    }

    @Override
    public int hashCode() {
        // TODO: include the hashcode of all variables.
        return super.hashCode();
    }
}
