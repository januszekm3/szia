package simulation;

import model.agent.Agent;
import model.agent.Car;
import model.agent.Decision;
import model.crossroad.Edge;
import model.crossroad.Node;

import java.util.*;

public class AgentState {
    public static class AgentPosition {
        public Edge edge;
        public float edgePosition;

        @Override
        public String toString() {
            return "AgentPosition [edge=" + edge + ", edgePosition=" + edgePosition + "]";
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((edge == null) ? 0 : edge.hashCode());
            result = prime * result + Float.floatToIntBits(edgePosition);
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            AgentPosition other = (AgentPosition) obj;
            if (edge == null) {
                if (other.edge != null)
                    return false;
            } else if (!edge.equals(other.edge))
                return false;
            if (Float.floatToIntBits(edgePosition) != Float.floatToIntBits(other.edgePosition))
                return false;
            return true;
        }
    }

    public static class InFrontOfComparator implements Comparator<AgentState> {
        @Override
        public int compare(AgentState o1, AgentState o2) {
            float position1 = o1.getTailPosition().edgePosition;
            float position2 = o2.getTailPosition().edgePosition;
            int value;
            if (position1 < position2) {
                value = -1;
            } else if (position1 > position2) {
                value = 1;
            } else {
                value = 0;
            }
            return value;
        }
    }

    private Agent agent;
    private LinkedList<Edge> edgesCurrentlyOccupied = new LinkedList<>();
    private float edgePosition;
    private float velocity;

    public AgentState(Agent agent, Node startNode) {
        this.agent = agent;
        Edge startEdge = startNode.getOutgoingEdges().iterator().next();
        this.edgesCurrentlyOccupied.add(startEdge);
        this.edgePosition = 0;
        this.velocity = 0;
    }

    public AgentPosition getHeadPosition() {
        AgentPosition position = new AgentPosition();
        position.edge = this.edgesCurrentlyOccupied.get(0);
        position.edgePosition = this.edgePosition;
        return position;
    }

    public AgentPosition getTailPosition() {
        AgentPosition position = new AgentPosition();
        float carLength = agent.getCar().getLength();
        carLength -= edgePosition;
        Iterator i = this.edgesCurrentlyOccupied.iterator();
        Edge edge = (Edge) i.next();
        while (carLength > 0) {
            if (!i.hasNext()) {
                position.edge = edge;
                position.edgePosition = 0;
                return position;
            }
            edge = (Edge) i.next();
            carLength -= edge.length();
        }
        position.edge = edge;//this.edgesCurrentlyOccupied.get(0);
        position.edgePosition = -carLength;
        return position;
    }

    public float getVelocity() {
        return velocity;
    }

    @Override
    public String toString() {
//        return "Position: " + edgePosition + ", Velocity: " + velocity;
        return "Head: " + getHeadPosition().toString() + ", Tail: " + getTailPosition().toString() + ", Vel: " + velocity;
    }

    void setVelocity(float velocity) {
        this.velocity = velocity;
    }

    void move(float distance, Decision decision) {

        Car car = this.agent.getCar();
        AgentPosition tailPosition = getTailPosition();
        float length = tailPosition.edge.length();
        if (length - tailPosition.edgePosition < distance) {
            if (edgesCurrentlyOccupied.size() > 1) {
                edgesCurrentlyOccupied.removeLast();
            } else {
                car.shorten(car.getLength());
            }
        }
        Edge currentEdge = edgesCurrentlyOccupied.get(0);
        float edgeDistanceLeft = currentEdge.length() - edgePosition;
        if (distance > edgeDistanceLeft) {
            if (decision.nextEdge != null) {
                edgesCurrentlyOccupied.addFirst(decision.nextEdge);
                edgePosition = distance - edgeDistanceLeft;
            } else {
                float edgeLength = edgesCurrentlyOccupied.getFirst().length();
                float deficit = distance - (edgeLength - edgePosition);
                car.shorten(deficit);
                this.edgePosition = edgeLength;
            }
        } else {
            edgePosition += distance;
        }
    }

    public Set<Node> getOccupiedNodes() {
        Set<Node> result = new HashSet<>();
        for (int i = 1; i < edgesCurrentlyOccupied.size(); i++) {
            Edge edge = edgesCurrentlyOccupied.get(i);
            result.add(edge.getEnd());
        }
        return result;
    }

    public boolean collidesWith(AgentState other) {
        return checkCollisionOnNodes(other) || checkCollisionOnEdges(other) || checkIfRearEnded(other);
    }

    private boolean checkIfRearEnded(AgentState other) {
        return rearEnded(this, other) || rearEnded(other, this);
    }

    private boolean rearEnded(AgentState a, AgentState b) {
        Edge aFaceEdge = a.edgesCurrentlyOccupied.getFirst();
        Edge bTailEdge = b.edgesCurrentlyOccupied.getLast();
        if (!aFaceEdge.equals(bTailEdge)) {
            return false;
        }
        Edge bFaceEdge = b.edgesCurrentlyOccupied.getFirst();
        if (a.getHeadPosition().edgePosition < b.getTailPosition().edgePosition) {
            return false;
        }
        return !aFaceEdge.equals(bFaceEdge) || a.getHeadPosition().edgePosition < b.getHeadPosition().edgePosition;
    }

    private boolean checkCollisionOnEdges(AgentState other) {
        if (this.edgesCurrentlyOccupied.size() < 3 || other.edgesCurrentlyOccupied.size() < 3) {
            return false;
        }
        HashSet<Edge> edgesOccupied1 = new HashSet<>(this.edgesCurrentlyOccupied.subList(1, this.edgesCurrentlyOccupied.size() - 1));
        HashSet<Edge> edgesOccupied2 = new HashSet<>(other.edgesCurrentlyOccupied.subList(1, other.edgesCurrentlyOccupied.size() - 1));
        edgesOccupied1.retainAll(edgesOccupied2);
        return !edgesOccupied1.isEmpty();
    }

    private boolean checkCollisionOnNodes(AgentState other) {
        Set<Node> occupied1Nodes = getOccupiedNodes();
        Set<Node> occupied2Nodes = other.getOccupiedNodes();
        occupied1Nodes.retainAll(occupied2Nodes);
        return !occupied1Nodes.isEmpty();
    }

    public Agent getAgent() {
        return agent;
    }
}
