package biemhTekniker.vision;

import biemhTekniker.logger.Logger;
import com.kuka.roboticsAPI.applicationModel.tasks.CycleBehavior;
import com.kuka.roboticsAPI.applicationModel.tasks.RoboticsAPICyclicBackgroundTask;
import com.kuka.generated.ioAccess.VisionIOGroup;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

public class SmartPickingClient extends RoboticsAPICyclicBackgroundTask {

    private static final Logger log = Logger.getLogger(SmartPickingClient.class);
    private static final String SERVER_IP = "172.31.1.69";
    private static final int PORT = 59002;

    private Socket _socket;
    private PrintWriter _writer;
    private BufferedReader _reader;
    private boolean _isConnected = false;

    @Inject
    private VisionIOGroup vision;

    @Override
    public void initialize() {
        initializeCyclic(0, 1000, TimeUnit.MILLISECONDS, CycleBehavior.BestEffort);
        
              
        log.info("SmartPickingClient initialized. Target: " + SERVER_IP + ":" + PORT);
        tryToConnect();
    }

    @Override
    public void runCyclic() {
        if (!_isConnected) {
            tryToConnect();
        } else {
            // Fix 2: Check for socket health explicitly
            if (_socket == null || _socket.isClosed()) {
                _isConnected = false;
                return;
            }

            if (vision.getTriggerRequest()) {
                sendAndReceive();
            }
        }
    }

    private void tryToConnect() {
        try {
            if (_socket != null && !_socket.isClosed()) return;

            log.debug("Attempting to connect to Smart Picking Server...");
            _socket = new Socket(SERVER_IP, PORT);
            
            // Fix 3: Set timeout to avoid halting the robot logic if server hangs
            _socket.setSoTimeout(500); 
            
            _writer = new PrintWriter(_socket.getOutputStream(), true);
            _reader = new BufferedReader(new InputStreamReader(_socket.getInputStream()));
            
            _isConnected = true;
            log.info("Connected successfully.");

        } catch (Exception e) {
            _isConnected = false;
            // Log sparingly in cyclic to avoid flooding
        }
    }

    private void sendAndReceive() {
        try {
            // Fix 4: Use print, not println, to control line endings exactly like Hercules
            String payload = "15;BIEMH26_105055\r\n";
            _writer.print(payload);
            _writer.flush();

            // Read Response
            String response = _reader.readLine();
            log.info("Server Response: " + response);
            
            // Acknowledge logic (Optional: Reset trigger here if not handled by PLC)
            // vision.setTriggerRequest(false); 

        } catch (Exception e) {
            log.error("Communication error: " + e.getMessage());
            _isConnected = false;
            try { _socket.close(); } catch (Exception ignored) {}
        }
    }

    @Override
    public void dispose() {
        try {
            if (_socket != null) _socket.close();
        } catch (Exception e) {
            log.error("Error closing vision socket: " + e.getMessage());
        }
        super.dispose();
    }
}