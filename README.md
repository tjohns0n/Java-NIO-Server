# Java NIO Server
This is a basic server built using Java NIO. It uses a simple thread pool built on top of bare Java Threads, which can be used for general purpose multi-threading programming outside of this project. Currently, the server calculates and responds with the SHA-1 hash of random 8 KB messages sent by clients. A report of the current number of connections and throughput will be printed to the console every 20 seconds. 

The clients generate the random 8 KB messages at a rate specified by the user. They maintain the SHA-1 hashes of sent messages in a linked list and remove the hash from the list when the server responds with a matching hash.

## Performance:
Running on a system with a Xeon E5-2650 v2 processor (@ 2.6 GHz) and 32 GB of RAM, the server can typically handle a load of 150 concurrent client connections, each sending four 8 KB messages per second. These numbers were obtained on a fairly congested lab network, so performance would likely be better on a quieter network. 

## Limitations:
As this is mostly a proof of concept, not all of the functionality of a typical server is present. Specifically:
- Clients disconnecting will break the server.
- The server does not accept user input once it has started. This means it must be killed from the terminal or a task manager to stop it. 
- Clients cannot be throttled or denied a connection, meaning the server will simply fall behind if overwhelmed.

## Building:

```bash
cd src
make
```

## Running:
### Server:

```bash
# From src directory:
cs455.scaling.server.Server [port] [thread pool size]
```

### Client:

```bash
# Server must be running first
# From src folder:
cs455.scaling.client.Client [server name or IP] [server port] [messages per second]
```

### Automated launch:
The `h2.sh` and `h2stop.sh` scripts can be used to start up multiple clients simultaneously. `gnome-terminal` must be installed. To use them:
1. Start the server on the desired machine and modify the `machine_list` file to have the name or IP of each client machine on its own line.
2. Edit the CLASSES variable in the script to point to the `src` directory of the project on the client machines. 
3. Modify the SCRIPT variable so that the desired server IP/name, port, and client messages per second are specified after `cs455.scaling.client.Client`
4. Run the script with `./h2.sh` or `bash h2.sh`
5. To stop the clients, run the `h2stop.sh` script in the same manner. For the stop script to work, the clients must all be launched under the same `$USER` as the server. 


## `src` directory overview:

```
cs455 - Main Java package
|-scaling - Contains all classes for this assignment
    |-client
        |-Client - Connects to the server and sends messages 
        |-Report - Contains statistics about the client's IO
        |-Reporter - Periodically queries the client and generates a report 
    |-message 
        |-HashMessage - Contains the SHA-1 hash of a Message's byte array 
        |-Message - Contains a random 8 KB array 
    |-pool 
        |-ThreadPool - Wrapper class for thread pool components
            |-ThreadPoolManager - Creates WorkerThreads and manages their work 
            |-WorkerThread - Once created, waits for work and then executes it 
    |-server 
        |-Report - Contains statistics about the server's IO 
        |-ReportCounter - Contains statistics about a single connection 
        |-Server - Main thread of the server. Starts the server and polls for reports 
    |-utils
        |-BlockingLinkedList - A basic wrapper class to provide some thread safety to LinkedLists
        |-SafeArrayList - A basic wrapper class to provide some thread safety to ArrayLists 
    |-work
        |-HashCommunication - Interface for passing hashes between objects
        |-ScalingMessageWork - Calculates the hash of a message and sends it back to SelectorWork
        |-SelectorWork - Constantly scans for incoming connections and messages, and sends hashes back to clients 
        |-Work - Super class of Work for thread pools 
```
