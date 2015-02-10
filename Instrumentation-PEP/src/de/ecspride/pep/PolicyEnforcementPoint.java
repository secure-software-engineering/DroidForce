package de.ecspride.pep;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
import soot.jimple.infoflow.entryPointCreators.AndroidEntryPointCreator;
import soot.jimple.infoflow.handlers.ResultsAvailableHandler;
import soot.jimple.infoflow.results.InfoflowResults;
import soot.jimple.infoflow.results.ResultSinkInfo;
import soot.jimple.infoflow.results.ResultSourceInfo;
import soot.jimple.infoflow.solver.IInfoflowCFG;
import soot.jimple.toolkits.ide.icfg.BiDiInterproceduralCFG;
import de.ecspride.Main;
import de.ecspride.Settings;
import de.ecspride.events.EventInformation;
import de.ecspride.events.Pair;
import de.ecspride.instrumentation.Instrumentation;
import de.ecspride.util.Util;

/**
 * Responsible for instrumenting statements which will trigger the PDP and waits for a 
 * response from it. The response could be either: statement execution is allowed or
 * statement execution is not allowed.
 * @author Siegfried Rasthofer
 */
public class PolicyEnforcementPoint implements ResultsAvailableHandler{
	/**
	 * key: method-signature indicating the statement which needs some instrumentation
	 * value: event-information about the event which will be triggered at the method-signature  
	 */
	private final Map<String, EventInformation> allEventInformation;
	
	private final Set<AndroidMethod> sources;
	private final Set<AndroidMethod> sinks;
	private final AndroidEntryPointCreator entryPointCreator;
	private int sourceSinkConnectionCounter = 0;
	private InfoflowResults results = null;
	private final String unknownCategory = "UNKNOWN_BUNDLE_DATA";
	
	public PolicyEnforcementPoint(Map<String, EventInformation> eventInformation, Set<AndroidMethod> sources, Set<AndroidMethod> sinks, AndroidEntryPointCreator entryPointCreator){
		this.allEventInformation = eventInformation; 
		this.sources = sources;
		this.sinks = sinks;
		this.entryPointCreator = entryPointCreator;
	}
	
	
	@Override
	public void onResultsAvailable(IInfoflowCFG cfg, InfoflowResults results) {
		System.out.println("xxxxxxxxxTime for static part: " + (System.currentTimeMillis() - Main.startTime));
		Main.startTime = System.currentTimeMillis();
		Settings.instance.setDummyMainToLibraryClass();
		this.results = results;
		
		//first instrument some statements which initializes the PEP
		Util.initializePePInAllPossibleClasses(Settings.instance.getApkPath());
		
		doAccessControlChecks(cfg);
	}
	
	private void doAccessControlChecks(BiDiInterproceduralCFG<Unit, SootMethod> cfg){
		for(SootClass sc : Scene.v().getApplicationClasses()){
			for(SootMethod sm : sc.getMethods()){
				if(sm.isConcrete()){
					Body body = sm.retrieveActiveBody();
					//is instrumentation necessary
					if(isInstrumentationNecessary(sm)){
					
						//important to use snapshotIterator here
						Iterator<Unit> i = body.getUnits().snapshotIterator();						
						
						while(i.hasNext()){
							Stmt s = (Stmt) i.next();
							
							if(s.containsInvokeExpr()) {
								InvokeExpr invExpr = s.getInvokeExpr();
								String methodSignature = invExpr.getMethod().getSignature();
								if(allEventInformation.containsKey(methodSignature)){
									ResultSinkInfo sink = null;
									outer : for(Map.Entry<ResultSinkInfo, Set<ResultSourceInfo>> result : results.getResults().entrySet()){
										for (Value v : invExpr.getArgs())
											if (v == result.getKey().getAccessPath().getPlainValue()) {
												sink = result.getKey();
												break outer;
										}
									}
																			
									if(sink != null){
										instrumentWithNoDataFlowInformation(methodSignature, s, invExpr, body, s instanceof AssignStmt);
										instrumentSourceToSinkConnections(cfg, sink, s instanceof AssignStmt);
									}
									else
										instrumentWithNoDataFlowInformation(methodSignature, s, invExpr, body, s instanceof AssignStmt);
								}
							}
						}
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
			
			StaticInvokeExpr sie = Instrumentation.createJimpleStaticInvokeExpr(Settings.instance.INSTRUMENTATION_HELPER_JAVA, "addTaintInformationToIntent", RefType.v("android.content.Intent"), intent, RefType.v(hashSetType), hashSetLocal);
			InvokeStmt invStmt = Jimple.v().newInvokeStmt(sie);
			generated.add(invStmt);
			
			return generated;
		}
		return Collections.emptyList();
	}
	
	private void instrumentSourceToSinkConnections(BiDiInterproceduralCFG<Unit, SootMethod> cfg, ResultSinkInfo sink, boolean assignmentStatement){
		sourceSinkConnectionCounter += 1;
		
		for(Map.Entry<ResultSinkInfo, Set<ResultSourceInfo>> result : results.getResults().entrySet()){
			if(result.getKey().getSink().equals(sink)){
				for(ResultSourceInfo si : result.getValue()){
					Stmt stmt = si.getSource();
					SootMethod sm = cfg.getMethodOf(stmt);
					Body body = sm.retrieveActiveBody();
					
					if(isInterComponentSourceCallback(si, cfg)){
						throw new RuntimeException("Callbacks as sources are not supported right now");
					}
					else if(isInterComponentSourceNoCallback(si, cfg)){
						//only invoke expression are treated here
						if(stmt.containsInvokeExpr()){
							//only statements that return a android.os.Bundle are currently supported
							if(stmt instanceof DefinitionStmt){
								DefinitionStmt defStmt = (DefinitionStmt)stmt;
								Value leftValue = defStmt.getLeftOp();
								
								if(leftValue.getType().equals(RefType.v("android.os.Bundle"))){
									InvokeExpr invExpr = Instrumentation.createJimpleStaticInvokeExpr(Settings.instance.INSTRUMENTATION_HELPER_JAVA, "registerNewSourceSinkConnection", IntType.v(), IntConstant.v(sourceSinkConnectionCounter), RefType.v("android.os.Bundle"), leftValue);
									InvokeStmt invStmt = Jimple.v().newInvokeStmt(invExpr);
									
									Unit instrumentationPoint = null;
									if(stmt instanceof IdentityStmt)
										instrumentationPoint = getLastIdentityStmt(body);
									else
										instrumentationPoint = stmt;
									body.getUnits().insertAfter(invStmt, instrumentationPoint);
								}
								else
									System.err.println("We do only support android.os.Bundle right now!");
							}
						}
					}
					else{
						//source instrumentation
						String sourceCat = getSourceCategory(si);
						if(sourceCat != null){
							InvokeExpr invExpr = Instrumentation.createJimpleStaticInvokeExpr(Settings.instance.INSTRUMENTATION_HELPER_JAVA, "registerNewSourceSinkConnection", IntType.v(), IntConstant.v(sourceSinkConnectionCounter), RefType.v("java.lang.String"), StringConstant.v(sourceCat));
							InvokeStmt invStmt = Jimple.v().newInvokeStmt(invExpr);
							
							Unit instrumentationPoint = null;
							if(stmt instanceof IdentityStmt)
								instrumentationPoint = getLastIdentityStmt(body);
							else
								instrumentationPoint = stmt;
							body.getUnits().insertAfter(invStmt, instrumentationPoint);
						}
					}
					
					//sink instrumentation
					if(sink.getSink().containsInvokeExpr()){	
						Body bodyOfSink = cfg.getMethodOf(result.getKey().getSink()).getActiveBody();
						InvokeExpr invExpr = sink.getSink().getInvokeExpr();
						List<Unit> generated = new ArrayList<Unit>();
						generated.addAll(instrumentIntentAddings(cfg, stmt, invExpr, result.getValue()));
						
						EventInformation eventInfo = allEventInformation.get(invExpr.getMethod().getSignature());
						
						generated.addAll(generatePolicyEnforcementPoint(result.getKey().getSink(), invExpr,
								bodyOfSink, sourceSinkConnectionCounter, assignmentStatement));
						
						if(eventInfo.isInstrumentAfterStatement())
							bodyOfSink.getUnits().insertAfter(generated, result.getKey().getSink());
						else
							bodyOfSink.getUnits().insertBefore(generated, result.getKey().getSink());
					}
					else
						throw new RuntimeException("Double-Check the assumption");
					
					
				}
			}
		}
	}
	
	
	private void instrumentWithNoDataFlowInformation(String methodSignature, Unit unit, InvokeExpr invExpr, Body body, boolean assignmentStatement){
		//do the instrumentation
		EventInformation eventInfo = allEventInformation.get(methodSignature);
		
		List<Unit> generated = generatePolicyEnforcementPoint(unit, invExpr, body, -1, assignmentStatement);
		
		if(eventInfo.isInstrumentAfterStatement())
			body.getUnits().insertAfter(generated, unit);
		else
			body.getUnits().insertBefore(generated, unit);
		
	}
	
	
	private List<Unit> generatePolicyEnforcementPoint(Unit unit, InvokeExpr invExpr, Body body, int dataFlowAvailable, boolean assignmentStatement){
		List<Unit> generated = new ArrayList<Unit>();
		String methodSignature = invExpr.getMethod().getSignature();
		EventInformation eventInfo = allEventInformation.get(methodSignature);
		
		String eventName = eventInfo.getEventName();
		Set<Pair<Integer, String>> allParameterInformation = eventInfo.getParameterInformation();
		
		List<Object> parameterForHelperMethod = new ArrayList<Object>();
		
		Type eventNameType = RefType.v("java.lang.String");
		parameterForHelperMethod.add(eventNameType);
		StringConstant eventNameConstant = StringConstant.v(eventName);
		parameterForHelperMethod.add(eventNameConstant);
		
		parameterForHelperMethod.add(IntType.v());
		parameterForHelperMethod.add(IntConstant.v(dataFlowAvailable));
		
		parameterForHelperMethod.add(getParameterArrayType());
		List<Value> paramValues = new ArrayList<Value>();
		
		for(Pair<Integer, String> parameterInfo : allParameterInformation){			
			paramValues.add(StringConstant.v("param" + parameterInfo.getLeft() + "value"));
			paramValues.add(invExpr.getArg(parameterInfo.getLeft()));				
		}			
		
		Pair<Value, List<Unit>> arrayRefAndInstrumentation = generateParameterArray(paramValues, body);
		//instrument new array
//		body.getUnits().insertBefore(arrayRefAndInstrumentation.getRight(), unit);
		generated.addAll(arrayRefAndInstrumentation.getRight());
		
		parameterForHelperMethod.add(arrayRefAndInstrumentation.getLeft());
		
		StaticInvokeExpr sie = Instrumentation.createJimpleStaticInvokeExpr(Settings.instance.INSTRUMENTATION_HELPER_JAVA, "isStmtExecutionAllowed", parameterForHelperMethod.toArray());
		
		
		Local conditionLocal = generateFreshLocal(body, soot.IntType.v());
		
		AssignStmt asssCondition = Jimple.v().newAssignStmt(conditionLocal, sie);
		
		generated.add(asssCondition);
		
		
		//condition check for pep - false case
		//we also have to care about the initialiation of the local in case it is an assignment statement
		if(assignmentStatement){
			if(unit instanceof DefinitionStmt){
				DefinitionStmt defStmt = (DefinitionStmt)unit; 
				
				Value pepCondition = Jimple.v().newEqExpr(conditionLocal, IntConstant.v(0));
				
				Unit dummyStatement = createCorrectDummyAssignment((Local)defStmt.getLeftOp());
				body.getUnits().insertAfter(dummyStatement, unit);
				
				IfStmt ifStmt = Jimple.v().newIfStmt(pepCondition, dummyStatement);
				generated.add(ifStmt);
			}
			else
				throw new RuntimeException("error: expected DefinitionStmt got "+ unit +" -> "+ unit.getClass());
		}
		else{
			//condition check for pep - true case
			Value pepCondition = Jimple.v().newEqExpr(conditionLocal, IntConstant.v(0));
			
			NopStmt nopStmt = Jimple.v().newNopStmt();
			body.getUnits().insertAfter(nopStmt, unit);
			
			IfStmt ifStmt = Jimple.v().newIfStmt(pepCondition, nopStmt);
			
			generated.add(ifStmt);
		}
		
		return generated;
	}
	
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
		if(method.getDeclaringClass().isJavaLibraryClass() ||
				method.getDeclaringClass().toString().startsWith("android.") ||
				method.getDeclaringClass().toString().equals(Settings.instance.EVENT_PEP_JAVA) ||
			method.getDeclaringClass().toString().equals(Settings.instance.REMOTE_SERVICE_CONNECTION_JAVA) ||
			method.getDeclaringClass().toString().equals(Settings.instance.INCOMING_HANDLER_JAVA) ||
			method.getDeclaringClass().toString().equals(Settings.instance.INSTRUMENTATION_HELPER_JAVA))
			return false;
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
				
				for(AndroidMethod am : sources)
					if(am.getSignature().equals(invExpr.getMethod().getSignature())){
							dataIdList.add(am.getCategory().toString());
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
						
			for(AndroidMethod am : sources)
				if(am.getSignature().equals(invExpr.getMethod().getSignature())){
						return am.getCategory().toString();
				}
		}
		else if(isSourceInfoParameter(sourceInfo)){
			return unknownCategory;
		}
		else
			throw new RuntimeException("Currently not supported");
		
		return null;
	}
	
	private boolean isMethodInterComponentSink(SootMethod sm){	
		for(AndroidMethod am : sinks){
			if(am.getCategory() == CATEGORY.INTER_APP_COMMUNICATION){
				if(am.getSubSignature().equals(sm.getSubSignature()))
					return true;
			}
		}
		
		return false;
	}
	
	private boolean isInterComponentSourceNoCallback(ResultSourceInfo si, BiDiInterproceduralCFG<Unit, SootMethod> cfg){
		if(!si.getSource().containsInvokeExpr())
			return false;
		
		InvokeExpr invExpr = si.getSource().getInvokeExpr();
		SootMethod sm = invExpr.getMethod();
				
		for(AndroidMethod am : sources){
			if(am.getCategory() == CATEGORY.INTER_APP_COMMUNICATION){
				if(am.getSubSignature().equals(sm.getSubSignature()))
					return true;
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
	
	private Unit createCorrectDummyAssignment(Local local){
		Unit dummyAssignemnt = null;
		if(local.getType() instanceof PrimType){
			if(local.getType() instanceof IntType){
				StaticInvokeExpr sie = Instrumentation.createJimpleStaticInvokeExpr(Settings.instance.SANITIZER, "dummyInteger");
				dummyAssignemnt = Jimple.v().newAssignStmt(local, sie);
			}
			else if(local.getType() instanceof BooleanType){
				StaticInvokeExpr sie = Instrumentation.createJimpleStaticInvokeExpr(Settings.instance.SANITIZER, "dummyBoolean");
				dummyAssignemnt = Jimple.v().newAssignStmt(local, sie);
			}
			else if(local.getType() instanceof ByteType){
				StaticInvokeExpr sie = Instrumentation.createJimpleStaticInvokeExpr(Settings.instance.SANITIZER, "dummyByte");
				dummyAssignemnt = Jimple.v().newAssignStmt(local, sie);
			}
			else if(local.getType() instanceof CharType){
				StaticInvokeExpr sie = Instrumentation.createJimpleStaticInvokeExpr(Settings.instance.SANITIZER, "dummyCharacter");
				dummyAssignemnt = Jimple.v().newAssignStmt(local, sie);
			}
			else if(local.getType() instanceof DoubleType){
				StaticInvokeExpr sie = Instrumentation.createJimpleStaticInvokeExpr(Settings.instance.SANITIZER, "dummyDouble");
				dummyAssignemnt = Jimple.v().newAssignStmt(local, sie);
			}
			else if(local.getType() instanceof FloatType){
				StaticInvokeExpr sie = Instrumentation.createJimpleStaticInvokeExpr(Settings.instance.SANITIZER, "dummyFloat");
				dummyAssignemnt = Jimple.v().newAssignStmt(local, sie);
			}
			else if(local.getType() instanceof LongType){
				StaticInvokeExpr sie = Instrumentation.createJimpleStaticInvokeExpr(Settings.instance.SANITIZER, "dummyLong");
				dummyAssignemnt = Jimple.v().newAssignStmt(local, sie);
			}
			else if(local.getType() instanceof ShortType){
				StaticInvokeExpr sie = Instrumentation.createJimpleStaticInvokeExpr(Settings.instance.SANITIZER, "dummyShort");
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
