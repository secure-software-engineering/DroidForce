package de.ecspride;

import java.util.Map;
import java.util.Set;

import soot.PackManager;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.jimple.infoflow.BiDirICFGFactory;
import soot.jimple.infoflow.IInfoflow.CallgraphAlgorithm;
import soot.jimple.infoflow.android.SetupApplication;
import soot.jimple.infoflow.android.data.AndroidMethod;
import soot.jimple.infoflow.solver.IInfoflowCFG;
import soot.jimple.infoflow.solver.InfoflowCFG;
import soot.jimple.infoflow.taintWrappers.EasyTaintWrapper;
import soot.jimple.infoflow.taintWrappers.ITaintPropagationWrapper;
import de.ecspride.events.EventInformation;
import de.ecspride.events.EventInformationParser;
import de.ecspride.pep.ConfigForPolicyEnforcementPoint;
import de.ecspride.pep.PolicyEnforcementPoint;
import de.ecspride.util.SourcesSinks;
import de.ecspride.util.Util;

public class Main {
	public static long startTime = 0;
	public static void main(String[] args) {
		startTime = System.currentTimeMillis();
		Set<AndroidMethod> sources, sinks;
		
		//arguments will be set
		Settings.instance.parseCommandLineArgs(args);
		
		//Soot is initialized
		Settings.instance.initialiseSoot();
		//clean the sootOutput dir before start
		Util.clearSootOutputJimpleDir();
		
		//parse the eventInformation.xml file in order to extract all information about the
		//events we will cover
		EventInformationParser eventInfoParser = new EventInformationParser();
		Map<String, EventInformation> eventInformation = eventInfoParser.parseEventInformation();
		
		SourcesSinks sourcesSinks = new SourcesSinks();
		//get Android sources
		sources = sourcesSinks.getAndroidSourcesMethods(Settings.instance.sourceFile);
		
		//get Android sinks
		sinks = sourcesSinks.getAndroidSinkMethods(Settings.instance.sinkFile);
		
		//get SetupApplication
		SetupApplication setupApp = new SetupApplication(Settings.instance.androidPlatforms, Settings.instance.apkFile);
		try{
			//initialize SetupApplication
			setupApp.calculateSourcesSinksEntrypoints(sources, sinks);
		}catch(Exception ex){
			ex.printStackTrace();
			System.exit(0);
		}
		
		
		runFlowDroid(setupApp, eventInformation);
		System.out.println("xxxxxxxxxTime dynamic part: " + (System.currentTimeMillis() - startTime));
	}
	
	private static void writeJimpleOutput() {
		// has to go over it again (also in internalTransform), because one
		// class can modify another class
		for (SootClass c : Scene.v().getApplicationClasses()) {
			if (!Util.isAndroidClass(c)) {
				Util.writeJimpleFiles(c);
			}
		}
	}
	
	private static void runFlowDroid(SetupApplication setupApp, Map<String, EventInformation> eventInformation){
		ITaintPropagationWrapper taintWrapper = null;
		try{
			taintWrapper = new EasyTaintWrapper(Settings.instance.taintWrapperFile);
		}
		catch(Exception ex){
			ex.printStackTrace();
			System.exit(0);
		}
		
		setupApp.setIcfgFactory(new BiDirICFGFactory() {
			@Override
			public IInfoflowCFG buildBiDirICFG(
					CallgraphAlgorithm callgraphAlgorithm) {
				return new InfoflowCFG();
			}
		});
		
		setupApp.setTaintWrapper(taintWrapper);
		setupApp.setSootConfig(new ConfigForPolicyEnforcementPoint());
		
		//settings
		setupApp.setEnableStaticFieldTracking(false);
		setupApp.setFlowSensitiveAliasing(false);
		setupApp.setAccessPathLength(1);
		setupApp.setComputeResultPaths(false);

		PolicyEnforcementPoint pep = new PolicyEnforcementPoint(eventInformation, setupApp.getSources(), setupApp.getSinks(), setupApp.getEntryPointCreator());
		setupApp.runInfoflow(pep); 
		
		//write output file (.class or .apk)
		for (SootClass sc : Scene.v().getClasses())
			for (SootMethod sm : sc.getMethods())
				if (sm.hasActiveBody())
					sm.getActiveBody().validate();
		PackManager.v().writeOutput();
		
//		writeJimpleOutput();
	}

}
