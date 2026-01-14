package hardware;

import javax.inject.Inject;

import com.kuka.generated.ioAccess.Gripper1IOGroup;
import com.kuka.generated.ioAccess.Gripper2IOGroup;
import com.kuka.roboticsAPI.applicationModel.IApplicationData;
import com.kuka.roboticsAPI.applicationModel.tasks.ITaskLogger;
import com.kuka.roboticsAPI.uiModel.userKeys.IUserKey;
import com.kuka.roboticsAPI.uiModel.userKeys.IUserKeyBar;
import com.kuka.roboticsAPI.uiModel.userKeys.IUserKeyListener;
import com.kuka.roboticsAPI.uiModel.userKeys.UserKeyAlignment;
import com.kuka.roboticsAPI.uiModel.userKeys.UserKeyEvent;

/**
 * Controller for Gripper 1 and Gripper 2 with HMI button support.
 * Creates SmartPad buttons for manual gripper control.
 * 
 * Java 1.7 compatible - no lambdas, no diamond operators
 */
public class GripperController {
    
    private Gripper1IOGroup gripper1IO;
    private Gripper2IOGroup gripper2IO;
    private ITaskLogger logger;
    private IApplicationData appData;
    
    private IUserKeyBar keyBar;
    private IUserKey gripper1OpenKey;
    private IUserKey gripper1CloseKey;
    private IUserKey gripper2OpenKey;
    private IUserKey gripper2CloseKey;
    
    /**
     * Create gripper controller
     * @param gripper1IO Gripper 1 I/O group
     * @param gripper2IO Gripper 2 I/O group
     * @param logger Task logger
     * @param appData Application data for HMI access
     */
    public GripperController(Gripper1IOGroup gripper1IO, Gripper2IOGroup gripper2IO, 
                            ITaskLogger logger, IApplicationData appData) {
        this.gripper1IO = gripper1IO;
        this.gripper2IO = gripper2IO;
        this.logger = logger;
        this.appData = appData;
    }
    
    /**
     * Initialize HMI buttons on SmartPad
     */
    public void initializeHMI() {
        keyBar = appData.getProcessData("GripperControl").getUserKeyBar(UserKeyAlignment.TOP);
        
        // Gripper 1 Open button
        gripper1OpenKey = keyBar.addUserKey(0, "Gripper 1 Open", true);
        gripper1OpenKey.setEnabled(true);
        gripper1OpenKey.addUserKeyListener(new IUserKeyListener() {
            public void onKeyEvent(IUserKey key, UserKeyEvent event) {
                if (event == UserKeyEvent.KeyDown) {
                    openGripper1();
                }
            }
        });
        
        // Gripper 1 Close button
        gripper1CloseKey = keyBar.addUserKey(1, "Gripper 1 Close", true);
        gripper1CloseKey.setEnabled(true);
        gripper1CloseKey.addUserKeyListener(new IUserKeyListener() {
            public void onKeyEvent(IUserKey key, UserKeyEvent event) {
                if (event == UserKeyEvent.KeyDown) {
                    closeGripper1();
                }
            }
        });
        
        // Gripper 2 Open button
        gripper2OpenKey = keyBar.addUserKey(2, "Gripper 2 Open", true);
        gripper2OpenKey.setEnabled(true);
        gripper2OpenKey.addUserKeyListener(new IUserKeyListener() {
            public void onKeyEvent(IUserKey key, UserKeyEvent event) {
                if (event == UserKeyEvent.KeyDown) {
                    openGripper2();
                }
            }
        });
        
        // Gripper 2 Close button
        gripper2CloseKey = keyBar.addUserKey(3, "Gripper 2 Close", true);
        gripper2CloseKey.setEnabled(true);
        gripper2CloseKey.addUserKeyListener(new IUserKeyListener() {
            public void onKeyEvent(IUserKey key, UserKeyEvent event) {
                if (event == UserKeyEvent.KeyDown) {
                    closeGripper2();
                }
            }
        });
        
        keyBar.publish();
        logger.info("Gripper HMI buttons initialized");
    }
    
    /**
     * Remove HMI buttons from SmartPad
     */
    public void removeHMI() {
        if (keyBar != null) {
            if (gripper1OpenKey != null) {
                keyBar.removeUserKey(gripper1OpenKey);
            }
            if (gripper1CloseKey != null) {
                keyBar.removeUserKey(gripper1CloseKey);
            }
            if (gripper2OpenKey != null) {
                keyBar.removeUserKey(gripper2OpenKey);
            }
            if (gripper2CloseKey != null) {
                keyBar.removeUserKey(gripper2CloseKey);
            }
            keyBar.publish();
        }
        logger.info("Gripper HMI buttons removed");
    }
    
    /**
     * Open Gripper 1
     */
    public void openGripper1() {
        gripper1IO.setClose(Boolean.valueOf(false));
        gripper1IO.setOpen(Boolean.valueOf(true));
        logger.info("Gripper 1: OPEN");
    }
    
    /**
     * Close Gripper 1
     */
    public void closeGripper1() {
        gripper1IO.setOpen(Boolean.valueOf(false));
        gripper1IO.setClose(Boolean.valueOf(true));
        logger.info("Gripper 1: CLOSE");
    }
    
    /**
     * Open Gripper 2
     */
    public void openGripper2() {
        gripper2IO.setClose(Boolean.valueOf(false));
        gripper2IO.setOpen(Boolean.valueOf(true));
        logger.info("Gripper 2: OPEN");
    }
    
    /**
     * Close Gripper 2
     */
    public void closeGripper2() {
        gripper2IO.setOpen(Boolean.valueOf(false));
        gripper2IO.setClose(Boolean.valueOf(true));
        logger.info("Gripper 2: CLOSE");
    }
    
    /**
     * Wait for Gripper 1 to reach open position
     * @param timeoutMs Timeout in milliseconds
     * @return true if gripper opened, false if timeout
     */
    public boolean waitForGripper1Open(long timeoutMs) {
        long startTime = System.currentTimeMillis();
        while (!gripper1IO.getIsOpen()) {
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                return false;
            }
            if (System.currentTimeMillis() - startTime > timeoutMs) {
                logger.warn("Gripper 1 open timeout");
                return false;
            }
        }
        return true;
    }
    
    /**
     * Wait for Gripper 1 to reach closed position
     * @param timeoutMs Timeout in milliseconds
     * @return true if gripper closed, false if timeout
     */
    public boolean waitForGripper1Close(long timeoutMs) {
        long startTime = System.currentTimeMillis();
        while (!gripper1IO.getIsClosed()) {
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                return false;
            }
            if (System.currentTimeMillis() - startTime > timeoutMs) {
                logger.warn("Gripper 1 close timeout");
                return false;
            }
        }
        return true;
    }
    
    /**
     * Wait for Gripper 2 to reach open position
     * @param timeoutMs Timeout in milliseconds
     * @return true if gripper opened, false if timeout
     */
    public boolean waitForGripper2Open(long timeoutMs) {
        long startTime = System.currentTimeMillis();
        while (!gripper2IO.getIsOpen()) {
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                return false;
            }
            if (System.currentTimeMillis() - startTime > timeoutMs) {
                logger.warn("Gripper 2 open timeout");
                return false;
            }
        }
        return true;
    }
    
    /**
     * Wait for Gripper 2 to reach closed position
     * @param timeoutMs Timeout in milliseconds
     * @return true if gripper closed, false if timeout
     */
    public boolean waitForGripper2Close(long timeoutMs) {
        long startTime = System.currentTimeMillis();
        while (!gripper2IO.getIsClosed()) {
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                return false;
            }
            if (System.currentTimeMillis() - startTime > timeoutMs) {
                logger.warn("Gripper 2 close timeout");
                return false;
            }
        }
        return true;
    }
    
    /**
     * Check if Gripper 1 is fully open
     * @return true if open
     */
    public boolean isGripper1Open() {
        return gripper1IO.getIsOpen();
    }
    
    /**
     * Check if Gripper 1 is fully closed
     * @return true if closed
     */
    public boolean isGripper1Closed() {
        return gripper1IO.getIsClosed();
    }
    
    /**
     * Check if Gripper 2 is fully open
     * @return true if open
     */
    public boolean isGripper2Open() {
        return gripper2IO.getIsOpen();
    }
    
    /**
     * Check if Gripper 2 is fully closed
     * @return true if closed
     */
    public boolean isGripper2Closed() {
        return gripper2IO.getIsClosed();
    }
}
