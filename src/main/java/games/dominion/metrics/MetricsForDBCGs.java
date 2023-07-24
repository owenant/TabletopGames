package games.dominion.metrics;

import static games.GameType.Dominion;
import static utilities.Utils.getArg;

import core.AbstractParameters;
import core.AbstractPlayer;
import evaluation.listeners.IGameListener;
import evaluation.tournaments.RoundRobinTournament;
import evaluation.tournaments.RoundRobinTournament.TournamentMode;
import games.GameType;
import games.dominion.DominionParameters;
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

      //first focus on simple card metrics, for this we run a sequence of two player
      //games, with both players using MCTS and looking at frequency of different card types
      //in the final deck for each player
      long seed = 100;
      long startTime = System.currentTimeMillis();
      String filename = "/Users/anthonyowen/GitProjects/TabletopGames/ResultsFiles/Tournament/DominionTournamentResults";
      String destdirListener = "/Users/anthonyowen/GitProjects/TabletopGames/ResultsFiles/Tournament/Listeners";

      //first set-up AI agents
      LinkedList<AbstractPlayer> agents = new LinkedList<>();
      agents.add(new MCTSPlayer());
      agents.add(new RandomPlayer());

      //set-up game type and other tournament parameters
      GameType gameToPlay = Dominion;
      int playersPerGame = 2;
      int gamesPerMatchup = 5;
      TournamentMode mode = TournamentMode.SELF_PLAY;
      DominionParameters params = new DominionParameters(seed);

      //set-up tournament
      RoundRobinTournament tournament = new RoundRobinTournament(agents, gameToPlay, playersPerGame,
          gamesPerMatchup, mode, params);

      // Add listeners
      //One listener for VP points at end of the game
      //One listener for card types in player decks at end of game

      String listenerClass = "evaluation.listeners.MetricsGameListener";
      String metricsClass = "evaluation.metrics.GameMetrics";
      IGameListener gameTracker = IGameListener.createListener(listenerClass, metricsClass);
      List<IGameListener> dominionlisteners = new ArrayList<IGameListener>();
      dominionlisteners.add(gameTracker);
      tournament.setListeners(dominionlisteners);
      gameTracker.setOutputDirectory(destdirListener);

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
