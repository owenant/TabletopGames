package test.games.descent;

import core.properties.PropertyString;
import games.descent2e.*;
import games.descent2e.actions.attack.Rage;
import games.descent2e.actions.attack.SurgeAttackAction;
import games.descent2e.actions.heroabilities.Greedy;
import games.descent2e.actions.heroabilities.LuckyCharm;
import games.descent2e.actions.heroabilities.PrayerOfHealing;
import games.descent2e.components.Hero;
import org.junit.*;
import utilities.Utils;

import java.util.*;

import static org.junit.Assert.*;

public class SkillTests {

    DescentGameState state;
    DescentForwardModel fm = new DescentForwardModel();

    @Before
    public void setup() {

    }

    @Test
    public void berserkerSkills() {
        int seed = 234;
        do {
            seed += 1001;
            state = new DescentGameState(new DescentParameters(seed), 2);
            fm.setup(state);
            for (Hero h : state.getHeroes()) {
                DescentTypes.HeroClass heroClass = Utils.searchEnum(DescentTypes.HeroClass.class, ((PropertyString) h.getProperty("class")).value);
                if (heroClass == DescentTypes.HeroClass.Berserker) {
                    assertEquals(2, h.getAbilities().stream().filter(a -> a instanceof SurgeAttackAction).count());
                    assertEquals(1, h.getAbilities().stream().filter(a -> a instanceof Rage).count());
                    return;
                }
            }
        } while (seed < 100000);

        throw new AssertionError("No Berserker found to execute tests");
    }

    @Test
    public void thiefSkills() {
        int seed = 234;
        do {
            seed += 1001;
            state = new DescentGameState(new DescentParameters(seed), 2);
            fm.setup(state);
            for (Hero h : state.getHeroes()) {
                DescentTypes.HeroClass heroClass = Utils.searchEnum(DescentTypes.HeroClass.class, ((PropertyString) h.getProperty("class")).value);
                if (heroClass == DescentTypes.HeroClass.Thief) {
                    assertEquals(3, h.getAbilities().size());
                    assertEquals(1, h.getAbilities().stream().filter(a -> a instanceof Greedy).count());
                    assertEquals(1, h.getAbilities().stream().filter(a -> a instanceof LuckyCharm).count());

                    return;
                }
            }
        } while (seed < 100000);

        throw new AssertionError("No Thief found to execute tests");
    }


    @Test
    public void discipleSkills() {
        int seed = 234;
        do {
            seed += 1001;
            state = new DescentGameState(new DescentParameters(seed), 2);
            fm.setup(state);
            for (Hero h : state.getHeroes()) {
                DescentTypes.HeroClass heroClass = Utils.searchEnum(DescentTypes.HeroClass.class, ((PropertyString) h.getProperty("class")).value);
                if (heroClass == DescentTypes.HeroClass.Disciple) {
                    assertEquals(3, h.getAbilities().size());
                    assertEquals(1, h.getAbilities().stream().filter(a -> a instanceof PrayerOfHealing).count());
                    return;
                }
            }
        } while (seed < 100000);

        throw new AssertionError("No Disciple found to execute tests");
    }


    @Test
    public void runemasterSkills() {
        int seed = 234;
        do {
            seed += 1001;
            state = new DescentGameState(new DescentParameters(seed), 2);
            fm.setup(state);
            for (Hero h : state.getHeroes()) {
                DescentTypes.HeroClass heroClass = Utils.searchEnum(DescentTypes.HeroClass.class, ((PropertyString) h.getProperty("class")).value);
                if (heroClass == DescentTypes.HeroClass.Runemaster) {
                    assertEquals(0, h.getAbilities().size());
                    return;
                }
            }
        } while (seed < 100000);

        throw new AssertionError("No Runemaster found to execute tests");
    }


}
