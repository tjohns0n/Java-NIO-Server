package cs455.scaling.client;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Report {

    private final int sent;
    private final int received;
    private final String time;

    public Report(int sent, int received) {
        this.sent = sent;
        this.received = received;
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();
        time = dtf.format(now);
    }

    public String toString() {
        return "[" + time + "] Total Sent Count: " + sent + ", Total Received Count: " + received;
    }

}
