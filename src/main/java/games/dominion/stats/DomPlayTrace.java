package games.dominion.stats;

import core.AbstractGameState;
import games.dominion.cards.CardType;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import players.heuristics.AbstractStateFeature;

public class DomPlayTrace extends AbstractStateFeature {

  String[] localNames = Arrays.stream(CardType.values()).map(CardType::name).toArray(String[]::new);
  List<CardType> cardTypes = Arrays.stream(CardType.values()).collect(Collectors.toList());

  public DomPlayTrace(){
  }

  @Override
  protected double[] localFeatureVector(AbstractGameState gs, int playerID) {
    return new double[0];
  }

  @Override
  protected double maxScore() {
    return 50;
  }

  @Override
  protected double maxRounds() {
    return 50;
  }

  @Override
  protected String[] localNames() {
    return localNames;
  }
}
