package games.descent2e.actions.heroabilities;

import core.AbstractGameState;
import games.descent2e.DescentGameState;
import games.descent2e.actions.DescentAction;
import games.descent2e.actions.Triggers;

public class LuckyCharm extends DescentAction {

    public LuckyCharm() {
        super(Triggers.ROLL_OWN_DICE);
    }

    @Override
    public String getString(AbstractGameState gameState) {
        throw new AssertionError("Not yet implemented");

    }

    @Override
    public boolean execute(DescentGameState gs) {
        throw new AssertionError("Not yet implemented");

    }

    @Override
    public DescentAction copy() {
        throw new AssertionError("Not yet implemented");
    }

    @Override
    public boolean canExecute(DescentGameState dgs) {
        throw new AssertionError("Not yet implemented");

    }
}
