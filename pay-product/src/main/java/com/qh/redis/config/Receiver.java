package com.qh.redis.config;

import java.util.concurrent.CountDownLatch;

import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

public class Receiver {
	
	private static final org.slf4j.Logger logger = LoggerFactory.getLogger(Receiver.class);

    private CountDownLatch latch;

    @Autowired
    public Receiver(CountDownLatch latch) {
        this.latch = latch;
    }

    public void receiveMessage(String message) {
    	logger.info("Received <" + message + ">");
        latch.countDown();
    }
}