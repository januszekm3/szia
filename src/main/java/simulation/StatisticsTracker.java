package simulation;

import model.agent.Agent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class StatisticsTracker implements SimulatorStepObserver {
    private static final Logger logger = LoggerFactory.getLogger(StatisticsTracker.class);

    public static class RouteStatistics {
        public float averageSpeed;
        public int numberOfAgents = 1;
    }

    public static class SimulationStatistics {
        public float averageVelocity;
        public float averageWaitingTime;
        public float averageDrivingTime;
        public float averageAgentsWaiting;
        public int numberOfCollisions;
        public int numberOfAgents;
        public Map<String, RouteStatistics> routeStats = new HashMap<>();

        public float getAverageAgentsWaiting() {
            return averageAgentsWaiting;
        }

        public float getAverageWaitingTime() {
            return averageWaitingTime;
        }
    }

    private class AgentStatistics {
        public List<Float> velocities = new ArrayList<>();
        public int numberOfStepsWaiting;
        public int numberOfStepsDriving;
    }

    private class Statistics {
        public List<Integer> numbersOfWaitingAgents = new ArrayList<>();
        public int simulationTime = 0;
    }

    private Simulator simulator;
    private Map<Agent, AgentStatistics> agentsStats = new HashMap<>();
    private Statistics stats = new Statistics();

    public StatisticsTracker(Simulator simulator) {
        this.simulator = simulator;
    }

    @Override
    public void update() {
        stats.simulationTime++;
        for (Map.Entry<Agent, AgentState> entry : simulator.getAgentsStates().entrySet()) {
            Agent a = entry.getKey();
            AgentState state = entry.getValue();
            check_agent_entry(a);
            AgentStatistics agentStatistics = agentsStats.get(a);
            agentStatistics.numberOfStepsDriving++;
            agentStatistics.velocities.add(state.getVelocity());
        }
        Set<Agent> queuedAgents = simulator.getQueuedAgents();
        stats.numbersOfWaitingAgents.add(queuedAgents.size());
        for (Agent a : queuedAgents) {
            check_agent_entry(a);
            agentsStats.get(a).numberOfStepsWaiting++;
        }
    }

    public SimulationStatistics getStatistics() {
        SimulationStatistics stats = new SimulationStatistics();
        stats.averageVelocity = averageVelocity();
        stats.averageDrivingTime = averageDrivingTime();
        stats.averageAgentsWaiting = averageAgentsWaiting();
        stats.averageWaitingTime = averageWaitingTime();
        stats.numberOfCollisions = simulator.getCollisionTracker().getNumberOfCollisions();
        stats.numberOfAgents = agentsStats.size();
        stats.routeStats = routeStats();
        return stats;
    }

    private Map<String, RouteStatistics> routeStats() {
        Map<String, RouteStatistics> result = new HashMap<>();
        for (Agent a : agentsStats.keySet()) {
            if (!a.arrived()) {
                continue;
            }

            String routeId = a.getRouteBegin().getId() + ":" + a.getRouteEnd().getId();
            if (result.containsKey(routeId)) {
                RouteStatistics stats = result.get(routeId);
                stats.numberOfAgents++;
                stats.averageSpeed += averageRouteTime(agentsStats.get(a));
            } else {
                RouteStatistics stats = new RouteStatistics();
                stats.averageSpeed = averageRouteTime(agentsStats.get(a));
                result.put(routeId, stats);
            }
        }
        for (Map.Entry<String, RouteStatistics> entry : result.entrySet()) {
            entry.getValue().averageSpeed /= entry.getValue().numberOfAgents;
        }
        return result;
    }

    private float averageRouteTime(AgentStatistics agentStatistics) {
        float avg = 0;
        for (Float x : agentStatistics.velocities) {
            avg += x;
        }
        avg /= agentStatistics.velocities.size();
        return avg;
    }

    public void printStats() {
        StringBuffer output = new StringBuffer();
        output.append("Average velocity: " + averageVelocity() + "\n");
        output.append("Average waiting time: " + averageWaitingTime() + "\n");
        output.append("Average driving time: " + averageDrivingTime() + "\n");
        output.append("Average agents queue lenght: " + averageAgentsWaiting() + "\n");
        output.append("Number of collisons: " + simulator.getCollisionTracker().getNumberOfCollisions() + "\n");
        output.append("Simulation time: " + stats.simulationTime + "\n");
        System.out.print(output.toString());
    }

    private float averageVelocity() {
        float av = 0;
        int count = 0;
        for (AgentStatistics as : agentsStats.values()) {
            for (Float i : as.velocities) {
                av += i;
            }
            count += as.velocities.size();
        }
        return av / count;
    }

    private float averageWaitingTime() {
        float av = 0;
        int count = 0;
        for (AgentStatistics as : agentsStats.values()) {
            av += as.numberOfStepsWaiting;
            count++;
        }
        return av / count;
    }

    private float averageDrivingTime() {
        float av = 0;
        int count = 0;
        for (AgentStatistics as : agentsStats.values()) {
            av += as.numberOfStepsDriving;
            count++;
        }
        return av / count;
    }

    private float averageAgentsWaiting() {
        float av = 0;
        int count = 0;
        for (Integer i : stats.numbersOfWaitingAgents) {
            av += i;
            count++;
        }
        return av / count;
    }

    private void check_agent_entry(Agent a) {
        if (!agentsStats.containsKey(a)) {
            agentsStats.put(a, new AgentStatistics());
        }
    }
}
