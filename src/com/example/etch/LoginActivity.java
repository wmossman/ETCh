package com.example.etch;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class LoginActivity extends ActionBarActivity 
implements com.example.etch.MyServiceTask.ResultCallback {
	public static final int DISPLAY_NUMBER = 10;
	// since we're not interoperating with other services, the following string is arbitrary
	public static final String PROTOCOL = "ETCh";
    private Handler mUiHandler;
    public String with = "them";
    public String user;
    private int ONGOING_NOTIFICATION_ID = 10;
    Button loginButton;
    Button registerButton;
    EditText usernameField;
    EditText passwordField;
    private String hashedPassword="this is the hashed password";
    public static Context context;
    
    
    private static final String LOG_TAG = "MainActivity";
    
    // Service connection variables.

    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(MyApplication.getLoggedIn()){
        	openListActivity();
        }
        else{
	        setContentView(R.layout.fragment_login);
	        context = this;
	        MyApplication.setLoggedIn(false);
	        MyApplication.createPath();
	        loginButton = (Button) findViewById(R.id.button_login);
	        registerButton = (Button) findViewById(R.id.button_register);
	        usernameField = (EditText) findViewById(R.id.editor_username);
	        passwordField = (EditText) findViewById(R.id.editor_password);
	        MyApplication.resetFriendsPath();
	    	MyApplication.resetMessagesPath();
	        setTitle("ETCh   Login");
	        loginButton.setOnClickListener(new View.OnClickListener() {
	            public void onClick(View v) {
	                attemptLogin();
	            }
	        });
	        registerButton.setOnClickListener(new View.OnClickListener() {
	            public void onClick(View v) {
	                openRegisterActivity();
	            }
	        });
	        killMyNotification();
        }
        
    }
    @Override
    public void onResume(){
    	super.onResume();
    	killMyNotification();
    	MyApplication.setLoggedIn(false);
    }
    
    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }
    
    
    
    private void killMyNotification(){
    	Log.i("killMyNotification", "inside method");
    	if(MyService.notificationManager!=null){
    		MyService.notificationManager.cancel(ONGOING_NOTIFICATION_ID);
    		Log.i("killMyNotification", "manager isnt null");
    	}
    	
    }

    
    private boolean attemptLogin(){
    	if(false){//check to see if there is internets
    		passwordField.setText("");
    		usernameField.setHint("Username");
    		passwordField.setHint("Password");
    		noConnectionDialog();
    		return false;
    	}
    	String hashedPassword = passwordField.getText().toString();
    	String userName= usernameField.getText().toString();
    	if(false){//check to see if the login exists
    		usernameField.setText("");
    		passwordField.setText("");
    		usernameField.setHint("No Such User");
    		passwordField.setHint("Password");
    		return false;
    	}
    	if(false){//hash the password and check with hashed password with the login
    		passwordField.setText("");
    		usernameField.setHint("Username");
    		passwordField.setHint("Incorrect Password");
    		return false;
    	}
    	//call getAllFriends function (either that or do it in MainActivity onCreate()
    	Log.i("LoginActivity", userName + hashedPassword);
    	MyApplication.userName = userName;
    	MyApplication.hashedPassword = hashedPassword;
    	loginUser(userName,hashedPassword);
    	return true;
    }
    
    
private void noConnectionDialog(){
    	AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("Error");
        builder.setMessage("No server connection, try again later.");

        builder.setPositiveButton("Okay", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {

                dialog.dismiss();
            }

        });

        AlertDialog alert = builder.create();
        alert.show();

    }
    
    
    private void openListActivity(){
    	Intent intent = new Intent(this, MainActivity.class);
    	/** set username and hashed password in MyApplication to be what was passed in here **/
    	startActivity(intent);
    	//finish(); this will call onDestroy
    	
    }
    
    
    
    private void openRegisterActivity(){
    	Intent intent = new Intent(this, RegisterActivity.class);
    	startActivity(intent);
    	//finish(); this will call onDestroy
    	
    }
    
    private void loginUser(String username, String password){
    	List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
		nameValuePairs.add(new BasicNameValuePair("user", username));
		nameValuePairs.add(new BasicNameValuePair("pass", password));
		new LoginTask().execute(nameValuePairs);
    }
    
    private class LoginTask extends AsyncTask<List<NameValuePair>, Void, String>{
    	protected String doInBackground(List<NameValuePair>... pairsList){
        	Log.i("LoginTask", "sending message");
			List<NameValuePair> pairs = pairsList[0];
			HttpClient httpclient = new DefaultHttpClient();
			HttpPost httppost = new HttpPost("https://etch-messaging.appspot.com/users/login");
			
			String responseString = "No response";
			if(/*netStatusIsGood()*/true){
	    		if(/*inetAddr()*/true){
	    			
	    			try {
						httppost.setEntity(new UrlEncodedFormEntity(pairs));
		    			HttpResponse response = httpclient.execute(httppost);
		    			responseString = new BasicResponseHandler().handleResponse(response);
					} catch (UnsupportedEncodingException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (HttpResponseException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
	    		} else Log.i("RegisterTask", "No internet connection");
			}else Log.i("RegisterTask", "No Network connection");
			Log.i("doInBackground", responseString); 
    		return responseString;
    	}
    
		protected void onPostExecute(String result){
			Log.i("LoginTask", result);
			if(result.equals("login success")){
				MyApplication.setFriendsPath(MyApplication.userName);
		    	MyApplication.setMessagesPath(MyApplication.userName);
		    	MyApplication.createPath();
		    	openListActivity();
			}
			else{
				MyApplication.resetFriendsPath();
		    	MyApplication.resetMessagesPath();
				Toast.makeText(context,result, Toast.LENGTH_SHORT).show();
			}
		}
	    
    }


	@Override
    public void onStop() {
            
		
            //Check device supported Accelerometer senssor or not
            
            super.onStop();
           
    }
	
	@Override
	public void onDestroy() {
		
		
		
		
		//Check device supported Accelerometer senssor or not
		
		super.onDestroy();
			
	}






	@Override
	public void onResultReady(ServiceResult result) {
		// TODO Auto-generated method stub
		
	}

   
    

}
