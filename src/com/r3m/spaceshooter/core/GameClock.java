package com.r3m.spaceshooter.core;

/** Regulates a game loop to a target frame rate and measures elapsed time. */
public class GameClock {

	private final long nanosecondsPerFrame;
	private long lastFrameTime; 
	private double deltaTime;
	
    /**
     * Creates a clock for the requested frame rate.
     *
     * @param targetFPS desired frames per second; must be greater than zero
     * @throws ArithmeticException if {@code targetFPS} is zero
     */
	public GameClock(int targetFPS) {
        nanosecondsPerFrame = 1_000_000_000L / targetFPS;
        reset();
	}
	
    /** Resets frame timing to the current instant. */
	public void reset() {
		lastFrameTime = System.nanoTime();
		deltaTime = 0;
	}
	
    /**
     * Checks whether a frame interval has elapsed without blocking.
     *
     * @return {@code true} when the next frame should be processed
     */
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

    /**
     * Waits for the next frame boundary and returns the elapsed frame time.
     *
     * @return seconds elapsed since the previous frame
     * @throws InterruptedException if the sleeping thread is interrupted
     */
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
	   
    /**
     * Returns the last delta measured by {@link #isNextFrameReady()}.
     *
     * @return elapsed time in seconds, or zero immediately after a reset
     */
    public double getDeltaTime() {
        return deltaTime;
    }
}
