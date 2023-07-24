package games.dominion.metrics;

import static games.GameType.Dominion;
import static utilities.Utils.getArg;

import core.AbstractParameters;
import core.AbstractPlayer;
import core.Game;
import evaluation.listeners.IGameListener;
import evaluation.tournaments.RoundRobinTournament;
import evaluation.tournaments.AbstractTournament.TournamentMode;
import games.GameType;
import games.dominion.DominionFGParameters;
import games.dominion.DominionForwardModel;
import games.dominion.DominionGameState;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
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
      Game game = new Game(GameType.Dominion, players, new DominionForwardModel(), new DominionGameState(new DominionFGParameters(System.currentTimeMillis()), players.size()));
      DominionForwardModel fm = new DominionForwardModel();

      //set-total cost amount
      int totalCost = 10;

      //create an initial deck that conforms to total cost amount

      //check expected payoff amount from this deck (for checking to begin with)

      //set-up GA.......

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
