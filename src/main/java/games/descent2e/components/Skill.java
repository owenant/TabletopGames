package games.descent2e.components;

import core.components.Card;
import core.properties.Property;
import core.properties.PropertyBoolean;
import core.properties.PropertyInt;
import games.descent2e.actions.DescentAction;

import java.lang.reflect.Constructor;

public class Skill {


    // Currently this is immutable, with no internal state
    // Items are not stored as such, but are a useful wrapper around the Component
    // to give programmatic access to key properties

    final int referenceComponent;
    final int fatigueCost;
    final boolean exhaustToUse;
    Class<? extends DescentAction> clazz;
    Constructor<? extends DescentAction> constructor;


    public Skill(Card data) {
        referenceComponent = data.getComponentID();
        Property actionClass = data.getProperty("actionClass");
        fatigueCost = data.getProperty("fatigue") == null ? 0 : ((PropertyInt) data.getProperty("fatigue")).value;
        exhaustToUse = data.getProperty("exhaust") == null ? false : ((PropertyBoolean) data.getProperty("exhaust")).value;
        if (actionClass != null) {
            try {
                clazz = (Class<? extends DescentAction>) Class.forName("games.descent2e.actions." + actionClass);
                constructor = clazz.getConstructor(int.class, int.class);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
                throw new AssertionError("Problem with class " + actionClass);
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
                throw new AssertionError("Constructor not found for " + actionClass);
            }
        }
        // Then generate the DescentAction

    }

    public DescentAction getAction(int player, int figure) {
        if (constructor == null) return null;
        try {
            return constructor.newInstance(player, figure);
        } catch (Exception e) {
            e.printStackTrace();
            throw new AssertionError("Construction failed for " + player + " : " + figure);
        }
    }
}
