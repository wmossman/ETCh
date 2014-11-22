

package com.example.etch;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

public class MyServiceTask implements Runnable {

	
	
	
	
	
	
	

    private static float fullForce = 0;
    private static String currDate = "";
    public static boolean isOpen;
    public int receiveFrom = 0;
    private int ONGOING_NOTIFICATION_ID = 10;
    // you could use an OrientationListener array instead
    // if you plans to use more than one listener
 

	
	
	
	
	
	public static final String LOG_TAG = "MyService";
	private static boolean running;
	private Context context;
	private MyService myService;
	
    private Set<ResultCallback> resultCallbacks = Collections.synchronizedSet(
    		new HashSet<ResultCallback>());
    private ConcurrentLinkedQueue<ServiceResult> freeResults = 
    		new ConcurrentLinkedQueue<ServiceResult>();
	
    public MyServiceTask(Context _context, MyService _myService) {
    	context = _context;
    	myService = _myService;
    	// Put here what to do at creation.
    }
    
    public void check(){
    	
    	isOpen = MyApplication.getLoggedIn();
    	
    }
    
    
    @Override
    public void run() {
    	String username = MyApplication.userName; 
    	String password = MyApplication.hashedPassword;
        running = true;
        while (running) {
        	//if UI is open, check every 5 seconds, otherwise check every 20 seconds
        	username = MyApplication.userName; 
        	password = MyApplication.hashedPassword;
        	check();
        	if(MyApplication.getRunning()){
				try {
					Thread.sleep(5000);
				} catch (Exception e) {
					e.getLocalizedMessage();
				}
				check();
				if(isOpen){
					
					Log.i("Ping", "Pinging quickly");
					notifyResultCallback(fullForce);
					Log.i(username, password);
					String message = checkMessages(username, password);
					String incomingRequests = checkReceivedRequests(username, password);
					String acceptedRequests = checkAcceptedRequests(username, password);
					if(!message.equals("{}")) parseFromServer(message);
					if(!incomingRequests.equals("{}")) {
						Log.i("incoming", incomingRequests);
				    	JSONObject json = null;
				    	try {
							json = new JSONObject(incomingRequests);
						} catch (JSONException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
				    	if(json != null){
				    		int curr = 0;
				    		JSONObject newFriend;
				    		while(json.has(Integer.toString(curr))){
				    			try {
									newFriend = (JSONObject) json.get(Integer.toString(curr));
									String friendName = newFriend.getString("sender");
									MyApplication.incomingFriendRequests.add(friendName);
								} catch (JSONException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
									Log.i("run-incoming", "JSON Exception");
								}
				    			curr++;
				    		}
				    	}
					}
					if(!acceptedRequests.equals("{}")) {
						Log.i("accepted", acceptedRequests);
						JSONObject json = null;
				    	try {
							json = new JSONObject(acceptedRequests);
						} catch (JSONException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
				    	if(json != null){
				    		int curr = 0;
				    		JSONObject newFriend;
				    		while(json.has(Integer.toString(curr))){
				    			try {
									newFriend = (JSONObject) json.get(Integer.toString(curr));
									String friendName = newFriend.getString("friend");
									MyApplication.acceptedFriendRequests.add(friendName);							    	
								} catch (JSONException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
									Log.i("run-accepted", "JSON Exception");
								}
				    			curr++;
				    		}
				    	}
					}
				}
        	}
        	else{
        		try {
					Thread.sleep(15000);
				} catch (Exception e) {
					e.getLocalizedMessage();
				}
        		check();
        		if(isOpen){
        			
					Log.i("Ping", "Pinging slowly");
					notifyResultCallback(fullForce);
					String message = checkMessages(username, password);
					String incomingRequests = checkReceivedRequests(username, password);
					String acceptedRequests = checkAcceptedRequests(username, password);
					if(!message.equals("{}")) parseFromServer(message);
					if(!incomingRequests.equals("{}")) {
						Log.i("incoming", incomingRequests);
				    	JSONObject json = null;
				    	try {
							json = new JSONObject(incomingRequests);
						} catch (JSONException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
				    	if(json != null){
				    		int curr = 0;
				    		JSONObject newFriend;
				    		while(json.has(Integer.toString(curr))){
				    			try {
									newFriend = (JSONObject) json.get(Integer.toString(curr));
									String friendName = newFriend.getString("sender");
									MyApplication.incomingFriendRequests.add(friendName);							    	
								} catch (JSONException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
									Log.i("run-incoming", "JSON Exception");
								}
				    			curr++;
				    		}
				    	}
					}
					if(!acceptedRequests.equals("{}")) {
						Log.i("accepted", acceptedRequests);
						JSONObject json = null;
				    	try {
							json = new JSONObject(acceptedRequests);
						} catch (JSONException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
				    	if(json != null){
				    		int curr = 0;
				    		JSONObject newFriend;
				    		while(json.has(Integer.toString(curr))){
				    			try {
									newFriend = (JSONObject) json.get(Integer.toString(curr));
									String friendName = newFriend.getString("friend");
									MyApplication.acceptedFriendRequests.add(friendName);							    	
								} catch (JSONException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
									Log.i("run-accepted", "JSON Exception");
								}
				    			curr++;
				    		}
				    	}
					}
					MyApplication.newMessages=true;
        			
        		}
        		
        	}
        }
    }

    public void addResultCallback(ResultCallback resultCallback) {
        resultCallbacks.add(resultCallback);
    }

    public void removeResultCallback(ResultCallback resultCallback) {
    	// We remove the callback... 
        resultCallbacks.remove(resultCallback);
    	// ...and we clear the list of results.
    	// Note that this works because, even though mResultCallbacks is a synchronized set,
    	// its cardinality should always be 0 or 1 -- never more than that. 
    	// We have one viewer only.
        // We clear the buffer, because some result may never be returned to the
        // free buffer, so using a new set upon reattachment is important to avoid
        // leaks.
    	freeResults.clear();
    }

    // Creates result bitmaps if they are needed.
    private void createResultsBuffer() {
    	// I create some results to talk to the callback, so we can reuse these instead of creating new ones.
    	// The list is synchronized, because integers are filled in the service thread,
    	// and returned to the free pool from the UI thread.
    	freeResults.clear();
    	for (int i = 0; i < 10; i++) {
    		freeResults.offer(new ServiceResult());
    	}
    }
    
    // This is called by the UI thread to return a result to the free pool.
    public void releaseResult(ServiceResult r) {
        freeResults.offer(r);
    }
    
    public void stopProcessing() {
        running = false;
    }
    
    public void setTaskState(boolean b) {
    	// Do something with b.
    }
    
 
    /**
     * Call this function to return the integer i to the activity.
     * @param i
     */
    private void notifyResultCallback(float i) {
    	if (!resultCallbacks.isEmpty()) {
    		// If we have no free result holders in the buffer, then we need to create them.
    		if (freeResults.isEmpty()) {
    			createResultsBuffer();
    		}
    		ServiceResult result = freeResults.poll();
    		if (result != null) {
    			result.floatValue = i;
    			result.dateValue = currDate;
    			for (ResultCallback resultCallback : resultCallbacks) {
    				resultCallback.onResultReady(result);
    			}
    		}
    	}
    }

    public interface ResultCallback {
        void onResultReady(ServiceResult result);
    }
    
    
    
    public static String getCurrentTimeStamp(){
        try {

            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
            String currentTimeStamp = dateFormat.format(new Date()); // Find todays date

            return currentTimeStamp;
        } catch (Exception e) {
            e.printStackTrace();

            return null;
        }
    }
    
    
    
    
    
    //with is the person that you are messaging, which will be the name of the file
    //body is the date and message information
    //   /messages/ is the folder on the sd card that holds all of these files
    //¿ is the escape sequence for the message parser
 
    public static void writeToFile(String from, String with, String encryptedBody, String sentTime){
        FileOutputStream fos = null;

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
                Log.i("Write to file", "Inside create new file statement");
            } 

            fos = new FileOutputStream(myFile,true);
            fos.write("\n".getBytes());
            fos.write(from.getBytes());
            fos.write("   ".getBytes());
            fos.write(sentTime.getBytes());
            fos.write("\n".getBytes());
            fos.write(encryptedBody.getBytes());
            fos.write("\n".getBytes());
            fos.write(MyApplication.escapeSequence.getBytes());
            fos.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    public void parseFromServer(String message){
    	//Turn string into JSON
    	Log.i("parseFromServer", "inside parse func");
    	JSONObject json = null;
    	try {
			json = new JSONObject(message);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	if (json != null) {
	    	int curr = 0;
	    	String encryptedBody;
	    	String with;
	    	String sentTime;
    		JSONObject newMessage;
	    	while(json.has(Integer.toString(curr))){
		    	//parse JSON into the corresponding elements
	    		try {
					newMessage = (JSONObject) json.get(Integer.toString(curr));
			    	encryptedBody = newMessage.getString("body");
			    	with = newMessage.getString("from");
			    	sentTime = newMessage.getString("time_sent");
			    	receiveMessage(with, encryptedBody,sentTime);
			    	
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					Log.i("parseFromServer", "JSON Exception");
				}
	    		curr++;
	    	}
    	}
    }
    
    
    private void receiveMessage(String with, String encryptedBody, String sentTime){
    	MyServiceTask.writeToFile(with, with, encryptedBody, sentTime);
    	MyApplication.newMessages = true;
    	showMyNotification();
    	
    }
    
    /**THIS IS WRONG IT SHOULD NOT DECRYPT UNTIL IT IS DISPLAYED YOU IDIOT
    private void receiveMessage(String with, String encryptedBody, String sentTime){
    	String body = decryptText(encryptedBody);
    	MyServiceTask.writeToFile(with, with, body, sentTime);
    	MyApplication.newMessages = true;
    	
    }
    
    private String decryptText(String encryptedBody){
    	//Encryption algorithm
    	String body = encryptedBody;
    	return body;
    }
    **/
    
    //I just want to let everybody reading this comment know
    //that this showNotification method right here is full of wizard magic.
    @SuppressWarnings("deprecation")
	private void showMyNotification() {

        // Creates a notification.
		Notification notification = new Notification(
        		R.drawable.ic_launcher, 
        		context.getResources().getString(R.string.new_etch_messages),
                System.currentTimeMillis());
        
    	Intent notificationIntent = new Intent(MainActivity.context, MainActivity.class);
    	PendingIntent pendingIntent = PendingIntent.getActivity(MainActivity.context, 0, notificationIntent, 0);
    	notification.setLatestEventInfo(MainActivity.context, context.getResources().getText(R.string.new_etch_messages),
    			context.getResources().getText(R.string.new_etch_messages), pendingIntent);
    	MyService.notificationManager.notify(ONGOING_NOTIFICATION_ID, notification);
    	
    	
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
    
    private String checkMessages(String user, String pass){
    	Log.i("checkMessages", "Pinging the server");
    	if(/*netStatusIsGood()*/ true){
    		if(/*inetAddr()*/ true){
    			HttpClient httpclient = new DefaultHttpClient();
    			HttpPost httppost = new HttpPost("https://etch-messaging.appspot.com/check_messages");
    			try{
    				//Add the data
    				List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
    				nameValuePairs.add(new BasicNameValuePair("user", user));
    				nameValuePairs.add(new BasicNameValuePair("pass", pass));
    				httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
    				
    				//send the request
    				HttpResponse response = httpclient.execute(httppost);
    				String responseString = new BasicResponseHandler().handleResponse(response);
    				Log.i("checkMessages", responseString);
    				return responseString;
    			} catch (Exception e){
    				return "An error occurred." + e.getMessage();
    			}
    		}
    	}
    	return "Bad network connection";
    }
    
    private String checkReceivedRequests(String user, String pass){
    	if(/*netStatusIsGood()*/ true){
    		if(/*inetAddr()*/ true){
    			HttpClient httpclient = new DefaultHttpClient();
    			HttpPost httppost = new HttpPost("https://etch-messaging.appspot.com/users/check_friends");
    			try{
    				//Add the data
    				List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
    				nameValuePairs.add(new BasicNameValuePair("user", user));
    				nameValuePairs.add(new BasicNameValuePair("pass", pass));
    				httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
    				
    				//send the request
    				HttpResponse response = httpclient.execute(httppost);
    				String responseString = new BasicResponseHandler().handleResponse(response);
    				Log.i("checkIncomingRequests", responseString);
    				return responseString;
    			} catch (Exception e){
    				return "An error occurred." + e.getMessage();
    			}
    		}
    	}
    	return "Bad network connection";
    }
    
    private String checkAcceptedRequests(String user, String pass){
    	if(/*netStatusIsGood()*/ true){
    		if(/*inetAddr()*/ true){
    			HttpClient httpclient = new DefaultHttpClient();
    			HttpPost httppost = new HttpPost("https://etch-messaging.appspot.com/users/check_accepts");
    			try{
    				//Add the data
    				List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
    				nameValuePairs.add(new BasicNameValuePair("user", user));
    				nameValuePairs.add(new BasicNameValuePair("pass", pass));
    				httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
    				
    				//send the request
    				HttpResponse response = httpclient.execute(httppost);
    				String responseString = new BasicResponseHandler().handleResponse(response);
    				Log.i("checkAcceptedRequests", responseString);
    				return responseString;
    			} catch (Exception e){
    				return "An error occurred." + e.getMessage();
    			}
    		}
    	}
    	return "Bad network connection";
    }    
    
}
