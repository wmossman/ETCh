package com.example.etch;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
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

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.etch.MyService.MyBinder;

public class MainActivity extends ActionBarActivity 
implements com.example.etch.MyServiceTask.ResultCallback, Serializable {

	private static final long serialVersionUID = 1L;
	public static final int DISPLAY_NUMBER = 10;
	// since we're not interoperating with other services, the following string is arbitrary
	public static final String PROTOCOL = "ETCh";
    private Handler mUiHandler;
    public String with = "them";
    public String user;
    Button sendButton;
    Button btnAdd;
    EditText textAdd;
    EditText textEditor;
    TextView textView;
    ScrollView scrollView;
    private String hashedPassword="this is the hashed password";
    public static Context context;
    private int ONGOING_NOTIFICATION_ID = 10;
    public boolean isRunning;
    Handler mHandler = new Handler();
    public FriendList friendList;
    private PopupWindow pwindo;
    private PopupWindow pwindo2;
    private Menu menu;
    private ListView mList;
    private ListView myList;
    
    private static final String LOG_TAG = "MainActivity";
    
    // Service connection variables.
    private boolean serviceBound;
    private MyService myService;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_list);
        mUiHandler = new Handler(getMainLooper(), new UiCallback());    
        serviceBound = false;
        context = this;
        friendList = new FriendList();
        loadFriendList();
        user = MyApplication.userName;
        hashedPassword = MyApplication.hashedPassword;
        isRunning = true;
        MyApplication.setLoggedIn(true);
        MyApplication.setRunning(true);
        Intent intent = new Intent(this, MyService.class);
        startService(intent);        
    	bindMyService();
        

        
        runPopulate();
        setTitle("ETCh   " + user);
        populateList();
        killMyNotification();
    }
    
    /*@Override
    public void onBackPressed() {
    	if (pwindo.isShowing()) {
            pwindo.dismiss();
        }
    	else{
    		moveTaskToBack(true);
    	}
    }
    */
    @Override
    public void onBackPressed() {
    	moveTaskToBack(true);
    	
    }
    
    
    public void runPopulate(){
        new Thread(new Runnable() {
            @Override
            public void run() {
               
                while (isRunning) {
                    try {
                        Thread.sleep(1000);
                        mHandler.post(new Runnable() {

                            @Override
                            public void run() {
                            	//MyApplication.setRunning(true);
                            	if(MyApplication.newMessages) populateList();                               
                            }
                        });
                    } catch (Exception e) {
                        
                    }
                }
            }
        }).start();
    }
    
    
    
    private File[] sortFilesByDateModified(){
    	final File dir = new File(MyApplication.messagesPath );
	    File[] files = dir.listFiles();
	
	    Arrays.sort(files, new Comparator<File>(){
	        public int compare(File f1, File f2)
	        {
	            return Long.valueOf(f1.lastModified()).compareTo(f2.lastModified());
	        } });
	    return files;
    }
    
    private String[] getArrayOfFiles(){
    	File[] files = sortFilesByDateModified();
    	
    	ArrayList<String> arrayListOfListElements = new ArrayList<String>();
    	for(int i=0; i<files.length; ++i){
    		File file = files[i];
    		if(friendList.isFriend(removeType(file.getName()))){
    			arrayListOfListElements.add(file.getName());
    		}
    		
    	}
    	String[] arrayOfListElements = new String[arrayListOfListElements.size()];
    	//converts from array list to array, truncates type, and reverses
    	for(int i=0; i<arrayListOfListElements.size(); ++i){
    		arrayOfListElements[i] = removeType(arrayListOfListElements.get((arrayListOfListElements.size()-1)-i));
    	}
    	return arrayOfListElements;
    }
    
    
    private void populateList(){
    	String[] arrayOfListElements = getArrayOfFiles();
    	//bens array adapter code
    	ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, 
    			R.layout.custom_list_item, arrayOfListElements);
    	ListView myList = (ListView)findViewById(R.id.listView1);
    	myList.setAdapter(adapter);
    	myList.setOnItemClickListener(new OnItemClickListener(){
    		
    		
    		//calls openMessageActivity when you click on it
    		@Override
    		public void onItemClick(AdapterView<?> parent, View view, int position, long id){
    			String with = ((TextView)view).getText().toString();
    			//String with = removeType(item);
    			openMessageActivity(with);
    		}
    	});
    	
    	myList.setLongClickable(true);
    	myList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener(){
    		

			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
				String with = ((TextView)view).getText().toString();
    			deleteFileDialog(with);
    			return true;
			}
    	});
    	
    		
    	
    }
    
    private String removeType(String toTrunc){
    	String truncated="";
    	for(int i=0; i<toTrunc.length();i++){
    		if(toTrunc.charAt(i)!='.'){
    			truncated+=(toTrunc.charAt(i));
    		}
    		else break;
    	}
    	return truncated;
    }
    
    
    private void openMessageActivity(String with){
    	Intent intent = new Intent(this, MessageActivity.class);
    	Bundle b = new Bundle();
    	b.putString("with", with); //Your id
    	intent.putExtras(b); //Put your id to your next Intent
    	startActivity(intent);
    	//finish(); this will call onDestroy
    	
    }
    
    
    
    
    @Override
    protected void onResume() {
        super.onResume();
        // Starts the service, so that the service will only stop when explicitly stopped.
        if(MyApplication.userName=="") logOut();
        MyApplication.setRunning(true);
    	isRunning=true;
    	MyApplication.setLoggedIn(true);
        Intent intent = new Intent(this, MyService.class);
        startService(intent);
    	bindMyService();
    	populateList();
        killMyNotification();
        runPopulate();
    }
    
    private void bindMyService() {
    	// We are ready to show images, and we should start getting the bitmaps
    	// from the motion detection service.
    	// Binds to the service.
    	Intent intent = new Intent(this, MyService.class);
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
    		myService.addResultCallback(MainActivity.this);
    	}
    	
    	@Override
    	public void onServiceDisconnected(ComponentName arg0) {
    		serviceBound = false;
    	}
    };
    
    
    private void killMyNotification(){
    	if(MyService.notificationManager!=null){
    		MyService.notificationManager.cancel(ONGOING_NOTIFICATION_ID);
    	}
    	
    }
    
    
    
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        this.menu = menu;
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
            case R.id.action_add:
            	openAddPopup("");
            	return true;
            case R.id.action_friends:
            	openFriendsPopup();
            	return true;
            case R.id.action_unfriend_all:
            	unfriendDialog();
            	return true;
            
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    
    @SuppressLint("NewApi")
	@Override
    public void onConfigurationChanged(Configuration newConfig) {
    	super.onConfigurationChanged(newConfig);
    	if(pwindo!=null){    
        	if(pwindo.isShowing()){
        		String save = textAdd.getText().toString();
        		pwindo.dismiss();
        		openAddPopup(save);
	        }
	    }
    	if(pwindo2!=null){    
        	if(pwindo2.isShowing()){
        		pwindo2.dismiss();
        		openFriendsPopup();
	        }
	    }
    	/*
    	Display display = getWindowManager().getDefaultDisplay();
		Point size = new Point();
		display.getSize(size);
		int width = size.x;
		int height = size.y;
		if(width>600){
			Log.i("onconfigchange","width is greater than 600");
			MenuItem friends = menu.findItem(R.id.action_friends);
			//friends.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
			//friends.setVisible(true);
		}
		else{
			MenuItem friends = menu.findItem(R.id.action_friends);
			friends.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
		}
		*/
        
    }
    
    @SuppressLint("NewApi")
	@SuppressWarnings("deprecation")
	private void openAddPopup(String saved){
    	try {
    		// We need to get the instance of the LayoutInflater
    		LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    		View layout = inflater.inflate(R.layout.screen_add, (ViewGroup) findViewById(R.id.popup_add));
    		Display display = getWindowManager().getDefaultDisplay();
    		Point size = new Point();
    		display.getSize(size);
    		int width = size.x;
    		int height = size.y;
    		pwindo = new PopupWindow(layout, (width/10)*8, (height/10)*6, true);
    		//this allows there to be a logical backpress and loss of focus
    		pwindo.setBackgroundDrawable(new BitmapDrawable());
    		pwindo.showAtLocation(layout, Gravity.CENTER, 0, 0);
    		
    		btnAdd = (Button) layout.findViewById(R.id.button_add);
    		btnAdd.setOnClickListener(add_button_click_listener);
    		textAdd= (EditText) layout.findViewById(R.id.editor_add);
    		if(saved!=""){
    			textAdd.setText(saved);
    		}
    		

    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    }

	private OnClickListener add_button_click_listener = new OnClickListener() {
		public void onClick(View v) {
			requestFriend(textAdd.getText().toString());
			saveFriendList();

			printFriendList();

		}
	};
	
	

    
    @SuppressWarnings("deprecation")
	@SuppressLint("NewApi")
	private void openFriendsPopup(){
    	updateFriendsList();
    	try {
    		// We need to get the instance of the LayoutInflater
    		LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    		View layout = inflater.inflate(R.layout.screen_friends, (ViewGroup) findViewById(R.id.popup_friends));
    		Display display = getWindowManager().getDefaultDisplay();
    		Point size = new Point();
    		display.getSize(size);
    		int width = size.x;
    		int height = size.y;
    		//loadFriendList();
    		pwindo2 = new PopupWindow(layout, (width/10)*9, (height/20)*15, true);
    		//this allows there to be a logical backpress and loss of focus
    		pwindo2.setBackgroundDrawable(new BitmapDrawable());
    		pwindo2.showAtLocation(layout, Gravity.CENTER, 0, 0);
    		//LayoutInflater mSet = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    	    //Object mPopup = mSet.inflate(R.layout.menu_dummy, null, false);
    		mList = (ListView) layout.findViewById(R.id.list_friends);
    		populateFriends();


    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    }

    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    private Friend[] sortFriendsByName(Friend[] unsorted){
    	Friend[] toSort = unsorted;
    	
	    Arrays.sort(toSort, new Comparator<Friend>(){
	        public int compare(Friend f1, Friend f2)
	        {
	            return String.valueOf(f1.getUserName()).compareTo(f2.getUserName());
	        } });
	    return toSort;
    }
    
    private Friend[] getArrayOfFriends(){
    	Friend[] arrayOfUnsortedListElements = new Friend[friendList.list.size()];
    	for(int i = 0; i<arrayOfUnsortedListElements.length;i++) {
    		arrayOfUnsortedListElements[i]=friendList.list.get(i);
    	}
    	Friend[] arrayOfListElements = sortFriendsByName(arrayOfUnsortedListElements);
    	//converts from array list to array, truncates type, and reverses
    	for(int i=0; i<friendList.list.size(); ++i){
    		arrayOfListElements[i] = friendList.list.get(i);
    	}
    	return arrayOfListElements;
    }
    
    
    
    private void populateFriends(){
    	Friend[] arrayOfListElements = getArrayOfFriends();
    	for(int i = 0; i<arrayOfListElements.length;i++) Log.i("populateFriends",arrayOfListElements[i].userName);
    	ArrayAdapter<Friend> adapter = new ArrayAdapter<Friend>(this, 
    			R.layout.custom_list_item, arrayOfListElements);
    	
    	
    	if(mList==null){
    		Log.i("pop","mList is null");
    	}
    	mList.setAdapter(adapter);
    	mList.setOnItemClickListener(new OnItemClickListener(){
    		
    		
    		//calls openMessageActivity when you click on it
    		@Override
    		public void onItemClick(AdapterView<?> parent, View view, int position, long id){
    			String with = cutParenthesis(((TextView)view).getText().toString());
    			//String with = removeType(item);
    			int result = checkStatus(with);
    			Log.i("itemClick",Integer.toString(result));
    			if(result==2)
    				acceptRequest(with);
    			if(result==3)
    				openMessageActivity(with);
    		}
    	});
    	
    	mList.setLongClickable(true);
    	mList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener(){
    		

			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
				String with = cutParenthesis(((TextView)view).getText().toString());
    			deleteFriendDialog(with);
    			return true;
			}
    	});
    	saveFriendList();
    	
    }
    
    private int checkStatus(String with){
    	for(int i=0;i<friendList.list.size();i++){
    		if(friendList.list.get(i).getUserName().equals(with)){
    			if(friendList.list.get(i).getStatus().equals("requested"))
    				return 1;
    			else if(friendList.list.get(i).getStatus().equals("unverified")){
    				//acceptRequest(friendList.list.get(i).getUserName());
    				return 2;
    			}
    			else if(friendList.list.get(i).getStatus().equals("verified"))
    				return 3;
    		}
    	}
    	return 0;
    }
    
    private String cutParenthesis(String toCut){
    	String cut=new String();
    	for(int i=0; i<toCut.length();i++){
    		if(toCut.charAt(i)==' ') break;
    		cut+=toCut.charAt(i);
    	}
    	return cut;
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
    	MyApplication.setRunning(false);
    	isRunning=false;
    	MyApplication.setLoggedIn(false);
    	killMyNotification();
    	MyApplication.resetFriendsPath();
    	MyApplication.resetMessagesPath();
    	Intent intent = new Intent(this, LoginActivity.class);
    	startActivity(intent);
    }
    
    
    
    public void deleteFriendDialog(final String friend){
    	AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("Confirm");
        builder.setMessage("Permanently delete " + friend + "?" );

        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {
                friendList.removeFromList(friend);
                populateFriends();
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
    
    
    
    public void unfriendDialog(){
    	AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("Confirm");
        builder.setMessage("Permanently delete all friends?");

        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {
                deleteFriends();

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
    
    
    
    
    public void deleteFriends(){
		try{
    	final File dir = new File(MyApplication.friendsPath);

        if (!dir.exists())
        {
            dir.mkdirs(); 
        }
        
    	if (dir.isDirectory()) {
            File empty = new File(MyApplication.friendsPath+"saved_friends.bin");
            empty.createNewFile();
        }
        friendList.list = new ArrayList<Friend>();
        saveFriendList();
        populateFriends();
		}catch(IOException e){
			e.printStackTrace();
		}
    	
        
    }
    
    
    
    public void deleteFileDialog(final String _with){
    	AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("Confirm");
        builder.setMessage("Delete messages with " + _with + " from list?");

        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {
                deleteOneFile(_with);

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
    
    
    
    
    public void deleteOneFile(String _with){
		final File dir = new File(MyApplication.messagesPath );

        if (!dir.exists())
        {
            dir.mkdirs(); 
        }
        
        String[] fileArray= getArrayOfFiles();
        MessageActivity tempMessenger = new MessageActivity();
        
        //Tell other user to delete there messages as well
    	String sentTime = MyServiceTask.getCurrentTimeStamp();
    	String body = "--USER ["+ user + "] HAS JUST DELETED ALL MESSAGES. CONSIDER DELETING YOURS AS WELL.--";
    	String encryptedBody = tempMessenger.encryptText(body);
    	//sends the message to all other users
    	for(int i=0;i<fileArray.length;i++){
    		tempMessenger.sendToServer(user, fileArray[i], encryptedBody, sentTime, hashedPassword);
    	}
    	if (dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                if(removeType(children[i]).equals(_with)){
                	new File(dir, children[i]).delete();
            
                }
            }
        }
        
        populateList();
	        
    	
        
    }
    
    
    
    
    
    public void deleteDialog(){
    	AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("Confirm");
        builder.setMessage("Delete all message files?");

        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {
                deleteFiles();

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
    
    
    public void deleteFiles(){
		final File dir = new File(MyApplication.messagesPath );

        if (!dir.exists())
        {
            dir.mkdirs(); 
        }
        
        String[] fileArray= getArrayOfFiles();
        MessageActivity tempMessenger = new MessageActivity();
        
        //Tell other user to delete there messages as well
    	String sentTime = MyServiceTask.getCurrentTimeStamp();
    	String body = "--USER ["+ user + "] HAS JUST DELETED ALL MESSAGES. CONSIDER DELETING YOURS AS WELL.--";
    	String encryptedBody = tempMessenger.encryptText(body);
    	//sends the message to all other users
    	for(int i=0;i<fileArray.length;i++){
    		tempMessenger.sendToServer(user, fileArray[i], encryptedBody, sentTime, hashedPassword);
    	}
    	if (dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                new File(dir, children[i]).delete();
            }
        }
        
        populateList();
	        
    	
        
    }
    
    
    
    
    
    
    
    
    
    public int getFileCount(){
    	int count = 0;


    	    File f = new File(MyApplication.messagesPath );
    	    File[] files  = f.listFiles();

    	    if(files != null)
    	    for(int i=0; i < files.length; i++)
    	    {
    	        count ++;
    	     }
    	return count;
    }
    
    
    
    @Override
    protected void onPause() {
    	
    	//set global is running to false
    	MyApplication.setRunning(false);
    	//stop the populate runnable
    	isRunning = false;
        if (serviceBound) {
        	if (myService != null) {
        		myService.removeResultCallback(this);
        	}
    		Log.i("MyService", "Unbinding");
    		unbindService(serviceConnection);
        	serviceBound = false;
        	// If we like, stops the service.
        	/*
        	if (true) {
        		Log.i(LOG_TAG, "Stopping.");
        		Intent intent = new Intent(this, MyService.class);
        		stopService(intent);
        		Log.i(LOG_TAG, "Stopped.");
        	}
        	*/
        }
        super.onPause();
    }

	@Override
    public void onStop() {
            
		MyApplication.setRunning(false);
		//stop the populate runnable
    	isRunning = false;
            
            super.onStop();
           
    }
	
	@Override
	public void onDestroy() {
		
		MyApplication.setRunning(false);
		//stop the populate runnable
    	isRunning = false;

		
		
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
    
    public void requestFriend(String userName){
    	//send JSON to /user/send_friend_request
    	//send user-username pass-password friend-newfriendusername pub_key-mypublickey
    	//call request friend
    	Log.i("requestFriend",Boolean.toString((textAdd.getText().toString()!=MyApplication.userName)));
    	Log.i((textAdd.getText().toString()),MyApplication.userName);
    	if(!(textAdd.getText().toString().equals(MyApplication.userName))){
    		boolean exists = false;
    	
    		for(int i=0;i<friendList.list.size();i++){
    			if(textAdd.getText().toString().equals(friendList.list.get(i).userName)){
    				exists=true;
    				break;
    			}
    		}
    	
    		if(!exists){
	    		List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(4);
	    		nameValuePairs.add(new BasicNameValuePair("user", MyApplication.userName));
	    		nameValuePairs.add(new BasicNameValuePair("pass", MyApplication.hashedPassword));
	    		nameValuePairs.add(new BasicNameValuePair("friend", userName));
	    		nameValuePairs.add(new BasicNameValuePair("pub_key", ""));
	    		new AddFriendTask().execute(nameValuePairs);
				
    		}
    		else{
    			textAdd.setText("");
    			textAdd.setHint("Already friend");
    		}
    	}
    	else{
			textAdd.setText("");
			textAdd.setHint("That's you!");
    	}
		}
    
    
    private class AddFriendTask extends AsyncTask<List<NameValuePair>, Void, String>{
    	protected String doInBackground(List<NameValuePair>... pairsList){
        	Log.i("AddFriendTask", "sending message");
			List<NameValuePair> pairs = pairsList[0];
			HttpClient httpclient = new DefaultHttpClient();
			HttpPost httppost = new HttpPost("https://etch-messaging.appspot.com/users/send_friend_request");
			
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
    		return responseString;
    	}
    
		protected void onPostExecute(String result){
			Log.i("AddFriendTask", result);
			if(!(result.equals("validation incorrect") || result.equals("no such user") || result.equals("no such friend") || result.equals("No response"))){
		    	Friend temp = new Friend (result,"requested");
		    	friendList.addToList(temp);
		    	saveFriendList();
		    	textAdd.setText("");
				textAdd.setHint("Added");
			}
			else{
				//Toast.makeText(context,result, Toast.LENGTH_SHORT).show();
				textAdd.setText("");
				textAdd.setHint(result);
			}
		}
	    
    }


    public void acceptRequest(String userName){
    	//send JSON to /user/accept_friend_request
    	//send user-username pass-password friend-newfriendusername pub_key-mypublickey
    	//call request friend
    	List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(4);
		nameValuePairs.add(new BasicNameValuePair("user", MyApplication.userName));
		nameValuePairs.add(new BasicNameValuePair("pass", MyApplication.hashedPassword));
		nameValuePairs.add(new BasicNameValuePair("friend", userName));
		nameValuePairs.add(new BasicNameValuePair("pub_key", ""));
		new AcceptRequestTask().execute(nameValuePairs);
		}
    
    
    private class AcceptRequestTask extends AsyncTask<List<NameValuePair>, Void, String>{
    	protected String doInBackground(List<NameValuePair>... pairsList){
        	Log.i("AcceptRequestTask", "sending message");
			List<NameValuePair> pairs = pairsList[0];
			HttpClient httpclient = new DefaultHttpClient();
			HttpPost httppost = new HttpPost("https://etch-messaging.appspot.com/users/accept_friend_request");
			
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
			Log.i("AcceptRequestTask", result);
			if(!(result.equals("validation incorrect") || result.equals("no such user") || result.equals("no such friend") || result.equals("No response"))){
		    	friendList.changeStatus(result, "verified");
		    	saveFriendList();
			}
			else{
				Toast.makeText(context,result, Toast.LENGTH_SHORT).show();
			}
		}
	    
    }
    
    
    
    
    
    
    
 
    
    
    
    
    public static class Friend implements Serializable{
    	private static final long serialVersionUID = 1L;

		private String status;
    	private String userName;
    	
    	public Friend(String _userName, String _status){
    		this.status=_status;
    		this.userName=_userName;
    	}
    	
    	public void setStatus(String _status){
    		this.status=_status;
    	}
    	
    	public void setUserName(String _userName){
    		this.userName=_userName;
    	}
    	
    	
    	public String getStatus(){
    		return this.status;
    	}
    	
    	public String getUserName(){
    		return this.userName;
    	}
    	
    	@Override
        public String toString() {
    		if(this.status!="verified"){
    			return this.userName + " (" + this.status +")";
    		}
    		else{
    			return this.userName;
    		}
            
        }
    	
    	
    }
    
    public void updateFriendsList(){
    	if(!MyApplication.incomingFriendRequests.isEmpty()){
    		for(int i = 0; i < MyApplication.incomingFriendRequests.size(); i++){
    			receiveRequest(MyApplication.incomingFriendRequests.get(i));
    		}
    	}
    	if(!MyApplication.acceptedFriendRequests.isEmpty()){
    		for(int i = 0; i < MyApplication.acceptedFriendRequests.size(); i++){
    			accepted(MyApplication.acceptedFriendRequests.get(i));
    		}
    	}
    }
    
    public void accepted(String userName){
    	friendList.changeStatus(userName, "verified");
    	saveFriendList();
    }
    

    
    public void receiveRequest(String userName){
    	boolean exists = false;
    	Log.i("inside receiveRequest", userName);
		for(int i=0;i<friendList.list.size();i++){
			if(userName.equals(friendList.list.get(i).userName)){
				exists=true;
				break;
			}
		}
    		if(!exists){
    			Log.i("inside receiveRequest", "doesnt exist in friendList");
    		Log.i("recieveRequest", userName);
    		Friend tempFriend = new Friend (userName,"unverified");
    		friendList.addToList(tempFriend);
    		saveFriendList();
    	}
    }
    
    public static class FriendList implements Serializable{
       	
		private static final long serialVersionUID = 1L;
		
		
		public ArrayList<Friend> list;
   	
	   	public FriendList(){
	   		list = new ArrayList<Friend>();
	   	}
	   	
	   	public ArrayList<Friend> getList(){
	   		return this.list;
	   	}
	   	
	   	public void addToList(Friend friend){
	   		boolean friendExists = false;
	   		for(int i = 0; i<list.size();i++){
	   			Friend tempFriend = list.get(i);
	   			if(tempFriend.userName.equals(friend.userName)){
	   				friendExists = true;
	   			}
	   			
	   		}
	   		if(!friendExists) list.add(friend);
	   	}
	   	
	   	public void removeFromList(String friend){
	   		for(int i = 0; i<list.size();i++){
	   			Friend tempFriend = list.get(i);
	   			if(tempFriend.userName.equals(friend)){
	   				list.remove(i);
	   			}
	   			
	   		}
	   	}
	   	
	   	public boolean isFriend(String friend){
	   		for(int i = 0; i<list.size();i++){
	   			Friend tempFriend = list.get(i);
	   			if(tempFriend.userName.equals(friend)){
	   				return true;
	   			}
	   			
	   		}
	   		return false;
	   	}
	   	
	   	public void changeStatus(String userName, String newStatus){
	   		for(int i = 0; i<list.size();i++){
	   			Friend tempFriend = list.get(i);
	   			if(tempFriend.userName.equals(userName)){
	   				tempFriend.status=newStatus;
	   				list.set(i, tempFriend);
	   			}
	   		}
	   	}
   	
   	
   }
    
    
    public void printFriendList(){
    	for(int i=0;i<friendList.list.size();i++){
    		Log.i("printFriendList", friendList.list.get(i).getUserName());
    	}
    }
    
    public void saveFriendList(){
        try{
        	File myFile = new File(MyApplication.friendsPath+"saved_friends.bin");
        	if (!myFile.exists()){    
	            myFile.createNewFile();
        	} 
        	FriendList list = friendList;
        	ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(myFile)); //Select where you wish to save the file...
        	oos.writeObject(list); // write the class as an 'object'
        	oos.flush(); // flush the stream to insure all of the information was written to 'save_object.bin'
        	oos.close();// close the stream
        }
        catch(Exception ex){
        	Log.v("Serialization Save Error : ",ex.getMessage());
        	ex.printStackTrace();
        }
   }
    
    public void loadFriendList(){
    	try{
	    	File myFile = new File(MyApplication.friendsPath+"saved_friends.bin");
	    	if (!myFile.exists()){    
	            myFile.createNewFile();
	        }else{
	        	FriendList check = (FriendList)loadSerializedObject(myFile);
	        	if(check!=null) friendList = check;
	        }
    	}
    	catch (IOException e) {
            e.printStackTrace();
        }
    }
    
   public Object loadSerializedObject(File f){
       try
       {
           ObjectInputStream ois = new ObjectInputStream(new FileInputStream(f));
           Object o = ois.readObject();
           ois.close();
           return o;
       }
       catch(Exception ex)
       {
       Log.v("Serialization Read Error : ","some shit bro");
           ex.printStackTrace();
       }
       return null;
   }
    
    

    
    

}
