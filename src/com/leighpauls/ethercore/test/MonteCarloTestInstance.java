package com.leighpauls.ethercore.test;

import com.google.common.collect.Lists;
import com.leighpauls.ethercore.EtherTransactionInterface;
import com.leighpauls.ethercore.GraphCrawlingPrinter;
import com.leighpauls.ethercore.client.EtherClient;
import com.leighpauls.ethercore.connection.QueueConnection;
import com.leighpauls.ethercore.except.EtherRuntimeException;
import com.leighpauls.ethercore.node.ListNode;
import com.leighpauls.ethercore.node.Node;
import com.leighpauls.ethercore.node.StructNode;
import com.leighpauls.ethercore.server.EtherServer;
import com.leighpauls.ethercore.value.*;
import com.sun.tools.javac.util.Pair;

import java.util.ArrayList;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

/**
 * A single montecarlo run
 */
public class MonteCarloTestInstance implements Runnable {

    private final long mSeed;

    public MonteCarloTestInstance(long seed) {
        mSeed = seed;
    }

    @Override
    public void run() {
        Random random = new Random(mSeed);
        EtherServer server = new EtherServer();

        // make some clients
        ArrayList<Pair<EtherClient, QueueConnection>> connections =
                new ArrayList<Pair<EtherClient, QueueConnection>>();
        int numClients = 2; //random.nextInt(8) + 2;
        for (int i = 0; i < numClients; ++i) {
            connections.add(
                    QueueConnection.connect(
                            server,
                            new UUID(random.nextLong(), random.nextLong())));
        }

        int numCycles = random.nextInt(100);
        for (int i = 0; i < numCycles; ++i) {
            // make some local transactions
            for (Pair<EtherClient, QueueConnection> connection : connections) {
                int numTransactions = random.nextInt(5);
                EtherClient client = connection.fst;
                for (int j = 0; j < numTransactions; ++j) {
                    client.applyLocalTransaction(new RandomTransaction(client, random));
                }
            }

            // resolve some messages
            int numResolves = random.nextInt(7 * connections.size() * connections.size());
            for (int j = 0; j < numResolves; ++j) {
                QueueConnection connection =
                        connections.get(random.nextInt(connections.size())).snd;
                if (random.nextBoolean() && connection.numPendingMessagesToClient() > 0) {
                    connection.resolveMessageToClient();
                } else if (connection.numPendingMessagesToServer() > 0) {
                    connection.resolveMessageToServer();
                }
            }
        }

        // resolve the rest of the messages
        boolean done;
        do {
            done = true;
            for (Pair<EtherClient, QueueConnection> connectionPair : connections) {
                QueueConnection connection = connectionPair.snd;
                if (connection.numPendingMessagesToServer() > 0) {
                    connection.resolveMessageToServer();
                    done = false;
                } else if (connection.numPendingMessagesToClient() > 0) {
                    connection.resolveMessageToClient();
                    done = false;
                }
            }
        } while (!done);

        // TODO: compare the server copy against all the clients
        GraphCrawlingPrinter.printGraph(server.getNode(EtherServer.GOD_NODE_UUID), 5);
    }

    private static class RandomTransaction implements EtherTransactionInterface {
        private final EtherClient mClient;
        private final Random mRandom;

        public RandomTransaction(EtherClient client, Random random) {
            mClient = client;
            mRandom = random;
        }

        @Override
        public void executeTransaction() {
            int numOperations = mRandom.nextInt(10);
            for (int i = 0; i < numOperations; ++i) {
                // pick a node
                Node targetNode = getRandomNode();

                // operate on it
                if (targetNode instanceof StructNode) {
                    StructNode t = (StructNode) targetNode;
                    if (t.keySet().isEmpty() || mRandom.nextBoolean()) {
                        // place a new value
                        t.put(makeRandomString(), makeRandomValue());
                    } else {
                        String key = pickRandomKey(t);
                        if (mRandom.nextBoolean()) {
                            // remove the value
                            t.remove(key);
                        } else {
                            // overwrite a value
                            t.put(key, makeRandomValue());
                        }
                    }
                } else if (targetNode instanceof ListNode) {
                    ListNode t = (ListNode) targetNode;
                    if (t.size() == 0 || mRandom.nextBoolean()) {
                        // place a value
                        t.insert(mRandom.nextInt(t.size() + 1), makeRandomValue());
                    } else {
                        // remove a value
                        t.remove(mRandom.nextInt(t.size()));
                    }
                } else {
                    throw new EtherRuntimeException("Unknown node type: " + targetNode.getClass());
                }
            }
        }

        private String pickRandomKey(StructNode node) {
            Set<String> keys = node.keySet();
            return Lists.newArrayList(keys).get(mRandom.nextInt(keys.size()));
        }

        private static final byte[] VALID_CHARS =
                "ABCDEFGHIJKLMNOPQRSTUVWXYZ".getBytes();

        private String makeRandomString() {
            int length = mRandom.nextInt(32);
            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < length; ++i) {
                builder.append((char)VALID_CHARS[mRandom.nextInt(VALID_CHARS.length)]);
            }
            return builder.toString();
        }

        private Value makeRandomValue() {
            switch (mRandom.nextInt(5)) {
                case 0:
                    return new StringValue(makeRandomString());
                case 1:
                    return new IntegerValue(mRandom.nextInt());
                case 2: {
                    // reference to existing node
                    Node node = getRandomNode();
                    if (node instanceof StructNode) {
                        return ((StructNode) node).getReference();
                    } else if (node instanceof ListNode) {
                        return ((ListNode) node).getReference();
                    }
                    throw new EtherRuntimeException("Unknown node type: " + node.getClass());
                }
                case 3:
                    return mClient.makeStructNode().getReference();
                case 4:
                    return mClient.makeListNode().getReference();
            }
            throw new RuntimeException("can't get here...");
        }

        /**
         * Randomly navigates the graph to find a random node
         * @return Some random node, navigable to from the seed
         */
        private Node getRandomNode() {
            Node curNode = mClient.getSeedNode();
            while (true) {
                if (mRandom.nextBoolean()) {
                    return curNode;
                }
                Value nextValue;
                if (curNode instanceof ListNode) {
                    int size = ((ListNode) curNode).size();
                    if (size == 0) {
                        // can't navigate away from here
                        return curNode;
                    }
                    nextValue = ((ListNode) curNode).get(mRandom.nextInt(size));
                } else if (curNode instanceof StructNode) {
                    if (((StructNode) curNode).keySet().isEmpty()) {
                        // can't navigate away from here
                        return curNode;
                    }
                    nextValue = ((StructNode) curNode).get(pickRandomKey((StructNode) curNode));
                } else {
                    throw new EtherRuntimeException("Unknown node type: " + curNode.getClass());
                }

                if (nextValue instanceof StructReferenceValue) {
                    curNode = nextValue.asStructReference();
                } else if (nextValue instanceof ListReferenceValue) {
                    curNode = nextValue.asListReference();
                } else {
                    // can't navigate away from here
                    return curNode;
                }
            }
        }
    }
}
