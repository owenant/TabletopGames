package games.dominion.metrics;

import static games.GameType.Dominion;
import static utilities.Utils.getArg;

import core.AbstractGameState;
import core.AbstractParameters;
import core.AbstractPlayer;
import core.Game;
import core.actions.AbstractAction;
import core.components.Deck;
import core.CoreConstants;
import core.components.PartialObservableDeck;
import evaluation.listeners.IGameListener;
import evaluation.tournaments.RoundRobinTournament;
import evaluation.tournaments.AbstractTournament.TournamentMode;
import games.GameType;
import games.dominion.DominionConstants;
import games.dominion.DominionFGParameters;
import games.dominion.DominionForwardModel;
import games.dominion.DominionGameState;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import games.dominion.cards.CardType;
import games.dominion.cards.DominionCard;
import games.dominion.DominionConstants.DeckType;
import players.PlayerFactory;
import players.mcts.BasicMCTSPlayer;
import players.mcts.MCTSPlayer;
import players.rmhc.RMHCPlayer;
import players.simple.OSLAPlayer;
import players.simple.RandomPlayer;

//entry point for metric calculations for DBCGs. Currently focused on Dominion
//note tests for dominion might help in building functionality in here

public class MetricsForDBCGs {
  public static void main(String[] args) {
      System.out.println("Entry point to metrics for DBCGs....");
      //runTournament();
      runMaxPayoffDeckSearch();
  }

  public static void runMaxPayoffDeckSearch(){
      System.out.println("Search for optimal decks with different cost amounts....");
      List<AbstractPlayer> players = Arrays.asList(new MCTSPlayer(), new MCTSPlayer());
      DominionFGParameters params = new DominionFGParameters(System.currentTimeMillis());
      DominionGameState state = new DominionGameState(params, players.size());
      DominionForwardModel fm = new DominionForwardModel();
      //note creating a game initialises the state
      Game domGame = new Game(GameType.Dominion, players, fm, state);

      //focus player is the player whose deck we will be evolving
      int focusPlayer = state.getCurrentPlayer();

      //set-total cost amount
      int totalCost = 30;

      //create an initial deck that conforms to total cost amount - we do this by starting with a deck of coppers
      //boolean[] visibilityPerPlayer = new boolean[2];
      //for(int i =0; i < players.size(); i++){
      //    visibilityPerPlayer[i] = false;
      //}
      //PartialObservableDeck<DominionCard> draw = new PartialObservableDeck<DominionCard>("player_draw", playerID, CoreConstants.VisibilityMode.HIDDEN_TO_ALL);
      //PartialObservableDeck<DominionCard> hand = new PartialObservableDeck<DominionCard>("player_hand", playerID, CoreConstants.VisibilityMode.VISIBLE_TO_OWNER);
      //Deck<DominionCard> discard = new Deck<DominionCard>("player_discard", playerID, CoreConstants.VisibilityMode.VISIBLE_TO_ALL);
      boolean[] visibleToPlayer1 = {true, false};
      boolean[] notVisibleToEitherPlayer = {false, false};
      PartialObservableDeck<DominionCard> draw = new PartialObservableDeck<DominionCard>("player_draw", focusPlayer, notVisibleToEitherPlayer);
      PartialObservableDeck<DominionCard> hand = new PartialObservableDeck<DominionCard>("player_hand", focusPlayer, visibleToPlayer1);
      //Deck<DominionCard> discard = new Deck<DominionCard>("player_discard", activePlayer, notVisibleToEitherPlayer);
      for(int i =0; i < totalCost; i++) {
          draw.add(DominionCard.create(CardType.COPPER),notVisibleToEitherPlayer);
      }
      //issue by setting the deck in the state some how we cause problems with the state.copy() function
      //looks like we are not setting some parameters around the decks correctly. We need to set deckVisibility in addition to visibility mode?
      state.setDeck(DeckType.DRAW,focusPlayer,draw);
      state.setDeck(DominionConstants.DeckType.HAND,focusPlayer,hand);
      //state.setDeck(DominionConstants.DeckType.DISCARD,playerID,discard);

      //start by drawing cards into hand
      for (int i = 0; i < params.HAND_SIZE; i++)
          state.drawCard(focusPlayer);

      //store a copy of this initial starting state (arrghhh...no copy constructor is available)
      DominionGameState initialState = (DominionGameState) state.copy();

      int noMoves = 60;
      int noObsOfPayOffs = 0;
      double expectedPayout = 0;
      for (int i=0 ; i< noMoves; i++) {
          //observe current state
          AbstractGameState observation = state.copy(focusPlayer);
          List<AbstractAction> possibleActions = fm.computeAvailableActions(observation);
          AbstractAction aiAction = players.get(focusPlayer)
              .getAction(observation, possibleActions);
          //apply AI action to current state
          fm.next(state, aiAction);

          //if in buy phase for focus player figure out what the payoff was
          if (observation.getGamePhase() == DominionGameState.DominionGamePhase.Buy && state.getCurrentPlayer() == focusPlayer){
              //grab total treasure value
              int payout = state.availableSpend(focusPlayer);
              expectedPayout += payout;

              //reset state, ready to repeat
              state = (DominionGameState) initialState.copy();

              //keep a counter for a sense check
              noObsOfPayOffs += 1;

              //output result
              System.out.printf("Payout: %d, iteration: %d", payout,noObsOfPayOffs);
              System.out.println("");
          }
      }
      expectedPayout = expectedPayout/(noObsOfPayOffs*1.0);
      System.out.printf("Expected Payout: %f", expectedPayout);

      //set-up GA.......can we do this so that genetic code is number of cards of each type and we have
      //the constraint that the total cost of cards is fixed. Constrained GA?

  }

  public static int expectedDeckPayoff(Game domGame, DominionFGParameters domParams, int noSimulations)
  {
      //source.add(discard);
      //discard.clear();
      //source.shuffle(rnd);

      //note this function assumes that the player's hand and discard pile are empty to begin with and we wish to just play
      //one hand. Start by drawing into active player's hand
      DominionGameState domState = (DominionGameState) domGame.getGameState();
      int activePlayer = domState.getCurrentPlayer();
      AbstractGameState observation = domState.copy(activePlayer);
      List<AbstractAction> observedActions = domGame.getForwardModel().computeAvailableActions(observation);

      for (int i = 0; i < domParams.HAND_SIZE; i++)
          domState.drawCard(activePlayer);

      //check out the possible actions for active player
      //AbstractGameState observation = domState.copy(activePlayer);
      //List<AbstractAction> observedActions = domGame.getForwardModel().computeAvailableActions(observation);

      AbstractAction nextAction = domGame.oneAction();


      //play cards in hand, note AI might not be able to play all cards here, due to needing multiple actions

      //AbstractAction oneAction()
      //fwdModel._afterAction(domState, AbstractAction action);

      return 0;

  }

  public static void runTournament(){
      System.out.println("Run tournament....");
      long seed = 100;
      long startTime = System.currentTimeMillis();
      String filename = "/Users/anthonyowen/GitProjects/TabletopGames/ResultsFiles/Tournament/DominionTournamentResults";
      String destdirListener = "/Users/anthonyowen/GitProjects/TabletopGames/ResultsFiles/Tournament/Listeners";

      //first set-up AI agents
      LinkedList<AbstractPlayer> agents = new LinkedList<>();
      //agents.add(new MCTSPlayer());
      agents.add(new RandomPlayer());
      agents.add(new RandomPlayer());

      //set-up game type and other tournament parameters
      GameType gameToPlay = Dominion;
      int playersPerGame = 2;
      int gamesPerMatchup = 5;
      TournamentMode mode = TournamentMode.SELF_PLAY;
      DominionFGParameters params = new DominionFGParameters(seed);

      //set-up tournament
      RoundRobinTournament tournament = new RoundRobinTournament(agents, gameToPlay, playersPerGame,
          gamesPerMatchup, mode, params);

      // Add listeners
      String listenerClass = "evaluation.listeners.MetricsGameListener";
      String genericMetricsClass = "evaluation.metrics.GameMetrics";
      String dominionMetricsClass = "games.dominion.stats.DominionMetrics";
      IGameListener gameTracker1 = IGameListener.createListener(listenerClass, genericMetricsClass);
      IGameListener gameTracker2 = IGameListener.createListener(listenerClass, dominionMetricsClass);
      gameTracker1.setOutputDirectory(destdirListener);
      gameTracker2.setOutputDirectory(destdirListener);
      List<IGameListener> dominionlisteners = new ArrayList<IGameListener>();
      dominionlisteners.add(gameTracker1);
      dominionlisteners.add(gameTracker2);
      tournament.setListeners(dominionlisteners);

      //run tournament
      tournament.verbose = true;
      tournament.resultsFile = filename;
      tournament.runTournament();

      long elapsedTime = System.currentTimeMillis() - startTime;
      long elapsedSeconds = elapsedTime / 1000;
      long secondsDisplay = elapsedSeconds % 60;
      long elapsedMinutes = elapsedSeconds / 60;
      System.out.printf("Tournament completed in %d minutes and %d seconds", elapsedMinutes, elapsedSeconds);
  }
}
