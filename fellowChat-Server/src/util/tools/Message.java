package util.tools;

import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import util.runninglog.RunningLog;


public class Message {
	/**
	 * 从输入流中读取一条xml消息,以</msg>结尾即是
	 * 
	 * @return:从流中读取的一条xml消息
	 */
	public static String readString(InputStream in) throws Exception {
		String msg = "";
		int i = in.read();// 从输入流对象中读取
		StringBuffer stb = new StringBuffer();// 字符串缓冲区
		boolean end = false;
		while (!end) {
			char c = (char) i;
			stb.append(c);
			msg = stb.toString().trim();// 去除消息尾的空格
			if (msg.endsWith("</msg>")) {
				break;
			}
			i = in.read();// 继续读取字节
		}
		msg = new String(msg.getBytes("ISO-8859-1"), "GBK");
		return msg;
	}

	/**
	 * 从一条xmlMsg消息串中提取flagName标记的值,
	 * 
	 * @param flagName
	 *            :要提取的标记的名字
	 * @param xmlMsg
	 *            :要解析的xml消息字符串
	 * @return:提取到flagName标记对应的值
	 * @throws:如果解析失败，则是xml消息格式不符协议规范，抛出异常
	 */
	public static String getXMLValue(String flagName, String xmlMsg) throws Exception {
		try {
			// 1.<标记>头出现的位置
			int start = xmlMsg.indexOf("<" + flagName + ">");
			start += flagName.length() + 2;// 计算向后偏移长度
			// 2.</标记>结束符出现的位置
			int end = xmlMsg.indexOf("</" + flagName + ">");
			// 3.截取标记所代表的消息的值
			String value = xmlMsg.substring(start, end).trim();//解析出来的消息的内容
			return value;
		} catch (Exception ef) {
			throw new Exception("解析" + flagName + "失败：" + xmlMsg);
		}
	}
	/**
	 * 发送消息
	 * @param msg 消息内容
	 * @param out 输出流
	 */
	public static void sendMsg(String msg,OutputStream out){
    	String completeMsg = "<msg>"+msg+"</msg>";
    	try{
    		out.write(completeMsg.getBytes());
    		out.flush();//发送给客户端信息
    		
    	}catch(Exception e){
    		e.printStackTrace();
    	}
    }
	/**
	 * 保存用户的离线消息
	 * @param msg
	 * @param userName
	 */
	public static void saveDownlineMsg(String msg,String userName){
		Connection conn = DataBaseControl.conn;
		String sql1 = "select userdownlinemsg from userinfo where username = '"+userName+"'";
		try{
			Statement state = conn.createStatement();
			ResultSet set = state.executeQuery(sql1);
			if(set.next()){
				String str = set.getString("userdownlinemsg");
				str = str + msg;
				String sql2 = "update userinfo set userdownlinemsg = '"+str+"' where username = '"+userName+"'";
				state.executeUpdate(sql2);
			}
			
		}catch(Exception e){
			e.printStackTrace();
			RunningLog.record("*****错误：保存用户离线消息时出错！！！");
		}
	}
	
	public static void deleteDownlineMsg(String userName){
		Connection conn = DataBaseControl.conn;
		String sql = "update userinfo set userdownlinemsg=null where username='"+userName+"'";
		try{
			Statement state = conn.createStatement();
			state.executeUpdate(sql);
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public static void sendObject(Object obj,ObjectOutputStream out){
		try{
			out.writeObject(obj);
			out.flush();
		}catch(Exception e){
			e.printStackTrace();
		}
	}
}
