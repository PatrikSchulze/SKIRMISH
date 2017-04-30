package main;


/**
 * FrameSkipper.
 * Initialize with FPS value as parameter.
 * call FrameSkipper.sync() instead of Thread.yield()
 * in situation of no Vsync, ergo window mode
 *
 * if this class proves to work fine. use this all the time
 * because Vsync does not mean 60 FPS by default.
 * For example, A 100 Hz CRT withh sync at 100FPS.
 *
 * @author Patrik Schulze, Alchemic Tempest
 */
public class FrameSkipper
{
    private int target_fps;
    private long timeThen;
    
    public FrameSkipper(int targetFrameRate)
    {
        target_fps = targetFrameRate;
        timeThen = System.nanoTime();
    }

    public void sync()
    {
        long gapTo = 1000000000L / target_fps + timeThen;
        long timeNow = System.nanoTime();

        while (gapTo > timeNow)
        {
        	try {Thread.sleep(1);}catch(InterruptedException e){}
            timeNow = System.nanoTime();
        }

        timeThen = timeNow;
                
    }
    
}
