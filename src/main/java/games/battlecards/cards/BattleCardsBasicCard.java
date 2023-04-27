package games.battlecards.cards;

import core.components.Card;

public class BattleCardsBasicCard extends Card {

    // each attack card consists of a type, a damage and shield value, number of players targeted and a stamina cost. The card type defines the actions available to the player and the effect of the card
    public enum CardType {
        LightAttack(1, 0, 0, "Light attack card."),
        MediumAttack(2, 0, 1,  "Medium attack card."),
        StrongAttack(3, 0, 2,  "Strong attack card."),
        Bulldoze(1, 0, 2, "Hits all other opponents."),
        Block(0, 3, 1, "Block");
        private final int damage;
        private final int shield;
        private final int stamina;
        private final String cardText;
        CardType(int damage, int shield, int stamina, String text){
            this.damage = damage;
            this.shield = shield;
            this.stamina = stamina;
            this.cardText = text;
        }

        public int getDamage(){ return damage;}
        public int getShield(){ return shield;}
        public int getStamina(){ return stamina;}
        public String getCardText() {
            return this.name() + ": " + cardText;
        }
    }

    public final CardType cardType;

    public BattleCardsBasicCard(CardType cardType) {
        super(cardType.toString());
        this.cardType = cardType;
    }

    public BattleCardsBasicCard(CardType cardType, int componentID) {
        super(cardType.toString(), componentID);
        this.cardType = cardType;
    }

    public String toString(){
        return cardType.toString();
    }

    @Override
    public BattleCardsBasicCard copy() {
        return new BattleCardsBasicCard(cardType, componentID);
    }
}
