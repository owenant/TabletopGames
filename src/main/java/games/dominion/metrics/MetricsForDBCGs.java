package games.dominion.metrics;

import static games.GameType.Dominion;
import core.AbstractPlayer;
import evaluation.listeners.IGameListener;
import evaluation.metrics.Event;
import evaluation.metrics.Event.GameEvent;
import evaluation.tournaments.RoundRobinTournament;
import evaluation.tournaments.AbstractTournament.TournamentMode;
import games.GameType;
import games.dominion.DominionFGParameters;
import games.dominion.DominionSDParameters;
import games.dominion.DominionGameState;
import core.interfaces.IStatisticLogger;
import evaluation.listeners.StateFeatureListener;
import games.dominion.stats.DomStateFeatures;
import games.dominion.stats.DomPlayTrace;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.lang.Math;
import java.io.File;
import java.io.FileWriter;
import players.PlayerFactory;
import players.mcts.MCTSParams;
import players.simple.RandomPlayer;
import players.mcts.MCTSPlayer;
import players.rhea.RHEAPlayer;
import players.PlayerFactory;
import games.dominion.players.BigMoney;
import java.util.Collections;

//entry point for metric calculations for DBCGs. Currently focused on Dominion
//note tests for dominion might help in building functionality in here

public class MetricsForDBCGs {
  public static void main(String[] args) {
      System.out.println("Entry point to metrics for DBCGs....");
      //simpleTournament();
      runTournament();
      //runMaxPayoffDeckSearch();
      //testingExpPayOff();
  }

  public static void simpleTournament() {
      String mctsJsonDir = "/Users/anthonyowen/GitProjects/TabletopGames/ResultsFiles/Tournament/JSON for Dominion MCTS";
      String fileMCTSJson = mctsJsonDir + "/DominionFG_4P_256+ms.json";

      //first set-up AI agents
      LinkedList<AbstractPlayer> agents = new LinkedList<>();
      MCTSPlayer mctsplayer = (MCTSPlayer) PlayerFactory.createPlayer(fileMCTSJson);
      agents.add(new RandomPlayer());
      agents.add(mctsplayer);

      //set-up game type and other tournament parameters
      GameType gameToPlay = Dominion;
      int playersPerGame = 2;
      int gamesPerMatchup = 200;
      TournamentMode mode = TournamentMode.NO_SELF_PLAY;
      long seed = System.currentTimeMillis();
      DominionFGParameters params = new DominionFGParameters(seed);

      //set-up tournament
      RoundRobinTournament tournament = new RoundRobinTournament(agents, gameToPlay, playersPerGame,
          gamesPerMatchup, mode, params);

      //set-up listeners
      //DomPlayTrace features = new DomPlayTrace();
      //StateFeatureListener gameTrackerDominionFeatures = new StateFeatureListener(features, GameEvent.TURN_OVER, false);
      //IStatisticLogger statsLogger = IStatisticLogger.createLogger("evaluation.loggers.FileStatsLogger", featureslogfile);
      //gameTrackerDominionFeatures.setLogger(statsLogger);
      //gameTrackerDominionFeatures.setOutputDirectory(destdirFeatures);
      //List<IGameListener> dominionlisteners = new ArrayList<IGameListener>();
      //dominionlisteners.add(gameTrackerDominionFeatures);
      //tournament.setListeners(dominionlisteners);

      //run tournament
      tournament.runTournament();
  }

  public static void runTournament(){
    System.out.println("Run tournament....");
    //long seed = 100;
    long startTime = System.currentTimeMillis();
    String filename = "/Users/anthonyowen/GitProjects/TabletopGames/ResultsFiles/Tournament/DominionTournamentResults";
    String destdirGenericMetrics = "/Users/anthonyowen/GitProjects/TabletopGames/ResultsFiles/Tournament/Listeners/GenericMetrics";
    String destdirDominionMetrics = "/Users/anthonyowen/GitProjects/TabletopGames/ResultsFiles/Tournament/Listeners/DominionMetrics";
    String destdirFeatures = "/Users/anthonyowen/GitProjects/TabletopGames/ResultsFiles/Tournament/Listeners/Features";
    String featureslogfile = destdirFeatures + "/featureslogfile.txt";
    String mctsJsonDir = "/Users/anthonyowen/GitProjects/TabletopGames/ResultsFiles/Tournament/JSON for Dominion MCTS";
    String fileMCTSJson = mctsJsonDir + "/DominionFG_2P_64+ms.json";

    //first set-up AI agents
    LinkedList<AbstractPlayer> agents = new LinkedList<>();
    MCTSPlayer mctsplayer = (MCTSPlayer) PlayerFactory.createPlayer(fileMCTSJson);
    agents.add(new RandomPlayer());
    agents.add(mctsplayer);

    //set-up game type and other tournament parameters
    GameType gameToPlay = Dominion;
    int playersPerGame = 2;
    int gamesPerMatchup = 1;
    TournamentMode mode = TournamentMode.NO_SELF_PLAY;
    DominionFGParameters params = new DominionFGParameters(startTime);

    //set-up tournament
    RoundRobinTournament tournament = new RoundRobinTournament(agents, gameToPlay, playersPerGame,
        gamesPerMatchup, mode, params);

    // Add listeners
    String listenerClass = "evaluation.listeners.MetricsGameListener";
    String genericMetricsClass = "evaluation.metrics.GameMetrics";
    String dominionMetricsClass = "games.dominion.stats.DominionMetrics";
    IGameListener gameTrackerGenericMetrics = IGameListener.createListener(listenerClass, genericMetricsClass);
    IGameListener gameTrackerDominionMetrics = IGameListener.createListener(listenerClass, dominionMetricsClass);
    DomStateFeatures features = new DomStateFeatures();
    //DomPlayTrace features = new DomPlayTrace();
    StateFeatureListener gameTrackerDominionFeatures = new StateFeatureListener(features, Event.GameEvent.ROUND_OVER, true);
    IStatisticLogger statsLogger = IStatisticLogger.createLogger("evaluation.loggers.FileStatsLogger", featureslogfile);
    gameTrackerDominionFeatures.setLogger(statsLogger);
    gameTrackerGenericMetrics.setOutputDirectory(destdirGenericMetrics);
    gameTrackerDominionMetrics.setOutputDirectory(destdirDominionMetrics);
    gameTrackerDominionFeatures.setOutputDirectory(destdirFeatures);
    List<IGameListener> dominionlisteners = new ArrayList<IGameListener>();
    dominionlisteners.add(gameTrackerGenericMetrics);
    dominionlisteners.add(gameTrackerDominionMetrics);
    dominionlisteners.add(gameTrackerDominionFeatures);
    tournament.setListeners(dominionlisteners);

    //run tournament
    tournament.verbose = false;
    tournament.resultsFile = filename;
    tournament.runTournament();

    long elapsedTime = System.currentTimeMillis() - startTime;
    long elapsedSeconds = elapsedTime / 1000;
    long secondsDisplay = elapsedSeconds % 60;
    long elapsedMinutes = elapsedSeconds / 60;
    System.out.printf("Tournament completed in %d minutes and %d seconds", elapsedMinutes, elapsedSeconds);
}
  public static void testingExpPayOff() {
      //testing no of simulations for expected payoff
      String filename = "/Users/anthonyowen/GitProjects/TabletopGames/ResultsFiles/Testing/ExpPayOffConvergence";
      File csvFile = new File(filename);
      FileWriter fileWriter;

      //set genome parameters
      DominionDeckGenome.MAX_NO_OF_CARDS_OF_ANY_TYPE = 5;
      DominionDeckGenome.NO_SIMULATIONS_EXPPAYOFF = 20;
      DominionDeckGenome.MAX_COST_CONSTRAINT = 80;
      DominionDeckGenome.MIN_COST_CONSTRAINT = 40;

      //int[] pheno = {0, 3, 1, 0, 4, 0, 2, 0, 4, 3, 2};
      //int[] pheno = {0,2,0,1,3,2,2,0,4,2,3};
      //int[] pheno = {0,1,1,0,4,0,2,0,4,3,2};
      int[] pheno = {2,2,3,1,1,3,3,4,1,4,2};

      StringBuilder line = new StringBuilder();
      DominionDeckGenome genomeTest = new DominionDeckGenome(pheno);
      line.append(genomeTest.convertPhenoToString() + "\n");
      line.append("Deck cost: " + genomeTest.getCost() + "\n");
      line.append("No Sims, Fitness, Elapsed Time \n");
      int noSims;
      for (int i = 1; i <= 10; i++) {
          noSims = 5 * i;
          long startTime = System.currentTimeMillis();
          DominionDeckGenome.NO_SIMULATIONS_EXPPAYOFF = noSims;
          genomeTest = new DominionDeckGenome(pheno);
          System.out.println("No sims: " + noSims);
          System.out.println("Fitness: " + genomeTest.getFitness());
          long elapsedTime = System.currentTimeMillis() - startTime;
          long elapsedSeconds = elapsedTime / 1000;
          long elapsedMinutes = elapsedSeconds / 60;
          System.out.printf("Fitness for test genome computed in %d minutes and %d seconds: ", elapsedMinutes, elapsedSeconds);
          System.out.println("");

          long elapsedTimeInSecs = elapsedMinutes * 60 + elapsedSeconds;
          line.append(noSims + "," + genomeTest.getFitness() + "," + elapsedTimeInSecs + "\n");
      }

      try {
          fileWriter = new FileWriter(csvFile);
          fileWriter.write(line.toString());
          fileWriter.close();
      }catch(Exception e){
          System.out.println("Error opening file for expected payoff testing");
      }
      return;
  }

  public static void runMaxPayoffDeckSearch(){
      //set-up output file
      String filename = "/Users/anthonyowen/GitProjects/TabletopGames/ResultsFiles/GeneticAlgorithm/GAResults";
      File csvFile = new File(filename);
      FileWriter fileWriter;

      //parameters
      int noIndividuals = 50;
      int maxIterations = 10000;
      long initialPopSeed = 100;
      double probCrossOver = 0.8;
      double probMutation = 0.05;
      int noGenerations = 10;
      int maxChildCreationAttemptsPriorToFailure = 1000;
      int maxParentRedraws = 100;
      DominionDeckGenome.MAX_NO_OF_CARDS_OF_ANY_TYPE = 5;
      DominionDeckGenome.NO_SIMULATIONS_EXPPAYOFF = 1;
      DominionDeckGenome.MAX_COST_CONSTRAINT = 100000;
      DominionDeckGenome.MIN_COST_CONSTRAINT = 0;

      System.out.println("Search for optimal decks with different cost amounts....");

      //create initial population at random filtering out those that dont satisfy
      //the cost constraints
      ArrayList<DominionDeckGenome> population = genInitialPopulation(noIndividuals, maxIterations, initialPopSeed);
      System.out.println("Initial population size: " + population.size());

      //calculate fitness for initial population
      System.out.println("Computing fitness of initial population...");
      long startTime = System.currentTimeMillis();
      for (DominionDeckGenome genome : population) {
          //make sure to compute fitness for each child before adding to parents list
          genome.getFitness();
      }
      //sort population by fitness
      Collections.sort(population);

      long elapsedTime = System.currentTimeMillis() - startTime;
      long elapsedSeconds = elapsedTime / 1000;
      long elapsedMinutes = elapsedSeconds / 60;
      System.out.printf("Fitness for initial population computed in %d minutes and %d seconds: ", elapsedMinutes, elapsedSeconds);
      System.out.println("");

      //start building report file
      StringBuilder line = new StringBuilder();
      line.append("No of individuals in initial population: " + population.size() + "\n");
      line.append("Max cost constraint: " + DominionDeckGenome.MAX_COST_CONSTRAINT + "\n");
      line.append("Min cost constraint: " + DominionDeckGenome.MIN_COST_CONSTRAINT + "\n");
      //TODO: have different max cards for action vs treasury
      line.append("Max number of cards of any type: " + DominionDeckGenome.MAX_NO_OF_CARDS_OF_ANY_TYPE + "\n");
      line.append("Probability of cross-over: " + probCrossOver + "\n");
      line.append("Probability of mutation: " + probMutation + "\n");
      line.append("No of sims in expected payoff calculation: " + DominionDeckGenome.NO_SIMULATIONS_EXPPAYOFF + "\n");
      line.append("Number of generations: " + noGenerations + "\n");
      line.append("No of cycles of child generation before breaking: " + maxChildCreationAttemptsPriorToFailure + "\n");
      line.append("Generation, Fittest Deck, Fitness, Deck Cost, Population size, No feasible children produced, No of cross-overs, No of mutations\n");

      //start loop to evolve population, finish when termination condition is achieved
      Random rnd = new Random(System.currentTimeMillis());
      int Counter = 0;
      startTime = System.currentTimeMillis();
      while (Counter < noGenerations) {
          System.out.println("Generation: " + Counter);
          int childCreationCycles = 0;
          int noMutations = 0;
          int noCrossOvers = 0;

          ArrayList<DominionDeckGenome> children = new ArrayList<DominionDeckGenome>();
          while (children.size() < 1 && childCreationCycles <= maxChildCreationAttemptsPriorToFailure) {
              noMutations = 0;
              noCrossOvers = 0;
              //TODO: why this number of pairs?
              for (int pair = 0; pair < (int) Math.floor(population.size() / 2.0); pair++) {
                  //draw pairs of individuals randomly from the population
                  DominionDeckGenome parent1 = drawFromPopulation(population,rnd);

                  //keep redrawing parent2 until we find a genome different from parent 1
                  DominionDeckGenome parent2 = drawFromPopulation(population, rnd);
                  int parentReDraws = 0;
                  while (parent2.equals(parent1) && parentReDraws < maxParentRedraws){
                      parent2 = drawFromPopulation(population, rnd);
                      parentReDraws++;
                  }
                  if (parentReDraws == maxParentRedraws){
                      System.out.println("Warning: Unable to find a distinct set of parents");
                      break;
                  }

                  //cross-over pairs with a probability probCrossOver
                  if (rnd.nextFloat() < probCrossOver) {
                      DominionDeckGenome child1 = new DominionDeckGenome(parent1.getGenotype());
                      DominionDeckGenome child2 = new DominionDeckGenome(parent2.getGenotype());
                      ArrayList<DominionDeckGenome> childList = DominionDeckGenome.crossOver(
                          parent1, parent2);
                      child1 = childList.get(0);
                      child2 = childList.get(1);
                      noCrossOvers++;

                      //check that cross-overs satisfy cost constraint, max card constraint and are new genomes
                      if (checkToAddToPop(child1, population)){
                          child1.getFitness();
                          children.add(child1);
                      }

                      if (checkToAddToPop(child2, population)){
                          child2.getFitness();
                          children.add(child2);
                      }
                  }

                  //mutate parent1 with probability probMutation
                  if (rnd.nextFloat() < probMutation) {
                      DominionDeckGenome mutantChild = new DominionDeckGenome(parent1.getGenotype());
                      mutantChild = DominionDeckGenome.mutate(parent1, rnd);
                      noMutations++;
                      if (checkToAddToPop(mutantChild, population)){
                          mutantChild.getFitness();
                          children.add(mutantChild);
                      }
                  }

                  //mutate parent2 with probability probMutation
                  if (rnd.nextFloat() < probMutation) {
                      DominionDeckGenome mutantChild = new DominionDeckGenome(parent2.getGenotype());
                      mutantChild = DominionDeckGenome.mutate(parent2, rnd);
                      noMutations++;
                      if (checkToAddToPop(mutantChild, population)){
                          mutantChild.getFitness();
                          children.add(mutantChild);
                      }
                  }
              }
              if (children.size() == 0){
                  childCreationCycles++;
              }
          }

          //add children to exiting population and then sort by fitness
          for (DominionDeckGenome child : children){
              population.add(child);
          }
          Collections.sort(population);

          //next generation
          ArrayList<DominionDeckGenome> nextGen = new ArrayList<DominionDeckGenome>();
          for(int i = 0; i < noIndividuals; i++){
              nextGen.add(population.get(i));
          }

          //reset population list to new generation
          population.clear();
          for (int i = 0; i < nextGen.size(); i++){
              population.add(nextGen.get(i));
          }

          //output size of next generation population and fitness of fittest individual
          DominionDeckGenome fittestGenome = population.get(0);
          int deckCost = fittestGenome.getCost();
          System.out.println("Fittest Deck: " + fittestGenome.convertPhenoToString());
          System.out.println("Fitness: " + fittestGenome.getFitness());
          System.out.println("Deck Cost: " + deckCost);
          System.out.println("No of individuals remaining in population: " + population.size());
          System.out.println("No of feasible children generated this generation: " + children.size());
          System.out.println("No of cross-overs this generation: " + noCrossOvers);
          System.out.println("No of mutations this generation: " + noMutations);
          System.out.println("No of child creation cycles this generation: " + childCreationCycles);

          //create line for summary report file
          line.append(Counter + "," + fittestGenome.convertPhenoToString() + ","
              + fittestGenome.getFitness() + "," + deckCost
              + "," + population.size() + "," + children.size() + "," + noCrossOvers
              + "," + noMutations + "," + childCreationCycles + "\n");

          //output population to file every 10 generations
          if (Counter % 10 == 0) {
              String filenameForPop =
                  "/Users/anthonyowen/GitProjects/TabletopGames/ResultsFiles/GeneticAlgorithm/PopulationGeneration"
                      + Counter;
              File csvFilePop = new File(filenameForPop);
              FileWriter fileWriterForPop;
              try {
                  StringBuilder lineForPop = new StringBuilder();
                  for (int i = 0; i < population.size(); i++) {
                      lineForPop.append(
                          population.get(i).convertPhenoToString() + ":" + population.get(i)
                              .getFitness() + "\n");
                  }
                  fileWriterForPop = new FileWriter(csvFilePop);
                  fileWriterForPop.write(lineForPop.toString());
                  fileWriterForPop.close();
              } catch (Exception e) {
                  System.out.println("Error opening file for GA results");
              }
          }

          //increase generation counter
          Counter++;

          //if we were unable to generate any feasible children in this generation then
          //terminate the algorithm early.
          if(children.size() == 0){
              System.out.println("Unable to generate any feasible children in this generation, terminating algorithm");
              break;
          }
      }

      elapsedTime = System.currentTimeMillis() - startTime;
      elapsedSeconds = elapsedTime / 1000;
      elapsedMinutes = elapsedSeconds / 60;
      System.out.printf("Genetic algorithm completed in %d minutes and %d seconds", elapsedMinutes, elapsedSeconds);

      //output results to file
      try {
          fileWriter = new FileWriter(csvFile);
          fileWriter.write(line.toString());
          fileWriter.close();
      }catch(Exception e){
          System.out.println("Error opening file for GA results");
      }
  }

  public static boolean checkToAddToPop(DominionDeckGenome genome, ArrayList<DominionDeckGenome> pop){
      //checks whether or not we should add this genome to our population
      int deckCost = genome.getCost();
      if (deckCost <= DominionDeckGenome.MAX_COST_CONSTRAINT
          && deckCost >= DominionDeckGenome.MIN_COST_CONSTRAINT
          && genome.checkWithinMaxCardLimit() && !pop.contains(genome)){
            return true;
      }else{
          return false;
      }
  }
  public static DominionDeckGenome drawFromPopulation(ArrayList<DominionDeckGenome> population, Random rnd){
      //draw a sample from a population using the relative fitness of each sample
      //compared ot the whole population (Roulette wheel method)

      //total fitness values across population
      double totalfitness = 0;
      for(DominionDeckGenome genome : population){
          totalfitness += genome.getFitness();
      }

      //calculate probability interval for each genome
      ArrayList<Double> probInterval = new ArrayList<Double>();
      for(int i = 0; i < population.size(); i++){
          double scaledFitness = population.get(i).getFitness()/totalfitness;
          if(i == 0){
              probInterval.add(i,scaledFitness);
          }else if (i == population.size() -1){
              //to avoid rounding errors
              probInterval.add(i, 1.0);
          }else{
              probInterval.add(i, probInterval.get(i-1) + scaledFitness);
          }
      }

      //randomly select a sample from the population
      double randomDraw = rnd.nextFloat();
      for(int i = 0; i < probInterval.size(); i++){
          if (i == 0){
              if (randomDraw <= probInterval.get(0)) {
                  return population.get(0);
              }
          }else if (randomDraw > probInterval.get(i-1) && randomDraw <= probInterval.get(i)){
              return population.get(i);
          }
      }
      //should be impossible to reach here
      return null;
  }
  public static ArrayList<DominionDeckGenome> genInitialPopulation(int numberOfIndividuals, int maxIterations, long seed)
  {
      //generate random samples keeping only those that satisfy the cost constraint
      //set a maximum number of copies for any type of action card
      Random rnd = new Random(seed);
      int noOfSamplesGenerated = 0;
      //keep a track of total number of randomly generated decks
      int decksGenerated = 0;
      ArrayList<DominionDeckGenome> pop = new ArrayList<DominionDeckGenome>();
      while(noOfSamplesGenerated < numberOfIndividuals){
          int[] deck = new int[DominionDeckGenome.CARD_TYPES.length];
          for(int i = 0; i < DominionDeckGenome.CARD_TYPES.length; i++){
              deck[i] = rnd.nextInt(DominionDeckGenome.MAX_NO_OF_CARDS_OF_ANY_TYPE);
          }
          DominionDeckGenome genome = new DominionDeckGenome(deck);
          int cost = genome.getCost();
          if(cost <= DominionDeckGenome.MAX_COST_CONSTRAINT && cost >= DominionDeckGenome.MIN_COST_CONSTRAINT){
              if (!pop.contains(genome)) {
                  pop.add(genome);
                  noOfSamplesGenerated += 1;
              }
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
}
