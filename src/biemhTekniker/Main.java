package biemhTekniker;

import com.kuka.roboticsAPI.applicationModel.RoboticsAPIApplication;
import com.kuka.roboticsAPI.deviceModel.LBR;
import javax.inject.Inject;


@SuppressWarnings("unused")
public class Main extends RoboticsAPIApplication
{
    @Inject
    private LBR iiwa;

    private ConfigManager config;
    private Thread consoleClientThread;

    @Override
    public void initialize()
    {

    }

    @Override
    public void run()
    {

    }

    @Override
    public void dispose()
    {

        super.dispose();
    }

}