# Core Architecture Layer

This directory contains the core architectural components for the KUKA LBR iiwa robot system.

## Overview

The architecture follows strict separation of concerns, with components organized into distinct packages:

- **config**: Configuration management (singleton pattern)
- **communication**: TCP/IP communication and logging
- **hardware**: Hardware control (grippers, I/O, heartbeat)
- **ui**: User interface components (future)

## Key Constraints

- **Java Version**: 1.7 only (NO Java 8+ features like lambdas, streams, or diamond operators)
- **Concurrency**: Background tasks for non-blocking operation
- **Thread Safety**: Uses `java.util.concurrent.atomic` classes and `volatile` variables
- **PLC Ownership**: PLC is the cell master; all operations gated by Profinet handshake

## Package Structure

### config Package

**ConfigManager** - Singleton configuration manager
- Loads `robot.properties` and `plc.properties` from KRC drive
- Provides type-safe property access methods
- Default path: `/home/KRC/configs/` (configurable)

Usage:
```java
ConfigManager config = ConfigManager.getInstance();
config.setConfigBasePath("/home/KRC/configs/");
config.loadRobotConfig();
String visionIp = config.getRobotProperty("vision.ip", "192.168.1.100");
int visionPort = config.getRobotPropertyInt("vision.port", 5000);
```

### communication Package

**ILogger** - Simple logger interface for decoupling from KUKA API

**VisionFrame** - Data class for parsed vision system frames
- Parses delimited string data into structured coordinates
- Converts to KUKA Frame objects
- Based on legacy BinPicking_EKI.java protocol

Usage:
```java
VisionFrame frame = new VisionFrame();
if (frame.parseFromString(data, ",")) {
    Frame kukaFrame = frame.toKukaFrame(baseFrame);
    // Use kukaFrame for robot motion
}
```

**VisionClient** - Background TCP client for vision system
- Connects to vision system (e.g., Photoneo scanner)
- Continuously reads and parses incoming frames
- Non-blocking operation using background thread
- Thread-safe data exchange via AtomicBoolean flags

Usage:
```java
VisionClient client = new VisionClient(logger, "192.168.1.100", 5000, ",");
client.connect();
client.sendData("TRIGGER");
if (client.waitForData(5000)) {
    VisionFrame frame = client.getLatestFrame();
}
client.disconnect();
```

**LoggingServer** - Telnet-style logging broadcast server
- Runs ServerSocket on configured port
- Broadcasts robot log messages to all connected clients
- Multiple clients supported simultaneously

Usage:
```java
LoggingServer logServer = new LoggingServer(logger, 5001);
logServer.start();
logServer.broadcastLog("Robot moving to home position");
logServer.stop();
```

### hardware Package

**ILogger** - Simple logger interface (same as communication package)

**HeartbeatTask** - 100ms heartbeat toggle for PLC
- Toggles RobotStatus.ZRes1 signal every 100ms
- Indicates robot is alive to PLC (cell master)
- Runs in background thread (daemon)

Usage:
```java
HeartbeatTask heartbeat = new HeartbeatTask(logger, robotStatusIO, 100);
heartbeat.start();
// ... robot operations ...
heartbeat.stop();
```

**GripperController** - Control for Gripper 1 and Gripper 2
- Open/close operations via Profinet I/O
- Wait methods with timeout for position confirmation
- Status query methods

Usage:
```java
GripperController grippers = new GripperController(gripper1IO, gripper2IO, logger);
grippers.openGripper1();
if (grippers.waitForGripper1Open(3000)) {
    getLogger().info("Gripper 1 opened successfully");
}
```

**MeasurementGripperController** - PLC-based gripper request handler
- Polls PLC request signals in background
- Callback interface for PLC-initiated gripper operations
- Used for measurement machine gripper coordination

Usage:
```java
MeasurementGripperController mmGripper = new MeasurementGripperController(plcRequestsIO, logger);
mmGripper.setListener(new PlcRequestListener() {
    public void onGripper1OpenRequest() {
        grippers.openGripper1();
    }
    // Implement other callbacks...
});
mmGripper.startMonitoring();
```

## Configuration Files

### robot.properties
Located at: `/home/KRC/configs/robot.properties` (or configured path)

```properties
# Vision System Settings
vision.ip=192.168.1.100
vision.port=5000
vision.delimiter=,

# Logging Server Settings
logging.port=5001

# Robot Settings
robot.name=KUKA_iiwa_14_R820_1
robot.controller=KUKA_Sunrise_Cabinet_1
```

### plc.properties
Located at: `/home/KRC/configs/plc.properties` (or configured path)

```properties
# Heartbeat Settings
plc.heartbeat.signal=ZRes1
plc.heartbeat.interval=100

# PLC IP and Communication
plc.ip=192.168.1.200
plc.profinet.enabled=true
```

## Integration with RoboticsAPIApplication

Example integration in main application:

```java
@Override
public void initialize() {
    // Load configuration
    ConfigManager config = ConfigManager.getInstance();
    config.setConfigBasePath("/home/KRC/configs/");
    try {
        config.loadRobotConfig();
        config.loadPlcConfig();
    } catch (IOException e) {
        getLogger().error("Failed to load configuration: " + e.getMessage());
    }
    
    // Start logging server
    int logPort = config.getRobotPropertyInt("logging.port", 5001);
    LoggingServer logServer = new LoggingServer(new ILogger() {
        public void info(String msg) { getLogger().info(msg); }
        public void warn(String msg) { getLogger().warn(msg); }
        public void error(String msg) { getLogger().error(msg); }
    }, logPort);
    try {
        logServer.start();
    } catch (IOException e) {
        getLogger().error("Failed to start logging server: " + e.getMessage());
    }
    
    // Start heartbeat
    HeartbeatTask heartbeat = new HeartbeatTask(new ILogger() { /* ... */ }, robotStatusIO, 100);
    heartbeat.start();
    
    // Initialize grippers
    GripperController grippers = new GripperController(gripper1IO, gripper2IO, new ILogger() { /* ... */ });
    
    // Connect to vision system
    String visionIp = config.getRobotProperty("vision.ip", "192.168.1.100");
    int visionPort = config.getRobotPropertyInt("vision.port", 5000);
    VisionClient visionClient = new VisionClient(new ILogger() { /* ... */ }, visionIp, visionPort, ",");
    try {
        visionClient.connect();
    } catch (IOException e) {
        getLogger().error("Failed to connect to vision system: " + e.getMessage());
    }
}

@Override
public void dispose() {
    // Clean up all background tasks
    if (heartbeat != null) heartbeat.stop();
    if (logServer != null) logServer.stop();
    if (visionClient != null) visionClient.disconnect();
    super.dispose();
}
```

## Thread Safety Notes

- All background tasks use daemon threads
- Inter-thread communication via AtomicBoolean flags
- No blocking operations during robot motion
- All tasks can be started/stopped independently

## Legacy Reference

The communication protocol and data sequences are based on the legacy `Documentation/BinPicking_EKI.java` file. Key differences:
- Uses background tasks instead of blocking calls
- Proper separation of concerns
- Configuration externalized to properties files
- Thread-safe atomic flags instead of polling loops

## Future Enhancements

- Add HMI button support for manual gripper control (using IApplicationData)
- Implement retry logic for vision communication failures
- Add health monitoring for background tasks
- Implement graceful shutdown with timeout
