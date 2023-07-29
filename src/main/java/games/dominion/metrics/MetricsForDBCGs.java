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
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.HashSet;

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

      //set-up Dominion game and state
      List<AbstractPlayer> players = Arrays.asList(new MCTSPlayer(), new MCTSPlayer());
      DominionFGParameters params = new DominionFGParameters(System.currentTimeMillis());
      DominionGameState state = new DominionGameState(params, players.size());
      DominionForwardModel fm = new DominionForwardModel();
      //note creating a game initialises the state
      Game game = new Game(GameType.Dominion, players, fm, state);

      //set-total cost amount
      int totalCost = 30;

      //no of simulations for computing expected deck payoff
      int noSims = 10;

      //A deck will be represented by a chromosome (vector) consisting of a collection
      // of 17 genes which are integers representing the number of cards of a given
      //card type in the deck. Here we are assuming we are using the base collection of cards

      //set-up vector to  convert chromosome index to card type
      CardType[] indexToType = new CardType[11];
      indexToType[0] = CardType.CELLAR;
      indexToType[1] = CardType.MARKET;
      indexToType[2] = CardType.MERCHANT;
      indexToType[3] = CardType.MILITIA;//will impact, but full benefit wont be recognised
      indexToType[4] = CardType.MINE;
      indexToType[5] = CardType.MOAT; //will impact, but full benefit wont be recognised
      //indexToType[6] = CardType.REMODEL; //wont impact treasure
      indexToType[6] = CardType.SMITHY;
      indexToType[7] = CardType.VILLAGE;
      //indexToType[9] = CardType.WORKSHOP;//wont impact treasure
      indexToType[8] = CardType.GOLD;
      indexToType[9] = CardType.SILVER;
      indexToType[10] = CardType.COPPER;

      //set up initial chromosome consisting of 30 copper cards
      int[] initialGuess = {0,0,0,0,0,0,0,0,3,0,0};

      //check cost of initial deck
      int cost = deckCost(initialGuess, indexToType);

      //calculate fitness
      double fitnessResult = fitness(initialGuess, indexToType, game, params, noSims);
      System.out.printf("Expected Payout: %f", fitnessResult);

      //create initial population at random filtering out those that dont satisfy
      //the cost constraints
      int noIndividuals = 100;
      long seed = 100;
      int maxIterations = 100000;
      Set<int[]> initialPop = genInitialPopulation(totalCost, noIndividuals, indexToType,
          seed, maxIterations);

      //select parents - probability based on fitness function

      //create crossover offspring that still obeys cost constraint

      //apply max entropy mutation

      //note: use max entropy for mutations and penalty function for cost constraint.
      //fitness function is given by expected deck pay off
      //can I use JGAP package? Not sure I need to....
  }

  public static Set<int[]> genInitialPopulation(int costConstraint, int numberOfIndividuals,
      CardType[] cardTypes, long seed, int maxIterations){
      //generate random samples keeping only those that satisfy the cost constraint
      //set a maximum number of copies for any type of action card
      Random rnd = new Random(seed);
      int maxNoActionCardsOfAnyType = 5;
      //also set maximum for any type of treasure card
      int maxNoTreasureCardsOfAnyType = 10;
      int noOfSamplesGenerated = 0;
      //keep a track of total number of randomly generated decks
      int decksGenerated = 0;
      Set<int[]> pop = new HashSet<int[]>();
      int[] deck = new int[cardTypes.length];
      while(noOfSamplesGenerated < numberOfIndividuals){
          for(int i = 0; i < cardTypes.length; i++){
              if (cardTypes[i] == CardType.COPPER || cardTypes[i] == CardType.SILVER
                  || cardTypes[i] == CardType.GOLD){
                  deck[i] = rnd.nextInt(maxNoTreasureCardsOfAnyType);
              }else{
                  deck[i] = rnd.nextInt(maxNoActionCardsOfAnyType);
              }
          }
          int cost = deckCost(deck, cardTypes);
          if(cost == costConstraint){
              pop.add(deck);
              noOfSamplesGenerated += 1;
          }

          //for low costs we might generate all possible decks before generating
          //the required number of iterations, so exit if a max number of iterations are
          //attempted
          decksGenerated += 1;
          if (decksGenerated >= maxIterations){
              break;
          }
      }
      return pop;
  }

  public static int deckCost(int[] deckComposition, CardType[] cardTypes) {
      int cost = 0;
      for (int i = 0; i < deckComposition.length; i++){
          cost += deckComposition[i] * cardTypes[i].cost;
      }
      return cost;
  }

  public static double fitness(int[] deckComposition, CardType[] cardTypes, Game domGame, DominionFGParameters domParams, int noSimulations){
      //provides fitness function for deck
      //for now driven by treasures, but could be update to give credit to things like
      //card trashing or automatically gaining a new card etc. They could be translated into
      //an approximate treasure worth

      double result = expectedDeckPayoff(deckComposition, cardTypes, domGame, domParams,noSimulations);

      return result;
  }

  public static double expectedDeckPayoff(int[] deckComposition, CardType[] cardTypes, Game domGame, DominionFGParameters domParams, int noSimulations)
  {
      boolean[] notVisibleToEitherPlayer = {false, false};
      //focus player is the player whose deck we will be evolving
      DominionGameState state = (DominionGameState) domGame.getGameState();
      DominionForwardModel fm = (DominionForwardModel) domGame.getForwardModel();
      List<AbstractPlayer> players = domGame.getPlayers();
      int focusPlayer = state.getCurrentPlayer();
      //store a copy of this initial starting state to reset at end of function call
      DominionGameState initialState = (DominionGameState) state.copy();

      //create player draw deck
      PartialObservableDeck<DominionCard> draw = new PartialObservableDeck<DominionCard>("player_draw", focusPlayer, notVisibleToEitherPlayer);
      for(int i =0; i < deckComposition.length; i++) {
          for(int j = 0; j < deckComposition[i]; j++){
              draw.add(DominionCard.create(cardTypes[i]),notVisibleToEitherPlayer);
          }
      }
      state.setDeck(DeckType.DRAW,focusPlayer,draw);

      //make sure there are no cards in hand
      boolean[] visibleToFocusPlayer = {true, false};
      PartialObservableDeck<DominionCard> hand = new PartialObservableDeck<DominionCard>("player_hand", focusPlayer, visibleToFocusPlayer);
      state.setDeck(DeckType.HAND,focusPlayer,hand);

      //start by drawing cards into hand
      for (int i = 0; i < domParams.HAND_SIZE; i++)
          state.drawCard(focusPlayer);

      //store a copy of this starting state for this deck
      DominionGameState startState = (DominionGameState) state.copy();

      int noObsOfPayOffs = 0;
      double expectedPayout = 0;
      while(noObsOfPayOffs <= noSimulations){
          //observe current state
          AbstractGameState observation = state.copy(focusPlayer);
          List<AbstractAction> possibleActions = fm.computeAvailableActions(observation);
          AbstractAction aiAction = players.get(focusPlayer)
              .getAction(observation, possibleActions);

          //if in buy phase for focus player figure out what the payoff was
          if (observation.getGamePhase() == DominionGameState.DominionGamePhase.Buy && state.getCurrentPlayer() == focusPlayer){
              //grab total treasure value
              int payout = state.availableSpend(focusPlayer);
              expectedPayout += payout;

              //reset state, ready to repeat
              state = (DominionGameState) startState.copy();

              //keep a counter for a sense check
              noObsOfPayOffs += 1;
          }else{
              //otherwise perform the AI action
              fm.next(state, aiAction);
          }
      }
      //make sure state is reset
      state = (DominionGameState) initialState.copy();

      //compute final expected payoff
      expectedPayout = expectedPayout/(noObsOfPayOffs*1.0);

      return expectedPayout;
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
