package pandemic.engine.conditions;

import core.GameState;
import pandemic.engine.Node;

public abstract class ConditionNode extends Node {
    Node childYes, childNo;

    protected abstract boolean test(GameState gs);

    public final Node execute(GameState gs) {
        if (test(gs)) return childYes;
        else return childNo;
    }
    public final Node getNext() {
        return childYes;
    }

    public final void setNo(Node childNo) {
        this.childNo = childNo;
    }

    public final void setYes(Node childYes) {
        this.childYes = childYes;
    }
}
