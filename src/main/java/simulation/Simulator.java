package simulation;

import model.agent.Agent;
import model.agent.Car;
import model.agent.Decision;
import model.agent.Driver;
import model.crossroad.Crossroad;
import model.crossroad.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import simulation.AgentState.AgentPosition;

import java.util.*;

import static java.util.stream.Collectors.toSet;

public class Simulator implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(Simulator.class);
    private Crossroad crossroad;
    private Map<Agent, AgentState> agents = new HashMap<>();
    private int time = 0;
    private Set<SimulatorStepObserver> observers = new HashSet<>();
    private CollisionTracker collisionTracker;
    private Map<Node, Queue<Agent>> agentsQueues = new HashMap<>();
    private int queuedAgents = 0;
    private StatisticsTracker statTracker;

    public SimulationSettings getSettings() {
        return settings;
    }

    private SimulationSettings settings;

    public Simulator(Crossroad crossroad, SimulationSettings settings) {
        this.settings = settings;
        collisionTracker = new CollisionTracker(!settings.batchMode);
        statTracker = new StatisticsTracker(this);
        observers.add(statTracker);
        this.crossroad = crossroad;
        for (Node inputNode : crossroad.getInputNodes()) {
            agentsQueues.put(inputNode, new LinkedList<>());
        }
    }

    public CollisionTracker getCollisionTracker() {
        return collisionTracker;
    }

    @Override
    public void run() {
        while (!endSimulation()) {
            time++;
            createAgents();
            placeAgentsOnCrossroad();
            updateCrossroadState();
            detectCollisions();
            removeAgents();
            notifyObservers();
            printSimulationProgress();
            if (!settings.batchMode) {
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        if (settings.batchMode) {
            logger.info("Simulation finished");
        } else {
            statTracker.printStats();
        }
    }

    private void printSimulationProgress() {
        if (!settings.batchMode && time % 100 == 0) {
            logger.info("Simulation progress: {}%", Math.round((float) time / settings.simulationTime * 100 * 100) / 100);
        }
    }

    private void placeAgentsOnCrossroad() {
        int waitingAgents = 0;
        Set<Node> freeInputNodes = freeInputNodes();
        for (Map.Entry<Node, Queue<Agent>> entry : agentsQueues.entrySet()) {
            Node inputNode = entry.getKey();
            Queue<Agent> queue = entry.getValue();
            if (freeInputNodes.contains(inputNode) && !queue.isEmpty()) {
                Agent agent = queue.remove();
                agents.put(agent, new AgentState(agent, inputNode));
            }
            waitingAgents += queue.size();
        }
        if (queuedAgents != waitingAgents) {
//            logger.debug("Waiting agents: {}", waitingAgents);
            queuedAgents = waitingAgents;
        }
    }

    private void detectCollisions() {
        collisionTracker.detectCollisions(new ArrayList<>(this.agents.values()));
    }

    private void removeAgents() {
        Queue<Agent> agentsToRemove = new LinkedList<>();
        for (Map.Entry<Agent, AgentState> agentEntry : this.agents.entrySet()) {
            AgentPosition headPosition = agentEntry.getValue().getHeadPosition();
            AgentPosition tailPosition = agentEntry.getValue().getTailPosition();
            if (headPosition.equals(tailPosition) && !crossroad.getInputNodes().contains(headPosition.edge.getBegin())) {
                agentsToRemove.add(agentEntry.getKey());
            }
        }
        for (Agent agent : agentsToRemove) {
            this.agents.remove(agent);
        }
    }

    private void notifyObservers() {
        for (SimulatorStepObserver observer : observers) {
            observer.update();
        }
    }

    public void registerObserver(SimulatorStepObserver observer) {
        observers.add(observer);
    }

    private void updateCrossroadState() {
        Map<Agent, CrossroadState> states = new HashMap<>();
        for (Map.Entry<Agent, AgentState> agentEntry : agents.entrySet()) {
            Agent agent = agentEntry.getKey();
            states.put(agent, crossroadStateForAgent(agent));
        }

        for (Map.Entry<Agent, AgentState> agentEntry : agents.entrySet()) {
            Agent agent = agentEntry.getKey();
            AgentState state = agentEntry.getValue();

            Decision decision = agent.decision(states.get(agent));

            float distanceToGo = state.getVelocity();
            state.move(distanceToGo, decision);

            updateAgentVelocity(agent, state, decision);
        }
    }

    private void updateAgentVelocity(Agent agent, AgentState state, Decision decision) {
        Car car = agent.getCar();
        float newVelocity = state.getVelocity() + decision.acceleration.getVelocityDifference(car);
        if (newVelocity > car.getMaxVelocity()) {
            newVelocity = car.getMaxVelocity();
        }
        if (newVelocity < 0) {
            newVelocity = 0;
        }
        state.setVelocity(newVelocity);
    }

    private CrossroadState crossroadStateForAgent(Agent agentt) {
        AgentState agentState = null;
        HashSet<AgentState> states = new HashSet<>();
        for (Map.Entry<Agent, AgentState> agentEntry : agents.entrySet()) {
            Agent agent = agentEntry.getKey();
            AgentState state = agentEntry.getValue();

            if (agent == agentt) {
                agentState = state;
            } else {
                states.add(state);
            }
        }
        return new CrossroadState(agentState, states);
    }

    private boolean endSimulation() {
        return time >= settings.simulationTime;
    }

    private void createAgents() {
        Random random = new Random();
        if (random.nextFloat() < settings.traffic / 10) {
            float velocity = random.nextFloat() / 8 + 0.1f;
            float madnessFactor = madnessFactor(random);
            float safeDistanceWhileStaying = 1.15f - madnessFactor * 0.2f;
            float safeDistance = random.nextFloat() * 0.2f + 0.3f - madnessFactor * 0.2f;
            int timeout = random.nextInt(5) + 5;
            Agent agent = new Agent(new Driver(safeDistance, safeDistanceWhileStaying, madnessFactor, timeout), new Car(0.01f, 0.1f, velocity, 0.8f), crossroad.getRandomRoute());
            Node inputNode = agent.nextEdge().getBegin();
            agentsQueues.get(inputNode).add(agent);
        }
    }

    private float madnessFactor(Random random) {
        float v = ((float) random.nextGaussian() / 2) + settings.craziness;
        while (v < 0 || v > 1) {
            v = ((float) random.nextGaussian() / 2) + settings.craziness;
        }
        return v;
    }

    public Collection<AgentState> getStates() {
        return agents.values();
    }

    public Set<Node> freeInputNodes() {
        Set<Node> inputNodes = this.crossroad.getInputNodes();
        Set<Node> occupiedInputNodes = this.agents.values().stream().
                filter(agentState -> inputNodes.contains(agentState.getTailPosition().edge.getBegin())).
                filter(agentState -> agentState.getTailPosition().edgePosition < 0.1).
                map(agentState -> agentState.getTailPosition().edge.getBegin()).collect(toSet());
        inputNodes.removeAll(occupiedInputNodes);
        return inputNodes;
    }

    public Map<Agent, AgentState> getAgentsStates() {
        return agents;
    }

    public Set<Agent> getQueuedAgents() {
        Set<Agent> agents = new HashSet<>();
        for (Queue<Agent> a : agentsQueues.values()) {
            agents.addAll(a);
        }
        return agents;
    }

    public StatisticsTracker.SimulationStatistics getStatistics() {
        return statTracker.getStatistics();
    }
}

