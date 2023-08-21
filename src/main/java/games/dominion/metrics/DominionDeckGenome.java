package games.dominion.metrics;

import core.AbstractGameState;
import core.AbstractPlayer;
import core.Game;
import core.actions.AbstractAction;
import core.components.PartialObservableDeck;
import core.components.Deck;
import games.GameType;
import games.dominion.DominionConstants.DeckType;
import games.dominion.DominionFGParameters;
import games.dominion.DominionForwardModel;
import games.dominion.DominionGameState;
import games.dominion.cards.CardType;
import games.dominion.cards.DominionCard;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import players.mcts.MCTSPlayer;
import players.simple.RandomPlayer;

public class DominionDeckGenome implements Comparable<DominionDeckGenome>{
  private String genotype;
  private int[] phenotype;
  private double fitness;
  private boolean fitnessCalculated;
  public static int MAX_NO_OF_CARDS_OF_ANY_TYPE = 5;
  public static int NO_SIMULATIONS_EXPPAYOFF = 1;
  public static int MAX_COST_CONSTRAINT = 80;
  public static int MIN_COST_CONSTRAINT = 60;
  public static CardType[] CARD_TYPES = {CardType.CELLAR, CardType.MARKET, CardType.MERCHANT,CardType.MILITIA,
      CardType.MINE,CardType.MOAT,CardType.SMITHY,CardType.VILLAGE,CardType.GOLD,
      CardType.SILVER, CardType.COPPER};

  public DominionDeckGenome(String genome){
    genotype = new String(genome);

    //convert genotype of ones and zeros to phenotype, i.e vector of integers
    //representing number of cards of each type

    //number of bits per integer is dependent on integer upper bound
    int noBitsPerInt = (int)Math.floor(Math.log(MAX_NO_OF_CARDS_OF_ANY_TYPE)/Math.log(2)+1);

    //check genotype is compatible with maxInt constraint
    if (genotype.length() != CARD_TYPES.length * noBitsPerInt){
      System.out.println("Error: incompatible genotype, cant convert to phenotype");
    }

    //parse genotype in groups of noBitsPerInt bits
    phenotype = new int[CARD_TYPES.length];
    for (int i = 0; i < CARD_TYPES.length; i++){
      String singleIntSubString = genotype.substring(i*noBitsPerInt, (i+1)*noBitsPerInt);
      //convert bits back to integer
      phenotype[i] = Integer.parseInt(singleIntSubString, 2);
    }

    fitnessCalculated = false;
  }

  public DominionDeckGenome(int[] phenome){
    phenotype = phenome;

    //convert phenotype - i.e. vector of integers representing number of each type of
    // card in deck to genotype which is a vector of ones and zeros

    //check phenotype is compatible with maxInt constraint
    for(int i = 0; i < phenotype.length; i++){
      if (phenotype[i] > MAX_NO_OF_CARDS_OF_ANY_TYPE){
        System.out.println("Error - maxInt constraint violated");
      }
    }

    //number of bits per integer is dependent on integer upper bound
    int noBitsPerInt = (int)Math.floor(Math.log(MAX_NO_OF_CARDS_OF_ANY_TYPE)/Math.log(2)+1);

    //convert each integer to a sequence of four bits and add to genotype
    genotype = new String();
    String allele = new String();
    for(int i = 0; i < phenotype.length; i++){
      allele = Integer.toBinaryString(phenotype[i]);
      while(allele.length() < noBitsPerInt){
        //pad with zeros so we have noBitsPerInt bits for ever integer
        allele = "0" + allele;
      }
      genotype += allele;
    }

    fitnessCalculated = false;
  }
  public double getFitness(){
    if (fitnessCalculated == false){
      fitness = calcFitness();
      fitnessCalculated = true;
    }
    return fitness;
  }

  public String getGenotype(){
    return genotype;
  }

  public int[] getPhenotype(){
    return phenotype;
  }

  public boolean equals(DominionDeckGenome otherGenome){
    if (genotype.equals(otherGenome.genotype)){
      return true;
    }else{
      return false;
    }
  }

  public String convertPhenoToString(){
    //convert phenotype int[] to string, helps with displaying results
    String pheno = "[";
    for(int i = 0; i < phenotype.length; i++){
      if(i == (phenotype.length-1)){
        pheno+= phenotype[i] + "]";
      }else{
        pheno+= phenotype[i] + ",";
      }
    }
    return pheno;
  }

  public int compareTo(DominionDeckGenome otherGenome){
    if (fitness > otherGenome.fitness){
      return 1;
    }else if (fitness < otherGenome.fitness){
      return -1;
    }else{
      return 0;
    }
  }
  private double calcFitness(){
    //provides fitness function for deck
    //for now driven by treasures, but could be update to give credit to things like
    //card trashing or automatically gaining a new card etc. They could be translated into
    //an approximate treasure worth

    //check to see if genotype is compatible with cost constraint
    int deckCost = getCost();
    if (deckCost >= MAX_COST_CONSTRAINT || deckCost <= MIN_COST_CONSTRAINT){
      return 0;
    }else {
      //long startTime = System.currentTimeMillis();

      //set-up Dominion game and state

      //start by setting up MCTS player which will be used to play the deck
      //parameters need to be chosen here so that the cards are played in an
      //optimal order but we also dont need to see impact of future turns
      AbstractPlayer focusAIAgent = new MCTSPlayer();
      List<AbstractPlayer> players = Arrays.asList(focusAIAgent, new RandomPlayer());
      DominionFGParameters params = new DominionFGParameters(new Random(System.currentTimeMillis()).nextInt());
      DominionGameState state = new DominionGameState(params, players.size());
      DominionForwardModel fm = new DominionForwardModel();
      //note creating a game initialises the state
      Game game = new Game(GameType.Dominion, players, fm, state);

      //create player draw deck
      boolean[] notVisibleToEitherPlayer = {false, false};
      //focus player is the player whose deck we will be evolving
      int focusPlayer = state.getCurrentPlayer();
      PartialObservableDeck<DominionCard> draw = new PartialObservableDeck<DominionCard>("player_draw", focusPlayer, notVisibleToEitherPlayer);
      for(int i =0; i < phenotype.length; i++) {
        for(int j = 0; j < phenotype[i]; j++){
          draw.add(DominionCard.create(CARD_TYPES[i]),notVisibleToEitherPlayer);
        }
      }
      state.setDeck(DeckType.DRAW,focusPlayer,draw);

      //make sure there are no cards in hand
      boolean[] visibleToFocusPlayer = {true, false};
      PartialObservableDeck<DominionCard> hand = new PartialObservableDeck<DominionCard>("player_hand", focusPlayer, visibleToFocusPlayer);
      state.setDeck(DeckType.HAND,focusPlayer,hand);

      //store a copy of this starting state for this deck
      DominionGameState startState = (DominionGameState) state.copy();

      int noObsOfPayOffs = 0;
      double expectedPayout = 0;
      while(noObsOfPayOffs <= NO_SIMULATIONS_EXPPAYOFF){
        //if start of round shuffle draw deck and draw cards
        if(state.getGamePhase() == DominionGameState.DominionGamePhase.Play && state.getCurrentPlayer() == focusPlayer &&
            state.getDeck(DeckType.HAND, focusPlayer).getSize() == 0) {
          //shuffle draw deck for focus player
          state.getDeck(DeckType.DRAW, focusPlayer).shuffle(new Random(System.currentTimeMillis()));

          //start by drawing cards into hand
          for (int i = 0; i < params.HAND_SIZE; i++) {
            state.drawCard(focusPlayer);
          }
        }

        //compute next action for current player
        int currentPlayer = state.getCurrentPlayer();
        AbstractGameState observation = state.copy(currentPlayer);
        List<AbstractAction> possibleActions = fm.computeAvailableActions(observation);
        AbstractAction aiAction = players.get(currentPlayer)
            .getAction(observation, possibleActions);

        //if in buy phase for focus player figure out what the payoff was
        if (observation.getGamePhase() == DominionGameState.DominionGamePhase.Buy && state.getCurrentPlayer() == focusPlayer){
          //grab total treasure value
          int payout = state.availableSpend(focusPlayer);
          expectedPayout += payout;
          //System.out.println(payout);

          //reset state, ready to repeat
          state = (DominionGameState) startState.copy();

          //keep a counter for a sense check
          noObsOfPayOffs += 1;
        }else{
          //otherwise perform the AI action
          fm.next(state, aiAction);
        }
      }
      //compute final expected payoff
      expectedPayout = expectedPayout/(noObsOfPayOffs*1.0);

      return expectedPayout;
    }
  }
  public int getCost() {
    int cost = 0;
    for (int i = 0; i < phenotype.length; i++){
      cost += phenotype[i] * CARD_TYPES[i].cost;
    }
    return cost;
  }
  public static ArrayList<DominionDeckGenome> crossOver(DominionDeckGenome parent1, DominionDeckGenome parent2){
    //cross-over genes of two parents to make two new off spring

    //we split both genotypes down the middle and create new off spring
    int midPoint = (int)Math.floor(parent1.genotype.length()/2);
    String child1 = parent1.genotype.substring(0,midPoint) + parent2.genotype.substring(midPoint, parent2.genotype.length());
    String child2 = parent2.genotype.substring(0,midPoint) + parent1.genotype.substring(midPoint, parent1.genotype.length());

    ArrayList<DominionDeckGenome> children = new ArrayList<DominionDeckGenome>();
    children.add(new DominionDeckGenome(child1));
    children.add(new DominionDeckGenome(child2));

    return children;
  }
  public static DominionDeckGenome mutate(DominionDeckGenome genome){
    //randomly flip a bit in genotype and return a new genome
    //Random rnd = new Random(System.currentTimeMillis());
    Random rnd = new Random(100);
    int rndIndex = rnd.nextInt(genome.genotype.length());

    String newGenotype = new String();
    if(genome.genotype.charAt(rndIndex) == '0'){
      newGenotype = genome.genotype.substring(0,rndIndex) + '1' + genome.genotype.substring(rndIndex+1, genome.genotype.length());
    }else{
      newGenotype = genome.genotype.substring(0,rndIndex) + '0' + genome.genotype.substring(rndIndex+1, genome.genotype.length());
    }

    return new DominionDeckGenome(newGenotype);
  }
}
