package cs455.scaling.server;

import java.util.ArrayList;

/**
 * Generate a report of the server's active connections and throughput. This class assumes a Report object will be
 * constructed every 20 seconds.
 */
public class Report {

    public final int machinesRegistered;
    public final double totalMessagesPerSecond;
    public final double averageMessagesPerSecond;
    public final double messageStdDev;

    // TODO: Standard deviation

    /**
     * Creates a report.
     * @param counters A list of ReportCounter objects to be included in the report
     */
    public Report(ArrayList<ReportCounter> counters) {
        this.machinesRegistered = counters.size();
        int totalMessages = 0;
        for (ReportCounter counter : counters) {
            totalMessages += counter.getCount();
        }
        totalMessagesPerSecond = totalMessages / 20.;
        if (machinesRegistered != 0 && !Double.isNaN(totalMessagesPerSecond)) {
            averageMessagesPerSecond = totalMessagesPerSecond / machinesRegistered;
            double distanceSum = 0;
            for (ReportCounter counter : counters) {
                distanceSum += Math.pow((counter.getCount() / 20.) - averageMessagesPerSecond, 2);
            }
            messageStdDev = Math.sqrt(distanceSum / machinesRegistered);
        } else {
            averageMessagesPerSecond = 0;
            messageStdDev = 0;
        }

    }

}
