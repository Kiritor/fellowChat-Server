/*��ʱ�洢һЩ��Ϣ����*/

package util.tools;

import java.util.HashMap;

public class TemporaryStorage {

	public static HashMap<String,Object> temporaryStorage = new HashMap<String,Object>();
	
	public static void storeObject(String name,Object obj){
		temporaryStorage.put(name, obj);
	}
	
	public static void removeObject(String name){
		temporaryStorage.remove(name);
	}
}
