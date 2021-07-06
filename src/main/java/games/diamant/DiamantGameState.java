package games.diamant;

import core.AbstractGameState;
import core.AbstractParameters;
import core.components.Component;
import core.components.Counter;
import core.components.Deck;
import core.interfaces.IPrintable;
import games.GameType;
import games.diamant.cards.DiamantCard;

import java.util.*;

import static core.CoreConstants.PARTIAL_OBSERVABLE;

public class DiamantGameState extends AbstractGameState implements IPrintable {
    Deck<DiamantCard>          mainDeck;
    Deck<DiamantCard>          discardDeck;
    Deck<DiamantCard>          path;

    List<Counter> treasureChests;
    List<Counter> gemsInHand;
    List<Boolean> playerInCave;

    Counter nGemsOnPath;
    HashMap<DiamantCard.HazardType, Counter> nHazardsOnPath;

    /**
     * Constructor. Initialises some generic game state variables.
     *
     * @param gameParameters - game parameters.
     * @param nPlayers      - number of players for this game.
     */
    public DiamantGameState(AbstractParameters gameParameters, int nPlayers) {
        super(gameParameters, new DiamantTurnOrder(nPlayers, ((DiamantParameters)gameParameters).nCaves), GameType.Diamant);
    }

    @Override
    protected List<Component> _getAllComponents() {
        return new ArrayList<Component>() {{
            add(mainDeck);
            add(discardDeck);
            add(path);
            add(nGemsOnPath);
            addAll(treasureChests);
            addAll(gemsInHand);
            addAll(nHazardsOnPath.values());
        }};
    }

    @Override
    protected AbstractGameState _copy(int playerId)
    {
        Random r = new Random(getGameParameters().getRandomSeed());

        DiamantGameState dgs = new DiamantGameState(gameParameters.copy(), getNPlayers());

        dgs.mainDeck    = mainDeck.copy();
        dgs.discardDeck = discardDeck.copy();
        dgs.path        = path.copy();

        dgs.nGemsOnPath             = nGemsOnPath;

        dgs.gemsInHand = new ArrayList<>();
        dgs.treasureChests = new ArrayList<>();
        dgs.playerInCave   = new ArrayList<>(playerInCave);
        dgs.nGemsOnPath = nGemsOnPath.copy();
        dgs.nHazardsOnPath = new HashMap<>();
        for (DiamantCard.HazardType ht: nHazardsOnPath.keySet()) {
            dgs.nHazardsOnPath.put(ht, nHazardsOnPath.get(ht).copy());
        }

        for (Counter c : gemsInHand)
            dgs.gemsInHand.add(c.copy());

        for (Counter c : treasureChests)
            dgs.treasureChests.add(c.copy());

        // mainDeck and is actionsPlayed are hidden.
        if (PARTIAL_OBSERVABLE && playerId != -1)
        {
            dgs.mainDeck.shuffle(new Random(getGameParameters().getRandomSeed()));
            for (int i = 0; i < getNPlayers(); i++) {
                if (i != playerId) {
                    dgs.treasureChests.get(i).setValue(0);
                }
            }

            // TODO: We also should remove the history entries for the removed actions
            // This is not formally necessary, as nothing currently uses this information, but in
            // a competition setting for example, it would be critical. There is no simple way to do this at the moment
            // because history (as part of the super-class) is only copied after we return from this _copy() method.

           // Randomize actions for other players (or any other modelling approach)
            // is now the responsibility of the deciding agent (see for example OSLA)

        }
        return dgs;
    }

    @Override
    protected double _getHeuristicScore(int playerId)
    {
        return new DiamantHeuristic().evaluateState(this, playerId);
    }
    /**
     * This provides the current score in game turns. This will only be relevant for games that have the concept
     * of victory points, etc.
     * If a game does not support this directly, then just return 0.0
     *
     * @param playerId - ID of player
     * @return - double, score of current state
     */
    @Override
    public double getGameScore(int playerId) {
         return treasureChests.get(playerId).getValue() + gemsInHand.get(playerId).getValue();
    }

    @Override
    protected ArrayList<Integer> _getUnknownComponentsIds(int playerId)
    {
        ArrayList<Integer> ids = new ArrayList<>();
        ids.add(mainDeck.getComponentID());
        for (int i = 0; i < getNPlayers(); i++) {
            if (i != playerId) {
                ids.add(treasureChests.get(i).getComponentID());
            }
        }
        return ids;
    }

    @Override
    protected void _reset() {
        mainDeck       = null;
        discardDeck    = null;
        path           = null;
        treasureChests = new ArrayList<>();
        gemsInHand = new ArrayList<>();
        playerInCave   = new ArrayList<>();
        nGemsOnPath = null;
        nHazardsOnPath = null;
    }

    @Override
    public boolean _equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DiamantGameState)) return false;
        if (!super.equals(o)) return false;
        DiamantGameState that = (DiamantGameState) o;
        return Objects.equals(mainDeck, that.mainDeck) && Objects.equals(discardDeck, that.discardDeck) && Objects.equals(path, that.path) && Objects.equals(treasureChests, that.treasureChests) && Objects.equals(gemsInHand, that.gemsInHand) && Objects.equals(playerInCave, that.playerInCave) && Objects.equals(nGemsOnPath, that.nGemsOnPath) && Objects.equals(nHazardsOnPath, that.nHazardsOnPath);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), mainDeck, discardDeck, path, treasureChests, gemsInHand, playerInCave, nGemsOnPath, nHazardsOnPath);
    }

    /**
     * Returns the number of player already in the cave
    */

    public int getNPlayersInCave()
    {
        int n = 0;
        for (Boolean b: playerInCave)
            if (b) n++;
        return n;
    }

    @Override
    public void printToConsole() {
        String[] strings = new String[13];

        StringBuilder str_gemsOnHand          = new StringBuilder();
        StringBuilder str_gemsOnTreasureChest = new StringBuilder();
        StringBuilder str_playersOnCave       = new StringBuilder();

        for (Counter c: gemsInHand)          { str_gemsOnHand.         append(c.getValue()).append(" "); }
        for (Counter c:treasureChests) { str_gemsOnTreasureChest.append(c.getValue()).append(" "); }
        for (Boolean b : playerInCave)
        {
            if (b) str_playersOnCave.append("T");
            else   str_playersOnCave.append("F");
        }

        strings[0]  = "----------------------------------------------------";
        strings[1]  = "Cave:                       " + ((DiamantTurnOrder)getTurnOrder()).caveCounter;
        strings[2]  = "Players on Cave:            " + str_playersOnCave;
        strings[3]  = "Path:                       " + path.toString();
        strings[4]  = "Gems on Path:               " + nGemsOnPath;
        strings[5]  = "Gems on hand:               " + str_gemsOnHand;
        strings[6]  = "Gems on treasure chest:     " + str_gemsOnTreasureChest;
        strings[7]  = "Hazard scorpions in Path:   " + nHazardsOnPath.get(DiamantCard.HazardType.Scorpions).getValue()  + ", in Main deck: " + getNHazardCardsInMainDeck(DiamantCard.HazardType.Scorpions);
        strings[8]  = "Hazard snakes in Path:      " + nHazardsOnPath.get(DiamantCard.HazardType.Snakes).getValue()     + ", in Main deck: " + getNHazardCardsInMainDeck(DiamantCard.HazardType.Snakes);
        strings[9]  = "Hazard rockfalls in Path:   " + nHazardsOnPath.get(DiamantCard.HazardType.Rockfalls).getValue()  + ", in Main deck: " + getNHazardCardsInMainDeck(DiamantCard.HazardType.Rockfalls);
        strings[10] = "Hazard poisson gas in Path: " + nHazardsOnPath.get(DiamantCard.HazardType.PoisonGas).getValue() + ", in Main deck: " + getNHazardCardsInMainDeck(DiamantCard.HazardType.PoisonGas);
        strings[11] = "Hazard explosions in Path:  " + nHazardsOnPath.get(DiamantCard.HazardType.Explosions).getValue() + ", in Main deck: " + getNHazardCardsInMainDeck(DiamantCard.HazardType.Explosions);
        strings[12] = "----------------------------------------------------";

        for (String s : strings){
            System.out.println(s);
        }
    }

    private int getNHazardCardsInMainDeck(DiamantCard.HazardType ht)
    {
        int n = 0;
        for (int i=0; i<mainDeck.getSize(); i++)
        {
            if (mainDeck.get(i).getHazardType() == ht)
                n ++;
        }
        return n;
    }

    public Deck<DiamantCard> getMainDeck()       { return mainDeck;       }
    public Deck<DiamantCard> getDiscardDeck()    { return discardDeck;    }
    public List<Counter>     getGemsInHand()     { return gemsInHand;     }
    public List<Counter>     getTreasureChests() { return treasureChests; }
    public Deck<DiamantCard> getPath()           { return path;           }
    public Counter getnGemsOnPath() {
        return nGemsOnPath;
    }
    public HashMap<DiamantCard.HazardType, Counter> getnHazardsOnPath() {
        return nHazardsOnPath;
    }
    public boolean isPlayerInCave(int player) {
        return playerInCave.get(player);
    }
}
