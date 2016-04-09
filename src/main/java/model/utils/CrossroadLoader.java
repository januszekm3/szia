package model.utils;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;
import model.crossroad.Crossroad;
import model.crossroad.Node;
import model.crossroad.Position;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


public class CrossroadLoader {
    private static final Logger logger = LoggerFactory.getLogger(CrossroadLoader.class);

    public static Crossroad loadFromFile(String fileName) {
        Crossroad crossroad;
        try {
            JsonValue json = Json.parse(new InputStreamReader(new FileInputStream(fileName)));
            JsonObject jsonContents = json.asObject();
            Map<Integer, Node> idToNode = new HashMap<>();
            Set<Node> inputNodes = new HashSet<>();
            loadNodes(jsonContents, inputNodes, idToNode);
            loadEdges(jsonContents, idToNode);
            crossroad = new Crossroad(inputNodes);
        } catch (FileNotFoundException e) {
            logger.error("Crossroad file not found", e);
            throw new RuntimeException(e);
        } catch (IOException e) {
            logger.error("IO error while reading crossroad file", e);
            throw new RuntimeException(e);
        }
        return crossroad;
    }

    private static void loadEdges(JsonObject contents, Map<Integer, Node> nodes) {
        for (JsonValue edgeValue : contents.get("edges").asArray()) {
            JsonObject edge = edgeValue.asObject();
            int startNodeId = edge.get("start").asInt();
            int endNodeId = edge.get("end").asInt();
            Node startNode = nodes.get(startNodeId);
            Node endNode = nodes.get(endNodeId);
            startNode.addConnection(endNode);
        }
    }

    private static void loadNodes(JsonObject contents, Set<Node> inputNodes, Map<Integer, Node> nodes) {
        for (JsonValue nodeValue : contents.get("nodes").asArray()) {
            JsonObject node = nodeValue.asObject();
            int id = node.get("id").asInt();
            int x = node.get("x").asInt();
            int y = node.get("y").asInt();
            JsonValue probabilityValue = node.get("probability");

            Node nodeObject;
            if (probabilityValue != null) {
                nodeObject = new Node(id, new Position(x, y), probabilityValue.asFloat());
            } else {
                nodeObject = new Node(id, new Position(x, y));
            }
            nodes.put(id, nodeObject);

            if (node.get("input") != null && node.get("input").asBoolean() == true) {
                inputNodes.add(nodeObject);
            }
        }
    }
}
