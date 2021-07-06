package games.diamant.gui;

import core.actions.DoNothing;
import games.diamant.actions.ContinueInCave;
import utilities.ImageIO;
import core.actions.AbstractAction;

import javax.swing.*;
import java.awt.*;

import static games.diamant.gui.DiamantGUI.cardHeight;
import static games.diamant.gui.DiamantGUI.cardWidth;

public class DiamantDecisionView extends JComponent {

    AbstractAction playerDecision;
    // Back of card image
    Image backOfCard;
    // Path to assets
    String dataPath;
    // Player in cave?
    boolean playerInCave;

    public DiamantDecisionView(String dataPath) {
        backOfCard = ImageIO.GetInstance().getImage(dataPath + "cardback.png");
        this.dataPath = dataPath;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Image cardFace;
        if (playerDecision != null) {
            if (playerDecision instanceof ContinueInCave) {
                if (playerInCave) {
                    cardFace = ImageIO.GetInstance().getImage(dataPath + "continue.png");
                } else {
                    cardFace = backOfCard;
                }
            } else {
                cardFace = ImageIO.GetInstance().getImage(dataPath + "exit.png");
            }
        } else {
            cardFace = backOfCard;
        }
        g.drawImage(cardFace, 0, 0, cardWidth, cardHeight, null, null);
    }

    public void updateDecision(AbstractAction aa, boolean playerInCave) {
        this.playerDecision = aa;
        this.playerInCave = playerInCave;
        this.repaint();
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(cardWidth, cardHeight);
    }
}
