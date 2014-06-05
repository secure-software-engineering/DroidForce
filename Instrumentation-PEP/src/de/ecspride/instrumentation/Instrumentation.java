package de.ecspride.instrumentation;

import java.util.ArrayList;

import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.Type;
import soot.Value;
import soot.jimple.Jimple;
import soot.jimple.StaticInvokeExpr;

public class Instrumentation {
	
	public static StaticInvokeExpr createJimpleStaticInvokeExpr(String javaClass, String call, Object... args) {
		SootClass sootClass = Scene.v().getSootClass(javaClass);

		ArrayList<Type> argTypes = new ArrayList<Type>();
		ArrayList<Value> argList = new ArrayList<Value>();

		if (args != null) {
		if (args.length % 2 != 0) {
			throw new RuntimeException(
					"Mismatched argument types:values in static call to "
							+ call);
		} else {
			for (int i = 0; i < args.length; i++)
				if (i % 2 == 0) // First type, then argument
					argTypes.add((Type) args[i]);
				else
					argList.add((Value) args[i]);
		}
		}

		SootMethod createAndAdd = sootClass.getMethod(call, argTypes);
		StaticInvokeExpr sie = Jimple.v().newStaticInvokeExpr(
				createAndAdd.makeRef(), argList);
		
		return sie;
	}
}
