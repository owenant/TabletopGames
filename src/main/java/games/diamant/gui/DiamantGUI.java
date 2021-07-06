package games.diamant.gui;

import core.AbstractGUI;
import core.AbstractGameState;
import core.AbstractPlayer;
import core.Game;
import games.diamant.DiamantForwardModel;
import games.diamant.DiamantGameState;
import games.diamant.DiamantParameters;
import games.diamant.DiamantTurnOrder;
import gui.ScaledImage;
import players.human.ActionController;
import players.human.HumanGUIPlayer;
import utilities.ImageIO;
import utilities.Utils;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Collection;
import java.util.stream.IntStream;

import static core.CoreConstants.PAUSE_GUI_KEY_MOMENTS;
import static java.util.stream.Collectors.joining;

public class DiamantGUI extends AbstractGUI {
    // Settings for display areas
    final static int playerAreaWidth = 320;
    final static int playerAreaHeight = 135;
    final static int cardWidth = 90;
    final static int cardHeight = 115;

    // Width and height of total window
    int width, height;
    // List of player action chosen + gems in hand + total gems
    DiamantPlayerView[] playerViews;
    // Draw path view
    DiamantDeckView path;
    JLabel nGemsOnPath;
    JLabel nCave;

    // ID of human player
    int humanID;

    int highlightPlayerIdx = 0;

    // Border highlight of active player
    Border highlightActive = BorderFactory.createLineBorder(new Color(245, 162, 61), 3);
    Border[] playerViewBorders, playerViewBordersHighlight;

    DiamantGameState dgs;
    DiamantForwardModel fm;

    List<String> actionHistoryText;

    public DiamantGUI(Game game, ActionController ac, int humanID) {
        super(ac, 2);
        this.humanID = humanID;

        UIManager.put("TabbedPane.contentOpaque", false);
        UIManager.put("TabbedPane.opaque", false);
        UIManager.put("TabbedPane.tabsOpaque", false);

        if (game != null) {
            AbstractGameState gameState = game.getGameState();
            fm = (DiamantForwardModel) game.getForwardModel();
            if (gameState != null) {
                actionHistoryText = new ArrayList<>();

                dgs = (DiamantGameState) gameState.copy();
                JTabbedPane pane = new JTabbedPane();
                JPanel main = new JPanel();
                main.setOpaque(false);
                main.setLayout(new BorderLayout());
                JPanel rules = new JPanel();
                pane.add("Main", main);
                pane.add("Rules", rules);
                JLabel ruleText = new JLabel(getRuleText());
                rules.add(ruleText);

                // Find required size of window
                int nPlayers = gameState.getNPlayers();
                int nHorizAreas = 1 + (nPlayers <= 3 ? 2 : nPlayers == 4 ? 3 : nPlayers <= 8 ? 4 : 5);
                double nVertAreas = 4;
                this.width = playerAreaWidth * nHorizAreas;
                this.height = (int) (playerAreaHeight * nVertAreas);
                ruleText.setPreferredSize(new Dimension(width*2/3+60, height*2/3+100));

                ScaledImage backgroundImage = new ScaledImage(ImageIO.GetInstance().getImage("data/diamant/jamil-dar-mayanbg.jpg"), width, height, this);
                setContentPane(backgroundImage);

                DiamantParameters dp = (DiamantParameters) gameState.getGameParameters();

                // Create main game area that will hold all game views
                playerViews = new DiamantPlayerView[nPlayers];
                playerViewBorders = new Border[nPlayers];
                playerViewBordersHighlight = new Border[nPlayers];
                JPanel mainGameArea = new JPanel();
                mainGameArea.setLayout(new BorderLayout());
                mainGameArea.setOpaque(false);

                // Player hands go on the edges
                String[] locations = new String[]{BorderLayout.NORTH, BorderLayout.EAST, BorderLayout.SOUTH, BorderLayout.WEST};
                JPanel[] sides = new JPanel[]{new JPanel(), new JPanel(), new JPanel(), new JPanel()};
                int next = 0;
                for (int i = 0; i < nPlayers; i++) {
                    DiamantPlayerView playerView = new DiamantPlayerView(i, humanID, "data/diamant/");
                    playerView.setOpaque(false);

                    // Get agent name
                    String[] split = game.getPlayers().get(i).getClass().toString().split("\\.");
                    String agentName = split[split.length - 1];

                    // Create border, layouts and keep track of this view
                    TitledBorder title = BorderFactory.createTitledBorder(
                            BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), "Player " + i + " [" + agentName + "]",
                            TitledBorder.CENTER, TitledBorder.BELOW_BOTTOM);
                    playerViewBorders[i] = BorderFactory.createCompoundBorder(highlightActive,title);
                    playerViewBordersHighlight[i] = BorderFactory.createTitledBorder(
                                    BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), "Player " + i + " [" + agentName + "] - Out of cave",
                                    TitledBorder.CENTER, TitledBorder.BELOW_BOTTOM);
                    playerView.setBorder(playerViewBorders[i]);

                    sides[next].setOpaque(false);
                    sides[next].add(playerView);
                    sides[next].setLayout(new GridBagLayout());
                    next = (next + 1) % (locations.length);
                    playerViews[i] = playerView;
                }
                for (int i = 0; i < locations.length; i++) {
                    mainGameArea.add(sides[i], locations[i]);
                }

                // Discard and draw piles go in the center
                JPanel centerArea = new JPanel();
                centerArea.setOpaque(false);
                centerArea.setLayout(new BoxLayout(centerArea, BoxLayout.Y_AXIS));
                path = new DiamantDeckView(-1, dgs.getPath(), true, "data/diamant/",
                        new Rectangle(0, 0, playerAreaWidth, cardHeight));
                nGemsOnPath = new JLabel("# Gems on path: 0");
                centerArea.add(new JLabel("Path:"));
                centerArea.add(path);
                centerArea.add(nGemsOnPath);
                JPanel jp = new JPanel();
                jp.setOpaque(false);
                jp.setLayout(new GridBagLayout());
                jp.add(centerArea);
                mainGameArea.add(jp, BorderLayout.CENTER);

                // Top area will show state information
                JPanel infoPanel = createGameStateInfoPanel("Diamant", gameState, width, defaultInfoPanelHeight);
                infoPanel.setOpaque(false);
                // Bottom area will show actions available
                JComponent actionPanel = createActionPanel(new Collection[0], width, defaultActionPanelHeight, false);
                actionPanel.setOpaque(false);

                main.add(infoPanel, BorderLayout.NORTH);
                main.add(mainGameArea, BorderLayout.CENTER);
                main.add(actionPanel, BorderLayout.SOUTH);

                getContentPane().add(pane, BorderLayout.CENTER);
            }
        }

        setFrameProperties();
    }


    @Override
    protected JPanel createGameStateInfoPanel(String gameTitle, AbstractGameState gameState, int width, int height) {
        JPanel gameInfo = new JPanel();
        gameInfo.setOpaque(false);
        gameInfo.setLayout(new BoxLayout(gameInfo, BoxLayout.Y_AXIS));
        gameInfo.add(new JLabel("<html><h1>" + gameTitle + "</h1></html>"));

        nCave = new JLabel("Cave: 0 / " + ((DiamantParameters)gameState.getGameParameters()).nCaves);

        updateGameStateInfo(gameState);

        gameInfo.add(gameStatus);
        gameInfo.add(playerStatus);
        gameInfo.add(gamePhase);
        gameInfo.add(turnOwner);
        gameInfo.add(turn);
        gameInfo.add(currentPlayer);
        gameInfo.add(nCave);

        gameInfo.setPreferredSize(new Dimension(width/2 - 10, height));

        JPanel wrapper = new JPanel();
        wrapper.setOpaque(false);
        wrapper.setLayout(new FlowLayout());
        wrapper.add(gameInfo);

        historyInfo.setOpaque(false);
        historyInfo.setPreferredSize(new Dimension(width/2 - 10, height));
        historyContainer = new JScrollPane(historyInfo);
        historyContainer.setOpaque(false);
//        historyContainer.getViewport().setOpaque(false);
        historyContainer.getViewport().setBackground(new Color(155, 160, 151, 200));
        historyContainer.setPreferredSize(new Dimension(width/2 - 25, height));
        wrapper.add(historyContainer);
        return wrapper;
    }

    @Override
    protected void updateGameStateInfo(AbstractGameState gameState) {
        if (actionHistoryText.size() > actionsAtLastUpdate) {
            // this is to stop the panel updating on every tick during one's own turn
            actionsAtLastUpdate = actionHistoryText.size();
            historyInfo.setText(String.join("\n", actionHistoryText));
            historyInfo.setCaretPosition(historyInfo.getDocument().getLength());
        }
        gameStatus.setText("Game status: " + gameState.getGameStatus());
        playerStatus.setText(Arrays.toString(gameState.getPlayerResults()));
        playerScores.setText("Player Scores: " + IntStream.range(0, gameState.getNPlayers())
                .mapToObj(p -> String.format("%.0f", gameState.getGameScore(p)))
                .collect(joining(", ")));
        gamePhase.setText("Game phase: " + gameState.getGamePhase());
        turnOwner.setText("Turn owner: " + gameState.getTurnOrder().getTurnOwner());
        turn.setText("Turn: " + gameState.getTurnOrder().getTurnCounter() +
                "; Round: " + gameState.getTurnOrder().getRoundCounter());
        currentPlayer.setText("Current player: " + gameState.getTurnOrder().getCurrentPlayer(gameState));
        nCave.setText("Cave: " + ((DiamantTurnOrder)gameState.getTurnOrder()).getCaveCounter() + " / " + ((DiamantParameters)gameState.getGameParameters()).nCaves);
    }

    @Override
    protected JComponent createActionPanel(Collection[] highlights, int width, int height, boolean boxLayout) {
        JPanel actionPanel = new JPanel();
        actionPanel.setOpaque(false);
        if (boxLayout) {
            actionPanel.setLayout(new BoxLayout(actionPanel, BoxLayout.Y_AXIS));
        }

        actionButtons = new ActionButton[maxActionSpace];
        for (int i = 0; i < maxActionSpace; i++) {
            ActionButton ab = new ActionButton(ac, highlights);
            actionButtons[i] = ab;
            actionButtons[i].setVisible(false);
            actionPanel.add(actionButtons[i]);
        }
        for (ActionButton actionButton : actionButtons) {
            actionButton.informAllActionButtons(actionButtons);
        }

        JScrollPane pane = new JScrollPane(actionPanel);
        pane.setOpaque(false);
        pane.getViewport().setOpaque(false);
        pane.setPreferredSize(new Dimension(width, height));
        if (boxLayout) {
            pane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        }
        return pane;
    }

    @Override
    protected void _update(AbstractPlayer player, AbstractGameState gameState) {
        if (gameState != null) {

            // Execute last action in the previous game state without any end of round computations to get final state
            if (PAUSE_GUI_KEY_MOMENTS && (gameState.getGameStatus() == Utils.GameResult.GAME_END ||
                    gameState.getHistory().size() > 0 && gameState.getHistory().size() > dgs.getHistory().size())) {
                gameState.getHistory().get(gameState.getHistory().size() - 1).execute(dgs);
                DiamantTurnOrder to = (DiamantTurnOrder) dgs.getTurnOrder();
                // Pause after round finished, full display
                if (to.allPlayed) {
                    // Paint final state of previous round, showing all decisions
                    fm.distributeGemsExitingPlayers(dgs);

                    // Show all hands
                    String decisions = "Decisions: ";
                    for (int i = 0; i < dgs.getNPlayers(); i++) {
                        playerViews[i].update(dgs);
                        if (!dgs.isPlayerInCave(i)) decisions += "-OutOfCave-, ";
                        else decisions += to.actionsThisTurn.get(i).get(0).toString() + ", ";
                    }
                    decisions += ".";
                    actionHistoryText.add(decisions.replace(", .", ""));
                    updateGameStateInfo(dgs);

                    // Repaint
                    repaint();

                    // Message for pause and clarity
                    if (gameState.getGameStatus() == Utils.GameResult.GAME_END) {
                        JOptionPane.showMessageDialog(this, "GAME OVER! Winners: " + fm.getWinners((DiamantGameState) gameState).toString());
                    } else {
                        JOptionPane.showMessageDialog(this, "All decisions finished!");
                    }
                }
            }
            
            // Update decks and visibility
            dgs = (DiamantGameState) gameState.copy();
            for (int i = 0; i < gameState.getNPlayers(); i++) {
                playerViews[i].update(dgs);
                if (!dgs.isPlayerInCave(i)) {
                    playerViews[i].setBorder(playerViewBordersHighlight[i]);
                }
            }
            path.updateComponent(dgs.getPath());
            nGemsOnPath.setText("# Gems on path: " + dgs.getnGemsOnPath().getValue());

            // Update actions
            if (player instanceof HumanGUIPlayer) {
                updateActionButtons(player, gameState);
            }
        }
        repaint();
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(width, height + defaultActionPanelHeight + defaultCardHeight + 20);
    }

    private String getRuleText() {
        String rules = "<html><center><h1>Diamant</h1></center><br/><hr><br/>";
//        rules += "<p>You try to earn the favour of the princess and get your love letter delivered to her. The closer you are (the higher your card number) at the end, the better. The closest player, or the only one left in the game, is the winner of the round. Win most rounds to win the game.</p><br/>";
//        rules += "<p>On your turn, you draw a card to have 2 in hand, and then play one of the cards, discarding it and executing its effect.</p>";
//        rules += "<hr><p><b>INTERFACE: </b> Find actions available at any time at the bottom of the screen. Each player has 2 components in their area: their hand (hidden; left) and their cards played/discarded (right). Click on cards in a deck to see them better / select them to see actions associated. Click on player areas (e.g. player names) to see actions targetting them.</p>";
        rules += "</html>";
        return rules;
    }
}
