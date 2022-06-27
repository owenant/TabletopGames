package games.descent2e.components;

import core.AbstractForwardModel;
import core.actions.AbstractAction;
import core.components.Card;
import core.properties.Property;

public class Skill {


    // Currently this is immutable, with no internal state
    // Items are not stored as such, but are a useful wrapper around the Component
    // to give programmatic access to key properties

    final int referenceComponent;
    Class<? extends AbstractAction> clazz;

    public Skill(Card data) {
        referenceComponent = data.getComponentID();
        Property actionClass = data.getProperty("actionClass");
        Property fatigue = data.getProperty("fatigue");
        Property exhaust = data.getProperty("exhaust");
        if (actionClass != null) {
            try {
                clazz = (Class<? extends AbstractAction>) Class.forName("descent2e.actions." + actionClass);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
                throw new AssertionError("Problem with class " + actionClass);
            }
        }
        // TODO: Load in fatigue and exhaust
        // Then generate the DescentAction

    }

}
