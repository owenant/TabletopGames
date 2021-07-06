package games.diamant.gui;

import core.components.Deck;
import games.diamant.cards.DiamantCard;
import gui.views.DeckView;
import utilities.ImageIO;

import java.awt.*;

import static games.diamant.gui.DiamantGUI.cardHeight;
import static games.diamant.gui.DiamantGUI.cardWidth;

public class DiamantDeckView extends DeckView<DiamantCard> {

    // Path to assets
    String dataPath;

    /**
     * Constructor initialising information and adding key/mouse listener for card highlight (left click or ALT + hover
     * allows showing the highlighted card on top of all others).
     * @param d - deck to draw
     * @param visible - true if whole deck visible
     * @param dataPath - path to assets
     * @param rect - the location of the Deck
     */
    public DiamantDeckView(int human, Deck<DiamantCard> d, boolean visible, String dataPath, Rectangle rect) {
        super(human, d, visible, cardWidth, cardHeight, rect);
        this.dataPath = dataPath;
    }

    /**
     * Draws the specified component at the specified place
     *
     * @param g         Graphics object
     * @param rect      Where the item is to be drawn
     * @param card The item itself
     * @param front     true if the item is visible (e.g. the card details); false if only the card-back
     */
    @Override
    public void drawComponent(Graphics2D g, Rectangle rect, DiamantCard card, boolean front) {
        drawCard(g, rect.x, rect.y, rect.width, rect.height, card);
    }

    public void drawCard(Graphics2D g, int x, int y, int width, int height, DiamantCard card) {
        Image cardFace;
        if (card.getCardType() == DiamantCard.DiamantCardType.Hazard) {
            cardFace = ImageIO.GetInstance().getImage(dataPath + card.getHazardType().name().toLowerCase() + ".png");
            g.drawImage(cardFace, x, y, width, height, null, null);
        }
        else {
            cardFace = ImageIO.GetInstance().getImage(dataPath + "treasure.png");
            g.drawImage(cardFace, x, y, width, height, null, null);

            // Draw number of gems
            Font f = g.getFont();
            g.setFont(new Font(f.getName(), Font.BOLD, 18));
            g.setColor(Color.orange);
            String value = ""+card.getNumberOfGems();
            g.drawString(value, x + cardWidth/2 - 10, y + 25);
            g.setFont(f);
        }
    }

}
