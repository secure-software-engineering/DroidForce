package de.ecspride.javaclasses;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

/**
 * Activity used to wait for the application to be connected to the PDP.
 * 
 * @author alex
 *
 */
public class WaitPDPActivity extends Activity {

	// the default value of this field 
	// will be setup automatically during 
	// the instrumenting of an app.
	String mainActivityClassname = "";
	String applicationPackageName = "";
	
	Handler mHandler = new Handler();

	@Override
	public void onCreate(Bundle savedInstance) {
		super.onCreate(savedInstance);

		Log.i("DroidForce", "Starting activity to synchronize with PDP...");
		Log.i("DroidForce", "When PDP is initialized, activity '"+ mainActivityClassname +"' will be started.");

		// initiate connection to PDP
		InstrumentationHelper.initializeEventPEP(this);

		// Context.getResources() cannot be called in onCreate().
		// Since, the background image is loaded from the assets directory
		// located in the apk, we setup the layout in a new thread which
		// is executed after onCreate() has returned.
		startThreadToPutLayout(); 

		// Start a new thread which waits for the application to be 
		// connected to the PDP.
		// Once the application is connected, it starts the "original"
		// activity.
		startThreadToCheckPDPConnection();

	}

	/**
	 * Setup the layout for the activity
	 */
	private void setLayout() {		
		Log.i("DroidForce", "Setting up layout...");
		LinearLayout llayout = new LinearLayout(this);

		LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams( 
				LinearLayout.LayoutParams.FILL_PARENT,
				LinearLayout.LayoutParams.FILL_PARENT);

		llayout.setBackgroundDrawable(loadBackground());
		llayout.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL);
		
        TextView tv = new TextView(this);
        LayoutParams lpView = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        tv.setText("Powered by DroidForce.");
        tv.setLayoutParams(lpView);  
        
        llayout.addView(tv);
		
		setContentView(llayout);
		

	}

	/**
	 * 
	 */
	public void startThreadToPutLayout() {

		Log.i("DroidForce", "Starting new thread to set layout.");

		new Thread(new Runnable() {
			@Override
			public void run() {
				mHandler.post(new Runnable() {
					@Override
					public void run() {
						setLayout();
					}
				});

			}
		}).start();
	}

	/**
	 * 
	 */
	public void startThreadToCheckPDPConnection() {

		Log.i("DroidForce", "Starting new thread to check status of PDP connection.");
		// only start the "original activity" after 4 seconds
		// so that the user has time to see the background image
		new Thread(new Runnable() {
			@Override
			public void run() {
				while (true) {
					try {
						Log.i("DroidForce", "sleeping 2 seconds...");
						Thread.sleep(4000);
						mHandler.post(new Runnable() {

							@Override
							public void run() {
								if (EventPEP.isBound()) {
									Log.i("DroidForce", "connected to PDP.");
									startOriginalActivity();
								} else {
									Log.i("DroidForce", "no connection to PDP yet.");
								}
							}
						});
						if (EventPEP.isBound) {
							break;
						}
					} catch (Exception e) {
						throw new RuntimeException("error: "+ e);
					}
				}
			}
		}).start();

		Log.i("DroidForce", "End of onCreate()");

	}

	/**
	 * This method starts the 'original' activity.
	 * 
	 * Remember that this code is added to the instrumented application.
	 * After the instrumentation, 'mainActivityClassname' contains
	 * the class name of the 'original' activity.
	 */
	private void startOriginalActivity() {
		Intent i = new Intent();
		String targetClass = mainActivityClassname.replaceAll("\\.[^\\.]*$", "");
		i.setClassName(targetClass, this.mainActivityClassname);
		Log.i("DroidForce", "start main activity with packageName = '" + targetClass + "' and className = '" + this.mainActivityClassname + "'...");
		this.startActivity(i);
	}


	/**
	 * Load background image from the assets directory.
	 * @return Drawable object representing the background image.
	 */
	private Drawable loadBackground() {

		Resources r = getResources();
		Log.d("DroidForce", "resources: "+ r);
		String[] files = null;
		try {
			files = r.getAssets().list("");
		} catch (IOException e) {
			Log.e("DroidForce", "exception "+ e);
			e.printStackTrace();
		}
		Log.d("DroidForce", "number of files in assets: "+ files.length);
		for (String s: files) {
			Log.d("DroidForce", "file in asset: "+ s);
			if (s.endsWith("protect.png")) {
				File file = copyFileFromAssetsToInternalStorage(s, true);
				String path = file.getAbsolutePath();
				Log.d("DroidForce", "Path to file is '"+ path +"'");
				return Drawable.createFromPath(path);
			}
		}
		throw new RuntimeException("error: drawable not found.");
	}

	private File copyFileFromAssetsToInternalStorage(String fileName, boolean forceRewrite) {
		String fileInternally = this.getFilesDir().toString() + File.separator + fileName;
		File file = new File(fileInternally);
		try {
			if (!file.exists() || forceRewrite) {
				copyFileFromAssetsToInternalStorage(getApplicationContext(), fileName, fileInternally);
			}
		} catch(Exception ex) {
			Log.e("ERROR", ex.getMessage());
		}

		return file;
	}

	public static void copyFileFromAssetsToInternalStorage(Context context, String srcFile, String targetFolder) throws IOException{		
		copyAsset(context.getAssets(), srcFile, targetFolder);
	}

	public static boolean copyAsset(AssetManager assetManager,
			String fromAssetPath, String toPath) {
		InputStream in = null;
		OutputStream out = null;
		try {
			in = assetManager.open(fromAssetPath);
			new File(toPath).createNewFile();
			out = new FileOutputStream(toPath);
			copyFile(in, out);
			in.close();
			in = null;
			out.flush();
			out.close();
			out = null;
			return true;
		} catch(Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	private static void copyFile(InputStream in, OutputStream out) throws IOException {
		byte[] buffer = new byte[1024];
		int read;
		while((read = in.read(buffer)) != -1){
			out.write(buffer, 0, read);
		}
	}

}
