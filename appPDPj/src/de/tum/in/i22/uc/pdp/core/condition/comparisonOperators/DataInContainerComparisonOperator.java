package de.tum.in.i22.uc.pdp.core.condition.comparisonOperators;

import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.tum.in.i22.uc.cm.datatypes.basic.NameBasic;
import de.tum.in.i22.uc.cm.datatypes.interfaces.IData;
import de.tum.in.i22.uc.cm.interfaces.IPdp2Pip;

public class DataInContainerComparisonOperator extends GenericComparisonOperator{
	private static Logger _logger = LoggerFactory.getLogger(DataInContainerComparisonOperator.class);

	private IPdp2Pip _pip;
	
	public DataInContainerComparisonOperator(IPdp2Pip pip){
		super();
		_pip=pip;
	}
		
	public boolean compare(String cont, String data){
		_logger.trace("Data in Container comparison. cont = [" +cont+"], data = ["+data+"]");
		
		if (cont==null) {
			_logger.debug("cont == null. returnign false");
		}
		
		Set<IData> dataSet=_pip.getDataInContainer(new NameBasic(cont));
		for (IData d: dataSet){
			if (data.equals(d.getId())) {
				_logger.trace("data-container match found. returning true");
				return true;
			}
		}
		_logger.trace("data-container match not found. returning false");
		return false;
	}
}
