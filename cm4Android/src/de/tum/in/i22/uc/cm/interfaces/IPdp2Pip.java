package de.tum.in.i22.uc.cm.interfaces;

import java.util.Set;

import de.tum.in.i22.uc.cm.datatypes.interfaces.IContainer;
import de.tum.in.i22.uc.cm.datatypes.interfaces.IData;
import de.tum.in.i22.uc.cm.datatypes.interfaces.IEvent;
import de.tum.in.i22.uc.cm.datatypes.interfaces.IName;
import de.tum.in.i22.uc.cm.datatypes.interfaces.IStatus;
//import de.tum.in.i22.uc.thrift.generator.AThriftMethod;
//import de.tum.in.i22.uc.thrift.generator.AThriftService;

/**
 * Interface defining methods a PDP can invoke on a PIP.
 * @author Kelbert & Lovat
 *
 */
//@AThriftService(name="TPdp2Pip")
public interface IPdp2Pip {
//	@AThriftMethod(signature="bool evaluatePredicateSimulatingNextState(1:Types.TEvent eventToSimulate, 2:string predicate)")
	public boolean evaluatePredicateSimulatingNextState(IEvent eventToSimulate, String predicate);

//	@AThriftMethod(signature="bool evaluatePredicatCurrentState(1:string predicate)")
	public boolean evaluatePredicateCurrentState(String predicate);

//	@AThriftMethod(signature="set<Types.TContainer> getContainerForData(1:Types.TData data)")
	public Set<IContainer> getContainersForData(IData data);

//	@AThriftMethod(signature="set<Types.TData> getDataInContainer(1:Types.TName containerName)")
	public Set<IData> getDataInContainer(IName containerName);

//	@AThriftMethod(signature="Types.TStatus startSimulation()")
	public IStatus startSimulation();

//	@AThriftMethod(signature="Types.TStatus stopSimulation()")
	public IStatus stopSimulation();

//	@AThriftMethod(signature="bool isSimulating()")
	public boolean isSimulating();

//	@AThriftMethod(signature="Types.TStatus update(1:Types.TEvent updateEvent)")
	public IStatus update(IEvent updateEvent);

}
