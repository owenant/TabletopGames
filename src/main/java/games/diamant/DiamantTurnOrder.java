package games.diamant;
import core.actions.AbstractAction;
import core.turnorders.SimultaneousTurnOrder;

import java.util.ArrayList;
import java.util.HashMap;

import static utilities.Utils.GameResult.GAME_END;

public class DiamantTurnOrder extends SimultaneousTurnOrder {
    int caveCounter;
    int maxCaves;

    public DiamantTurnOrder(int nPlayers, int maxCaves) {
        super(nPlayers);
        caveCounter = 1;
        this.maxCaves = maxCaves;
    }

    public void endCave(DiamantGameState gameState) {
        caveCounter++;
        if (caveCounter > maxCaves) {
            gameState.setGameStatus(GAME_END);
        }
    }

    @Override
    protected DiamantTurnOrder _copy() {
        DiamantTurnOrder copy = new DiamantTurnOrder(nPlayers, maxCaves);
        copy.allPlayed = allPlayed;
        copy.actionsThisTurn = new HashMap<>();
        for (int p : actionsThisTurn.keySet()) {
            copy.actionsThisTurn.put(p, new ArrayList<>());
            for (AbstractAction a: actionsThisTurn.get(p)) {
                copy.actionsThisTurn.get(p).add(a.copy());
            }
        }
        copy.caveCounter = caveCounter;
        return copy;
    }

    public int getCaveCounter() {
        return caveCounter;
    }

    public int getMaxCaves() {
        return maxCaves;
    }
}
