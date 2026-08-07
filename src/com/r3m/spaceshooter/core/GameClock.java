package com.r3m.spaceshooter.core;

public class GameClock {

	private final long nanosecondsPerFrame;
	private long lastFrameTime; 
	private double deltaTime;
	
	public GameClock(int targetFPS) {
        nanosecondsPerFrame = 1_000_000_000L / targetFPS;
        reset();
	}
	
	public void reset() {
		lastFrameTime = System.nanoTime();
		deltaTime = 0;
	}
	
	public boolean isNextFrameReady() {
        long now = System.nanoTime();
        long elapsed = now - lastFrameTime;

        if (elapsed >= nanosecondsPerFrame) {
            lastFrameTime = now;
            deltaTime = elapsed / 1_000_000_000.0;
            return true;
        }

        return false;
    }

	   public double waitForNextFrame() throws InterruptedException {
	        long now = System.nanoTime();
	        long elapsed = now - lastFrameTime;
	        long remainingTime = nanosecondsPerFrame - elapsed;

	        if (remainingTime > 0) {
	            long sleepMilliseconds = remainingTime / 1_000_000L;
	            int sleepNanoseconds = (int) (remainingTime % 1_000_000L);

	            Thread.sleep(sleepMilliseconds, sleepNanoseconds);
	        }

	        now = System.nanoTime();
	        elapsed = now - lastFrameTime;
	        lastFrameTime = now;

	        return elapsed / 1_000_000_000.0;
	    }
	   
    public double getDeltaTime() {
        return deltaTime;
    }
}
