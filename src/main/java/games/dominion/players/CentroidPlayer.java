package games.dominion.players;

import games.dominion.DominionConstants.DeckType;
import games.dominion.DominionGameState;
import games.dominion.actions.BuyCard;
import games.dominion.actions.EndPhase;
import games.dominion.actions.TrashCard;
import games.dominion.actions.Witch;
import games.dominion.actions.MoveCard;
import games.dominion.cards.CardType;
import core.AbstractGameState;
import core.AbstractPlayer;
import core.actions.AbstractAction;
import utilities.Pair;
import utilities.JSONUtils;
import org.json.simple.JSONObject;

import java.util.Collections;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import utilities.JSONUtils;

public class CentroidPlayer extends AbstractPlayer {

    //centroid path gives for each round the target card amounts for each card type
    public ArrayList<Map<CardType, Double>> centroidPath;

    public CentroidPlayer(String centroidJsonFile){
        //read data
        JSONUtils jsonSupport = new JSONUtils();
        JSONObject centroidJSON = jsonSupport.loadJSONFile(centroidJsonFile);
        String centroidString = jsonSupport.readJSONFile(centroidJsonFile, null);

        //calculate maximum number of rounds
        Integer maxRounds = 0;
        for (Object key : centroidJSON.keySet()) {
            //split key into card type and round number
            String keyStr = (String) key;
            Object keyValue = centroidJSON.get(keyStr);
            int index = keyStr.lastIndexOf('R');
            Integer round_no = Integer.valueOf(keyStr.substring(index + 1, keyStr.length()));
            if (round_no > maxRounds) {
                maxRounds = round_no;
            }
        }

        //create array of Maps
        ArrayList<Map<CardType, Double>> centroidPathTmp = new ArrayList<Map<CardType, Double>>(maxRounds+1);
        //initialise empty maps for each round
        for (int round = 0; round <= maxRounds; round++){
            centroidPathTmp.add(round, new HashMap<CardType, Double>());
        }

        for (Object key : centroidJSON.keySet()) {
            //split key into card type and round number
            String keyStr = (String) key;
            Object keyValue = centroidJSON.get(keyStr);
            int index = keyStr.lastIndexOf('R');
            Integer round_no = Integer.valueOf(keyStr.substring(index + 1, keyStr.length()));
            String cardTypeStr = keyStr.substring(0, index-1);
            Double amount = (Double) keyValue;

            //extract existing map
            Map<CardType, Double> roundMap = centroidPathTmp.get(round_no);

            //add cardtype and amount
            CardType card = null;
            switch (cardTypeStr) {
                case "CURSE":
                    card = CardType.CURSE;
                    break;
                case "ESTATE":
                    card = CardType.ESTATE;
                    break;
                case "DUCHY":
                    card = CardType.DUCHY;
                    break;
                case "PROVINCE":
                    card = CardType.PROVINCE;
                    break;
                case "COPPER":
                    card = CardType.COPPER;
                    break;
                case "SILVER":
                    card = CardType.SILVER;
                    break;
                case "GOLD":
                    card = CardType.GOLD;
                    break;
                case "CELLAR":
                    card = CardType.CELLAR;
                    break;
                case "CHAPEL":
                    card = CardType.CHAPEL;
                    break;
                case "MOAT":
                    card = CardType.MOAT;
                    break;
                case "HARBINGER":
                    card = CardType.HARBINGER;
                    break;
                case "MERCHANT":
                    card = CardType.MERCHANT;
                    break;
                case "VASSAL":
                    card = CardType.VASSAL;
                    break;
                case "VILLAGE":
                    card = CardType.VILLAGE;
                    break;
                case "WORKSHOP":
                    card = CardType.WORKSHOP;
                    break;
                case "BUREAUCRAT":
                    card = CardType.BUREAUCRAT;
                    break;
                case "GARDENS":
                    card = CardType.GARDENS;
                    break;
                case "MILITIA":
                    card = CardType.MILITIA;
                    break;
                case "MONEYLENDER":
                    card = CardType.MONEYLENDER;
                    break;
                case "POACHER":
                    card = CardType.POACHER;
                    break;
                case "REMODEL":
                    card = CardType.REMODEL;
                    break;
                case "SMITHY":
                    card = CardType.SMITHY;
                    break;
                case "THRONE_ROOM":
                    card = CardType.THRONE_ROOM;
                    break;
                case "BANDIT":
                    card = CardType.BANDIT;
                    break;
                case "COUNCIL_ROOM":
                    card = CardType.COUNCIL_ROOM;
                    break;
                case "FESTIVAL":
                    card = CardType.FESTIVAL;
                    break;
                case "LABORATORY":
                    card = CardType.LABORATORY;
                    break;
                case "LIBRARY":
                    card = CardType.LIBRARY;
                    break;
                case "MARKET":
                    card = CardType.MARKET;
                    break;
                case "MINE":
                    card = CardType.MINE;
                    break;
                case "SENTRY":
                    card = CardType.SENTRY;
                    break;
                case "WITCH":
                    card = CardType.WITCH;
                    break;
                case "ARTISAN":
                    card = CardType.ARTISAN;
                    break;
                case "WOODCUTTER":
                    card = CardType.WOODCUTTER;
                    break;
            }

            roundMap.put(card, amount);
            //update array
            centroidPathTmp.set(round_no, roundMap);
        }

        this.centroidPath = centroidPathTmp;
    }

    public AbstractAction _getAction(AbstractGameState gameState, List<AbstractAction> possibleActions) {

        //Loop over all possible actions and find the one that is closest to the centroid for the current round
        double minDistance = Double.MAX_VALUE;
        AbstractAction bestAction = null;
        for (AbstractAction action : possibleActions) {
            double distance = 0;
            DominionGameState state = (DominionGameState) gameState;
            DominionGameState clone = (DominionGameState) state.clone();
            action.execute(clone);
            //get the new card amounts for the sum of hand, draw and discard
            Map<CardType, Double> newCardAmountsDiscard = clone.getDeck(DeckType.DISCARD).getCardTypeCount();
            Map<CardType, Double> newCardAmountsDraw = clone.getDeck(DeckType.DRAW).getCardTypeCount();
            Map<CardType, Double> newCardAmountsHand = clone.getDeck(DeckType.HAND).getCardTypeCount();

            //get the target card amounts
            Map<CardType, Double> targetCardAmounts = centroidPath[state.getRoundNumber()];
            //calculate the distance
            for (CardType cardType : CardType.values()) {
                distance += Math.pow(newCardAmounts.get(cardType) - targetCardAmounts.get(cardType), 2);
            }
            //update the best action
            if (distance < minDistance) {
                minDistance = distance;
                bestAction = action;
            }
        }

        return new EndPhase();
    }

    @Override
    public String toString() {
        return "BigMoneyWithGardens";
    }

    @Override
    public CentroidPlayer copy() {
        return this;
    }

    @Override
    public Map<AbstractAction, Map<String, Object>> getDecisionStats() {
        return Collections.emptyMap();
    }
}
