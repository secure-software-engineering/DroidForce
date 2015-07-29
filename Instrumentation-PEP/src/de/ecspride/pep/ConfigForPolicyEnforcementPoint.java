package de.ecspride.pep;

import java.io.File;
import java.util.Collections;

import soot.Scene;
import soot.jimple.infoflow.config.IInfoflowConfig;
import soot.options.Options;
import de.ecspride.Settings;

public class ConfigForPolicyEnforcementPoint implements IInfoflowConfig {
	
	@Override
	public void setSootOptions(Options options) {
		options.set_validate(true);
		
		Options.v().set_allow_phantom_refs(true);
		Options.v().set_prepend_classpath(false);
		Options.v().set_whole_program(true);
		
		
		//input format
		Options.v().set_src_prec(Options.src_prec_apk);
		//the output is an unsigned apk file
		Options.v().set_output_format(Options.output_format_dex);	
		Options.v().set_process_dir(Collections.singletonList(Settings.instance.apkFile));
		
		options.set_soot_classpath(Settings.instance.bin
				+ File.pathSeparator + Scene.v().defaultClassPath());
		
		Settings.instance.initialiseInstrumentationClasses();
	}

}
