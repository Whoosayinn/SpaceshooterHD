package com.r3m.spaceshooter.system;


/**
 * ShipAnimator maps the ship's current speed to the correct
 * sprite frame in the spritesheet.
 *
 * The sheet has 25 frames divided into 5 animation zones:
 *   Frames  0- 4: idle loop        (no movement)
 *   Frames  5- 9: light thrust
 *   Frames 10-14: medium thrust
 *   Frames 15-19: heavy thrust
 *   Frames 20-24: max velocity loop
 *
 * The current "position" in the sheet is a float that moves
 * forward (accelerating) or backward (decelerating) based on speed.
 * A separate frame timer cycles through the 5 frames of whichever
 * zone the ship is currently in.
 */
public class ShipAnimator {

    /** Creates an animator in its idle state. */
    public ShipAnimator() {
    }

    // --- ANIMATION ZONES ---
    // each zone is 5 frames wide in the sprite sheet
    private static final int FRAMES_PER_ZONE = 5;
    private static final int TOTAL_ZONES     = 5;  // idle + 4 thrust levels
    private static final int TOTAL_FRAMES    = FRAMES_PER_ZONE * TOTAL_ZONES; // 25

    // --- ZONE BOUNDARIES (0-based frame indices) ---
    private static final int IDLE_START    = 0;   // frames 0-4
    private static final int MAX_START     = 20;  // frames 20-24

    // how fast the animation position moves through zones
    // higher = faster transition between idle and max thrust zones
    private static final double ZONE_TRANSITION_SPEED = 15.0;

    // how many seconds each frame is displayed before advancing
    // 0.1 = 10fps animation cycle — adjust for faster/slower flicker
    private static final double FRAME_DURATION = 0.1;

    // current position in the 25-frame sheet (float for smooth transitions)
    // 0.0 = fully idle, 20.0 = fully at max thrust zone
    // this is the "needle" that slides along the sheet based on speed
    private double animationPosition = 0.0;

    // which frame within the current 5-frame zone we're showing
    // cycles 0 → 1 → 2 → 3 → 4 → 0 → ...
    private int currentFrameInZone = 0;

    // accumulates deltaTime — when it exceeds FRAME_DURATION, advance frame
    private double frameTimer = 0.0;

    /**
     * Update animation state — called every frame from GameController.update()
     *
     * @param velocityMagnitude current speed of the ship (0 to MAX_SPEED)
     * @param maxSpeed          the MAX_SPEED constant from GameController
     * @param deltaTime         seconds since last frame
     */
    public void update(double velocityMagnitude, double maxSpeed, double deltaTime) {

        // --- Step 1: Calculate target animation position ---
        // map velocity (0 to maxSpeed) to sheet position (0 to 20)
        // 0   velocity → position 0  (idle zone, frames 0-4)
        // max velocity → position 20 (max zone, frames 20-24)
        double speedRatio = Math.min(velocityMagnitude / maxSpeed, 1.0);
        double targetPosition = speedRatio * MAX_START;

        // --- Step 2: Smoothly slide animationPosition toward target ---
        // this creates the gradual ramp-up and ramp-down effect
        // when accelerating: position slides forward (0 → 20)
        // when decelerating: position slides backward (20 → 0)
        double diff = targetPosition - animationPosition;
        animationPosition += diff * ZONE_TRANSITION_SPEED * deltaTime;

        // clamp to valid range
        animationPosition = Math.max(0, Math.min(animationPosition, MAX_START));

        // --- Step 3: Advance the frame cycle timer ---
        // independently of zone position, cycle through the 5 frames
        // of whichever zone we're currently in
        frameTimer += deltaTime;
        if (frameTimer >= FRAME_DURATION) {
            frameTimer -= FRAME_DURATION;
            // advance to next frame in the zone, loop back at 5
            currentFrameInZone = (currentFrameInZone + 1) % FRAMES_PER_ZONE;
        }
    }

    /**
     * Returns the index (0-24) of the frame to display this frame.
     *
     * Takes the current zone (derived from animationPosition)
     * and adds the current frame offset within that zone.
     *
     * @return frame index from 0 through 24
     */
    public int getCurrentFrameIndex() {
        // convert animationPosition to a zone index (0-4)
        // e.g. position 7.3 → zone 1 (light thrust, frames 5-9)
        int zoneIndex = (int)(animationPosition / FRAMES_PER_ZONE);

        // clamp zone to valid range
        zoneIndex = Math.max(0, Math.min(zoneIndex, TOTAL_ZONES - 1));

        // base frame = start of the current zone
        int baseFrame = zoneIndex * FRAMES_PER_ZONE;

        // final frame = base + current cycle position within zone
        return baseFrame + currentFrameInZone;
    }
}
