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

public class RegisterActivity extends ActionBarActivity 
implements com.example.etch.MyServiceTask.ResultCallback {
	public static final int DISPLAY_NUMBER = 10;
    private Handler mUiHandler;
    public String with = "them";
    public String user;
    Button returnButton;
    Button registerButton;
    EditText usernameField;
    EditText passwordField;
    EditText confirmPasswordField;
    private String hashedPassword;
    public static Context context;
    
    
    private static final String LOG_TAG = "MainActivity";
    
    // Service connection variables.

    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_register);
        context = this;
        returnButton = (Button) findViewById(R.id.button_return);
        registerButton = (Button) findViewById(R.id.button_register_send);
        usernameField = (EditText) findViewById(R.id.editor_username);
        passwordField = (EditText) findViewById(R.id.editor_password);
        confirmPasswordField = (EditText) findViewById(R.id.editor_confirm);

        MyApplication.resetFriendsPath();
    	MyApplication.resetMessagesPath();
        
        registerButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                attemptRegister();
            }
        });
        returnButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	openLoginActivity();
            }
        });
        
        
    }
    
    
    
    

    
    private boolean attemptRegister(){
    	if(usernameField.getText().toString().length()<3){
    		usernameField.setText("");
    		passwordField.setText("");
    		confirmPasswordField.setText("");
    		usernameField.setHint("3 Char Min");
    		passwordField.setHint("Password");
    		confirmPasswordField.setHint("Confirm");
    		return false;
    	}
    	if(usernameField.getText().toString().length()>10){
    		usernameField.setText("");
    		passwordField.setText("");
    		confirmPasswordField.setText("");
    		usernameField.setHint("10 Char Max");
    		passwordField.setHint("Password");
    		confirmPasswordField.setHint("Confirm");
    		return false;
    	}
    	if(false){//check to see if there is internets
    		passwordField.setText("");
    		confirmPasswordField.setText("");
    		usernameField.setHint("Username");
    		passwordField.setHint("Password");
    		confirmPasswordField.setHint("Confirm");
    		noConnectionDialog();
    		return false;
    	}
    	if(false){//check to see if username already exists
    		usernameField.setText("");
    		passwordField.setText("");
    		confirmPasswordField.setText("");
    		usernameField.setHint("Username Exists");
    		passwordField.setHint("Password");
    		confirmPasswordField.setHint("Confirm");
    		return false;
    	}
    	if(!passwordField.getText().toString().equals(confirmPasswordField.getText().toString())){
    		passwordField.setText("");
    		confirmPasswordField.setText("");
    		usernameField.setHint("Username");
    		passwordField.setHint("Passwords Must");
    		confirmPasswordField.setHint("Match");
    		return false;
    	}
    	if(passwordField.getText().toString().length()<6){
    		passwordField.setText("");
    		confirmPasswordField.setText("");
    		usernameField.setHint("Username");
    		passwordField.setHint("6 Char Min");
    		confirmPasswordField.setHint("Confirm");
    		return false;
    	}
    	MyApplication.hashedPassword = passwordField.getText().toString();
    	MyApplication.userName= usernameField.getText().toString();
    	registerUser(MyApplication.userName, MyApplication.hashedPassword);
    	
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
    
    private void openLoginActivity(){
    	Intent intent = new Intent(this, LoginActivity.class);
    	startActivity(intent);
    	//finish(); this will call onDestroy
    	
    }
    
    private void registerUser(String username, String password){
    	List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
		nameValuePairs.add(new BasicNameValuePair("user", username));
		nameValuePairs.add(new BasicNameValuePair("pass", password));
		new RegisterTask().execute(nameValuePairs);
    }
    
    private class RegisterTask extends AsyncTask<List<NameValuePair>, Void, String>{
    	protected String doInBackground(List<NameValuePair>... pairsList){
        	Log.i("RegisterTask", "sending message");
			List<NameValuePair> pairs = pairsList[0];
			HttpClient httpclient = new DefaultHttpClient();
			HttpPost httppost = new HttpPost("https://etch-messaging.appspot.com/users/new");
			
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
			Log.i("RegisterTask", result);
			if(result.equals("user created successfully")){
				MyApplication.setFriendsPath(MyApplication.userName);
		    	MyApplication.setMessagesPath(MyApplication.userName);
		    	MyApplication.createPath();
		    	openListActivity();
			}
			else{
				MyApplication.resetFriendsPath();
		    	MyApplication.resetMessagesPath();
				Toast.makeText(context,"username taken", Toast.LENGTH_SHORT).show();
			}
    	}
    }

    
    
	@Override
    public void onStop() {
		MyApplication.setRunning(false);
            //Check device supported Accelerometer senssor or not
            super.onStop();
    }
	
	@Override
	public void onDestroy() {
		MyApplication.setRunning(false);
		//Check device supported Accelerometer senssor or not
		super.onDestroy();	
	}

	@Override
	public void onResultReady(ServiceResult result) {
		// TODO Auto-generated method stub
		
	}
}
