package cs455.scaling.server;

import java.net.SocketAddress;

public class ReportCounter {

    private final SocketAddress address;
    private Integer messagesProcessed;

    public ReportCounter(SocketAddress address) {
        this.address = address;
        messagesProcessed = 0;
    }

    public void increment() {
        messagesProcessed++;
    }

    public int getCount() {
        return messagesProcessed;
    }

    public void reset() {
        messagesProcessed = 0;
    }

    @Override
    public boolean equals(Object o) {
        return (o instanceof ReportCounter && ((ReportCounter)o).address.equals(address));
    }

}
