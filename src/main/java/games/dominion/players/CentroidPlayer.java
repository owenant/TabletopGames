package games.dominion.players;

import games.dominion.DominionConstants.DeckType;
import games.dominion.DominionForwardModel;
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
import games.dominion.cards.DominionCard;
import core.components.Deck;
import players.PlayerFactory;
import players.mcts.MCTSPlayer;
import utilities.Pair;
import utilities.JSONUtils;
import org.json.simple.JSONObject;

import java.util.Collections;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import utilities.JSONUtils;
import players.PlayerFactory;
import players.mcts.MCTSPlayer;

public class CentroidPlayer extends AbstractPlayer {

    //centroid path gives for each round the target card amounts for each card type
    public ArrayList<Map<CardType, Double>> centroidPath;
    public MCTSPlayer mctsActionPlayer;
    public CentroidPlayer(String centroidJsonFile, String MCTSFileForActionPhase) {
        super(null, "CentroidPlayer");

        //read data
        JSONUtils jsonSupport = new JSONUtils();
        JSONObject centroidJSON = jsonSupport.loadJSONFile(centroidJsonFile);
        String centroidString = jsonSupport.readJSONFile(centroidJsonFile, null);

        //store a MCTS player to query in action phase
        mctsActionPlayer = (MCTSPlayer) PlayerFactory.createPlayer(MCTSFileForActionPhase);
        DominionForwardModel fwdModel = new DominionForwardModel();
        mctsActionPlayer.setForwardModel(fwdModel);

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
        ArrayList<Map<CardType, Double>> centroidPathTmp = new ArrayList<Map<CardType, Double>>(maxRounds + 1);
        //initialise empty maps for each round
        for (int round = 0; round <= maxRounds; round++) {
            centroidPathTmp.add(round, new HashMap<CardType, Double>());
        }

        for (Object key : centroidJSON.keySet()) {
            //split key into card type and round number
            String keyStr = (String) key;
            Object keyValue = centroidJSON.get(keyStr);
            int index = keyStr.lastIndexOf('R');
            Integer round_no = Integer.valueOf(keyStr.substring(index + 1, keyStr.length()));
            String cardTypeStr = keyStr.substring(0, index - 1);
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
        DominionGameState state = (DominionGameState) gameState;
        int player = state.getCurrentPlayer();
        int round_count = state.getRoundCounter();
        DominionGameState.DominionGamePhase phase = (DominionGameState.DominionGamePhase) state.getGamePhase();

        //use MCTS for action phase, then for buy phase use centroid if possible otherwise BMWG
        if (phase == DominionGameState.DominionGamePhase.Play){
            return this.mctsActionPlayer._getAction(state, possibleActions);
        }else{
            if(round_count < centroidPath.size()) {
                for (AbstractAction action : possibleActions) {
                    DominionGameState clone = (DominionGameState) state.copy();
                    action.execute(clone);

                    //calculate the distance
                    double distance = 0;
                    for (CardType cardType : CardType.values()) {
                        Integer currCardAmt = clone.cardsOfType(cardType, player, DeckType.ALL);
                        Double tgtCardAmt = 0.0;
                        if (centroidPath.get(state.getRoundCounter()).containsKey(cardType)) {
                            tgtCardAmt = centroidPath.get(state.getRoundCounter()).get(cardType);
                        }
                        distance += Math.pow(currCardAmt - tgtCardAmt, 2);
                    }
                    distance = Math.sqrt(distance);

                    //update the best action
                    if (distance < minDistance) {
                        minDistance = distance;
                        bestAction = action;
                    }
                }
                return bestAction;
            } else {
                //if centroid path isn't long enough use BMWG
                int noOfProvinceInSupply = state.cardsOfType(CardType.PROVINCE, -1, DeckType.SUPPLY);
                int noOfGardensInSupply = state.cardsOfType(CardType.GARDENS, -1, DeckType.SUPPLY);
                int noOfDuchyInSupply = state.cardsOfType(CardType.DUCHY, -1, DeckType.SUPPLY);
                int noOfEstateInSupply = state.cardsOfType(CardType.ESTATE, -1, DeckType.SUPPLY);
                int noOfGoldInSupply = state.cardsOfType(CardType.GOLD, -1, DeckType.SUPPLY);
                int noOfSilverInSupply = state.cardsOfType(CardType.SILVER, -1, DeckType.SUPPLY);

                int cash = state.availableSpend(player);
                if (gameState.getRoundCounter() > 15) {
                    if (cash >= 8 && (noOfProvinceInSupply > 0)) {
                        return new BuyCard(CardType.PROVINCE, player);
                    } else if (cash >= 4 && (noOfGardensInSupply > 0)) {
                        return new BuyCard(CardType.GARDENS, player);
                    } else if (cash >= 5 && (noOfDuchyInSupply > 0)) {
                        return new BuyCard(CardType.DUCHY, player);
                    } else if (cash >= 2 && (noOfEstateInSupply > 0)) {
                        return new BuyCard(CardType.ESTATE, player);
                    } else {
                        return new EndPhase(phase);
                    }
                } else {
                    if (cash >= 6 && (noOfGoldInSupply > 0)) {
                        return new BuyCard(CardType.GOLD, player);
                    } else if (cash >= 3 && (noOfSilverInSupply > 0)) {
                        return new BuyCard(CardType.SILVER, player);
                    }
                }

                //if we get here we just need to do nothing
                return new EndPhase(phase);
            }
        }
    }

    @Override
    public String toString() {
        return "CentroidPlayer";
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
