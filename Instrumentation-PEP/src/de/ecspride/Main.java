package de.ecspride;

import java.util.Map;
import java.util.Set;






import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
	public static Logger log = LoggerFactory.getLogger(Main.class);
	public static long startTime = 0;
	
	public static void main(String[] args) {
		startTime = System.currentTimeMillis();
		long d = 0;
		Set<AndroidMethod> sources, sinks;
		
		log.info("Starting Intrumentation-PEP");
		
		//arguments will be set
		Settings.instance.parseCommandLineArgs(args);
		
		log.info("Initialize Soot and FlowDroid.");
		//Soot is initialized
		Settings.instance.initialiseSoot();
		//clean the sootOutput dir before start
		Util.clearSootOutputJimpleDir();
		
		//parse the eventInformation.xml file in order to extract all information about the
		//events we will cover
		EventInformationParser eventInfoParser = new EventInformationParser();
		Map<String, EventInformation> eventInformation = eventInfoParser.parseEventInformation();

		log.debug("\n[ ] Event Information:");
		for (String k: eventInformation.keySet()) {
			log.debug("");
			log.debug("Event: "+ k);
			log.debug("", eventInformation.get(k));	
		}
		
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
		d = (System.currentTimeMillis() - startTime);
		log.info("Initialization done. Duration: "+ d +" ms.");
		
		log.info("Starting taint analysis and bytecode instrumentation.");
		startTime = System.currentTimeMillis();
		runFlowDroid(setupApp, eventInformation);
		d = (System.currentTimeMillis() - startTime);
		log.info("Taint analysis and bytecode instrumentation have finished. Duration: " + d +" ms");
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
		
		PolicyEnforcementPoint pep = new PolicyEnforcementPoint(eventInformation, setupApp.getSources(), setupApp.getSinks(), setupApp.getEntryPointCreator());
		setupApp.runInfoflow(pep); 
		
		//write output file (.class or .apk)
		for (SootClass sc : Scene.v().getClasses())
			for (SootMethod sm : sc.getMethods())
				if (sm.hasActiveBody())
					sm.getActiveBody().validate();
		PackManager.v().writeOutput();
	}

}
