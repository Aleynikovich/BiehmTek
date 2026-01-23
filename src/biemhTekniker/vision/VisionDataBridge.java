package biemhTekniker.vision;

/**
 * A thread-safe singleton to share vision data between the
 * Background Task (SmartPickingClient) and the Robot Application (Main).
 */
public class VisionDataBridge {

    private static final VisionDataBridge INSTANCE = new VisionDataBridge();

    // Volatile ensures changes are immediately visible to other threads
    private volatile double x, y, z, rx, ry, rz;
    private volatile boolean isFresh = false;

    private VisionDataBridge() {}

    public static VisionDataBridge get() {
        return INSTANCE;
    }

    /**
     * Updates the coordinates and marks data as fresh.
     * Call this from SmartPickingClient when a part is found.
     */
    public void update(double x, double y, double z, double rx, double ry, double rz) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.rx = rx;
        this.ry = ry;
        this.rz = rz;
        this.isFresh = true;
    }

    /**
     * Checks if new data has arrived since the last time we looked.
     */
    public boolean hasNewData() {
        return isFresh;
    }

    /**
     * Marks the data as "read" or "stale" so we don't pick the same part twice.
     */
    public void consume() {
        this.isFresh = false;
    }

    // --- Getters ---

    public double getX() { return x; }
    public double getY() { return y; }
    public double getZ() { return z; }
    public double getRx() { return rx; }
    public double getRy() { return ry; }
    public double getRz() { return rz; }
}