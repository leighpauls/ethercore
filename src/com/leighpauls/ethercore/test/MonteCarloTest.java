package com.leighpauls.ethercore.test;


/**
 * Tests random valid combinations of transactions for consistency
 */
public class MonteCarloTest {

    public static void main(String[] args) {
        new MonteCarloTestInstance(0).run();
        System.out.println("Success!!!");
    }

}
