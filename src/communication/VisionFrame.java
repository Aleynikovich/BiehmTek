package communication;

import com.kuka.roboticsAPI.geometricModel.Frame;

/**
 * Data class representing a frame received from the vision system.
 * Parses incoming string data into structured robot frame coordinates.
 * 
 * Java 1.7 compatible - no lambdas, no diamond operators
 */
public class VisionFrame {
    
    private String operationType;
    private String timestamp;
    private double x;
    private double y;
    private double z;
    private double alpha;
    private double beta;
    private double gamma;
    private boolean valid;
    
    public VisionFrame() {
        this.valid = false;
    }
    
    /**
     * Parse vision system data string into frame components
     * Expected format from legacy system: "type,timestamp,reserved,reserved,reserved,x,y,z,gamma,beta,alpha"
     * 
     * @param datagram Raw data string from vision system
     * @param delimiter Field delimiter (e.g., "," or ";")
     * @return true if parsing successful, false otherwise
     */
    public boolean parseFromString(String datagram, String delimiter) {
        if (datagram == null || datagram.isEmpty()) {
            this.valid = false;
            return false;
        }
        
        try {
            String[] tokens = datagram.split(delimiter);
            
            if (tokens.length < 3) {
                this.valid = false;
                return false;
            }
            
            // Parse operation type (first field)
            this.operationType = tokens[0];
            
            // Parse coordinate data if present (legacy format: fields 5-10 contain x,y,z,gamma,beta,alpha)
            // Field layout: [0:type, 1-4:reserved, 5:x, 6:y, 7:z, 8:gamma, 9:beta, 10:alpha]
            if (tokens.length >= 8) {
                this.x = Double.parseDouble(tokens[5]);
                this.y = Double.parseDouble(tokens[6]);
                this.z = Double.parseDouble(tokens[7]);
                
                // Rotation angles
                if (tokens.length >= 11) {
                    this.gamma = Double.parseDouble(tokens[8]);
                    this.beta = Double.parseDouble(tokens[9]);
                    this.alpha = Double.parseDouble(tokens[10]);
                } else {
                    this.gamma = 0.0;
                    this.beta = 0.0;
                    this.alpha = 0.0;
                }
            }
            
            this.valid = true;
            return true;
            
        } catch (NumberFormatException e) {
            this.valid = false;
            return false;
        } catch (ArrayIndexOutOfBoundsException e) {
            this.valid = false;
            return false;
        }
    }
    
    /**
     * Convert parsed data to KUKA Frame object
     * @param baseFrame Base frame to copy from (e.g., /BinPicking)
     * @return Frame object with parsed coordinates
     */
    public Frame toKukaFrame(Frame baseFrame) {
        Frame result = new Frame(baseFrame);
        if (this.valid) {
            result.setX(this.x);
            result.setY(this.y);
            result.setZ(this.z);
            result.setAlphaRad(this.alpha);
            result.setBetaRad(this.beta);
            result.setGammaRad(this.gamma);
        }
        return result;
    }
    
    // Getters
    
    public String getOperationType() {
        return operationType;
    }
    
    public String getTimestamp() {
        return timestamp;
    }
    
    public double getX() {
        return x;
    }
    
    public double getY() {
        return y;
    }
    
    public double getZ() {
        return z;
    }
    
    public double getAlpha() {
        return alpha;
    }
    
    public double getBeta() {
        return beta;
    }
    
    public double getGamma() {
        return gamma;
    }
    
    public boolean isValid() {
        return valid;
    }
    
    public String toString() {
        return "VisionFrame[type=" + operationType + 
               ", x=" + x + ", y=" + y + ", z=" + z +
               ", alpha=" + alpha + ", beta=" + beta + ", gamma=" + gamma +
               ", valid=" + valid + "]";
    }
}
