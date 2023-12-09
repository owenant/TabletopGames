package games.dominion;

import core.AbstractParameters;
import core.Game;
import games.GameType;
import games.dominion.cards.CardType;

public class DominionFG1EParameters extends DominionParameters {
    public DominionFG1EParameters(long seed) {
        super(seed);
        cardsUsed.add(CardType.CELLAR);
        cardsUsed.add(CardType.MARKET);
        cardsUsed.add(CardType.WOODCUTTER);
        cardsUsed.add(CardType.MILITIA);
        cardsUsed.add(CardType.MINE);
        cardsUsed.add(CardType.MOAT);
        cardsUsed.add(CardType.REMODEL);
        cardsUsed.add(CardType.SMITHY);
        cardsUsed.add(CardType.VILLAGE);
        cardsUsed.add(CardType.WORKSHOP);
    }
}
