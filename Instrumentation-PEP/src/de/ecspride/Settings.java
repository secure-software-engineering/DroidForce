package de.ecspride;

import java.io.File;
import java.util.Collections;

import soot.Scene;
import soot.SootClass;
import soot.options.Options;

/**
 * @author Siegfried Rasthofer
 */
public class Settings {
	
	//singleton
	public static Settings instance = new Settings();
	
	//bin folder for the java classes that will be added to the apk
	public final String bin = "./bin/";
	
	//information about all events (method signatrues) we do care
	public final static String eventInformationFile = "./files/eventInformation.xml";	
	
	//java classes that will be added to the apk
	public final String REMOTE_SERVICE_CONNECTION_JAVA = "de.ecspride.javaclasses.RemoteServiceConnection";
	public final String INSTRUMENTATION_HELPER_JAVA = "de.ecspride.javaclasses.InstrumentationHelper";
	public final String EVENT_PEP_JAVA = "de.ecspride.javaclasses.EventPEP";
	public final String INCOMING_HANDLER_JAVA = "de.ecspride.javaclasses.IncomingHandler";
	public final String SANITIZER = "de.ecspride.javaclasses.Sanitizer";
	
	public final String INSTRUMENTATION_HELPER_INITIALIZE_METHOD = "initializeEventPEP";
	
	//source categories that should be considered by the analysis (all the corresponding methods in the InstrumentationOptions.sourceFile are considered)
	public final String sourceCategories = "HARDWARE_INFO|UNIQUE_IDENTIFIER|LOCATION_INFORMATION|NETWORK_INFORMATION|ACCOUNT_INFORMATION|" 
											+ "EMAIL_INFORMATION|FILE_INFORMATION|BLUETOOTH_INFORMATION|VOIP_INFORMATION|DATABASE_INFORMATION|PHONE_INFORMATION|INTER_APP_COMMUNICATION";
	
	//sink categories that should be considered by the analysis (all the corresponding methods in the InstrumentationOptions.sinkFile are considered)
	public final String sinkCategories = "LOG|NETWORK|SMS_MMS|VIDEO|INTER_APP_COMMUNICATION";
	
	//absolute path to the file that contains source information
	public String sourceFile;
		
	//absolute path to the file that contains sink information
	public String sinkFile;
	
	//root-folder for different Android sdks (e.g., /AndroidSDK for /AndroidSDK/android-3/android.jar, /AndroidSDK/android-4/android.jar, ...) 
	public String androidPlatforms;
		
	//the concrete Android jar file (e.g., /AndroidSDK/android-18/android.jar)
	public String androidJar;
		
	//the input apkFile
	public String apkFile = null;
	
	//this file contains information about the taint-wrapper
	public String taintWrapperFile;

	//initialize soot
	public void initialiseSoot(){
		if(apkFile == null)
			throw new RuntimeException("There has to be an apk for the analysis");
		
		Options.v().set_allow_phantom_refs(true);
		Options.v().set_validate(true);
		Options.v().set_prepend_classpath(true);
		//no cfg needed
		Options.v().set_whole_program(false);
		
		
		//input format
		Options.v().set_src_prec(Options.src_prec_apk);
		//the output is an unsigned apk file
		Options.v().set_output_format(Options.output_format_dex);
			
		Options.v().set_process_dir(Collections.singletonList(apkFile));
		if (instance.androidJar != null)
			Options.v().set_force_android_jar(instance.androidJar);
		else if (instance.androidPlatforms != null)
			Options.v().set_android_jars(instance.androidPlatforms);
		else
			throw new RuntimeException("Neither single Android JAR not platform "
					+ "directory specified");
		
		//the bin folder has to be added to the classpath in order to
		//use the Java part for the instrumentation (JavaClassForInstrumentation)
		Options.v().set_soot_classpath(instance.bin + File.pathSeparator + instance.androidJar);
		initialiseInstrumentationClasses();
		Scene.v().loadNecessaryClasses();
		addInstrumentedClassToApplicationClass();
	}
	
	/**
	 * Add all java-classes to the basic classes with the BODIES settings, since they will be
	 * set as application class in addInstrumentedClassToApplicationClass()
	 */
	public void initialiseInstrumentationClasses(){
		Scene.v().addBasicClass(instance.REMOTE_SERVICE_CONNECTION_JAVA, SootClass.BODIES);
		Scene.v().addBasicClass(instance.INSTRUMENTATION_HELPER_JAVA, SootClass.BODIES);
		Scene.v().addBasicClass(instance.INCOMING_HANDLER_JAVA, SootClass.BODIES);
		Scene.v().addBasicClass(instance.EVENT_PEP_JAVA, SootClass.BODIES);
		Scene.v().addBasicClass(instance.SANITIZER, SootClass.BODIES);

	}
	
	/**
	 * Set java-classes to application class in order to be added to the apk.
	 */
	public void addInstrumentedClassToApplicationClass(){
		Scene.v().getSootClass(instance.REMOTE_SERVICE_CONNECTION_JAVA).setApplicationClass();
		Scene.v().getSootClass(instance.INSTRUMENTATION_HELPER_JAVA).setApplicationClass();
		Scene.v().getSootClass(instance.INCOMING_HANDLER_JAVA).setApplicationClass();
		Scene.v().getSootClass(instance.EVENT_PEP_JAVA).setApplicationClass();
		Scene.v().getSootClass(instance.SANITIZER).setApplicationClass();

	}
	
	/**
	 * Get the path of the apk to analyze. 
	 * @return: apk path
	 */
	public String getApkPath(){
		return apkFile;
	}
	
	
	//prints out the different options for the instrumentation
		public void printHelp(){
			StringBuilder output = new StringBuilder();
			output.append("\nUsage:  de.ecspride.runsecure.Main\n"
					+ "-sourceFile </path/to/CategorizesSouceList> \n"
					+ "-sinkFile </path/to/CategorizedSinkList> \n"
					+ "-apkFile </path/to/file.apk> \n"
					+ "-androidPlatforms </path/to/root/folder/of/jars> (e.g., AndroidSDK for /AndroidSDK/android-3/android.jar, /AndroidSDK/android-4/android.jar, ...)\n"
					+ "-androidJar </path/to/android.jar>\n"
					+ "-instrumentationType [hybrid|complete]\n"
					+ "-taintWrapper </path/to/taintWrapper.txt>\n"
					+ "-jimpleOutput [true|false]");
			System.out.println(output.toString());
		}
		
		//parses the user's command line input and saves it into the corresponding data structure
		public void parseCommandLineArgs(String[] args){		
			for(int i = 0; i < args.length; i++){
				if(args[i].equals("-help")){
					printHelp();
					System.exit(0);
				}
				else if(args[i].equals("-androidPlatforms")){
					++i;
					correctArgumentCheck(args[i]);
					androidPlatforms = args[i];
				}
				else if(args[i].equals("-androidJar")){
					++i;
					correctArgumentCheck(args[i]);
					androidJar = args[i];
				}
				else if(args[i].equals("-sourceFile")){
					++i;
					correctArgumentCheck(args[i]);
					sourceFile = args[i];
				}
				else if(args[i].equals("-sinkFile")){
					++i;
					correctArgumentCheck(args[i]);
					sinkFile = args[i];
				}
				else if(args[i].equals("-taintWrapper")){
					++i;
					correctArgumentCheck(args[i]);
					taintWrapperFile = args[i];
				}
				else if(args[i].equals("-apkFile")){
					++i;
					correctArgumentCheck(args[i]);
					apkFile = args[i];	
				}
				else{
					printHelp();
					System.exit(0);
				}
					
			}
			
			//check for correct settings
			if(sourceFile == null || sinkFile == null || apkFile == null){
				System.err.println("Ooops, the command line arguments are not correct!"
						+ "\nInstrumentation stopped...");
				printHelp();
				System.exit(0);
			}
			
		}
		
		//simple checks whether the option starts with a "-" or not
		private void correctArgumentCheck(String argument){
			if(argument.startsWith("-"))
				throw new RuntimeException("Ooops, wrong command line argument: " + argument);
		}
		
		//reset all Settings
		public void reset(){
			instance = new Settings();
		}
		
		/**
		 * The dummy main class is from the FlowDroid project which basically emulates the Android operating system with its 
		 * lifecycle. This is an external class which is used during the analysis, but is not needed to be included in the
		 * output file. Therefore, it is set to "Library Class".
		 */
		public void setDummyMainToLibraryClass(){
			Scene.v().getSootClass("dummyMainClass").setLibraryClass();
		}
}
