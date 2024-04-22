package games.dominion.metrics;

import static games.dominion.DominionConstants.DeckType.DISCARD;
import static games.dominion.DominionConstants.DeckType.DRAW;
import static games.dominion.DominionConstants.DeckType.HAND;
import static games.dominion.DominionConstants.DeckType.TABLE;

import core.AbstractGameState;
import games.dominion.DominionGameState;
import games.dominion.cards.CardType;
import games.dominion.cards.DominionCard;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import players.heuristics.AbstractStateFeature;
import java.util.function.Function;

public class DomPlayTrace extends AbstractStateFeature {

  String[] localNames;
  List<CardType> cardTypes = Arrays.stream(CardType.values()).collect(Collectors.toList());

  public DomPlayTrace(){
  }

  @Override
  //state vector consisting of number of unique cards in deck
  protected double[] localFeatureVector(AbstractGameState gs, int playerID) {
    DominionGameState state = (DominionGameState) gs;
    double[] retValue = new double[cardTypes.size()];
    localNames = new String[cardTypes.size()];
    for (int i = 0; i < cardTypes.size(); i++) {
      retValue[i] = 0;
      localNames[i] = cardTypes.get(i).name();
      if (!(cardTypes.get(i).name().equals(CardType.VASSAL.name()) || cardTypes.get(i).name().equals(CardType.COUNCIL_ROOM.name())
          || cardTypes.get(i).name().equals(CardType.LIBRARY.name()) )){
        DominionCard tgtCard = DominionCard.create(cardTypes.get(i));
        Function<DominionCard, Integer> cardvaluer = (DominionCard c) -> {
          return tgtCard.equals(c) ? 1 : 0;};
        retValue[i] += state.getTotal(playerID, HAND, cardvaluer);
        retValue[i] += state.getTotal(playerID, DISCARD, cardvaluer);
        retValue[i] += state.getTotal(playerID, TABLE, cardvaluer);
        retValue[i] += state.getTotal(playerID, DRAW, cardvaluer);
      }else{
        //set to zero any cards that are not implemented
        retValue[i] = 0;
      }
    }

    return retValue;
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
