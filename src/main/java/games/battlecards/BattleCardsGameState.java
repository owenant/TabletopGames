package games.battlecards;

import static java.util.stream.Collectors.toList;

import core.AbstractGameState;
import core.AbstractParameters;
import core.CoreConstants;
import core.components.Component;
import core.components.Counter;
import core.components.Deck;
import core.interfaces.IGamePhase;
import games.GameType;
import games.battlecards.cards.BattleCardsBasicCard;
import games.loveletter.LoveLetterGameState;
import games.loveletter.cards.LoveLetterCard;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Random;

/**
 * <p>The game state encapsulates all game information. It is a data-only class, with game functionality present
 * in the Forward Model or actions modifying the state of the game.</p>
 * <p>Most variables held here should be {@link Component} subclasses as much as possible.</p>
 * <p>No initialisation or game logic should be included here (not in the constructor either). This is all handled externally.</p>
 * <p>Computation may be included in functions here for ease of access, but only if this is querying the game state information.
 * Functions on the game state should never <b>change</b> the state of the game.</p>
 */
public class BattleCardsGameState extends AbstractGameState {
    /**
     * @param gameParameters - game parameters.
     * @param nPlayers       - number of players in the game
     */
    BattleCardsParameters params;

    // List of cards in player hands
    List<Deck<BattleCardsBasicCard>> playerHandCards;
    // Discarded cards for each player
    List<Deck<BattleCardsBasicCard>> playerDiscardCards;
    // Cards in draw pile for each player
    List<Deck<BattleCardsBasicCard>> playerDrawPile;
    // Cards in central draw pile for deck building between turns
    Deck<BattleCardsBasicCard> deckConstructionPile;
    //Indicates the card chosen by the players for this turn, saved for simultaneous execution
    Deck<BattleCardsBasicCard> playedCards;
    Counter[] playerScore;
    Counter[] playerHealth;
    Counter[] playerStamina;
    //stores for each player who they are targeting
    Integer[] playerTarget;
    public enum BattleCardsGamePhase implements IGamePhase {
        Play,
        Resolve,
        Construct
    }
    Random rnd;

    public BattleCardsGameState(AbstractParameters gameParameters, int nPlayers) {
        super(gameParameters, nPlayers);
        params = (BattleCardsParameters) gameParameters;
    }

    /**
     * @return the enum value corresponding to this game, declared in {@link GameType}.
     */
    @Override
    protected GameType _getGameType() {
        return GameType.BattleCards;
    }

    /**
     * Returns all Components used in the game and referred to by componentId from actions or rules.
     * This method is called after initialising the game state, so all components will be initialised already.
     *
     * @return - List of Components in the game.
     */
    @Override
    protected List<Component> _getAllComponents() {
        List<Component> components = new ArrayList<>();
        components.addAll(playerHandCards);
        components.addAll(playerDiscardCards);
        components.addAll(playerDrawPile);
        components.add(deckConstructionPile);
        components.add(playedCards);
        return components;
    }

    /**
     * <p>Create a deep copy of the game state containing only those components the given player can observe.</p>
     * <p>If the playerID is NOT -1 and If any components are not visible to the given player (e.g. cards in the hands
     * of other players or a face-down deck), then these components should instead be randomized (in the previous examples,
     * the cards in other players' hands would be combined with the face-down deck, shuffled together, and then new cards drawn
     * for the other players).</p>
     * <p>If the playerID passed is -1, then full observability is assumed and the state should be faithfully deep-copied.</p>
     *
     * <p>Make sure the return type matches the class type, and is not AbstractGameState.</p>
     *
     * @param playerId - player observing this game state.
     */
    @Override
    protected BattleCardsGameState _copy(int playerId) {
        BattleCardsGameState copiedState = new BattleCardsGameState(gameParameters, getNPlayers());
        // Deep copy all variables to the new game state, that are always visible from perspective
        //of all players
        copiedState.playerScore = new Counter[getNPlayers()];
        copiedState.playerHealth = new Counter[getNPlayers()];
        copiedState.playerStamina = new Counter[getNPlayers()];
        for (int p = 0; p < getNPlayers(); p++) {
            copiedState.playerScore[p] = playerScore[p].copy();
            copiedState.playerHealth[p] = playerHealth[p].copy();
            copiedState.playerStamina[p] = playerStamina[p].copy();
        }

        //Note that the player discard piles should just also be deep copied, as although face down
        //other players will have observed those cards being drawn in previous turns (does this
        //give the AI an advantage of perfect memory?)
        for (int p = 0; p < getNPlayers(); p++) {
            copiedState.playerDiscardCards.set(p, playerDiscardCards.get(p).copy());
        }

        //next deal with the variables that can be hidden dependent on who the player is
        if (playerId == -1) {
            //if playerId is -1 we just deep copy everything that is not dealt with already above
            for (int p = 0; p < getNPlayers(); p++) {
                copiedState.playerHandCards.add(
                    playerHandCards.get(p).copy()); //does this do a deep copy?
                copiedState.playerDrawPile.add(playerDrawPile.get(p).copy());
            }
            copiedState.deckConstructionPile = deckConstructionPile.copy();
            copiedState.playedCards = playedCards.copy();
            copiedState.playerTarget = new Integer[getNPlayers()];
            for (int p = 0; p < getNPlayers(); p++) {
                copiedState.playerTarget[p] = playerTarget[p];
            }
        } else {
            //Now we need to randomise anything hidden from the player

            //Firstly, We need to shuffle the hands of other players with their own draw deck and then redraw
            for (int p = 0; p < getNPlayers(); p++) {
                //TODO:I am assuming playerID is an int from 0 to no of players minus 1 is this true?
                if (p != playerId) {
                    // Add player hands unseen back into their own draw pile
                    copiedState.playerDrawPile.get(p).add(playerHandCards.get(p));
                    //next we shuffle the players draw pile
                    copiedState.playerDrawPile.get(p).shuffle(rnd);
                    //then draw cards back into the player's hand
                    copiedState.playerHandCards.set(p, playerHandCards.get(p).copy());
                    Deck<BattleCardsBasicCard> hand = copiedState.playerHandCards.get(p);
                    int handSize = hand.getSize();
                    hand.clear();
                    for (int i = 0; i < handSize; i++) {
                        hand.add(copiedState.playerDrawPile.get(p).draw());
                    }
                } else {
                    //for the player's own cards they remain visible and we do a standard deep copy
                    copiedState.playerHandCards.set(playerId, playerHandCards.get(playerId).copy());
                    //we also need to shuffle their draw pile
                    copiedState.playerDrawPile.get(playerId).shuffle(rnd);
                }
            }
            //also the construction deck should be shuffled, as it also represents
            // hidden information for all players
            copiedState.deckConstructionPile = deckConstructionPile.copy();
            copiedState.deckConstructionPile.shuffle(rnd);

            // We don't know what other players have chosen for this round, hide card choices for this round
            copiedState.playedCards = playedCards.copy();
            copiedState.playedCards.clear();
            for (int p = 0; p < getNPlayers(); p++) {
                if (p != playerId) {
                    //return played card to players hand and choose another one randomly
                    copiedState.playerHandCards.get(p).add(playedCards.get(p));
                    copiedState.playedCards.setComponent(p,
                        copiedState.playerHandCards.get(p).pick(rnd));
                } else {
                    //maintain choice for player with ID given by playerId
                    copiedState.playedCards.setComponent(playerId, playedCards.get(playerId));
                }
            }

            //Finally we need to hide the player target for all players other than the one
            //with ID given by playerId
            copiedState.playerTarget = new Integer[getNPlayers()];
            //create a set of integers corresponding to players to randomly draw from
            List<Integer> targets = Arrays.asList(new Integer[getNPlayers()]);
            for (int p = 0; p < getNPlayers(); p++) {
                targets.set(p,p);
            }
            //now randomly select from this set
            for (int p = 0; p < getNPlayers(); p++) {
                if (p != playerId) {
                    int nextInt = rnd.nextInt(targets.size());
                    playerTarget[p] = targets.get(nextInt);
                    targets.remove(nextInt);
                } else {
                    //maintain visibility of players own choice
                    copiedState.playerTarget[playerId] = playerTarget[playerId];
                }
            }
        }
        return copiedState;
    }

    /**
     * @param playerId - player observing the state.
     * @return a score for the given player approximating how well they are doing (e.g. how close they are to winning
     * the game); a value between 0 and 1 is preferred, where 0 means the game was lost, and 1 means the game was won.
     */
    @Override
    protected double _getHeuristicScore(int playerId) {
        if (isNotTerminal()) {
            return playerScore[playerId].getValue()/params.NO_PTS_TO_WIN;
        } else {
            // The game finished, we can instead return the actual result of the game for the given player.
            return getPlayerResults()[playerId].value;
        }
    }

    /**
     * @param playerId - player observing the state.
     * @return the true score for the player, according to the game rules. May be 0 if there is no score in the game.
     */
    @Override
    public double getGameScore(int playerId) {
        //What is this player's score (if any)?
        return playerScore[playerId].getValue();
    }

    @Override
    protected boolean _equals(Object o) {
        //compare all variables in the state
        if (this == o) return true;
        if (!(o instanceof BattleCardsGameState)) return false;
        if (!super.equals(o)) return false;
        BattleCardsGameState that = (BattleCardsGameState) o;
        return Objects.equals(playerHandCards, that.playerHandCards) &&
            Objects.equals(playerDiscardCards, that.playerDiscardCards) &&
            Objects.equals(playerDrawPile, that.playerDrawPile) &&
            Objects.equals(deckConstructionPile, that.deckConstructionPile) &&
            Objects.equals(playedCards, that.playedCards) &&
            Objects.equals(playerScore, that.playerScore) &&
            Objects.equals(playerHealth, that.playerHealth) &&
            Objects.equals(playerStamina, that.playerStamina) &&
            Arrays.equals(playerTarget, that.playerTarget);
    }

    @Override
    public int hashCode() {
        //include the hash code of all variables. Copied and adapted from loveletters code
        int result = Objects.hash(super.hashCode(), playerHandCards, playerDiscardCards, playerDrawPile,
            deckConstructionPile, playedCards, playerScore, playerHealth, playerStamina);
        //TODO: why 31 here?
        result = 31 * result + Arrays.hashCode(playerTarget);
        return result;
    }

    /**
     * Sets this player as KO'd and updates game and player status
     * @param attackingPlayer - ID of player doing the attack
     * @param targetPlayer - ID of player KOd
     */
    public void playerKO(int attackingPlayer, int targetPlayer){
        // a losing player needs to discard all cards
        while (playerHandCards.get(targetPlayer).getSize() > 0)
            playerDiscardCards.get(targetPlayer).add(playerHandCards.get(targetPlayer).draw());

        logEvent("KO'd player: " + attackingPlayer + "," + targetPlayer);
    }

    /**
     * Prints the game state.
     */
    public void printToConsole() {
        System.out.println("======================");
        System.out.println("BattleCards Game-State");
        System.out.println("----------------------");

        for (int i = 0; i < getNPlayers(); i++){
            if (getCurrentPlayer() == i)
                System.out.print(">>> Player " + i + ":");
            else
                System.out.print("Player " + i + ": ");
            System.out.print(playerHandCards.get(i));
            System.out.print(";\t Discarded: ");
            System.out.print(playerDiscardCards.get(i));
            System.out.print(";\t Draw pile: ");
            System.out.print(playerDrawPile.get(i));

            System.out.print(";\t Health: ");
            System.out.print(playerHealth[i].getValue());
            System.out.print(";\t Stamina: ");
            System.out.print(playerStamina[i].getValue());
            System.out.print(";\t Score: ");
            System.out.println(playerScore[i].getValue());
        }
        System.out.println("\nDeck Construction Pile" + ":" + deckConstructionPile.toString());

        System.out.println("Current GamePhase: " + gamePhase);
        System.out.println("======================");
    }
}
