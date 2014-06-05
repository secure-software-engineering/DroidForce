
package de.tum.pip.structures;

public class PIPName {
    /**
     * the ID of the process, the representation belongs to (if representation
     * is system-wide unique: PID = -1)
     */
    public int PID;

    /**
     * the name = representation of a data container
     */
    public String name;

    /**
	 * 
	 */
    public PIPName(int PID, String name) {
        this.PID = PID;
        this.name = name;
    }

}
