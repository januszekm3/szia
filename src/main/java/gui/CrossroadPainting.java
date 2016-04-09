package gui;

import model.crossroad.*;
import simulation.AgentState;
import simulation.AgentState.AgentPosition;
import simulation.Simulator;
import simulation.SimulatorStepObserver;

import javax.swing.*;
import java.awt.*;

public class CrossroadPainting extends JPanel implements SimulatorStepObserver {
    private static final Color carHeadColor = new Color(75, 157, 255);
    private static final Color carTailColor = new Color(55, 108, 179);
    private static final int NODE_RADIUS = 5;
    private static final int EDGE_THICKNESS = 3;
    private static final float CROSSROAD_MARGIN = 0.1f;
    private final Crossroad crossroad;
    private final CrossroadDimensions dimensions;
    private Dimension canvasDimensions;
    private float scalingFactor;
    private Simulator simulator;

    public CrossroadPainting(Crossroad crossroad, Simulator simulator) {
        this.crossroad = crossroad;
        this.dimensions = crossroad.getDimensions();
        this.simulator = simulator;
        this.simulator.registerObserver(this);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        updateCanvasSize();
        updateScalingFactor();
        drawCrossroad((Graphics2D) g);
    }

    private void drawCrossroad(Graphics2D g) {
        g.setStroke(new BasicStroke(EDGE_THICKNESS));
        drawEdges(g);
        drawNodes(g);
        drawCars(g);
    }

    private void drawCars(Graphics2D g) {
        for (AgentState state : simulator.getStates()) {
            drawHead(state.getHeadPosition(), g);
            drawTail(state.getTailPosition(), g);
        }
    }

    private void drawTail(AgentPosition tailPosition, Graphics2D g) {
        int edgeX = tailPosition.edge.getBegin().getPosition().getX();
        int edgeY = tailPosition.edge.getBegin().getPosition().getY();
        int dx = tailPosition.edge.getEnd().getPosition().getX() - edgeX;
        int dy = tailPosition.edge.getEnd().getPosition().getY() - edgeY;
        float edgeProgress = (float) tailPosition.edgePosition / tailPosition.edge.length();
        float x = edgeX + edgeProgress * dx;
        float y = edgeY + edgeProgress * dy;
        g.setColor(carTailColor);
        g.fillOval(scaledX(x) - NODE_RADIUS, scaledY(y) - NODE_RADIUS, 2 * NODE_RADIUS, 2 * NODE_RADIUS);
    }

    private void drawHead(AgentState.AgentPosition headPosition, Graphics2D g) {
        int edgeX = headPosition.edge.getBegin().getPosition().getX();
        int edgeY = headPosition.edge.getBegin().getPosition().getY();
        int dx = headPosition.edge.getEnd().getPosition().getX() - edgeX;
        int dy = headPosition.edge.getEnd().getPosition().getY() - edgeY;
        float edgeProgress = (float) headPosition.edgePosition / headPosition.edge.length();
        float x = edgeX + edgeProgress * dx;
        float y = edgeY + edgeProgress * dy;
        g.setColor(carHeadColor);
        g.fillOval(scaledX(x) - NODE_RADIUS, scaledY(y) - NODE_RADIUS, 2 * NODE_RADIUS, 2 * NODE_RADIUS);
    }

    private void drawNodes(Graphics2D g) {
        for (Node node : crossroad.getNodes()) {
            g.setColor(node.getNodeColor());
            Position beginPosition = node.getPosition();
            int x = beginPosition.getX();
            int y = beginPosition.getY();
            g.fillOval(scaledX(x) - NODE_RADIUS, scaledY(y) - NODE_RADIUS, 2 * NODE_RADIUS, 2 * NODE_RADIUS);
        }
    }

    private void drawEdges(Graphics2D g) {
        g.setColor(Color.GRAY);
        for (Node node : crossroad.getNodes()) {
            Position beginPosition = node.getPosition();
            int x = beginPosition.getX();
            int y = beginPosition.getY();
            for (Edge edge : node.getOutgoingEdges()) {
                Node end = edge.getEnd();
                Position endPosition = end.getPosition();
                g.drawLine(scaledX(x), scaledY(y), scaledX(endPosition.getX()), scaledY(endPosition.getY()));
            }
        }
    }

    private void updateCanvasSize() {
        canvasDimensions = getSize();
    }

    public void updateScalingFactor() {
        float xFactor = (1 - 2 * CROSSROAD_MARGIN) * canvasDimensions.width / (dimensions.maxX - dimensions.minX);
        float yFactor = (1 - 2 * CROSSROAD_MARGIN) * canvasDimensions.height / (dimensions.maxY - dimensions.minY);
        scalingFactor = Math.min(xFactor, yFactor);
    }

    private int scaledX(float x) {
        return (int) ((x - dimensions.minX) * scalingFactor + CROSSROAD_MARGIN * canvasDimensions.width);
    }

    private int scaledY(float y) {
        return (int) ((y - dimensions.minY) * scalingFactor + CROSSROAD_MARGIN * canvasDimensions.height);
    }

    public void redraw() {
        repaint();
    }

    @Override
    public void update() {
        removeAll();
        revalidate();
        repaint();
    }
}
