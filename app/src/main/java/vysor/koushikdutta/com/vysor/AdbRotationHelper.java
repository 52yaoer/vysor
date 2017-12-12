package vysor.koushikdutta.com.vysor;

/**
 * Created by zhaofei on 16/12/31.
 */

public class AdbRotationHelper
{
    public static void forceRotation(final int paramInt)
    {
        new Thread()
        {
            public void run()
            {
                try
                {
                    Runtime.getRuntime().exec("/system/bin/content insert --uri content://settings/system --bind name:s:accelerometer_rotation --bind value:i:0").waitFor();
                    Runtime.getRuntime().exec("/system/bin/content insert --uri content://settings/system --bind name:s:user_rotation --bind value:i:" + paramInt).waitFor();
                    return;
                }
                catch (Exception localException) {}
            }
        }.start();
    }

    public static Process resetForcedRotation()
    {
        try
        {
            Process localProcess = Runtime.getRuntime().exec("/system/bin/content insert --uri content://settings/system --bind name:s:accelerometer_rotation --bind value:i:1");
            return localProcess;
        }
        catch (Exception localException) {}
        return null;
    }
}
