package de.ecspride.instrumentation;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.Type;
import soot.Value;
import soot.jimple.Jimple;
import soot.jimple.StaticInvokeExpr;

public class Instrumentation {

	private static Logger log = LoggerFactory.getLogger(Instrumentation.class);

	public static StaticInvokeExpr createJimpleStaticInvokeExpr(String javaClass, String call, List<Object> args) {
		SootClass sootClass = Scene.v().getSootClass(javaClass);

		ArrayList<Type> argTypes = new ArrayList<Type>();
		ArrayList<Value> argList = new ArrayList<Value>();

		if (args != null) {
			if (args.size() % 2 != 0) {
				throw new RuntimeException(
						"Mismatched argument types:values in static call to "
								+ call);
			} else {
				for (int i = 0; i < args.size(); i++)
					if (i % 2 == 0) // First type, then argument
						argTypes.add((Type) args.get(i));
					else
						argList.add((Value) args.get(i));
			}
		}

		SootMethod createAndAdd = sootClass.getMethod(call, argTypes);
		StaticInvokeExpr sie = Jimple.v().newStaticInvokeExpr(
				createAndAdd.makeRef(), argList);

		log.debug("new invoke expression: "+ sie);

		return sie;
	}
}
