package com.example.etch;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;

import android.app.Application;
import android.os.Environment;
import android.util.Log;

public class MyApplication extends Application {

    private static boolean running;
    
    private static boolean loggedIn;
    
    
    
    public static boolean newMessages = false;
    
    public static String userName = "";
    
    public static String hashedPassword = "";
    
    public static String messagesPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/messages/";
    
    public static String friendsPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/friends/";

    public static final ArrayList<String> incomingFriendRequests = new ArrayList<String>();    
    public static final ArrayList<String> acceptedFriendRequests = new ArrayList<String>();
    
    
    public static final String escapeSequence = "¿";
    
    public static void createPath(){
    	final File dir = new File(messagesPath);
    	if(!dir.isDirectory()){
    		dir.mkdirs();
    	}
    	final File dir2 = new File(friendsPath);
    	if(!dir2.isDirectory()){
    		dir2.mkdirs();
    	}
    }
    
    public static void setFriendsPath(String user){
    	friendsPath+= user + "/";
    }
    
    public static void resetFriendsPath(){
    	friendsPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/friends/";
    }
    
    public static void setMessagesPath(String user){
    	messagesPath+= user + "/";
    }
    
    public static void resetMessagesPath(){
    	messagesPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/messages/";
    }
    
    public static boolean getRunning() {
        return running;
    }

    public static void setRunning(boolean running) {
        MyApplication.running = running;
    }
    public static boolean getLoggedIn() {
        return loggedIn;
    }

    public static void setLoggedIn(boolean loggedIn) {
        MyApplication.loggedIn = loggedIn;
    }
    

    
    
}
