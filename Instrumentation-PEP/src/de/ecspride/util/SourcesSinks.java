package de.ecspride.util;

import java.util.HashSet;
import java.util.Set;

import soot.jimple.infoflow.android.data.AndroidMethod;
import soot.jimple.infoflow.android.data.AndroidMethod.CATEGORY;
import soot.jimple.infoflow.android.data.parsers.CategorizedAndroidSourceSinkParser;
import de.ecspride.Settings;

public class SourcesSinks {
	public Set<AndroidMethod> getAndroidSourcesMethods(String sourceFile){	
		Set<AndroidMethod> sources = new HashSet<AndroidMethod>();
		Set<CATEGORY> categories = new HashSet<CATEGORY>();
		
		if(Settings.instance.sourceCategories.equals(CATEGORY.ALL))
			categories.add(CATEGORY.ALL);
		else{
			for(String category : Settings.instance.sourceCategories.split("\\|"))
				categories.add(CATEGORY.valueOf(category));
		}
		
		try{
			CategorizedAndroidSourceSinkParser parser = new CategorizedAndroidSourceSinkParser(categories, sourceFile, true, false);
			for (AndroidMethod am : parser.parse()){
				if (am.isSource())
					sources.add(am);
			}
			
		}catch(Exception ex){
			ex.printStackTrace();
			System.exit(1);
		}
		
		return sources;
	}
	
	public Set<AndroidMethod> getAndroidSinkMethods(String sinkFile){	
		Set<AndroidMethod> sinks = new HashSet<AndroidMethod>();
		Set<CATEGORY> categories = new HashSet<CATEGORY>();
		
		for(String category : Settings.instance.sinkCategories.split("\\|"))
			categories.add(CATEGORY.valueOf(category));
		
		try{
			CategorizedAndroidSourceSinkParser parser = new CategorizedAndroidSourceSinkParser(categories, sinkFile, false, true);

			for (AndroidMethod am : parser.parse()){
				if (am.isSink())
					sinks.add(am);
			}
			
		}catch(Exception ex){
			ex.printStackTrace();
			System.exit(1);
		}
		
		return sinks;
	}
}
