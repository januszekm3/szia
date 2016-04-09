package model.crossroad;

import java.util.*;

public class Crossroad {
    private Set<Node> inputNodes = new HashSet<>();
    private Set<Node> outgoingNodes;

    public Crossroad(Set<Node> inputNodes) {
        this.inputNodes = inputNodes;
        this.outgoingNodes = computeOutgoingNodes();
    }

    public CrossroadDimensions getDimensions() {
        CrossroadDimensions dimensions = new CrossroadDimensions();
        dimensions.minX = Integer.MAX_VALUE;
        dimensions.minY = Integer.MAX_VALUE;
        dimensions.maxX = 0;
        dimensions.maxY = 0;
        for (Node node : getNodes()) {
            Position position = node.getPosition();
            int x = position.getX();
            int y = position.getY();
            if (x > dimensions.maxX) {
                dimensions.maxX = x;
            }
            if (x < dimensions.minX) {
                dimensions.minX = x;
            }
            if (y > dimensions.maxY) {
                dimensions.maxY = y;
            }
            if (y < dimensions.minY) {
                dimensions.minY = y;
            }
        }
        return dimensions;
    }

    public Set<Node> getNodes() {
        Set<Node> nodes = new HashSet<>();
        for (Node node : inputNodes) {
            addNode(nodes, node);
        }
        return nodes;
    }

    public Set<Node> getInputNodes() {
        return new HashSet<>(inputNodes);
    }

    private void addNode(Set<Node> nodes, Node node) {
        nodes.add(node);
        for (Edge edge : node.getOutgoingEdges()) {
            Node end = edge.getEnd();
            if (!nodes.contains(end)) {
                addNode(nodes, end);
            }
        }
    }

    public LinkedList<Edge> getRandomRoute() {
        Random random = new Random();
        Object[] inputNodes = this.inputNodes.toArray();
        Node inputNode = (Node) inputNodes[random.nextInt(inputNodes.length)];

        Node outputNode = getRandomOutputNode(random);

        LinkedList<Node> queue = new LinkedList<>();
        HashSet<Node> visitedNodes = new HashSet<>();
        Map<Node, Edge> clew = new HashMap<>();

        queue.add(inputNode);
        if (!searchForRoute(visitedNodes, queue, clew, outputNode)) {
            throw new RuntimeException("Fuckuuuuuup!");
        }

        return recreateRoute(outputNode, clew);
    }

    private Node getRandomOutputNode(Random random) {
        float probabilitySum = 0;
        float rnd = random.nextFloat();
        Node fkp = null;
        for (Node n : outgoingNodes) {
            fkp = n;
            probabilitySum += n.getProbability();
            if (probabilitySum >= rnd) {
                return n;
            }
        }
        return fkp;
    }

    private LinkedList<Edge> recreateRoute(Node endNode, Map<Node, Edge> clew) {
        Node currentNode = endNode;
        LinkedList<Edge> route = new LinkedList<>();
        while (!currentNode.getIncomingEdges().isEmpty()) {
            Edge edge = clew.get(currentNode);
            route.addFirst(edge);
            currentNode = edge.getBegin();
        }
        return route;
    }

    private boolean searchForRoute(Set<Node> visitedNodes, LinkedList<Node> queue, Map<Node, Edge> clew, Node endNode) {
        while (!queue.isEmpty()) {
            Node currentNode = queue.removeFirst();
            if (endNode.equals(currentNode)) {
                return true;
            } else {
                for (Edge edge : currentNode.getOutgoingEdges()) {
                    Node node = edge.getEnd();
                    if (!visitedNodes.contains(node)) {
                        visitedNodes.add(node);
                        queue.add(node);
                        clew.put(node, edge);
                    }
                }
            }
        }
        return false;
    }

    private Set<Node> computeOutgoingNodes() {
        Set<Node> outgoingNodes = new HashSet<>();
        for (Node inputNode : getInputNodes()) {
            Set<Node> visitedNodes = new HashSet<>();
            searchForOutgoingNode(visitedNodes, outgoingNodes, inputNode);
        }
        return outgoingNodes;
    }

    private void searchForOutgoingNode(Set<Node> visitedNodes, Set<Node> outgoingNodes, Node node) {
        Set<Edge> outgoingEdges = node.getOutgoingEdges();
        if (outgoingEdges.isEmpty()) {
            outgoingNodes.add(node);
        } else {
            for (Edge edge : outgoingEdges) {
                Node nextNode = edge.getEnd();
                if (!visitedNodes.contains(nextNode)) {
                    visitedNodes.add(nextNode);
                    searchForOutgoingNode(visitedNodes, outgoingNodes, nextNode);
                }
            }
        }
    }
}
