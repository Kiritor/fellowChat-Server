package util.tools;

import java.net.Socket;
import java.util.HashMap;

public class Online {
   public static HashMap<String,Socket> onlineMap = new HashMap<String,Socket>();
   
   public static void add(String userName,Socket client){
	   onlineMap.put(userName,client);
   }
   public static void remove(String userName){
	   onlineMap.remove(userName);
   }
}
