package com.example.etch;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

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
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;

import com.example.etch.MyService.MyBinder;

public class MessageActivity extends ActionBarActivity 
implements com.example.etch.MyServiceTask.ResultCallback {
	public static final int DISPLAY_NUMBER = 10;
    private Handler mUiHandler;
    public String with = "uninitialized";
    public String user;
    private static final int DIALOG_DELETE = 10;
    Button sendButton;
    EditText textEditor;
    TextView textView;
    ScrollView scrollView;
    private String hashedPassword;
    public static Context context;
    private int ONGOING_NOTIFICATION_ID = 10;
    public boolean isRunning;
    Handler mHandler = new Handler();    
    
    private static final String LOG_TAG = "MainActivity";
    
    // Service connection variables.
    private boolean serviceBound;
    private MyService myService;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_main);
        mUiHandler = new Handler(getMainLooper(), new UiCallback());    
        serviceBound = false;
        context = this;
        user=MyApplication.userName;
        hashedPassword = MyApplication.hashedPassword;
        // creates the state for OTR (i.e. encryption)

        isRunning = true;
        MyApplication.setRunning(true);
        Bundle b = getIntent().getExtras();
        with = b.getString("with");
        

        //sets global running to true
        //MyApplication.setRunning(true);
        sendButton = (Button) findViewById(R.id.button_send);
        textEditor = (EditText)findViewById(R.id.text_editor);
        textView = (TextView)findViewById(R.id.text_view);
        scrollView = (ScrollView)findViewById(R.id.scroll_view);
        
        sendButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                sendMessage();
            }
        });

        setTitle("ETCh   " + user + " - " + with);
       
       
       runParse();
       
       
       parseMessages();
       
       
       
    }
    
    
    public void runParse()
    {
        new Thread(new Runnable() {
            @Override
            public void run() {
            	Log.i("runParse",Boolean.toString(isRunning));
                // TODO Auto-generated method stub
                while (isRunning) {
                    try {
                        Thread.sleep(1000);
                        mHandler.post(new Runnable() {

                            @Override
                            public void run() {
                            	//MyApplication.setRunning(true);
                            	if(MyApplication.newMessages) parseMessages();                               
                            }
                        });
                    } catch (Exception e) {
                        
                    }
                }
            }
        }).start();
    }
    
    
    
    @Override
    public void onBackPressed() {
    	Intent intent = new Intent(this, MainActivity.class);
    	startActivity(intent);
    }
    
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.messaging, menu);
        return super.onCreateOptionsMenu(menu);
    }
    
    @SuppressWarnings("deprecation")
	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case R.id.action_discard:
            	deleteDialog();
                return true;
            case R.id.action_logout:
            	logOutDialog();
                return true;
            
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    
    
private void logOutDialog(){
    	
    	AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("Confirm");
        builder.setMessage("Logout?");

        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {
            	
            	logOut();
                dialog.dismiss();
            }

        });

        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Do nothing
                dialog.dismiss();
            }
        });

        AlertDialog alert = builder.create();
        alert.show();
    	
    	
    }
    
    private void logOut(){
    	MyApplication.userName="";
    	MyApplication.hashedPassword="";
    	MyApplication.setLoggedIn(false);
    	MyApplication.setRunning(false);
    	isRunning=false;
    	killMyNotification();
    	MyApplication.resetFriendsPath();
    	MyApplication.resetMessagesPath();
    	Intent intent = new Intent(this, LoginActivity.class);
    	startActivity(intent);
    }
    
    private void killMyNotification(){
    	Log.i("killMyNotification", "inside method");
    	if(MyService.notificationManager!=null){
    		MyService.notificationManager.cancel(ONGOING_NOTIFICATION_ID);
    		Log.i("killMyNotification", "manager isnt null");
    	}
    	
    }
    
    
    public void deleteDialog(){
    	AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("Confirm");
        builder.setMessage("Delete all messages?");

        builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {
                deleteMessages();

                dialog.dismiss();
            }

        });

        builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Do nothing
                dialog.dismiss();
            }
        });

        AlertDialog alert = builder.create();
        alert.show();
    }
    
    
    
    @Override
    protected void onResume() {
        super.onResume();
        // Starts the service, so that the service will only stop when explicitly stopped.
        Intent intent = new Intent(this, MyService.class);
        startService(intent);        
    	bindMyService();
    	MyApplication.setRunning(true);
    	//start the parse runnable
    	isRunning = true;
    	runParse();
    	parseMessages();
    }
    
    
    
    private void bindMyService() {
    	// We are ready to show images, and we should start getting the bitmaps
    	// from the motion detection service.
    	// Binds to the service.
    	Log.i(LOG_TAG, "Starting the service");
    	Intent intent = new Intent(this, MyService.class);
    	Log.i("LOG_TAG", "Trying to bind");
    	bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
    	//Toast.makeText(getBaseContext(), "Service Bound", Toast.LENGTH_SHORT).show();
    }
    

    // Service connection code.
    private ServiceConnection serviceConnection = new ServiceConnection() {
    	@Override
    	public void onServiceConnected(ComponentName className, IBinder serviceBinder) {
    		// We have bound to the camera service.
    		MyBinder binder = (MyBinder) serviceBinder;
    		myService = binder.getService();
    		serviceBound = true;
    		// Let's connect the callbacks.
    		Log.i("MyService", "Bound succeeded");
    	}
    	
    	@Override
    	public void onServiceDisconnected(ComponentName arg0) {
    		serviceBound = false;
    	}
    };
    
    
    public void deleteMessages(){
	    try{	
    		final File dir = new File(MyApplication.messagesPath );
	
	        if (!dir.exists())
	        {
	            dir.mkdirs(); 
	        }
	        
	        final File myFile = new File(dir, with + ".txt");
	        PrintWriter writer = new PrintWriter(myFile);
	        writer.print("");
	        writer.close();
	        //myFile.createNewFile();
	        Log.i("deleteMessages","end of try");
	        
	        //Tell other user to delete there messages as well
	    	String sentTime = MyServiceTask.getCurrentTimeStamp();
	    	String body = "--USER ["+ user + "] HAS JUST DELETED ALL MESSAGES. CONSIDER DELETING YOURS AS WELL.--";
	    	String encryptedBody = encryptText(body);
	    	sendToServer(user, with, encryptedBody, sentTime, MyApplication.hashedPassword);
	        
	        parseMessages();
	        
    	}
        catch (IOException e) {
            // TODO Auto-generated catch block
        	Log.i("deleteMessages","try failed");
            e.printStackTrace();
        }
    }
    
    

    private void sendMessage(){
    	String sentTime = MyServiceTask.getCurrentTimeStamp();
    	String body = textEditor.getText().toString();
    	String encryptedBody = encryptText(body);
    	Log.i("stuff", user);
    	Log.i("stuff", hashedPassword);
    	MyServiceTask.writeToFile(user, with, encryptedBody, sentTime);
    	sendToServer(user, with, encryptedBody, sentTime, hashedPassword);
    	textEditor.setText(""); //empties textEditor
    	parseMessages();
    }
    
    private void parseMessages(){
    	//parse entire text file and print it to the textview
    	
    	
    	
    	scrollView.post(new Runnable() {
    	    @Override
    	    public void run() {
    	        scrollView.fullScroll(ScrollView.FOCUS_DOWN);
    	    }
    	});
    	
    	
    	FileInputStream in = null;

        try {
            final File dir = new File(MyApplication.messagesPath );

            if (!dir.exists())
            {
                dir.mkdirs(); 
            }
            
            final File myFile = new File(dir, with + ".txt");

            if (!myFile.exists()) 
            {    
                myFile.createNewFile();
            } 
            in = new FileInputStream(MyApplication.messagesPath + with + ".txt");
            //in=openFileInput(MyApplication.messagesPath + with + ".txt");
            InputStreamReader inputStreamReader = new InputStreamReader(in);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            StringBuilder sb = new StringBuilder();
            String line;
            String decryptedLine;
            //sb.append("\n");
            int i = 0;
            while ((line = bufferedReader.readLine()) != null) {
            	if(i>1) sb.append("\n");
                if(line.equals(MyApplication.escapeSequence)){
                	//if it hits the escape sequence it makes 3 newlines in sb and skips the line in the file
                	
                	
                }
                else{
                	//otherwise it appends the new line to the string builder
                	decryptedLine=decryptText(line);
                	sb.append(decryptedLine);
                }
                i++;
                
            }
            
            //reset textView to new string of messages
            textView.setText(sb.toString());
            
            inputStreamReader.close();
            
            
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    	
    	
    	//sets newMessages to false so runnable doesn't continuously call this method
    	MyApplication.newMessages=false;
    	MyApplication.setRunning(true);
    }
    

    
    String decryptText(String encryptedBody){
    	//Encryption algorithm
    	String body;
    	/*try {
			StringTLV stlv = us1.messageReceiving(user, PROTOCOL,
			        with, encryptedBody, callback);
			body = stlv.toString(); // assumes no non-text data was sent
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			body = null;
		}*/
    	//return body;
    	return encryptedBody;
    }
    
    String encryptText(String body){
    	//Encryption algorithm
    	String encryptedBody;
		try {
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			encryptedBody = null;
		}
    	//return encryptedBody;
		return body;
    }
    
    @Override
    protected void onPause() {
    	
    	//set global is running to false
    	MyApplication.setRunning(false);
    	//stop the parse runnable
    	isRunning = false;
        if (serviceBound) {
        	if (myService != null) {
        		myService.removeResultCallback(this);
        	}
    		Log.i("MyService", "Unbinding");
    		unbindService(serviceConnection);
        	serviceBound = false;
        	
        }
        super.onPause();
    }

	@Override
    public void onStop() {
		//stop the parse runnable
    	isRunning = false;    
		MyApplication.setRunning(false);
            //Check device supported Accelerometer senssor or not
            
            super.onStop();
           
    }
	
	@Override
	public void onDestroy() {
		//stop the parse runnable
    	isRunning = false;
		MyApplication.setRunning(false);
		
		
		//Check device supported Accelerometer senssor or not
		
		super.onDestroy();
			
	}

    /**
     * This function is called from the service thread.  To process this, we need 
     * to create a message for a handler in the UI thread.
     */
    @Override
    public void onResultReady(ServiceResult result) {
    	if (result != null) {
    		Log.i(LOG_TAG, "Preparing a message for " + result.floatValue);
    		Log.i(LOG_TAG, "Preparing a message for " + result.dateValue);
    	} else {
    		Log.e(LOG_TAG, "Received an empty result!");
    	}
        mUiHandler.obtainMessage(DISPLAY_NUMBER, result).sendToTarget();
    }
    
    /**
     * This Handler callback gets the message generated above. 
     * It is used to display the integer on the screen.
     */
    private class UiCallback implements Handler.Callback {
        @Override
        public boolean handleMessage(Message message) {
            if (message.what == DISPLAY_NUMBER) {
            	// Gets the result.
            	ServiceResult result = (ServiceResult) message.obj;
            	// Displays it.
            	if (result != null) {
            		Log.i(LOG_TAG, "Displaying: " + result.floatValue);
            		//TextView tv = (TextView) findViewById(R.id.max_text);
            		//tv.setText(Integer.toString(result.floatValue));
            		// Tell the worker that the bitmap is ready to be reused
            		if (serviceBound && myService != null) {
            			Log.i(LOG_TAG, "Releasing result holder for " + result.floatValue);
            			myService.releaseResult(result);
            		}
            	} else {
            		Log.e(LOG_TAG, "Error: received empty message!");
            	}
            }
            return true;
        }
    }

    public boolean netStatusIsGood(){
    	boolean connected = false;
    	ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
    	if (connMgr != null){
    		NetworkInfo[] netInfo = connMgr.getAllNetworkInfo();
    		for(NetworkInfo ni : netInfo){
    			if((ni.getTypeName().equalsIgnoreCase("WIFI") || ni.getTypeName().equalsIgnoreCase("MOBILE"))& ni.isConnected() & ni.isAvailable()){
    				connected = true;
    			}
    		}
    	}
    	return connected;
    }
    
    public boolean inetAddr(){
    	boolean x1 = false;
    	try{
    		Socket s = new Socket("utcnist.colorado.edu", 37);
    		InputStream i = s.getInputStream();
    		Scanner scan = new Scanner(i);
    		while(scan.hasNextLine()){
    			x1 = true;
    		}
    		scan.close();
    		s.close();
    	} catch (Exception e){
    		x1 = false;
    	}
    	return x1;
    }




    @SuppressWarnings("unchecked")
	public void sendToServer(String user, String with, String encryptedBody,
    		String sentTime, String hashedPassword){
    	//send to the server 
    				//Add the data
		List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(5);
		nameValuePairs.add(new BasicNameValuePair("from", user));
		nameValuePairs.add(new BasicNameValuePair("pass", hashedPassword));
		nameValuePairs.add(new BasicNameValuePair("to", with));
		nameValuePairs.add(new BasicNameValuePair("body", encryptedBody));
		nameValuePairs.add(new BasicNameValuePair("time_sent", sentTime));
		new SendMessageTask().execute(nameValuePairs);
    }
    



    private class SendMessageTask extends AsyncTask<List<NameValuePair>, Void, String>{
    	protected String doInBackground(List<NameValuePair>... pairsList){
        	Log.i("SendMessageTask", "sending message");
			List<NameValuePair> pairs = pairsList[0];
			HttpClient httpclient = new DefaultHttpClient();
			HttpPost httppost = new HttpPost("https://etch-messaging.appspot.com/send_message");
			
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
	    		} else Log.i("SendMessageTask", "No internet connection");
			}else Log.i("SendMessageTask", "No Network connection");
			Log.i("doInBackground", responseString); 
    		return responseString;
    	}
    	
    	protected void onPostExecute(String result){
			Log.i("SendMessageTask", result);
    	}
    }

}
