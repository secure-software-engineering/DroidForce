package de.ecspride.pep;

import java.io.File;
import java.util.Collections;

import soot.Scene;
import soot.SootClass;
import soot.jimple.infoflow.config.IInfoflowConfig;
import soot.options.Options;
import de.ecspride.Settings;

public class ConfigForPolicyEnforcementPoint implements IInfoflowConfig {
	
	@Override
	public void setSootOptions(Options options) {
		options.set_validate(true);
		
		Options.v().set_allow_phantom_refs(true);
		Options.v().set_prepend_classpath(true);
		//no cfg needed
		Options.v().set_whole_program(true);
		
		
		//input format
		Options.v().set_src_prec(Options.src_prec_apk);
		//the output is an unsigned apk file
		Options.v().set_output_format(Options.output_format_dex);
			
		Options.v().set_process_dir(Collections.singletonList(Settings.instance.apkFile));
		Options.v().set_force_android_jar(Settings.instance.androidJar);
		
		options.set_soot_classpath(Settings.instance.apkFile + File.pathSeparator + Settings.instance.bin + File.pathSeparator + Settings.instance.androidJar);
		
		Settings.instance.initialiseInstrumentationClasses();
		Scene.v().loadNecessaryClasses();	
		Settings.instance.addInstrumentedClassToApplicationClass();
		Settings.instance.setDummyMainToLibraryClass();
	}

}
