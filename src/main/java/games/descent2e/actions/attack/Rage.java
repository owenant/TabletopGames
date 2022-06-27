package games.descent2e.actions.attack;

import core.AbstractGameState;
import core.actions.AbstractAction;
import core.components.BoardNode;
import core.interfaces.IExtendedSequence;
import games.descent2e.DescentGameState;
import games.descent2e.actions.DescentAction;
import games.descent2e.actions.Triggers;
import games.descent2e.components.Figure;
import games.descent2e.components.Monster;
import utilities.Vector2D;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Rage extends DescentAction implements IExtendedSequence {

    boolean targetChosen;
    final int actingfigureId;
    final int playerId;

    public Rage(int playerId, int actingFigureId) {
        super(Triggers.ACTION_POINT_SPEND);
        this.actingfigureId = actingFigureId;
        this.playerId = playerId;
    }

    @Override
    public boolean execute(DescentGameState gs) {
        gs.setActionInProgress(this);
        return true;
    }


    @Override
    public boolean equals(Object other) {
        if (other instanceof Rage) {
            Rage o = (Rage) other;
            return targetChosen == o.targetChosen && actingfigureId == o.actingfigureId && playerId == o.playerId;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return 207240 + Objects.hash(targetChosen, playerId);
    }

    @Override
    public String getString(AbstractGameState gameState) {
        return toString();
    }

    @Override
    public String toString() {
        return "Rage";
    }

    @Override
    public List<AbstractAction> _computeAvailableActions(AbstractGameState state) {
        DescentGameState dgs = (DescentGameState) state;
        List<AbstractAction> actions = new ArrayList<>();
        Figure f = dgs.getActingFigure();
        if (f.getComponentID() != actingfigureId) {
            throw new AssertionError("We have a mismatch with the expected actingFigure : " + actingfigureId + " vs " + f.getComponentID());
        }
        Vector2D currentLocation = f.getPosition();
        BoardNode currentTile = dgs.getMasterBoard().getElement(currentLocation.getX(), currentLocation.getY());
        // Find valid neighbours in master graph - used for melee attacks

        List<Figure> neighbours = dgs.getAdjacentFigures(currentTile);
        for (Figure n : neighbours) {
            if (n instanceof Monster) {
                // Player attacks a monster
                MeleeAttack attack = new MeleeAttack(f.getComponentID(), n.getComponentID());
                attack.specialSauce = (a, s) -> {  // the special bits that trigger on execution
                    a.extraDamage += 1;
                    s.getActingFigure().changeFatigue(1);
                };
                actions.add(attack);
            }
        }
        return actions;
    }

    @Override
    public int getCurrentPlayer(AbstractGameState state) {
        return playerId;
    }

    @Override
    public void registerActionTaken(AbstractGameState state, AbstractAction action) {
        if (action instanceof MeleeAttack)
            targetChosen = true;
    }

    @Override
    public boolean executionComplete(AbstractGameState state) {
        return targetChosen;
    }

    @Override
    public Rage copy() {
        Rage retValue = new Rage(playerId, actingfigureId);
        retValue.targetChosen = targetChosen;
        return retValue;
    }

    @Override
    public boolean canExecute(DescentGameState dgs) {
        return true;
    }
}
