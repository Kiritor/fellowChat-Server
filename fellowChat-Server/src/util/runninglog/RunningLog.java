/*处理日志文件的类*/

package util.runninglog;

import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public class RunningLog {
	public static boolean flag = false;
	//日志记录
    public static void record(String msg){
    	try{
    		FileOutputStream out = new FileOutputStream("D:\\serverlog.txt",flag);
    		
    		Date date = new Date();
    		SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd a  hh:mm:ss");
    		String str = f.format(date);
    		
    		String runningMsg = str+"--->"+msg+"\r\n";
    		out.write(runningMsg.getBytes());
    		flag = true;
    		
    	}catch(Exception e){
    		e.printStackTrace();
    	}
    }
}
