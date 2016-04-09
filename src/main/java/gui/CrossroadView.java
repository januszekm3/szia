package gui;

import model.crossroad.Crossroad;
import simulation.Simulator;

import javax.swing.*;
import java.awt.*;


public class CrossroadView extends JFrame {
    private static final String WINDOW_NAME = "Crossroad Simulator";
    private static final int WINDOW_WIDTH = 800;
    private static final int WINDOW_HEIGHT = 600;
    private final CrossroadPainting painting;

    public CrossroadView(Crossroad crossroad, Simulator simulator) {
        super(WINDOW_NAME);
        setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
        centerWindow();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        painting = new CrossroadPainting(crossroad, simulator);
        add(painting);
    }

    private void centerWindow() {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension windowSize = getSize();
        setLocation((screenSize.width - windowSize.width) / 2, (screenSize.height - windowSize.height) / 2);
    }
}
