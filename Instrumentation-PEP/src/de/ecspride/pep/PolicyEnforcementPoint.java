package de.ecspride.pep;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import soot.ArrayType;
import soot.Body;
import soot.BooleanType;
import soot.ByteType;
import soot.CharType;
import soot.DoubleType;
import soot.FloatType;
import soot.IdentityUnit;
import soot.IntType;
import soot.Local;
import soot.LongType;
import soot.PrimType;
import soot.RefType;
import soot.Scene;
import soot.ShortType;
import soot.SootClass;
import soot.SootMethod;
import soot.Type;
import soot.Unit;
import soot.Value;
import soot.javaToJimple.LocalGenerator;
import soot.jimple.ArrayRef;
import soot.jimple.AssignStmt;
import soot.jimple.DefinitionStmt;
import soot.jimple.IdentityStmt;
import soot.jimple.IfStmt;
import soot.jimple.IntConstant;
import soot.jimple.InterfaceInvokeExpr;
import soot.jimple.InvokeExpr;
import soot.jimple.InvokeStmt;
import soot.jimple.Jimple;
import soot.jimple.NewArrayExpr;
import soot.jimple.NewExpr;
import soot.jimple.NopStmt;
import soot.jimple.NullConstant;
import soot.jimple.ParameterRef;
import soot.jimple.SpecialInvokeExpr;
import soot.jimple.StaticInvokeExpr;
import soot.jimple.Stmt;
import soot.jimple.StringConstant;
import soot.jimple.infoflow.android.data.AndroidMethod;
import soot.jimple.infoflow.android.data.AndroidMethod.CATEGORY;
import soot.jimple.infoflow.android.source.data.SourceSinkDefinition;
import soot.jimple.infoflow.entryPointCreators.AndroidEntryPointCreator;
import soot.jimple.infoflow.handlers.ResultsAvailableHandler;
import soot.jimple.infoflow.results.InfoflowResults;
import soot.jimple.infoflow.results.ResultSinkInfo;
import soot.jimple.infoflow.results.ResultSourceInfo;
import soot.jimple.infoflow.solver.cfg.IInfoflowCFG;
import soot.jimple.toolkits.ide.icfg.BiDiInterproceduralCFG;
import de.ecspride.Main;
import de.ecspride.Settings;
import de.ecspride.events.EventInformation;
import de.ecspride.events.Pair;
import de.ecspride.instrumentation.Instrumentation;
import de.ecspride.util.UpdateManifestAndCodeForWaitPDP;
import de.ecspride.util.Util;

/**
 * Responsible for instrumenting statements which will trigger the PDP and waits for a 
 * response from it. The response could be either: statement execution is allowed or
 * statement execution is not allowed.
 * @author Siegfried Rasthofer
 */
public class PolicyEnforcementPoint implements ResultsAvailableHandler{
	private static Logger log = LoggerFactory.getLogger(PolicyEnforcementPoint.class);
	
	/**
	 * key: method-signature indicating the statement which needs some instrumentation
	 * value: event-information about the event which will be triggered at the method-signature  
	 */
	private final Map<String, EventInformation> allEventInformation;
	
	private final Set<SourceSinkDefinition> sources;
	private final Set<SourceSinkDefinition> sinks;
	private final AndroidEntryPointCreator entryPointCreator;
	private int sourceSinkConnectionCounter = 0;
	private InfoflowResults results = null;
	private final String unknownCategory = "UNKNOWN_BUNDLE_DATA";
	
	public PolicyEnforcementPoint(Map<String, EventInformation> eventInformation,
			Set<SourceSinkDefinition> sources,
			Set<SourceSinkDefinition> sinks,
			AndroidEntryPointCreator entryPointCreator){
		this.allEventInformation = eventInformation; 
		this.sources = sources;
		this.sinks = sinks;
		this.entryPointCreator = entryPointCreator;
	}
	
	
	@Override
	public void onResultsAvailable(IInfoflowCFG cfg, InfoflowResults results) {
		log.info("FlowDroid has finished. Duration: " + (System.currentTimeMillis() - Main.startTime) +" ms.");
		Main.startTime = System.currentTimeMillis();
		Settings.instance.setDummyMainToLibraryClass();
		this.results = results;
		
		if (log.isDebugEnabled()) {
			log.debug("");
			log.debug("InfoFlow Results");
			Map<ResultSinkInfo, Set<ResultSourceInfo>> r = results.getResults();
			for (ResultSinkInfo k : r.keySet()) {
				log.debug("ResultSinkInfo: "+ k);

				for (ResultSourceInfo rsi: r.get(k)) {
					log.debug("  source: "+ rsi);
				}
			}
			log.debug("");
		}
	
	
		log.info("Starting bytecode instrumentation.");
		
		log.info("Adding code to initialize PEPs.");
		Util.initializePePInAllPossibleClasses(Settings.instance.getApkPath());
		
		log.info("Redirect main activity");
		String mainActivityClass = UpdateManifestAndCodeForWaitPDP.redirectMainActivity(Settings.instance.getApkPath());
		UpdateManifestAndCodeForWaitPDP.updateWaitPDPActivity(mainActivityClass);
		
		log.info("Adding Policy Enforcement Points (PEPs).");
		doAccessControlChecks(cfg);
		
		log.info("Instrumentation is done.");
		
		if (Settings.mustOutputJimple()) {
			log.info("-------- Dumping Jimple bodies.");
			Main.dumpJimple();
			log.info("--------");
		}
	}
	
	/**
	 * For a concrete method (declared in an application class), find statements 
	 * containing an invoke expression for which the method is one of the method 
	 * in 'allEventInformation' (i.e., getLine1Number(), ...).
	 * 
	 * @param cfg
	 */
	private void doAccessControlChecks(BiDiInterproceduralCFG<Unit, SootMethod> cfg){
		for(SootClass sc : Scene.v().getApplicationClasses()){
			for(SootMethod sm : sc.getMethods()){
				if(sm.isConcrete()){
					Body body = sm.retrieveActiveBody();
					// only instrument application methods (i.e., not methods declared in PEP helper classes
					// or in a Java library classes or in an Android classes, ...)
					if(isInstrumentationNecessary(sm)){
					
						// important to use snapshotIterator here
						Iterator<Unit> i = body.getUnits().snapshotIterator();						
						log.debug("method: "+ sm);
						while(i.hasNext()){
							Stmt s = (Stmt) i.next();
							
							if(s.containsInvokeExpr()) {
								InvokeExpr invExpr = s.getInvokeExpr();
								String methodSignature = invExpr.getMethod().getSignature();
								
								if(allEventInformation.containsKey(methodSignature)){
									log.debug("statement "+ s +" matches "+ methodSignature +".");
									ResultSinkInfo sink = null;
									
									outer:
									for(Map.Entry<ResultSinkInfo, Set<ResultSourceInfo>> result : results.getResults().entrySet()){

										// iterate over all the arguments of the invoke expression
										// and check if an argument is a tainted sink. If one is
										// set variable 'sink' to the ResultSinkInfo key.
										for (Value v : invExpr.getArgs()) {
											Value pathValue = result.getKey().getAccessPath().getPlainValue();
											if (v == pathValue) {
												sink = result.getKey();
												log.debug("found a sink: "+ pathValue);
												break outer;
											}
										}
									}
																			
									if(sink != null){
										log.debug("instrument with data flow information )" + s + ")");
										instrumentSourceToSinkConnections(cfg, sink, s instanceof AssignStmt);
										instrumentWithNoDataFlowInformation(methodSignature, s, invExpr, body, s instanceof AssignStmt);
									} else {
										log.debug("instrument without data flow information (" + s + ")");
										instrumentWithNoDataFlowInformation(methodSignature, s, invExpr, body, s instanceof AssignStmt);
									}
								}
							} // if stmt containts invoke expression
						} // loop on statements
					}
				}
			}
		}
	}
	
	private List<Unit> instrumentIntentAddings(BiDiInterproceduralCFG<Unit, SootMethod> cfg,
			Unit unit, InvokeExpr sinkExpr, Set<ResultSourceInfo> sourceInfo){
		if(isMethodInterComponentSink(sinkExpr.getMethod())){
			SootMethod method = cfg.getMethodOf(unit);
			Body body = null;
			if(method.hasActiveBody())
				body = method.retrieveActiveBody();
			else
				throw new RuntimeException("No body found!");
			
			Set<String> sourceCategories = getDataIdList(sourceInfo);
			
			final String hashSetType = "java.util.HashSet";
			List<Unit> generated = new ArrayList<Unit>();
			
			//HashSet initialization
			Local hashSetLocal = generateFreshLocal(body, RefType.v(hashSetType));
			NewExpr newExpr = Jimple.v().newNewExpr(RefType.v(hashSetType));
			AssignStmt assignStmt = Jimple.v().newAssignStmt(hashSetLocal, newExpr);
			generated.add(assignStmt);
			
			//constructor call
			SpecialInvokeExpr constructorCall = Jimple.v().newSpecialInvokeExpr(hashSetLocal, Scene.v().getMethod("<java.util.HashSet: void <init>()>").makeRef());
			InvokeStmt constructorCallStmt = Jimple.v().newInvokeStmt(constructorCall);
			generated.add(constructorCallStmt);
			
			//add categories to HashSet
			for(String cat : sourceCategories){
				InterfaceInvokeExpr addCall = Jimple.v().newInterfaceInvokeExpr(hashSetLocal, Scene.v().getMethod("<java.util.Set: boolean add(java.lang.Object)>").makeRef(), StringConstant.v(cat));
				InvokeStmt addCallStmt = Jimple.v().newInvokeStmt(addCall);
				generated.add(addCallStmt);
			}
			
			//get Intent
			Value intent = sinkExpr.getArg(0);
			List<Object> args = new ArrayList<Object>();
			args.add(RefType.v("android.content.Intent"));
			args.add(intent);
			args.add(RefType.v(hashSetType));
			args.add(hashSetLocal);
			StaticInvokeExpr sie = Instrumentation.createJimpleStaticInvokeExpr(
					Settings.INSTRUMENTATION_HELPER_JAVA,
					"addTaintInformationToIntent",
					args);
			InvokeStmt invStmt = Jimple.v().newInvokeStmt(sie);
			generated.add(invStmt);
			
			return generated;
		}
		return Collections.emptyList();
	}
	
	/**
	 * 
	 * @param cfg
	 * @param sink
	 * @param assignmentStatement
	 */
	private void instrumentSourceToSinkConnections(BiDiInterproceduralCFG<Unit, SootMethod> cfg, ResultSinkInfo sink, boolean assignmentStatement){
		sourceSinkConnectionCounter += 1;
		
		// loop through the sinks
		for (Map.Entry<ResultSinkInfo, Set<ResultSourceInfo>> result : results.getResults().entrySet()) {
			
			log.debug("compare: "+ result.getKey());
			log.debug("     to: "+ sink);
			
			// if the current sink is the sink at the unit tagged with 'sink'
			if(result.getKey().equals(sink)){
				
				// loop through the sources
				for(ResultSourceInfo si : result.getValue()){
					
					Stmt stmt = si.getSource();
					SootMethod sm = cfg.getMethodOf(stmt);
					Body body = sm.retrieveActiveBody();
					
					// Source instrumentation. The three type categories for the source are:
					// - callback
					// - ICC source method (i.e., Intent.getExtras())
					// - not a callback and not an ICC source method (i.e., getLine1Number())
					//
					if (isInterComponentSourceCallback(si, cfg)) {
						throw new RuntimeException("Callbacks as sources are not supported right now");
					} else if (isInterComponentSourceNoCallback(si, cfg)) {
						//only invoke expression are treated here
						if (stmt.containsInvokeExpr()) {
							//only statements that return a android.os.Bundle are currently supported
							if (stmt instanceof DefinitionStmt) {
								DefinitionStmt defStmt = (DefinitionStmt)stmt;
								Value leftValue = defStmt.getLeftOp();
								
								if (leftValue.getType().equals(RefType.v("android.os.Bundle"))) {
									List<Object> args = new ArrayList<Object>();
									args.add(IntType.v());
									args.add(IntConstant.v(sourceSinkConnectionCounter));
									args.add(RefType.v("android.os.Bundle"));
									args.add(leftValue);
									InvokeExpr invExpr = Instrumentation.createJimpleStaticInvokeExpr(
											Settings.INSTRUMENTATION_HELPER_JAVA,
											"registerNewSourceSinkConnection",
											args);
									InvokeStmt invStmt = Jimple.v().newInvokeStmt(invExpr);
									
									Unit instrumentationPoint = null;
									if (stmt instanceof IdentityStmt) {
										instrumentationPoint = getLastIdentityStmt(body);
									} else {
										instrumentationPoint = stmt;
									}
									body.getUnits().insertAfter(invStmt, instrumentationPoint);
									log.debug("insert a: "+ invStmt);
								} else {
									System.err.println("We do only support android.os.Bundle right now!");
								}
							}
						}
					} else {

						String sourceCat = getSourceCategory(si);
						if(sourceCat != null){
							List<Object> args = new ArrayList<Object>();
							args.add(IntType.v());
							args.add(IntConstant.v(sourceSinkConnectionCounter));
							args.add(RefType.v("java.lang.String"));
							args.add(StringConstant.v(sourceCat));
							InvokeExpr invExpr = Instrumentation.createJimpleStaticInvokeExpr(
									Settings.INSTRUMENTATION_HELPER_JAVA,
									"registerNewSourceSinkConnection",
									args);
							InvokeStmt invStmt = Jimple.v().newInvokeStmt(invExpr);
							
							Unit instrumentationPoint = null;
							if(stmt instanceof IdentityStmt)
								instrumentationPoint = getLastIdentityStmt(body);
							else
								instrumentationPoint = stmt;
							body.getUnits().insertAfter(invStmt, instrumentationPoint);
							log.debug("insert b: "+ invStmt);
						}
					}
					
					// sink instrumentation
					if(sink.getSink().containsInvokeExpr()){	
						Body bodyOfSink = cfg.getMethodOf(result.getKey().getSink()).getActiveBody();
						InvokeExpr invExpr = sink.getSink().getInvokeExpr();
						List<Unit> generated = new ArrayList<Unit>();
						generated.addAll(instrumentIntentAddings(cfg, stmt, invExpr, result.getValue()));
						
						EventInformation sinkEventInfo = allEventInformation.get(invExpr.getMethod().getSignature());
						EventInformation sourceEventInfo = allEventInformation.get(si.getSource().getInvokeExpr().getMethod().getSignature());
						
						generated.addAll(generatePolicyEnforcementPoint(result.getKey().getSink(), invExpr,
								bodyOfSink, sourceSinkConnectionCounter, assignmentStatement));
						
						log.debug("body with data flow:\n"+body);
						for (Unit u: generated) {
							log.debug("gen: "+ u);
						}
						
						if(sinkEventInfo.isInstrumentAfterStatement())
							bodyOfSink.getUnits().insertAfter(generated, result.getKey().getSink());
						else
							bodyOfSink.getUnits().insertBefore(generated, result.getKey().getSink());
					}
					else
						throw new RuntimeException("Double-Check the assumption");
					
					
				} // loop through the sources
				
			} // if the sink at the unit is the current sink
			
		} // loop through the sinks
		
	}
	
	
	/**
	 * Add Policy Enforcement Point (PEP) for Unit 'unit'.
	 * @param methodSignature
	 * @param unit
	 * @param invExpr
	 * @param body
	 * @param assignmentStatement
	 */
	private void instrumentWithNoDataFlowInformation(String methodSignature, Unit unit, InvokeExpr invExpr, Body body, boolean assignmentStatement){
		log.debug("add PEP without dataflow information for unit "+ unit);
		
		EventInformation eventInfo = allEventInformation.get(methodSignature);
		List<Unit> generated = generatePolicyEnforcementPoint(unit, invExpr, body, -1, assignmentStatement);
		
		log.debug("body no data flow:\n"+body);
		for (Unit u: generated) {
			log.debug("gen: "+ u);
		}
		
		if(eventInfo.isInstrumentAfterStatement()) {
			body.getUnits().insertAfter(generated, unit);
		} else {
			body.getUnits().insertBefore(generated, unit);
		}
		
	}
	
	/**
	 * Generate Policy Enforcement Point (PEP) for Unit 'unit'.
	 * @param unit
	 * @param invExpr
	 * @param body
	 * @param dataFlowAvailable
	 * @param assignmentStatement
	 * @return
	 */
	private List<Unit> generatePolicyEnforcementPoint(Unit unit, InvokeExpr invExpr, Body body, int dataFlowAvailable, boolean assignmentStatement){
		
		log.debug("Dataflow available: "+ dataFlowAvailable);
		
		List<Unit> generated = new ArrayList<Unit>(); // store all new units that are generated
		
		String methodSignature = invExpr.getMethod().getSignature();
		EventInformation eventInfo = allEventInformation.get(methodSignature);
		String eventName = eventInfo.getEventName();
		
		Set<Pair<Integer, String>> allParameterInformation = eventInfo.getParameterInformation();
		
		// This list containts types and parameters that are used to build the
		// invoke expression to "isStmtExecutionAllowed':
		//
		// java.lang.String   "eventName"
		// IntType            "dataFlowAvailable"
		// java.lang.Object[] "parameters"
		// 
		List<Object> parameterForHelperMethod = new ArrayList<Object>();
		List<Object> categories = new ArrayList<Object>();
		
		// add event name information
		Type eventNameType = RefType.v("java.lang.String");
		parameterForHelperMethod.add(eventNameType);
		StringConstant eventNameConstant = StringConstant.v(eventName);
		parameterForHelperMethod.add(eventNameConstant);
		
		// add information about dataflow availability
		parameterForHelperMethod.add(IntType.v());
		parameterForHelperMethod.add(IntConstant.v(dataFlowAvailable));
		
		// add information about parameters
		parameterForHelperMethod.add(getParameterArrayType());
		List<Value> paramValues = new ArrayList<Value>();
		
		for(Pair<Integer, String> parameterInfo : allParameterInformation){			
			paramValues.add(StringConstant.v("param" + parameterInfo.getLeft() + "value"));
			paramValues.add(invExpr.getArg(parameterInfo.getLeft()));				
		}			
		
		Pair<Value, List<Unit>> arrayRefAndInstrumentation = generateParameterArray(paramValues, body);

		generated.addAll(arrayRefAndInstrumentation.getRight());
		
		parameterForHelperMethod.add(arrayRefAndInstrumentation.getLeft());
		
		
		// Generate PEP call to the PDP. Store the result send by the PDP to 'resultPDPLocal'
		// Pseudo code looks like this:
		//
		// resultPDPLocal = isStmtExecutionAllowed(eventName, dataFlowAvailable, parameters);
		//
		StaticInvokeExpr sie = Instrumentation.createJimpleStaticInvokeExpr(
				Settings.INSTRUMENTATION_HELPER_JAVA, 
				"isStmtExecutionAllowed", 
				parameterForHelperMethod
				);
		
		Local resultPDPLocal = generateFreshLocal(body, soot.IntType.v());
		AssignStmt asssCondition = Jimple.v().newAssignStmt(resultPDPLocal, sie);
		generated.add(asssCondition);
		
		for (Unit u: generated) {
			System.out.println("isStmt gen: "+ u);
		}
		
		
		if(assignmentStatement){
			// If the method call before which the PEP in inserted is an assignment statement of
			// the form "resultPDPLocal = originalCallThatIsChecked()", generate a new assignment 
			// statement that stores a default value to "resultPDPLocal" if the PDP does not 
			// allow the call of method originalCallThatIsChecked().
			//
			// Pseudo-code:
			//
			// if(resultPDPLocal == 0) goto dummyLabel:
			// result = originalCallThatIsChecked();
			// goto dummyLabel2:
			// dummyLabel:
			// result = dummyValue (i.e., 0 for IntType, false for BooleanType, ...)
			// dummyLabel2:
			// nop
			//
			
			if(unit instanceof DefinitionStmt){
				DefinitionStmt defStmt = (DefinitionStmt)unit; 
				
				Value pepCondition = Jimple.v().newEqExpr(resultPDPLocal, IntConstant.v(0));

				// insert nop
				Unit label2Nop = Jimple.v().newNopStmt();
				body.getUnits().insertAfter(label2Nop, unit);
			
				// insert result = dummyValue
				Unit dummyStatement = createCorrectDummyAssignment((Local)defStmt.getLeftOp());
				body.getUnits().insertAfter(dummyStatement, unit);
				log.debug("insert c: "+ dummyStatement);
				
				// insert goto dummyLabel2:
				body.getUnits().insertAfter(Jimple.v().newGotoStmt(label2Nop), unit);
				
				IfStmt ifStmt = Jimple.v().newIfStmt(pepCondition, dummyStatement);
				generated.add(ifStmt);
			} else {
				throw new RuntimeException("error: expected DefinitionStmt got "+ unit +" -> "+ unit.getClass());
			}
			
		} else {
			// If the method call before which the PEP in inserted is a call statement of
			// the form "originalCallThatIsChecked()", generate a new nop statement
			// to jump to if the PDP does not allow the call of method originalCallThatIsChecked().
			//
			// Pseudo-code:
			//
			// if(resultPDPLocal == 0) goto nopLabel:
			// result = originalCallThatIsChecked();
			// nopLabel:
			// nop
			//
			Value pepCondition = Jimple.v().newEqExpr(resultPDPLocal, IntConstant.v(0));
			
			NopStmt nopStmt = Jimple.v().newNopStmt();
			body.getUnits().insertAfter(nopStmt, unit);
			log.debug("insert d: "+ nopStmt);
			
			IfStmt ifStmt = Jimple.v().newIfStmt(pepCondition, nopStmt);
			
			generated.add(ifStmt);
		}
		
		return generated;
	}
	
	/**
	 * 
	 * @param parameter
	 * @param body
	 * @return
	 */
	private Pair<Value, List<Unit>> generateParameterArray(List<Value> parameter, Body body){
		List<Unit> generated = new ArrayList<Unit>();
		
		NewArrayExpr arrayExpr = Jimple.v().newNewArrayExpr(RefType.v("java.lang.Object"), IntConstant.v(parameter.size()));
		
		Value newArrayLocal = generateFreshLocal(body, getParameterArrayType());
		Unit newAssignStmt = Jimple.v().newAssignStmt(newArrayLocal, arrayExpr);
		generated.add(newAssignStmt);
		
		for(int i = 0; i < parameter.size(); i++){
			Value index = IntConstant.v(i);
			ArrayRef leftSide = Jimple.v().newArrayRef(newArrayLocal, index);
			Value rightSide = generateCorrectObject(body, parameter.get(i), generated);
			
			Unit parameterInArray = Jimple.v().newAssignStmt(leftSide, rightSide);
			generated.add(parameterInArray);
		}
		
		return new Pair<Value, List<Unit>>(newArrayLocal, generated);
	}
	
	private Type getParameterArrayType(){
		Type parameterArrayType = RefType.v("java.lang.Object");
		Type parameterArray = ArrayType.v(parameterArrayType, 1);
		
		return parameterArray;
	}
	
	private Value generateCorrectObject(Body body, Value value, List<Unit> generated){
		if(value.getType() instanceof PrimType){
			//in case of a primitive type, we use boxing (I know it is not nice, but it works...) in order to use the Object type
			if(value.getType() instanceof BooleanType){
				Local booleanLocal = generateFreshLocal(body, RefType.v("java.lang.Boolean"));
				
				SootClass sootClass = Scene.v().getSootClass("java.lang.Boolean");
				SootMethod valueOfMethod = sootClass.getMethod("java.lang.Boolean valueOf(boolean)");
				StaticInvokeExpr staticInvokeExpr = Jimple.v().newStaticInvokeExpr(valueOfMethod.makeRef(), value);
				
				Unit newAssignStmt = Jimple.v().newAssignStmt(booleanLocal, staticInvokeExpr);
				generated.add(newAssignStmt);
				
				return booleanLocal;
			}
			else if(value.getType() instanceof ByteType){
				Local byteLocal = generateFreshLocal(body, RefType.v("java.lang.Byte"));
				
				SootClass sootClass = Scene.v().getSootClass("java.lang.Byte");
				SootMethod valueOfMethod = sootClass.getMethod("java.lang.Byte valueOf(byte)");
				StaticInvokeExpr staticInvokeExpr = Jimple.v().newStaticInvokeExpr(valueOfMethod.makeRef(), value);
				
				Unit newAssignStmt = Jimple.v().newAssignStmt(byteLocal, staticInvokeExpr);
				generated.add(newAssignStmt);
				
				return byteLocal;
			}
			else if(value.getType() instanceof CharType){
				Local characterLocal = generateFreshLocal(body, RefType.v("java.lang.Character"));
				
				SootClass sootClass = Scene.v().getSootClass("java.lang.Character");
				SootMethod valueOfMethod = sootClass.getMethod("java.lang.Character valueOf(char)");
				StaticInvokeExpr staticInvokeExpr = Jimple.v().newStaticInvokeExpr(valueOfMethod.makeRef(), value);
				
				Unit newAssignStmt = Jimple.v().newAssignStmt(characterLocal, staticInvokeExpr);
				generated.add(newAssignStmt); 
				
				return characterLocal;
			}
			else if(value.getType() instanceof DoubleType){
				Local doubleLocal = generateFreshLocal(body, RefType.v("java.lang.Double"));
				
				SootClass sootClass = Scene.v().getSootClass("java.lang.Double");
				SootMethod valueOfMethod = sootClass.getMethod("java.lang.Double valueOf(double)");
																
				StaticInvokeExpr staticInvokeExpr = Jimple.v().newStaticInvokeExpr(valueOfMethod.makeRef(), value);
				
				Unit newAssignStmt = Jimple.v().newAssignStmt(doubleLocal, staticInvokeExpr);
				generated.add(newAssignStmt); 
				
				return doubleLocal;
			}
			else if(value.getType() instanceof FloatType){
				Local floatLocal = generateFreshLocal(body, RefType.v("java.lang.Float"));
				
				SootClass sootClass = Scene.v().getSootClass("java.lang.Float");
				SootMethod valueOfMethod = sootClass.getMethod("java.lang.Float valueOf(float)");
				StaticInvokeExpr staticInvokeExpr = Jimple.v().newStaticInvokeExpr(valueOfMethod.makeRef(), value);
				
				Unit newAssignStmt = Jimple.v().newAssignStmt(floatLocal, staticInvokeExpr);
				generated.add(newAssignStmt); 
				
				return floatLocal;
			}
			else if(value.getType() instanceof IntType){
				Local integerLocal = generateFreshLocal(body, RefType.v("java.lang.Integer"));
				
				SootClass sootClass = Scene.v().getSootClass("java.lang.Integer");
				SootMethod valueOfMethod = sootClass.getMethod("java.lang.Integer valueOf(int)");
				StaticInvokeExpr staticInvokeExpr = Jimple.v().newStaticInvokeExpr(valueOfMethod.makeRef(), value);
				
				Unit newAssignStmt = Jimple.v().newAssignStmt(integerLocal, staticInvokeExpr);
				generated.add(newAssignStmt); 
				
				return integerLocal;
			}
			else if(value.getType() instanceof LongType){
				Local longLocal = generateFreshLocal(body, RefType.v("java.lang.Long"));
				
				SootClass sootClass = Scene.v().getSootClass("java.lang.Long");
				SootMethod valueOfMethod = sootClass.getMethod("java.lang.Long valueOf(long)");
				StaticInvokeExpr staticInvokeExpr = Jimple.v().newStaticInvokeExpr(valueOfMethod.makeRef(), value);
				
				Unit newAssignStmt = Jimple.v().newAssignStmt(longLocal, staticInvokeExpr);
				generated.add(newAssignStmt); 
				
				return longLocal;
			}
			else if(value.getType() instanceof ShortType){
				Local shortLocal = generateFreshLocal(body, RefType.v("java.lang.Short"));
				
				SootClass sootClass = Scene.v().getSootClass("java.lang.Short");
				SootMethod valueOfMethod = sootClass.getMethod("java.lang.Short valueOf(short)");
				StaticInvokeExpr staticInvokeExpr = Jimple.v().newStaticInvokeExpr(valueOfMethod.makeRef(), value);
				
				Unit newAssignStmt = Jimple.v().newAssignStmt(shortLocal, staticInvokeExpr);
				generated.add(newAssignStmt); 
				
				return shortLocal;
			}
			else
				throw new RuntimeException("Ooops, something went all wonky!");
		}
		else
			//just return the value, there is nothing to box
			return value;
	}
	
	private Local generateFreshLocal(Body b, Type type){
		LocalGenerator lg = new LocalGenerator(b);
		return lg.generateLocal(type);
	}
	
	private boolean isInstrumentationNecessary(SootMethod method){
		if (method.getDeclaringClass().isJavaLibraryClass())
			return false;
		if (method.getDeclaringClass().toString().startsWith("android."))
			return false;
		for (String cn: Settings.class2AddList) {
			if (method.getDeclaringClass().toString().startsWith(cn))
				return false;
		}
		return true;
	}
	
	/**
	 * This method iterates over all sources from the FlowDroid-results and extracts the 
	 * category of the specific source. If there is no category found, it will return an empty set,
	 * otherwise the correct categories will be added. 
	 * @param sourcesInfo: all possible sources from which we try to identify the category
	 * @return: set of categories for specific sink
	 */
	private Set<String> getDataIdList(Set<ResultSourceInfo> sourcesInfo){
		Set<String> dataIdList = new HashSet<String>();
		for(ResultSourceInfo sInfo : sourcesInfo){
			if(sInfo.getSource().containsInvokeExpr()){
				InvokeExpr invExpr = sInfo.getSource().getInvokeExpr();
				
				for(SourceSinkDefinition meth : sources) {
					AndroidMethod am = (AndroidMethod) meth.getMethod();
					if(am.getSignature().equals(invExpr.getMethod().getSignature())) {
						dataIdList.add(am.getCategory().toString());
					}
				}
			}
			else if (isSourceInfoParameter(sInfo)){
				dataIdList.add(unknownCategory);
			}
			else
				throw new RuntimeException("Currently not supported");
		}
		
		return dataIdList;
	}


	private boolean isSourceInfoParameter(ResultSourceInfo sInfo) {
		return sInfo.getSource() instanceof IdentityStmt
				&& ((IdentityStmt) sInfo.getSource()).getRightOp() instanceof ParameterRef;
	}
	
	private String getSourceCategory(ResultSourceInfo sourceInfo){
		if(sourceInfo.getSource().containsInvokeExpr()){
			InvokeExpr invExpr = sourceInfo.getSource().getInvokeExpr();
						
			for(SourceSinkDefinition meth : sources) {
				AndroidMethod am = (AndroidMethod) meth.getMethod();
				if(am.getSignature().equals(invExpr.getMethod().getSignature())){
						return am.getCategory().toString();
				}
			}
		}
		else if(isSourceInfoParameter(sourceInfo)){
			return unknownCategory;
		}
		else
			throw new RuntimeException("Currently not supported");
		
		return null;
	}
	
	private boolean isMethodInterComponentSink(SootMethod sm) {	
		for (SourceSinkDefinition meth : sinks) {
			AndroidMethod am = (AndroidMethod) meth.getMethod();
			if(am.getCategory() == CATEGORY.INTER_APP_COMMUNICATION){
				if(am.getSubSignature().equals(sm.getSubSignature()))
					return true;
			}
		}
		
		return false;
	}
	
	/**
	 * Return true if the method corresponding to the source 'si' is an
	 * Inter Component Communication source method such as "Intent.getExtras()".
	 * @param si
	 * @param cfg
	 * @return
	 */
	private boolean isInterComponentSourceNoCallback(ResultSourceInfo si, BiDiInterproceduralCFG<Unit, SootMethod> cfg){
		if(!si.getSource().containsInvokeExpr())
			return false;
		
		InvokeExpr invExpr = si.getSource().getInvokeExpr();
		SootMethod sm = invExpr.getMethod();
				
		for(SourceSinkDefinition meth : sources){
			AndroidMethod am = (AndroidMethod) meth.getMethod();
			if(am.getCategory() == CATEGORY.INTER_APP_COMMUNICATION){
				if(am.getSubSignature().equals(sm.getSubSignature())) {
					log.info("source is: "+ am);
					return true;
				}
			}
		}
		
		return false;
	}
	
	private boolean isInterComponentSourceCallback(ResultSourceInfo si,
			BiDiInterproceduralCFG<Unit, SootMethod> cfg){
		if(isSourceInfoParameter(si)){
			SootMethod sm = cfg.getMethodOf(si.getSource());
			
			if(entryPointCreator.getCallbackFunctions().containsKey(sm.getDeclaringClass())){
				if(entryPointCreator.getCallbackFunctions().get(sm.getDeclaringClass()).contains(sm.getSignature()))
					return true;
			}
		}
		
		return false;
	}
	
	/**
	 * Generate a default assignment with 'local' as left-hand-side value.
	 * @param local
	 * @return
	 */
	private Unit createCorrectDummyAssignment(Local local){
		Unit dummyAssignemnt = null;
		if(local.getType() instanceof PrimType){
			if(local.getType() instanceof IntType){
				StaticInvokeExpr sie = Instrumentation.createJimpleStaticInvokeExpr(
						Settings.SANITIZER, "dummyInteger", null);
				dummyAssignemnt = Jimple.v().newAssignStmt(local, sie);
			}
			else if(local.getType() instanceof BooleanType){
				StaticInvokeExpr sie = Instrumentation.createJimpleStaticInvokeExpr(
						Settings.SANITIZER, "dummyBoolean", null);
				dummyAssignemnt = Jimple.v().newAssignStmt(local, sie);
			}
			else if(local.getType() instanceof ByteType){
				StaticInvokeExpr sie = Instrumentation.createJimpleStaticInvokeExpr(
						Settings.SANITIZER, "dummyByte", null);
				dummyAssignemnt = Jimple.v().newAssignStmt(local, sie);
			}
			else if(local.getType() instanceof CharType){
				StaticInvokeExpr sie = Instrumentation.createJimpleStaticInvokeExpr(
						Settings.SANITIZER, "dummyCharacter", null);
				dummyAssignemnt = Jimple.v().newAssignStmt(local, sie);
			}
			else if(local.getType() instanceof DoubleType){
				StaticInvokeExpr sie = Instrumentation.createJimpleStaticInvokeExpr(
						Settings.SANITIZER, "dummyDouble", null);
				dummyAssignemnt = Jimple.v().newAssignStmt(local, sie);
			}
			else if(local.getType() instanceof FloatType){
				StaticInvokeExpr sie = Instrumentation.createJimpleStaticInvokeExpr(
						Settings.SANITIZER, "dummyFloat", null);
				dummyAssignemnt = Jimple.v().newAssignStmt(local, sie);
			}
			else if(local.getType() instanceof LongType){
				StaticInvokeExpr sie = Instrumentation.createJimpleStaticInvokeExpr(
						Settings.SANITIZER, "dummyLong", null);
				dummyAssignemnt = Jimple.v().newAssignStmt(local, sie);
			}
			else if(local.getType() instanceof ShortType){
				StaticInvokeExpr sie = Instrumentation.createJimpleStaticInvokeExpr(
						Settings.SANITIZER, "dummyShort", null);
				dummyAssignemnt = Jimple.v().newAssignStmt(local, sie);
			}
			else
				throw new RuntimeException("Oops, the primitive type is not correct");
		}
		else{
			if(local.getType().equals(RefType.v("java.lang.String")))
				dummyAssignemnt = Jimple.v().newAssignStmt(local, StringConstant.v(""));
			else
				dummyAssignemnt = Jimple.v().newAssignStmt(local, NullConstant.v());
		}
		
		return dummyAssignemnt;
	}
	
	private Unit getLastIdentityStmt(Body b) {
		Unit u = b.getUnits().getFirst();
		while (u instanceof IdentityUnit)
			u = b.getUnits().getSuccOf(u);
		
		return b.getUnits().getPredOf(u);
	}
}
