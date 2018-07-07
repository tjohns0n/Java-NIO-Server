package cs455.scaling.client;

public class Reporter implements Runnable {

    Client client;

    public Reporter(Client client) {
        this.client = client;
    }

    @Override
    public void run() {
        while (true) {
            try {
                Thread.sleep(20000);
                System.out.println(client.getReport());
            } catch (InterruptedException e) {
                System.err.println("Reporter: Interrupted");
            }

        }
    }

}
