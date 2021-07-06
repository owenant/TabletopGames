package core.turnorders;

import core.AbstractGameState;
import core.CoreConstants;
import core.actions.AbstractAction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static utilities.Utils.GameResult.GAME_ONGOING;

/**
 * This implements a simple simultaneous turn order, for example in Diamant.
 *
 * The difference to AlternatingTurnOrder is that when a state is copied from the
 * perspective of a player (i.e. when they are about to make a decision), any decisions made, but not yet revealed, are
 * deleted.
 * This will then require the algorithm to make appropriate decisions about what the unrevealed actions are.
 *
 * This copying/deletion is performed in the GameState.copy(). This TurnOrder is a marker so that players
 * can take appropriate action. This is used for example in OSLA Player.
 * (It would be equally possible to add a marker interface to the Game...this feels slightly better as
 * simultaneous moves is
 */
public class SimultaneousTurnOrder extends TurnOrder {
    public Map<Integer, List<AbstractAction>> actionsThisTurn;
    public boolean allPlayed;

    public SimultaneousTurnOrder(int nPlayers) {
        super(nPlayers);
        actionsThisTurn = new HashMap<>();
        for (int i = 0; i < nPlayers; i++) {
            actionsThisTurn.put(i, new ArrayList<>());
        }
    }

    @Override
    public void endPlayerTurn(AbstractGameState gameState) {
        if (gameState.getGameStatus() != GAME_ONGOING) return;

        gameState.getPlayerTimer()[getCurrentPlayer(gameState)].incrementTurn();

        listeners.forEach(l -> l.onEvent(CoreConstants.GameEvents.TURN_OVER, gameState, null));

        turnCounter++;
        if (turnCounter >= nPlayers) {
            allPlayed = true; // All players played
        } else {
            moveToNextPlayer(gameState, nextPlayer(gameState));
        }
    }

    @Override
    public void endRound(AbstractGameState gameState) {
        super.endRound(gameState);
        allPlayed = false;
        clearActions();
    }

    public void clearActions() {
        for (int i = 0; i < nPlayers; i++) {
            actionsThisTurn.get(i).clear();
        }
        allPlayed = false;
        turnCounter = 0;
    }

    public void play (int player, AbstractAction aa) {
        actionsThisTurn.get(player).add(aa);
    }

    @Override
    protected void _reset() {
        // no additional actions required
    }

    @Override
    protected SimultaneousTurnOrder _copy() {
        SimultaneousTurnOrder copy = new SimultaneousTurnOrder(nPlayers);
        copy.allPlayed = allPlayed;
        copy.actionsThisTurn = new HashMap<>();
        for (int p : actionsThisTurn.keySet()) {
            copy.actionsThisTurn.put(p, new ArrayList<>());
            for (AbstractAction a: actionsThisTurn.get(p)) {
                copy.actionsThisTurn.get(p).add(a.copy());
            }
        }
        return copy;
    }

    public boolean playerPlayed(int p) {
        return actionsThisTurn.get(p).size() > 0;
    }
}
