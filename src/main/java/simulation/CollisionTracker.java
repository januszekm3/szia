package simulation;

import model.agent.Agent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CollisionTracker {
    private static final Logger logger = LoggerFactory.getLogger(CollisionTracker.class);
    private Set<Set<Agent>> previousCollisions = new HashSet<>();
    private boolean printCollisions;

    public CollisionTracker(boolean printCollisions) {
        this.printCollisions = printCollisions;
    }

    public void detectCollisions(List<AgentState> agents) {
        for (int i = 0; i < agents.size(); i++) {
            AgentState agent1State = agents.get(i);
            for (int j = i + 1; j < agents.size(); j++) {
                AgentState agent2State = agents.get(j);

                if (agent1State.collidesWith(agent2State)) {
                    HashSet<Agent> collisionSet = new HashSet<>();
                    collisionSet.add(agent1State.getAgent());
                    collisionSet.add(agent2State.getAgent());
                    if (!previousCollisions.contains(collisionSet)) {
                        previousCollisions.add(collisionSet);
                        if (printCollisions) {
                            logger.info("Number of collisions: {}", previousCollisions.size());
                        }
                    }
                }
            }
        }
    }

    public int getNumberOfCollisions() {
        return previousCollisions.size();
    }
}
