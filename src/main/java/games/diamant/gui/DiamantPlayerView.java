package games.diamant.gui;

import games.diamant.DiamantGameState;
import games.diamant.DiamantParameters;
import games.diamant.DiamantTurnOrder;

import javax.swing.*;
import java.awt.*;

import static core.CoreConstants.ALWAYS_DISPLAY_CURRENT_PLAYER;
import static core.CoreConstants.ALWAYS_DISPLAY_FULL_OBSERVABLE;
import static games.diamant.gui.DiamantGUI.*;

public class DiamantPlayerView extends JPanel {

    // ID of player
    int playerId;
    // ID of human looking
    int humanId;
    // Number of gems in hand, total number of gems
    JLabel nGemsInHand, nGemsTotal;
    // Player decision
    DiamantDecisionView playerDecision;

    // Border offsets
    int border = 5;
    int borderBottom = 25;
    int buffer = 10;
    int width, height;

    public DiamantPlayerView(int playerId, int humanId, String dataPath) {
        JLabel label1 = new JLabel("Player decision:");
        nGemsInHand = new JLabel("# Gems in hand: 0");
        nGemsTotal = new JLabel("# Total gems: 0");
        playerDecision = new DiamantDecisionView(dataPath);
        JPanel wrap = new JPanel();
        wrap.setLayout(new BoxLayout(wrap, BoxLayout.Y_AXIS));
        wrap.setOpaque(false);
        wrap.add(label1);
        wrap.add(playerDecision);
        JPanel wrap2 = new JPanel();
        wrap2.setLayout(new BoxLayout(wrap2, BoxLayout.Y_AXIS));
        wrap2.setOpaque(false);
        wrap2.add(nGemsInHand);
        wrap2.add(nGemsTotal);

        this.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
        this.add(wrap);
        this.add(wrap2);

        this.width = playerAreaWidth + border * 2;
        this.height = playerAreaHeight + border + borderBottom*2;
        this.humanId = humanId;
        this.playerId = playerId;
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(width, height);
    }

    /**
     * Updates information
     *
     * @param gameState - current game state
     */
    public void update(DiamantGameState gameState) {
        DiamantParameters params = (DiamantParameters) gameState.getGameParameters();
        DiamantTurnOrder to = (DiamantTurnOrder) gameState.getTurnOrder();


        if ((playerId == gameState.getCurrentPlayer() && ALWAYS_DISPLAY_CURRENT_PLAYER
                || playerId == humanId
                || ALWAYS_DISPLAY_FULL_OBSERVABLE
                || to.allPlayed) && to.actionsThisTurn.get(playerId).size() > 0) {
            playerDecision.updateDecision(to.actionsThisTurn.get(playerId).get(0), gameState.isPlayerInCave(playerId));
        } else {
            playerDecision.updateDecision(null, gameState.isPlayerInCave(playerId));
        }
        nGemsInHand.setText("# Gems in hand: " + gameState.getGemsInHand().get(playerId).getValue());
        nGemsTotal.setText("# Total gems: " + gameState.getTreasureChests().get(playerId).getValue());
    }
}
