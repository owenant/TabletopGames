package games.dominion.metrics;

import static games.GameType.Dominion;
import core.AbstractPlayer;
import evaluation.listeners.IGameListener;
import evaluation.tournaments.RoundRobinTournament;
import evaluation.tournaments.AbstractTournament.TournamentMode;
import games.GameType;
import games.dominion.DominionFGParameters;
import games.dominion.DominionGameState;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.lang.Math;
import java.io.File;
import java.io.FileWriter;
import players.simple.RandomPlayer;
import java.util.Collections;

//entry point for metric calculations for DBCGs. Currently focused on Dominion
//note tests for dominion might help in building functionality in here

public class MetricsForDBCGs {
  public static void main(String[] args) {
      System.out.println("Entry point to metrics for DBCGs....");
      //runTournament();
      runMaxPayoffDeckSearch();
      //testingExpPayOff();
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
      int noIndividuals = 10;
      int maxIterations = 10000;
      long initialPopSeed = 100;
      double probCrossOver = 0.8;
      double probMutation = 0.05;
      int noGenerations = 2;
      int maxChildCreationAttemptsPriorToFailure = 1000;
      DominionDeckGenome.MAX_NO_OF_CARDS_OF_ANY_TYPE = 5;
      DominionDeckGenome.NO_SIMULATIONS_EXPPAYOFF = 20;
      DominionDeckGenome.MAX_COST_CONSTRAINT = 80;
      DominionDeckGenome.MIN_COST_CONSTRAINT = 40;

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
          int noFeasibleChildrenInGeneration = 0;
          int childCreationCycles = 0;
          int noMutations = 0;
          int noCrossOvers = 0;
          ArrayList<DominionDeckGenome> children = new ArrayList<DominionDeckGenome>();
          while (noFeasibleChildrenInGeneration < 1 && childCreationCycles <= maxChildCreationAttemptsPriorToFailure) {
              noMutations = 0;
              noCrossOvers = 0;
              for (int pair = 0; pair < (int) Math.floor(population.size() / 2.0); pair++) {
                  //draw pairs of individuals randomly from the population
                  DominionDeckGenome parent1 = drawFromPopulation(population);
                  DominionDeckGenome parent2 = drawFromPopulation(population);

                  //cross-over pairs with a probability probCrossOver
                  DominionDeckGenome child1 = new DominionDeckGenome(parent1.getGenotype());
                  DominionDeckGenome child2 = new DominionDeckGenome(parent2.getGenotype());
                  if (rnd.nextFloat() < probCrossOver) {
                      ArrayList<DominionDeckGenome> childList = DominionDeckGenome.crossOver(
                          parent1, parent2);
                      child1 = childList.get(0);
                      child2 = childList.get(1);
                      noCrossOvers++;
                  }
                  //mutate each new child with probability probMutation
                  if (rnd.nextFloat() < probMutation) {
                      child1 = DominionDeckGenome.mutate(child1);
                      noMutations++;
                  }
                  if (rnd.nextFloat() < probMutation) {
                      child2 = DominionDeckGenome.mutate(child2);
                      noMutations++;
                  }

                  //check if child satisfies cost constraint and if so add to population (avoiding duplicates)
                  if (!child1.getGenotype().equals(parent1.getGenotype())) {
                      int deckCost1 = child1.getCost();
                      if (deckCost1 <= DominionDeckGenome.MAX_COST_CONSTRAINT
                          && deckCost1 >= DominionDeckGenome.MIN_COST_CONSTRAINT
                          && !population.contains(child1)) {
                          noFeasibleChildrenInGeneration++;
                          child1.getFitness();
                          population.add(child1);
                      }
                  }

                  if (!child2.getGenotype().equals(parent2.getGenotype())) {
                      int deckCost2 = child2.getCost();
                      if (deckCost2 <= DominionDeckGenome.MAX_COST_CONSTRAINT
                          && deckCost2 >= DominionDeckGenome.MIN_COST_CONSTRAINT
                          && !population.contains(child2)) {
                          noFeasibleChildrenInGeneration++;
                          child2.getFitness();
                          population.add(child2);
                      }
                  }

                  //track number of times attempted to create children
                  childCreationCycles++;
              }
          }

          //sort population by fitness
          Collections.sort(population);

          //next generation
          ArrayList<DominionDeckGenome> nextGen = new ArrayList<DominionDeckGenome>();
          for(int i = 1; i <= noIndividuals; i++){
              nextGen.add(population.get(population.size()-i));
          }

          //reset population list to new generation
          population.clear();
          for (int i = 0; i < nextGen.size(); i++){
              population.add(nextGen.get(i));
          }

          //output size of next generation population and fitness of fittest individual
          DominionDeckGenome fittestGenome = population.get(0);
          int deckCost = fittestGenome.getCost();
          System.out.println("Generation: " + Counter);
          System.out.println("Fittest Deck: " + fittestGenome.convertPhenoToString());
          System.out.println("Fitness: " + fittestGenome.getFitness());
          System.out.println("Deck Cost: " + deckCost);
          System.out.println("No of individuals remaining in population: " + population.size());
          System.out.println("No of feasible children generated this generation: "
              + noFeasibleChildrenInGeneration);
          System.out.println("No of cross-overs this generation: " + noCrossOvers);
          System.out.println("No of mutations this generation: " + noMutations);

          //create line for summary report file
          line.append(Counter + "," + fittestGenome.convertPhenoToString() + ","
              + fittestGenome.getFitness() + "," + deckCost
              + "," + population.size() + "," + noFeasibleChildrenInGeneration + "," + noCrossOvers
              + "," + noMutations + "\n");

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
          if(noFeasibleChildrenInGeneration == 0){
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

  public static DominionDeckGenome drawFromPopulation(ArrayList<DominionDeckGenome> population){
      //draw a sample from a population using the relative fitness of each sample
      //compared ot the whole population
      Random rnd = new Random(System.currentTimeMillis());

      //total fitness values across population
      double totalfitness = 0;
      for(DominionDeckGenome genome : population){
          totalfitness += genome.getFitness();
      }

      //randomly select a sample from the population
      for(DominionDeckGenome genome : population){
          double probOfSelection = genome.getFitness()/totalfitness;
          if (rnd.nextFloat() < probOfSelection){
              return genome;
          }
      }

      //if nothing chosen, just choose a random entry in the map
      return population.get(rnd.nextInt(population.size()));
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
