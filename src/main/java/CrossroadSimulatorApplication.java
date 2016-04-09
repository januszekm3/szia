import gui.CrossroadView;
import model.crossroad.Crossroad;
import model.utils.CrossroadLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import simulation.SimulationSettings;
import simulation.Simulator;
import simulation.StatisticsTracker;

import javax.swing.*;
import java.awt.*;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CrossroadSimulatorApplication {
    private static int NUMBER_OF_SIMULATORS = 40;

    private static final Logger logger = LoggerFactory.getLogger(CrossroadSimulatorApplication.class);
    private Crossroad crossroad;
    private CrossroadView window;
    private Simulator simulator;
    private SimulationSettings settings;

    public CrossroadSimulatorApplication(SimulationSettings settings) {
        this.settings = settings;
        this.crossroad = CrossroadLoader.loadFromFile(settings.crossroadFile);
        this.simulator = new Simulator(this.crossroad, settings);
        if (!settings.batchMode) {
            this.window = new CrossroadView(this.crossroad, this.simulator);
            createUi();
        }
    }

    public void run() {
        if (!settings.batchMode) {
            this.window = new CrossroadView(this.crossroad, this.simulator);
            createUi();
            simulator.run();
        } else {
            Set<Simulator> simulators = new HashSet<>();
            ExecutorService executor = Executors.newFixedThreadPool(16);
            for (int i = 0; i < NUMBER_OF_SIMULATORS; i++) {
                Simulator sim = new Simulator(this.crossroad, settings);
                simulators.add(sim);
                executor.execute(sim);
            }
            executor.shutdown();
            while (!executor.isTerminated()) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    logger.error("Fuckup");
                }
            }
            computeAverageStatistics(simulators);
        }
    }

    private void computeAverageStatistics(Set<Simulator> simulators) {
        StatisticsTracker.SimulationStatistics avgStats = new StatisticsTracker.SimulationStatistics();
        double[] waitingAgents = simulators.stream().map(Simulator::getStatistics).
                mapToDouble(StatisticsTracker.SimulationStatistics::getAverageAgentsWaiting).sorted().toArray();
        double[] waitingTimes = simulators.stream().map(Simulator::getStatistics).
                mapToDouble(StatisticsTracker.SimulationStatistics::getAverageWaitingTime).sorted().toArray();
        avgStats.routeStats = new HashMap<>();
        for (Simulator s : simulators) {
            StatisticsTracker.SimulationStatistics statistics = s.getStatistics();
            avgStats.averageDrivingTime += statistics.averageDrivingTime;
            avgStats.averageVelocity += statistics.averageVelocity;
            avgStats.numberOfCollisions += statistics.numberOfCollisions;
            avgStats.numberOfAgents += statistics.numberOfAgents;

            for (Map.Entry<String, StatisticsTracker.RouteStatistics> x : statistics.routeStats.entrySet()) {
                String routeId = x.getKey();
                StatisticsTracker.RouteStatistics stats = x.getValue();
                if (avgStats.routeStats.containsKey(routeId)) {
                    StatisticsTracker.RouteStatistics routeStatistics = avgStats.routeStats.get(routeId);
                    routeStatistics.averageSpeed += stats.averageSpeed;
                    routeStatistics.numberOfAgents += stats.numberOfAgents;
                } else {
                    avgStats.routeStats.put(routeId, stats);
                }
            }
        }
        for (StatisticsTracker.RouteStatistics x : avgStats.routeStats.values()) {
            x.averageSpeed /= NUMBER_OF_SIMULATORS;
            x.numberOfAgents /= NUMBER_OF_SIMULATORS;
        }
        avgStats.averageAgentsWaiting = (float) waitingAgents[waitingAgents.length / 2];
        avgStats.averageDrivingTime /= NUMBER_OF_SIMULATORS;
        avgStats.averageVelocity /= NUMBER_OF_SIMULATORS;
        avgStats.averageWaitingTime = (float) waitingTimes[waitingTimes.length / 2];
        avgStats.numberOfCollisions = (int) ((float) avgStats.numberOfCollisions / NUMBER_OF_SIMULATORS);
        avgStats.numberOfAgents = (int) ((float) avgStats.numberOfAgents / NUMBER_OF_SIMULATORS);
        saveToFile(avgStats);
    }

    private void saveToFile(StatisticsTracker.SimulationStatistics statitics) {
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new FileWriter("results.txt", true));
            writeSimulationSettings(writer);
            writeStatistics(writer, statitics);
        } catch (IOException e) {
            logger.error("Results file writing error");
        } finally {
            try {
                writer.close();
            } catch (IOException e) {
                logger.error("File stream closing error");
            }
        }

        writer = null;
        try {
            writer = new BufferedWriter(new FileWriter("route_results.txt", false));
            writeRouteStatistics(writer, statitics);
        } catch (IOException e) {
            logger.error("Results file writing error");
        } finally {
            try {
                writer.close();
            } catch (IOException e) {
                logger.error("File stream closing error");
            }
        }
    }

    private void writeRouteStatistics(BufferedWriter writer, StatisticsTracker.SimulationStatistics statitics) throws IOException {
        for (Map.Entry<String, StatisticsTracker.RouteStatistics> entry : statitics.routeStats.entrySet()) {
            String[] split = entry.getKey().split(":");
            writer.write(split[0]);
            writer.write("\t");
            writer.write(split[1]);
            writer.write("\t");
            writer.write("" + entry.getValue().averageSpeed);
            writer.write("\t");
            writer.write("" + entry.getValue().numberOfAgents);
            writer.write("\n");
        }
    }

    private void writeStatistics(BufferedWriter writer, StatisticsTracker.SimulationStatistics statistics) throws IOException {
        writer.write("" + statistics.averageVelocity);
        writer.write("\t");
        writer.write("" + statistics.averageWaitingTime);
        writer.write("\t");
        writer.write("" + statistics.averageDrivingTime);
        writer.write("\t");
        writer.write("" + statistics.averageAgentsWaiting);
        writer.write("\t");
        writer.write("" + statistics.numberOfCollisions);
        writer.write("\t");
        writer.write("" + statistics.numberOfAgents);
        writer.write("\n");
    }

    private void writeSimulationSettings(BufferedWriter writer) throws IOException {
        writer.write("" + settings.simulationTime);
        writer.write("\t");
        writer.write("" + settings.traffic);
        writer.write("\t");
        writer.write("" + settings.craziness);
        writer.write("\t");
    }


    public static void main(String args[]) {
        logger.info("Starting...");
        SimulationSettings settings = new SimulationSettings();
        if (args.length > 0) {
            settings.crossroadFile = args[0];
            settings.batchMode = Integer.parseInt(args[1]) == 1;
            settings.simulationTime = Integer.parseInt(args[2]);
            settings.traffic = Float.parseFloat(args[3]);
            settings.craziness = Float.parseFloat(args[4]);
        }
        CrossroadSimulatorApplication simulator = new CrossroadSimulatorApplication(settings);
        simulator.run();
    }

    private void createUi() {
        EventQueue.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
                logger.error("Unable to set system look and feel");
            }
            window.setVisible(true);
        });
    }
}
