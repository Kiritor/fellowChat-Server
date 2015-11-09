/*��������ڵõ������������õ���Ϣ
 * �������ݿ�������Լ��������Ϣ*/

package util.tools;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.StringTokenizer;

public class Tools {

	public static String[] getConfig(){
		String[] str = new String[2];//�ַ����������ڱ������ݿ�������������Ϣ
		File file = new File("serverConfigure.cfg");
		if(file.exists()){
			try{
				FileInputStream fin = new FileInputStream(file);
				BufferedInputStream bin = new BufferedInputStream(fin);
				int length = bin.available();//���ؿ��ԴӴ���������ȡ�������������Ҳ��ܴ��������������ķ������������Ĺ����ֽ���
				if(length>0){
					byte[] data = new byte[length];
					bin.read(data);
					//������������ת�����ַ���
					String s = new String(data);
					//stringTokenizer����Ӧ�ó����ַ����ֽ�Ϊ���
					StringTokenizer token = new StringTokenizer(s," ");
					str[0] = token.nextToken();
					str[1] = token.nextToken();
					
					return str;
				}
				
			}catch(Exception e){
				e.printStackTrace();
			}
		}
		return null;
	}
}
