package de.tum.in.i22.uc.pdp.core.condition.comparisonOperators;

import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;


public class ListInListComparisonOperator extends GenericComparisonOperator{
	public boolean compare(String parameter1, String parameter2){
		if (parameter2==null) return false;
	    Set<String> set1=new HashSet<String>();
	    Set<String> set2=new HashSet<String>();
		StringTokenizer st1 = new StringTokenizer(parameter1);
		StringTokenizer st2 = new StringTokenizer(parameter2);
		while (st1.hasMoreTokens()) {
	         set1.add(st1.nextToken());
	    }
		while (st2.hasMoreTokens()) {
	         set2.add(st2.nextToken());
	    }
		return set1.containsAll(set2);
	}
}
