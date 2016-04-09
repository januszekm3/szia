package model.crossroad;

import java.awt.*;
import java.util.HashSet;
import java.util.Set;

public class Node {
    private int id;
    private Position position;
    private Set<Edge> outgoingEdges = new HashSet<>();
    private Set<Edge> incomingEdges = new HashSet<>();
    private TrafficLight light;
    private Sign sign;
    private float probability;

    public Node(int id, Position position) {
        this(id, position, null, null, 0);
    }

    private Node(int id, Position position, TrafficLight light, Sign sign, float probability) {
        this.id = id;
        this.position = position;
        this.light = light;
        this.sign = sign;
        this.probability = probability;
    }

    public Node(int id, Position position, float probability) {
        this(id, position, null, null, probability);
    }

    public void addConnection(Node node) {
        Edge edge = new Edge(this, node);
        outgoingEdges.add(edge);
        node.addIncomingEdge(edge);
    }

    public Position getPosition() {
        return position;
    }

    public Set<Edge> getOutgoingEdges() {
        return outgoingEdges;
    }

    public Set<Edge> getIncomingEdges() {
        return new HashSet<>(incomingEdges);
    }

    public Color getNodeColor() {
        Color color;
        if (light != null) {
            color = light.getLightColor();
        } else if (sign != null) {
            color = sign.getColor();
        } else {
            color = Color.BLACK;
        }
        return color;
    }

    public void addIncomingEdge(Edge edge) {
        incomingEdges.add(edge);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Node node = (Node) o;
        return position.equals(node.position);
    }

    @Override
    public int hashCode() {
        return position.hashCode();
    }

    public boolean isCrossing() {
        return incomingEdges.size() >= 2;
    }

    public Set<Edge> rightHandIncomingEdges(Edge incomingEdge) {
        Set<Edge> edges = getIncomingEdges();
        edges.remove(incomingEdge);
        Set<Edge> result = new HashSet<>();
        float dx1 = incomingEdge.getEnd().getPosition().getX() - incomingEdge.getBegin().getPosition().getX();
        float dy1 = incomingEdge.getEnd().getPosition().getY() - incomingEdge.getBegin().getPosition().getY();
        for (Edge e : edges) {
            float dx2 = e.getEnd().getPosition().getX() - e.getBegin().getPosition().getX();
            float dy2 = e.getEnd().getPosition().getY() - e.getBegin().getPosition().getY();
            if (dx1 * dy2 - dx2 * dy1 < 0) {
                result.add(e);
            }
        }
        return result;
    }

    public float getProbability() {
        return probability;
    }

    public int getId() {
        return id;
    }
}
