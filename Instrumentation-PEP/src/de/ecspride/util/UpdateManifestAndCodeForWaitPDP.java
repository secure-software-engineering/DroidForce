package de.ecspride.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;

import org.xmlpull.v1.XmlPullParserException;

import soot.Body;
import soot.IntType;
import soot.Local;
import soot.Modifier;
import soot.Scene;
import soot.SootClass;
import soot.SootField;
import soot.SootMethod;
import soot.Unit;
import soot.Value;
import soot.jimple.AssignStmt;
import soot.jimple.IntConstant;
import soot.jimple.InvokeExpr;
import soot.jimple.InvokeStmt;
import soot.jimple.Jimple;
import soot.jimple.SpecialInvokeExpr;
import soot.jimple.StringConstant;
import soot.jimple.infoflow.android.axml.AXmlAttribute;
import soot.jimple.infoflow.android.axml.AXmlHandler;
import soot.jimple.infoflow.android.axml.AXmlNode;
import soot.jimple.infoflow.android.axml.ApkHandler;
import soot.jimple.infoflow.android.manifest.ProcessManifest;
import soot.tagkit.ConstantValueTag;
import soot.tagkit.IntegerConstantValueTag;
import soot.tagkit.Tag;
import de.ecspride.Settings;

/**
 * This class contains methods to update the AndroidManifest.xml,
 * the code and resource files of the instrumented application to 
 * add an Activity that will be the first Activity instantiated when 
 * a user clicks on the icon to launch the application.
 * 
 * @author alex
 *
 */
public class UpdateManifestAndCodeForWaitPDP {
	
	
	/**
	 * 
	 * @param apkFileLocation
	 * @return
	 */
	public static String redirectMainActivity(String apkFileLocation){
		String mainActivityName = null;
		try {
			ProcessManifest pm = new ProcessManifest(apkFileLocation);
			AXmlHandler axmlh = pm.getAXml(); 
			
			// Find main activity and remove main intent-filter
			List<AXmlNode> anodes = axmlh.getNodesWithTag("activity");
			for (AXmlNode an: anodes) {
				boolean hasMain = false;
				boolean hasLauncher = false;
				AXmlNode filter = null;
				
				AXmlAttribute aname = an.getAttribute("name");
				String aval = (String)aname.getValue();
				System.out.println("activity: "+ aval);
				for (AXmlNode ch : an.getChildren()) {
					System.out.println("children: "+ ch);
				}
				List<AXmlNode> fnodes = an.getChildrenWithTag("intent-filter");
				for (AXmlNode fn: fnodes) {
					
					hasMain = false;
					hasLauncher = false;
					
					// check action
					List<AXmlNode> acnodes = fn.getChildrenWithTag("action");
					for (AXmlNode acn: acnodes) {
						AXmlAttribute acname = acn.getAttribute("name");
						String acval = (String)acname.getValue();
						System.out.println("action: "+ acval);
						if (acval.equals("android.intent.action.MAIN")) {
							hasMain = true;
						}
					}
					// check category
					List<AXmlNode> catnodes = fn.getChildrenWithTag("category");
					for (AXmlNode catn: catnodes) {
						AXmlAttribute catname = catn.getAttribute("name");
						String catval = (String)catname.getValue();
						System.out.println("category: "+ catval);
						if (catval.equals("android.intent.category.LAUNCHER")) {
							hasLauncher = true;
							filter = fn;
						}
					}
					if (hasLauncher && hasMain) {
						break;
					}
				}
				
				if (hasLauncher && hasMain) {
					// replace name with the activity waiting for the connection to the PDP
					System.out.println("main activity is: "+ aval);
					System.out.println("excluding filter: "+ filter);
					filter.exclude();
					mainActivityName = aval;
					break;
				}
				
			}
			
			// add new 'main' intent-filter for our activity that waits for a pdp connection
			axmlh.getDocument().getRootNode();
			List<AXmlNode> appnodes = axmlh.getNodesWithTag("application");
			if (appnodes.size() != 1) {
				throw new RuntimeException("error: number of application node != 1 (= "+ appnodes.size() +")");
			}
			AXmlNode appnode = appnodes.get(0);
			String attr_ns = "http://schemas.android.com/apk/res/android";
			String tag_ns = null;
			System.out.println("attr_ns: "+ attr_ns +" tag_ns: "+ tag_ns);
			
		    AXmlNode mainActivity = new AXmlNode("activity", tag_ns, null);
		    mainActivity.addAttribute(new AXmlAttribute<String>("name", "de.ecspride.javaclasses.WaitPDPActivity",  attr_ns));
		    appnode.addChild(mainActivity);
		    
		    AXmlNode filter = new AXmlNode("intent-filter", tag_ns, null);
		    mainActivity.addChild(filter);
		    
		    AXmlNode action = new AXmlNode("action", tag_ns, null);
		    action.addAttribute(new AXmlAttribute<String>("name", "android.intent.action.MAIN",  attr_ns));
		    filter.addChild(action);

		    AXmlNode category = new AXmlNode("category", tag_ns, null);
		    category.addAttribute(new AXmlAttribute<String>("name", "android.intent.category.LAUNCHER",  attr_ns));
		    filter.addChild(category);

		
			byte[] newManifestBytes = axmlh.toByteArray();
			FileOutputStream fileOuputStream = new FileOutputStream(Settings.sootOutput + File.separatorChar + "AndroidManifest.xml"); 
			fileOuputStream.write(newManifestBytes);
			fileOuputStream.close(); 
			
		}
		catch (IOException | XmlPullParserException ex) {
			System.err.println("Could not read Android manifest file: " + ex.getMessage());
			throw new RuntimeException(ex);
		}
		
		return mainActivityName;
	
	}
	
	/**
	 * 
	 * @param originalApk
	 */
	public static void replaceManifest(String originalApk) {
		File originalApkFile = new File(originalApk);
		String newManifest = Settings.sootOutput + File.separatorChar + "AndroidManifest.xml";
		String targetApk = Settings.sootOutput + File.separatorChar + originalApkFile.getName();
		File newMFile = new File(newManifest);
		try {
			ApkHandler apkH = new ApkHandler(targetApk);
			apkH.addFilesToApk(Collections.singletonList(newMFile));
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException("error when writing new manifest: "+ e);
		}
		newMFile.delete();
	}
	
	/**
	 * 
	 * @param mainActivityClass
	 */
	public static void updateWaitPDPActivity(String mainActivityClass) {
		SootClass sc = Scene.v().getSootClass("de.ecspride.javaclasses.WaitPDPActivity");
		SootMethod sm = sc.getMethodByName("<init>");
		Body b = sm.retrieveActiveBody();
		for (Unit u: b.getUnits()) {
			if (u instanceof AssignStmt) {
				AssignStmt asg = (AssignStmt)u;
				if (asg.getRightOp() instanceof StringConstant) {
					StringConstant cst = (StringConstant)asg.getRightOp();
					if (cst.value.equals("")) {
						asg.setRightOp(StringConstant.v(mainActivityClass));
					}
				}
			}
		}
	}

	/**
	 * 
	 * @param originalApk
	 */
	public static void addBackgroundFile(String originalApk) {
		ClassLoader classLoader = UpdateManifestAndCodeForWaitPDP.class.getClassLoader();
		URL fileURL = classLoader.getResource("resources" + File.separatorChar + "protect.png");
		File background_picture = new File(fileURL.getPath());
		File originalApkFile = new File(originalApk);
		String targetApk = Settings.sootOutput + File.separatorChar + originalApkFile.getName();
		try {
			ApkHandler apkH = new ApkHandler(targetApk);
			apkH.addFilesToApk(Collections.singletonList(background_picture), Collections.singletonMap(background_picture.getPath(), "assets/protect.png"));
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException("error when adding background image: "+ e);
		}
		
	}


}
