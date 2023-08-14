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
import java.util.HashMap;
import java.util.Random;
import java.util.Set;
import java.util.HashSet;
import java.lang.Math;

import games.dominion.cards.CardType;
import games.dominion.cards.DominionCard;
import games.dominion.DominionConstants.DeckType;
import players.PlayerFactory;
import players.mcts.BasicMCTSPlayer;
import players.mcts.MCTSPlayer;
import players.rmhc.RMHCPlayer;
import players.simple.OSLAPlayer;
import players.simple.RandomPlayer;
import java.util.Collections;

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

      //set-total cost amount
      int totalCost = 30;

      //A deck will be represented by a chromosome (vector) consisting of a collection
      // of 17 genes which are integers representing the number of cards of a given
      //card type in the deck. Here we are assuming we are using the base collection of cards

      //create initial population at random filtering out those that dont satisfy
      //the cost constraints
      int noIndividuals = 50;
      int maxIterations = 1000000;
      long seed = 100;
      ArrayList<DominionDeckGenome> parents = genInitialPopulation(noIndividuals, maxIterations, seed);

      //start loop to evolve population, finish when termination condition is achieved
      Random rnd = new Random(System.currentTimeMillis());
      double probCrossOver = 0.8;
      double probMutation = 0.05;
      int noGenerations = 10;
      int Counter = 0;
      while (Counter < noGenerations){
          ArrayList<DominionDeckGenome> children = new ArrayList<DominionDeckGenome>();
          for( int pair = 0; pair < (int)Math.floor(parents.size()/2); pair++ ) {
              //draw pairs of individuals randomly from the population
              DominionDeckGenome parent1 = drawFromPopulation(parents);
              DominionDeckGenome parent2 = drawFromPopulation(parents);

              //cross-over pairs with a probability probCrossOver
              DominionDeckGenome child1 = new DominionDeckGenome(parent1.getGenotype());
              DominionDeckGenome child2 = new DominionDeckGenome(parent2.getGenotype());
              if (rnd.nextFloat() < probCrossOver){
                  ArrayList<DominionDeckGenome> childList = DominionDeckGenome.crossOver(parent1, parent2);
                  child1 = childList.get(0);
                  child2 = childList.get(1);
              }
              //mutate each new child with probability probMutation
              if(rnd.nextFloat() < probMutation){
                  child1 = DominionDeckGenome.mutate(child1);
              }
              if(rnd.nextFloat() < probMutation){
                  child2 = DominionDeckGenome.mutate(child2);
              }
              //add to child population
              children.add(child1);
              children.add(child2);
          }

          //once child population is complete combine with previous population to generate next generation
          for (DominionDeckGenome child : children){
              parents.add(child);
          }

          //sort population by fitness
          Collections.sort(parents);

          //and take the top noIndividuals in terms of fitness as the new population
          parents = (ArrayList<DominionDeckGenome>)parents.subList(0,noIndividuals);

          //output size of next generation population and fitness of fittest individual
          DominionDeckGenome fittestGenome = parents.get(0);
          System.out.println("Fittest Genome: " + fittestGenome.getFitness());
          System.out.println("No of individuals remaining in population: " + parents.size());

          //increase generation counter
          Counter++;
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
          if(genome.getCost() == DominionDeckGenome.COST_CONSTRAINT){
              pop.add(genome);
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
