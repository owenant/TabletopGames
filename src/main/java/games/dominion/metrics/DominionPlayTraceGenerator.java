package games.dominion.metrics;

import static games.GameType.Dominion;

import core.AbstractParameters;
import core.AbstractPlayer;
import evaluation.RunArg;
import evaluation.listeners.IGameListener;
import evaluation.metrics.Event.GameEvent;
import evaluation.metrics.GameMetrics;
import evaluation.metrics.IDataLogger;
import evaluation.metrics.IDataLogger.ReportType;
import evaluation.tournaments.RoundRobinTournament;
import evaluation.tournaments.AbstractTournament.TournamentMode;
import games.GameType;
import games.dominion.DominionFG1EParameters;
import games.dominion.DominionSDParameters;
import core.interfaces.IStatisticLogger;
import evaluation.listeners.StateFeatureListener;
import evaluation.listeners.MetricsGameListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import players.PlayerFactory;
import players.simple.RandomPlayer;
import players.mcts.MCTSPlayer;
import games.dominion.players.DoubleWitchSD;
import games.dominion.players.BigMoneyWithGardensSD;
import games.dominion.players.BigMoney;

//entry point for producing log files for card count and action based playtraces for Dominion

public class DominionPlayTraceGenerator{
  public static void main(String[] args) {
      System.out.println("Entry point to generate Dominion playtraces....");
      runTournament();
      //runMaxPayoffDeckSearch();
      //testingExpPayOff();
  }

    public static void runTournament() {
        int gamesPerMatchup = 1;
        String destdir = "/Users/anthonyowen/GitProjects/TableTopGames_ForDominionPlayTraces/ResultsFiles/Tournament";
        String mctsJsonDir = "/JSON for Dominion MCTS";
        String mctsBudgetLow = destdir + mctsJsonDir + "/DomMCTSLowSkill_Budget_50.json";
        String mctsBudgetMedium = destdir + mctsJsonDir + "/DomMCTSMediumSkill_Budget_500.json";
        String mctsBudgetHigh = destdir + mctsJsonDir + "/DomMCTSHighSkill_Budget_5000.json";
        String tournamentResultsfilename = destdir + "/DominionTournamentResults.txt";
        String destdirGenericMetrics = destdir + "/Listeners/GenericMetrics";
        String destdirDominionMetrics = destdir + "/Listeners/DominionMetrics";
        String destdirDominionFeatures = destdir + "/Listeners/DominionFeatures";
        String destdirDomActions = destdir + "/Listeners/DominionActions";
        String domPlayTraceLogfile = destdirDominionFeatures + "/trace_logfile.txt";
        String domStateFeaturesLogfile = destdirDominionFeatures + "/features_logfile.txt";
        String domStateFeaturesReducedLogfile = destdirDominionFeatures + "/featuresreduced_logfile.txt";

        //filenames for stats loggers used by StateFeatureListener
        //String domPlayTraceLogfile = + "StatsLogs"
        //domStateFeaturesLogfile
        //domStateFeaturesReducedLogfile

        //toggle listeners on/off
        boolean useGenericListeners = false;
        boolean useDominionMetrics = false;
        boolean useDomPlayTrace = true;
        boolean useDomStateFeatures = false;
        boolean useDomStateFeaturesReduced = false;
        boolean useActionListener = true;

        //first set-up AI agents
        LinkedList<AbstractPlayer> agents = new LinkedList<>();
        MCTSPlayer mctsplayerLow = (MCTSPlayer) PlayerFactory.createPlayer(mctsBudgetLow);
        mctsplayerLow.setName("MCTS_BudgetLowSkill");
        MCTSPlayer mctsplayerMedium = (MCTSPlayer) PlayerFactory.createPlayer(mctsBudgetMedium);
        mctsplayerMedium.setName("MCTS_BudgetMediumSkill");
        MCTSPlayer mctsplayerHigh = (MCTSPlayer) PlayerFactory.createPlayer(mctsBudgetHigh);
        mctsplayerHigh.setName("MCTS_BudgetHighSkill");

        //and similar opponent agents if needed
        MCTSPlayer mctsplayerLowOpp = (MCTSPlayer) PlayerFactory.createPlayer(mctsBudgetLow);
        mctsplayerLowOpp.setName("MCTS_BudgetLowSkillOpp");
        MCTSPlayer mctsplayerMediumOpp = (MCTSPlayer) PlayerFactory.createPlayer(mctsBudgetMedium);
        mctsplayerMediumOpp.setName("MCTS_BudgetMediumSkillOpp");
        MCTSPlayer mctsplayerHighOpp = (MCTSPlayer) PlayerFactory.createPlayer(mctsBudgetHigh);
        mctsplayerHighOpp.setName("MCTS_BudgetHighSkillOpp");

        //set-up centroid player
        //String centroid_csv = destdir + "/CentroidPlayerData/MCTS_Centroid.json";
        //String centroid_csv = destdir + "/CentroidPlayerData/HumanCentroid.json";
        //String centroid_csv = destdir + "/CentroidPlayerData/DW_Centroid.json";
        //String centroid_csv = destdir + "/CentroidPlayerData/HumanCentroid_Rounded.json";
        //String centroid_csv = destdir + "/CentroidPlayerData/Budget500_vs_DW_GPM100_DWCentroid.json";

        //set up random player
        RandomPlayer rndPlayer = new RandomPlayer();
        rndPlayer.setName("RandomPlayer");
        RandomPlayer rndPlayerOpp = new RandomPlayer();
        rndPlayerOpp.setName("RandomPlayerOpp");

        //agents.add(mctsplayerLow);
        //agents.add(mctsplayerLowOpp);
        //agents.add(centroidAgent);
        //agents.add(mctsplayerMedium);
        //agents.add(mctsplayerMediumOpp);
        //agents.add(mctsplayerHigh);
        //agents.add(mctsplayerHighOpp);
        //agents.add(rndPlayer);
        //agents.add(rndPlayerOpp);
        agents.add(new BigMoneyWithGardensSD());
        //agents.add(new BigMoney());
        agents.add(new DoubleWitchSD());

        //set-up game type and other tournament parameters
        GameType gameToPlay = Dominion;
        int playersPerGame = 2;
        TournamentMode mode = TournamentMode.NO_SELF_PLAY;
        long startTime = System.currentTimeMillis();
        DominionSDParameters params = new DominionSDParameters();
        //DominionFG1EParameters params = new DominionFG1EParameters();

        //set-up tournament
        //RoundRobinTournament tournament = new RoundRobinTournament(agents, gameToPlay, playersPerGame,
        //gamesPerMatchup, mode, params, false);
        Map<RunArg, Object> config = new HashMap<>();
        config.put(RunArg.matchups, gamesPerMatchup);
        RoundRobinTournament tournament = new RoundRobinTournament(agents, gameToPlay, playersPerGame,
                params, mode, config);

        //set-up listeners
        List<IGameListener> dominionlisteners = new ArrayList<IGameListener>();

        //set-up listeners
        if (useGenericListeners) {
            GameMetrics genericMetrics = new GameMetrics();
            MetricsGameListener gameTrackerGenericMetrics = new MetricsGameListener(IDataLogger.ReportDestination.ToFile, genericMetrics.getAllMetrics());
            gameTrackerGenericMetrics.setOutputDirectory(destdirGenericMetrics);
            dominionlisteners.add(gameTrackerGenericMetrics);
        }

        if (useDominionMetrics) {
            String listenerClass = "evaluation.listeners.MetricsGameListener";
            String dominionMetricsClass = "games.dominion.stats.DominionMetrics";
            IGameListener gameTrackerDominionMetrics = IGameListener.createListener(listenerClass, dominionMetricsClass);
            //TODO:only outputs to console?
            gameTrackerDominionMetrics.setOutputDirectory(destdirDominionMetrics);
            dominionlisteners.add(gameTrackerDominionMetrics);
        }

        if (useDomPlayTrace) {
            //next set-up lister for DomPlayTrace
            DomPlayTrace DomTrace = new DomPlayTrace();
            StateFeatureListener gameTrackerDomTrace = new StateFeatureListener(DomTrace,
                    GameEvent.TURN_OVER, false, domPlayTraceLogfile);
            dominionlisteners.add(gameTrackerDomTrace);
        }

        if (useDomStateFeatures) {
            DomStateFeatures DomFeatures = new DomStateFeatures();
            StateFeatureListener gameTrackerDomFeatures = new StateFeatureListener(DomFeatures,
                    GameEvent.TURN_OVER, false, domStateFeaturesLogfile);
            //add to list of listeners
            dominionlisteners.add(gameTrackerDomFeatures);
        }

        if (useDomStateFeaturesReduced) {
            DomStateFeaturesReduced DomFeaturesReduced = new DomStateFeaturesReduced();
            StateFeatureListener gameTrackerDomFeaturesReduced = new StateFeatureListener(DomFeaturesReduced,
                    GameEvent.TURN_OVER, false, domStateFeaturesReducedLogfile);
            //IStatisticLogger statsLoggerstatsLoggerFeaturesReduced = IStatisticLogger.createLogger(
            //        "evaluation.loggers.FileStatsLogger", domStateFeaturesReducedLogfile);
            //gameTrackerDomFeaturesReduced.setLogger(statsLoggerstatsLoggerFeaturesReduced);
            //gameTrackerDomFeaturesReduced.setOutputDirectory(destdirDominionFeatures);

            //add to list of listeners
            dominionlisteners.add(gameTrackerDomFeaturesReduced);
        }

        if (useActionListener){
            DomActionFeatures domActions = new DomActionFeatures();
            IDataLogger.ReportType[] dataTypes = new IDataLogger.ReportType[1];
            dataTypes[0] = IDataLogger.ReportType.RawData;
            MetricsGameListener gameTrackerDomActions = new MetricsGameListener(IDataLogger.ReportDestination.ToFile, dataTypes, domActions.getAllMetrics());
            gameTrackerDomActions.setOutputDirectory(destdirDomActions);
            dominionlisteners.add(gameTrackerDomActions);
        }

        //run tournament
        tournament.setListeners(dominionlisteners);
        tournament.verbose = true;
        tournament.setResultsFile(tournamentResultsfilename);
        tournament.run();

        long elapsedTime = System.currentTimeMillis() - startTime;
        long elapsedSeconds = elapsedTime / 1000;
        long elapsedMinutes = elapsedSeconds / 60;
        System.out.printf("Tournament completed in %d minutes and %d seconds", elapsedMinutes, elapsedSeconds);
    }
}