package de.tum.in.i22.uc.pdp.core.condition.comparisonOperators;


public class EndsWithComparisonOperator extends GenericComparisonOperator{
	public boolean compare(String parameter1, String parameter2){
		return (parameter1!=null) && (parameter2 != null) && (parameter1.endsWith(parameter2));
	}
}
