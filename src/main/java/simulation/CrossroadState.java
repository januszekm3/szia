package simulation;

import java.util.HashSet;
import java.util.Set;

public class CrossroadState {
    private AgentState myState;
    private Set<AgentState> agentsStates = new HashSet<>();

    public CrossroadState(AgentState myState, Set<AgentState> agentsStates) {
        this.myState = myState;
        this.agentsStates = agentsStates;
    }

    public AgentState getMyState() {
        return myState;
    }

    public Set<AgentState> getAgentsStates() {
        return agentsStates;
    }
}
