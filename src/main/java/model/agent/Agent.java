package model.agent;

import model.crossroad.Edge;
import model.crossroad.Node;
import simulation.AgentState;
import simulation.CrossroadState;

import java.util.LinkedList;
import java.util.Optional;
import java.util.Set;

public class Agent {
    private static final float SLOW_SPEED = 0.1f;

    private Car car;
    private Driver driver;
    private LinkedList<Edge> route;
    private Node routeBegin;
    private Node routeEnd;

    public Agent(Driver driver, Car car, LinkedList<Edge> route) {
        this.driver = driver;
        this.car = car;
        this.route = route;
        this.routeBegin = route.getFirst().getBegin();
        this.routeEnd = route.getLast().getEnd();
    }

    public Decision decision(CrossroadState state) {
        if (ifEdgeChanged(state)) {
            route.removeFirst();
        }
        Decision decision = new Decision();
        decision.acceleration = getAccelerationDecision(state);
        decision.nextEdge = nextEdge();
        return decision;
    }

    private boolean ifEdgeChanged(CrossroadState state) {
        Edge currentEdge = state.getMyState().getHeadPosition().edge;
        return currentEdge.equals(nextEdge());
    }

    private Decision.Acceleration getAccelerationDecision(CrossroadState state) {
        Decision.Acceleration decision = Decision.Acceleration.ACCELERATE;
        if (willDriveIntoSomeonesAss(state) || willDriveIntoSomeonesSide(state) || willCrossSomeonesRoute(state) || willBlockCrossing(state)) {
            decision = Decision.Acceleration.BRAKE;
        }
        return decision;
    }

    private boolean willBlockCrossing(CrossroadState state) {
        RouteCrossing routeCrossing = nearestRouteCrossing(state);
        if (routeCrossing == null) {
            return false;
        }
        if (routeCrossing.distance > 1) {
            return false;
        }
        AgentState.AgentPosition headPosition = state.getMyState().getHeadPosition();
        if (!routeCrossing.node.equals(headPosition.edge.getEnd())) {
            return false;
        }
        AgentState nearestCar = getNearestCar(state);
        if (nearestCar == null) {
            return false;
        }
        AgentState.AgentPosition tailPosition = nearestCar.getTailPosition();
        if (!tailPosition.edge.equals(nextEdge()) ||
                tailPosition.edgePosition > car.getLength() + driver.getSafeDistanceToNextStayingCar() ||
                nearestCar.getVelocity() > 0.05f) {
            return false;
        }
        float distance = headPosition.edge.length() - headPosition.edgePosition;
        if (distance > driver.getSafeDistanceToNextStayingCar()) {
            return false;
        }
        return true;
    }

    private boolean willCrossSomeonesRoute(CrossroadState state) {
        RouteCrossing nearestCrossing = nearestRouteCrossing(state);
        if (nearestCrossing == null) {
            return false;
        }
        if (nearestCrossing.distance / state.getMyState().getVelocity() > 3 - (driver.getMadnessFactor() - 0.5) * 4 && nearestCrossing.distance > 0.5f) {
            return false;
        }
        Set<Edge> rightHandEdges = nearestCrossing.node.rightHandIncomingEdges(nearestCrossing.incomingEdge);
        if (rightHandEdges.isEmpty()) {
            return false;
        }
        for (Edge e : rightHandEdges) {
            Optional<AgentState> query = state.getAgentsStates().stream().filter(as -> as.getHeadPosition().edge.equals(e)).sorted((s1, s2) -> -Float.compare(
                    s1.getHeadPosition().edgePosition, s2.getHeadPosition().edgePosition)).findFirst();
            if (query.isPresent()) {
                AgentState nearestOnRightEdge = query.get();
                AgentState.AgentPosition nearestOnRightEdgeHeadPosition = nearestOnRightEdge.getHeadPosition();
                float distance = nearestOnRightEdgeHeadPosition.edge.length() - nearestOnRightEdgeHeadPosition.edgePosition;
                if (distance / nearestOnRightEdge.getVelocity() < 10 - (driver.getMadnessFactor() - 0.5) * 10) {
                    return true;
                }
            }
        }

        return false;
    }

    private class RouteCrossing {
        public Node node;
        public Edge incomingEdge;
        public float distance;
    }

    private RouteCrossing nearestRouteCrossing(CrossroadState state) {
        AgentState.AgentPosition headPosition = state.getMyState().getHeadPosition();
        float distance = headPosition.edge.length() - headPosition.edgePosition;
        Edge prevEdge = headPosition.edge;
        for (Edge e : this.route) {
            Node n = e.getBegin();
            if (n.isCrossing()) {
                RouteCrossing x = new RouteCrossing();
                x.node = n;
                x.distance = distance;
                x.incomingEdge = prevEdge;
                return x;
            }
            distance += e.length();
            prevEdge = e;
        }
        return null;
    }

    private boolean willDriveIntoSomeonesSide(CrossroadState state) {
        AgentState myState = state.getMyState();
        AgentState.AgentPosition headPosition = myState.getHeadPosition();
        Edge edge = headPosition.edge;
        Node nextNode = edge.getEnd();
        Optional<AgentState> query = state.getAgentsStates().stream().
                filter(agentState -> agentState.getOccupiedNodes().contains(nextNode)).
                findFirst();
        if (query.isPresent()) {
            float distance = edge.length() - headPosition.edgePosition;
            if (shouldIBreak(distance, myState.getVelocity(), 0)) {
                return true;
            }
        }
        return false;
    }

    private boolean willDriveIntoSomeonesAss(CrossroadState state) {
        AgentState.AgentPosition myHeadPosition = state.getMyState().getHeadPosition();
        Optional<AgentState> query = state.getAgentsStates().stream().
                filter(agentState -> agentState.getTailPosition().edge.equals(myHeadPosition.edge)).
                filter(agentState -> agentState.getTailPosition().edgePosition >= myHeadPosition.edgePosition).
                sorted(new AgentState.InFrontOfComparator()).findFirst();
        AgentState closestAgentsState;
        if (query.isPresent()) {
            closestAgentsState = query.get();
            float distance = closestAgentsState.getTailPosition().edgePosition - myHeadPosition.edgePosition;
            if (shouldIBreak(distance, state.getMyState().getVelocity(), closestAgentsState.getVelocity())) {
                return true;
            }
        } else {
            closestAgentsState = getNearestCar(state);
            if (closestAgentsState != null) {
                float distance = closestAgentsState.getTailPosition().edgePosition + state.getMyState().getHeadPosition().edge.length() - myHeadPosition.edgePosition;
                if (shouldIBreak(distance, state.getMyState().getVelocity(), closestAgentsState.getVelocity())) {
                    return true;
                }
            }
        }
        return false;
    }

    private AgentState getNearestCar(CrossroadState state) {
        Optional<AgentState> query = state.getAgentsStates().stream().
                filter(agentState -> agentState.getTailPosition().edge.equals(nextEdge())).
                sorted(new AgentState.InFrontOfComparator()).findFirst();
        return query.isPresent() ? query.get() : null;
    }

    public Edge nextEdge() {
        return route.peekFirst();
    }

    private boolean shouldIBreak(float distance, float mySpeed, float hisSpeed) {
        float speedDifference = mySpeed - hisSpeed;
        boolean toCloseWhileDriving = distance < driver.getSafeDistanceToNextCar() && mySpeed >= SLOW_SPEED;
        boolean toCloseWhileMovingSlowly = distance < driver.getSafeDistanceToNextStayingCar() && mySpeed < SLOW_SPEED;
        return toCloseWhileDriving || toCloseWhileMovingSlowly || speedDifference > 0 && distance / speedDifference < 30;
    }

    public Car getCar() {
        return car;
    }

    public Node getRouteEnd() {
        return routeEnd;
    }

    public Node getRouteBegin() {
        return routeBegin;
    }

    public boolean arrived() {
        return car.getLength() == 0;
    }
}
