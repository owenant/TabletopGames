package games.dominion.stats;

import static evaluation.metrics.Event.GameEvent.ACTION_CHOSEN;

import core.AbstractForwardModel;
import core.AbstractGameState;
import core.AbstractPlayer;
import core.Game;
import core.actions.AbstractAction;
import core.interfaces.IActionFeatureVector;
import core.interfaces.IGameEvent;
import evaluation.listeners.MetricsGameListener;
import evaluation.metrics.AbstractMetric;
import evaluation.metrics.Event;
import evaluation.metrics.IMetricsCollection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class DomActionFeatures implements IMetricsCollection {

  /*
  public static class Actions extends AbstractMetric {

    Set<String> playerNames;

    public Actions() {
      super();
    }

    public Actions(Event.GameEvent... args) {
      super(args);
    }

    @Override
    public boolean _run(MetricsGameListener listener, Event e, Map<String, Object> records) {
      Game g = listener.getGame();
      AbstractForwardModel fm = g.getForwardModel();
      AbstractAction a = e.action.copy();
      AbstractPlayer currentPlayer = g.getPlayers().get(e.playerID);
      int size = fm.computeAvailableActions(e.state, currentPlayer.getParameters().actionSpace)
          .size();

      if (e.state.isActionInProgress()) {
        e.action = null;
      }

      records.put("Player-" + e.playerID, e.action == null ? null : e.action.toString());
      records.put(currentPlayer.toString(), e.action == null ? null : e.action.toString());
      records.put("Size-" + currentPlayer, size);

      records.put("Actions Played", e.action == null ? null : e.action.toString());
      records.put("Actions Played Description",
          e.action == null ? null : e.action.getString(e.state));
      records.put("Action Space Size", size);

      e.action = a;
      return true;
    }

    @Override
    public Set<IGameEvent> getDefaultEventTypes() {
      return Collections.singleton(ACTION_CHOSEN);
    }

    @Override
    public Map<String, Class<?>> getColumns(int nPlayersPerGame, Set<String> playerNames) {
      this.playerNames = playerNames;
      Map<String, Class<?>> columns = new HashMap<>();
      for (int i = 0; i < nPlayersPerGame; i++) {
        columns.put("Player-" + i, String.class);
      }
      for (String playerName : playerNames) {
        columns.put(playerName, String.class);
        columns.put("Size-" + playerName, Integer.class);
      }
      columns.put("Actions Played", String.class);
      columns.put("Actions Played Description", String.class);
      columns.put("Action Space Size", Integer.class);
      return columns;
    }
  }*/

  public static class ActionsReduced extends AbstractMetric {
    public ActionsReduced() {
      super();
    }

    public ActionsReduced(Event.GameEvent... args) {
      super(args);
    }

    @Override
    public boolean _run(MetricsGameListener listener, Event e, Map<String, Object> records) {
      Game g = listener.getGame();
      AbstractForwardModel fm = g.getForwardModel();
      AbstractPlayer currentPlayer = g.getPlayers().get(e.playerID);
      int size = fm.computeAvailableActions(e.state, currentPlayer.getParameters().actionSpace).size();

      records.put("Player", e.playerID);
      records.put("PlayerType", currentPlayer.toString());
      records.put("Size", size);

      records.put("Action", e.action == null ? null : e.action.toString());
      records.put("ActionClass", e.action.getClass().getSimpleName());
      records.put("ActionDescription", e.action == null ? null : e.action.getString(e.state));
      return true;
    }

    @Override
    public Set<IGameEvent> getDefaultEventTypes() {
      return Collections.singleton(ACTION_CHOSEN);
    }

    @Override
    public Map<String, Class<?>> getColumns(int nPlayersPerGame, Set<String> playerNames) {
      Map<String, Class<?>> columns = new HashMap<>();
      columns.put("Player", Integer.class);
      columns.put("PlayerType", String.class);
      columns.put("Size", Integer.class);
      columns.put("Action", String.class);
      columns.put("ActionClass", String.class);
      columns.put("ActionDescription", String.class);
      return columns;
    }
  }
}
