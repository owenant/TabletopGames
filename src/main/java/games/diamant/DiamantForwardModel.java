package games.diamant;

import core.AbstractForwardModel;
import core.AbstractGameState;
import core.actions.AbstractAction;
import core.components.Counter;
import core.components.Deck;
import games.diamant.actions.ContinueInCave;
import games.diamant.actions.ExitFromCave;
import games.diamant.cards.DiamantCard;
import utilities.Utils;

import java.util.*;

import static core.CoreConstants.VisibilityMode.HIDDEN_TO_ALL;
import static core.CoreConstants.VisibilityMode.VISIBLE_TO_ALL;

public class DiamantForwardModel extends AbstractForwardModel {
    @Override
    protected void _setup(AbstractGameState firstState) {
        DiamantGameState dgs = (DiamantGameState) firstState;
        Random r = new Random(dgs.getGameParameters().getRandomSeed());

        dgs.gemsInHand = new ArrayList<>();
        dgs.treasureChests = new ArrayList<>();

        dgs.nGemsOnPath = new Counter(0, 0, 1000, "Gems on path");
        dgs.nHazardsOnPath = new HashMap<>();
        for (DiamantCard.HazardType ht: DiamantCard.HazardType.values()) {
            if (ht == DiamantCard.HazardType.None) continue;
            dgs.nHazardsOnPath.put(ht, new Counter(0, 0, ((DiamantParameters)dgs.getGameParameters()).nHazardsToDead, ht.name()));
        }

        for (int i = 0; i < dgs.getNPlayers(); i++)
        {
            String counter_hand_name  = "CounterHand" + i;
            String counter_chest_name = "CounterChest" + i;
            dgs.gemsInHand.add(new Counter(0,0,1000, counter_hand_name));
            dgs.treasureChests.add(new Counter(0,0,1000, counter_chest_name));
            dgs.playerInCave.add(true);
        }

        dgs.mainDeck    = new Deck("MainDeck", HIDDEN_TO_ALL);
        dgs.discardDeck = new Deck("DiscardDeck", VISIBLE_TO_ALL);
        dgs.path        = new Deck("Path", VISIBLE_TO_ALL);

        createCards(dgs);
        dgs.mainDeck.shuffle(r);

        // Draw first card and play it
        drawPathCard(dgs);

        dgs.getTurnOrder().setStartingPlayer(0);
    }

    /**
     * Create all the cards and include them into the main deck.
     * @param dgs - current game state.
     */
    private void createCards(DiamantGameState dgs) {
        DiamantParameters dp = (DiamantParameters) dgs.getGameParameters();

        // 3 of each hazard
        // 15 treasures :1,2,3,4,5,7,9,10,11,12,13,14,15,16,17

        // Add artifacts
        //for (int i=0; i< dp.nArtifactCards; i++)
        //    dgs.mainDeck.add(new DiamantCard(DiamantCard.DiamantCardType.Artifact, DiamantCard.HazardType.None, 0));

        // Add hazards
        for (int i=0; i< dp.nHazardCardsPerType; i++)
        {
            for (DiamantCard.HazardType h : DiamantCard.HazardType.values())
                if (h != DiamantCard.HazardType.None)
                    dgs.mainDeck.add(new DiamantCard(DiamantCard.DiamantCardType.Hazard, h, 0));
        }

        // Add treasures
        for (int t : dp.treasures)
            dgs.mainDeck.add(new DiamantCard(DiamantCard.DiamantCardType.Treasure, DiamantCard.HazardType.None, t));
    }

    /**
     * In this game, all players play the action at the same time.
     * When an agent call next, the action is just stored in the gameState.
     * @param currentState: current state of the game
     * @param action: action to be executed
    */
    @Override
    protected void _next(AbstractGameState currentState, AbstractAction action)
    {
        DiamantGameState dgs = (DiamantGameState) currentState;
        action.execute(dgs);
        DiamantTurnOrder to = (DiamantTurnOrder) dgs.getTurnOrder();

        // If all players have an action, execute them
        if (dgs.getActionsInProgress().size() == 0 && to.allPlayed) {
            HashSet<Integer> exitingPlayers = distributeGemsExitingPlayers(dgs);
            to.endRound(dgs);
            advanceCave(dgs, exitingPlayers);
        }
    }

    private void advanceCave(DiamantGameState dgs, HashSet<Integer> playersExit)
    {
        if (playersExit.size() == dgs.getNPlayersInCave()) {
            // All active players left the cave, set up a new one
            dgs.nGemsOnPath.setValue(0);
            prepareNewCave(dgs);
        }
        else {
            // Still players in cave, add new card
            for (int p : playersExit) {
                dgs.playerInCave.set(p, false);
            }
            drawPathCard(dgs);
        }
    }

    public HashSet<Integer> distributeGemsExitingPlayers(DiamantGameState dgs) {
        DiamantTurnOrder to = (DiamantTurnOrder) dgs.getTurnOrder();

        // How many players play ExitFromCave?
        HashSet<Integer> playersExit = new HashSet<>();
        for (int p : to.actionsThisTurn.keySet()) {
            if (to.actionsThisTurn.get(p).size() > 0 && to.actionsThisTurn.get(p).get(0) instanceof ExitFromCave) {
                playersExit.add(p);
            }
        }

        if (playersExit.size() > 0) {
            // Distribute gems for players leaving
            distributeGemsAmongPlayers(dgs, playersExit);
        }

        return playersExit;
    }

    private void distributeGemsAmongPlayers(DiamantGameState dgs, HashSet<Integer> playersExit)
    {
        int gemsToPlayers;
        if (playersExit.size() == 1) {
            gemsToPlayers = dgs.nGemsOnPath.getValue();
            dgs.nGemsOnPath.setValue(0);
        }
        else {
            gemsToPlayers = (int) Math.floor(dgs.nGemsOnPath.getValue() / (double) playersExit.size());
            dgs.nGemsOnPath.setValue(dgs.nGemsOnPath.getValue() % playersExit.size());
        }

        for (int p: playersExit) {
            dgs.gemsInHand.get(p).increment(gemsToPlayers);                             // increment hand gems
            dgs.treasureChests.get(p).increment(dgs.gemsInHand.get(p).getValue());   // hand gems to chest
            dgs.gemsInHand.get(p).setValue(0);                                 // hand gems <- 0
        }
    }

    /**
     * Prepare the game for playing a new Cave
      * @param dgs: current game state
     */
    private void prepareNewCave(DiamantGameState dgs)
    {
        DiamantParameters dp = (DiamantParameters) dgs.getGameParameters();

        ((DiamantTurnOrder)dgs.getTurnOrder()).endCave(dgs);

        // No more caves ?
        if (dgs.getGameStatus() == Utils.GameResult.GAME_END)
            calculateEndGame(dgs);
        else {
            Random r = new Random(dgs.getGameParameters().getRandomSeed());

            // Move path cards to maindeck and shuffle
            dgs.mainDeck.add(dgs.path);
            dgs.path.clear();
            dgs.mainDeck.shuffle(r);

            // Initialize game state
            for (DiamantCard.HazardType ht: DiamantCard.HazardType.values()) {
                if (ht == DiamantCard.HazardType.None) continue;
                if (dgs.nHazardsOnPath.containsKey(ht)) {
                    dgs.nHazardsOnPath.get(ht).setValue(0);
                } else {
                    dgs.nHazardsOnPath.put(ht, new Counter(0, 0, dp.nHazardsToDead, ht.name()));
                }
            }

            // All the players will participate in next cave
            for (int p=0; p < dgs.getNPlayers(); p++) {
                dgs.playerInCave.set(p, true);
            }

            drawPathCard(dgs);
        }
    }

    /**
     * Finishes the game and obtains who is the winner
     * @param dgs: current game state
     */
    private void calculateEndGame(DiamantGameState dgs)
    {
        int maxGems = 0;
        List<Integer> bestPlayers = new ArrayList<>();

        for (int p=0; p < dgs.getNPlayers(); p++)
        {
            int nGems = dgs.treasureChests.get(p).getValue();
            if (nGems > maxGems)
            {
                bestPlayers.clear();
                bestPlayers.add(p);
                maxGems = nGems;
            }
            else if (nGems == maxGems)
            {
                bestPlayers.add(p);
            }
        }

        boolean moreThanOneWinner = bestPlayers.size() > 1;

        for (int p=0; p < dgs.getNPlayers(); p++)
        {
            if (bestPlayers.contains(p)) {
                if (moreThanOneWinner)
                    dgs.setPlayerResult(Utils.GameResult.DRAW, p);
                else
                    dgs.setPlayerResult(Utils.GameResult.WIN, p);
            }
            else
                dgs.setPlayerResult(Utils.GameResult.LOSE, p);
        }

        dgs.setGameStatus(Utils.GameResult.GAME_END);
    }


    /**
     * Gets the possible actions to be played
     * If the player is not in the cave, only OutOfCave action can be played
     * If the player is in the cave, there are only two actions: ExitFromCave, ContinueInCave
     * @param gameState: current game state
     */
    @Override
    protected List<AbstractAction> _computeAvailableActions(AbstractGameState gameState)
    {
        DiamantGameState dgs = (DiamantGameState) gameState;
        ArrayList<AbstractAction> actions = new ArrayList<>();
        actions.add(new ContinueInCave());

        // If the player is still in the cave, they can leave it
        if (dgs.playerInCave.get(gameState.getCurrentPlayer())) {
            actions.add(new ExitFromCave());
        }

        return actions;
    }

    @Override
    protected AbstractForwardModel _copy()
    {
        return new DiamantForwardModel();
    }

    /**
     * Play the card
     * @param dgs: current game state
     */
    private void drawPathCard(DiamantGameState dgs)
    {
        DiamantCard card = dgs.mainDeck.draw();
        dgs.path.add(card);

        if (card.getCardType() == DiamantCard.DiamantCardType.Treasure) {
            int gemsToPlayers = (int) Math.floor(card.getNumberOfGems() / (double) dgs.getNPlayersInCave());
            int gemsToPath    = card.getNumberOfGems() % dgs.getNPlayersInCave();

            for (int p=0; p<dgs.getNPlayers(); p++)
                if (dgs.playerInCave.get(p))
                    dgs.gemsInHand.get(p).increment(gemsToPlayers);

            dgs.nGemsOnPath.increment(gemsToPath);
        }
        else if (card.getCardType() == DiamantCard.DiamantCardType.Hazard)
        {
            dgs.nHazardsOnPath.get(card.getHazardType()).increment(1);

            DiamantParameters dp = (DiamantParameters) dgs.getGameParameters();
            // If there are two hazards cards of the same type -> finish the cave
            if (dgs.nHazardsOnPath.get(card.getHazardType()).isMaximum())
            {
                // All active players loose all gems on hand.
                for (int p=0; p<dgs.getNPlayers(); p++)
                {
                    if (dgs.playerInCave.get(p))
                        dgs.gemsInHand.get(p).setValue(0);
                }
                // Gems on Path are also loosed
                dgs.nGemsOnPath.setValue(0);

                // Remove last card (it is the hazard one) from path and add to discardDeck
                dgs.path.draw();
                dgs.discardDeck.add(card);

                // Start new cave
                prepareNewCave(dgs);
            }
        }
    }
}
