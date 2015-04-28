package de.tum.pip.structures;

import java.io.Serializable;
import java.util.Hashtable;
import java.util.List;

public class PDPEvent implements Serializable {

    private static final long serialVersionUID = 5577576393784473750L;

    public String action;

    @SuppressWarnings("rawtypes")
    public List<Hashtable> parameters;

}
