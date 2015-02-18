package de.tum.in.i22.uc.pdp.android;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.FileObserver;
import android.os.Message;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import de.ecspride.Settings;
import de.util.FileUtil;

public class appPDP extends Activity
{
  private static final String TAG = "appPDP";
  private static FileObserver observer;

  private File currentPolicyFile = null;
  
  private String[] mFileList; // stores policies located in the internal storage of the app
  private static final String POLICY_FILE_TYPE = ".xml";
  private static final int DIALOG_LOAD_POLICY = 0xBEEF;

  private static RemoteServiceConnection deployPolicyConnection =new RemoteServiceConnection();

  @Override
  public void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.apppdpmain);
    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

    loadPoliciesFromAssets(); // copy policies in assets to the internal storage of the app
    loadFileList(); // list all available policies, so the user can choose a policy from a dialog

    Log.d(TAG, "Preparing pdpService");
    Intent start=new Intent("de.tum.in.i22.uc.pdp.android.pdpService");
    startService(start);
    Log.d(TAG, "pdpService instantiated");

    // Create the service connection if it's not already there
    if(!deployPolicyConnection.isBound())
    {
      Log.i(TAG, "pdpService not yet bound; binding...");
      Intent intent=new Intent();
      intent.setClassName("de.tum.in.i22.uc.pdp.android", "de.tum.in.i22.uc.pdp.android.pdpService");
      intent.setAction(pdpService.ACTION_PDP_SETPOLICY);
      getApplicationContext().bindService(intent, deployPolicyConnection, Context.BIND_AUTO_CREATE);

      deployPolicyConnection.addOnServiceBoundListener(new ServiceBoundListener()
      {

        @Override
        public void serviceBound(RemoteServiceConnection connection)
        {
          Log.i(TAG, "serviceBound");
          deployPolicy(currentPolicyFile);

          // register observer for policy.xml file and react on modifcations
          observer=new FileObserver(currentPolicyFile.getAbsolutePath())
          {
            @Override
            public void onEvent(int event, String file)
            {
              if(event == FileObserver.MODIFY)
              {
                try
                {
                  deployPolicy(currentPolicyFile);
                }
                catch(Exception ex)
                {
                  Toast.makeText(getApplicationContext(), "Could not load policy file", Toast.LENGTH_LONG).show();
                  ;
                }
              }
            }
          };
          observer.startWatching();
        }

      });
    }
    else
      Log.i(TAG, "pdpService already bound?!");
  }
  
  /**
   * Extract the policies from the application's assets directory to the internal storage 
   * of the application.
   */
  private void loadPoliciesFromAssets() {
	  Log.d(TAG, "loading policies from assets");
	  Resources r = getResources();
	  String[] files = null;
	  try {
		  files = r.getAssets().list("");
	  } catch (IOException e) {
		  Log.e(TAG, "exception "+ e);
		  e.printStackTrace();
	  }
	  Log.d(TAG, "number of files in assets: "+ files.length);
	  for (String s: files) {
		  if (!(s.startsWith("p") && s.endsWith(".xml"))) {
			  Log.d(TAG, "skipping '"+ s +"'");
			  continue;
		  }
		  Log.d(TAG, "loading '"+ s +"'");
		  File file = copyFileFromAssetsToInternalStorage(s);
		  if (s.equals("policy.xml")) {
			  setCurrentPolicy(s);
		  }
	  }
  }
  
  /**
   * Set current policy 'File' currentPolicyFile to
   * point to file 'policyName' located in the internal storage
   * of the application.
   * @param policyName
   */
  private void setCurrentPolicy(String policyName) {
	  String ppath = this.getFilesDir() + File.separator + policyName;
	  currentPolicyFile = new File(ppath);
	  Log.i(TAG, "current policy: "+ ppath);
  }
  
  /**
   * 
   */
  private void deployCurrentPolicy() {
	  deployPolicy(currentPolicyFile);
  }
  
  /**
   * Display the current policy in the activity (show policy content
   * and policy name).
   */
  private void displayCurrentPolicy() {
	  TextView textView = (TextView)findViewById(R.id.textView1);
	  textView.setText(currentPolicyFile.getName());

	  TextView textView2 = (TextView)findViewById(R.id.textView2);
	  FileReader fr = null;
	  String po = "";
	  try {
		  fr = new FileReader(currentPolicyFile);

		  BufferedReader br = new BufferedReader(fr);

		  String l = null;
		  int i = 1;
		  while ((l = br.readLine()) != null) {
			  po += String.format("%03d", i++) +": "+ l +"\n";
		  }
	  } catch (Exception e) {
		  Log.e(TAG, "error when reading policy file '"+ currentPolicyFile.getName() +"' "+ e);
		  e.printStackTrace();
	  } 
	  textView2.setText(po);
	  textView2.setMovementMethod(new ScrollingMovementMethod());
  }

  private File copyFileFromAssetsToInternalStorage(String fileName)
  {
    String fileInternally=this.getFilesDir().toString() + File.separator + fileName;
    File file=new File(fileInternally);
    try
    {
      if(!file.exists()) FileUtil.copyPolicyFileFromAssetsToInternalStorage(getApplicationContext(), fileName, fileInternally);
    }
    catch(Exception ex)
    {
      Log.e("ERROR", ex.getMessage());
    }

    try
    {
      Runtime.getRuntime().exec("chmod 777 " + fileInternally);
    }
    catch(Exception ex)
    {
      ex.printStackTrace();
    }

    return file;
  }
  
  private void deployPolicy(File pathToPolicy)
  {
    Log.d(TAG, "deployPolicy method");
    try
    {
      // Read in the policy file
      BufferedReader rdr=new BufferedReader(new FileReader(pathToPolicy));
      String line="";
      String data="";
      while((line=rdr.readLine()) != null)
        data+=line + "\n";
      rdr.close();
      Log.d(TAG, "Policy file read.");

      // Deploy the policy
      Bundle event=new Bundle();
      event.putString("policy", data);

      Message m=Message.obtain();
      m.setData(event);
      Log.d(TAG, "sending deployment message");
      deployPolicyConnection.getMessenger().send(m);
      Log.d(TAG, "deployment message sent");
      
      Toast.makeText(getApplicationContext(), "Policy deployed", Toast.LENGTH_LONG).show();
    }
    catch(Exception e)
    {
      Log.e(TAG, "Exception during deployment" + e);
      Log.e(TAG, e.getMessage());
      e.printStackTrace();
    }
  }



  public void deployPolicyWithButton(View v) {
	  showDialog(DIALOG_LOAD_POLICY);
  }
  
  

	private void loadFileList() {
	
		File mPath =  this.getFilesDir();
		Log.d(TAG, "loading files from internal directory '"+ mPath +"'");
	
		if(!mPath.exists()) {
			Log.e(TAG, "directory does not exist! "+ mPath);
			return;
		}
		FilenameFilter filter = new FilenameFilter() {
	
			@Override
			public boolean accept(File dir, String filename) {
				File sel = new File(dir, filename);
				return filename.contains(POLICY_FILE_TYPE) || sel.isDirectory();
			}
	
		};
		mFileList = mPath.list(filter);
	
		Log.d(TAG, "policy files loaded from internal directory: "+ mFileList.length);
		for (String s: mFileList) {
			Log.d(TAG, " -> '"+ s +"'");
		}
	
	}
	
	protected Dialog onCreateDialog(int id) {
		Dialog dialog = null;
		AlertDialog.Builder builder = new Builder(this);

	
		switch(id) {
		case DIALOG_LOAD_POLICY:
			builder.setTitle("Choose the policy");
			if(mFileList == null) {
				Log.e(TAG, "Showing file picker before loading the file list");
				dialog = builder.create();
				return dialog;
			}
			builder.setItems(mFileList, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					String chosenPolicy = mFileList[which];
					setCurrentPolicy(chosenPolicy);
					displayCurrentPolicy();
					deployCurrentPolicy();
				}
			});
			break;
		}
		dialog = builder.show();
		return dialog;
	}

}
