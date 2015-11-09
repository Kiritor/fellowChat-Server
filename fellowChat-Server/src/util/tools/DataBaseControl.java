/*�������ݿ��һЩ��������*/

package util.tools;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;

import javax.swing.JOptionPane;

import serverConfigure.ServerConfig;
import util.pojo.UserInfo;
import util.runninglog.RunningLog;


public class DataBaseControl {
	
	public static Connection conn;
    
	//�������ݿ������
	public static boolean buildConn(String name,String pwd){
		
	    try{
	    	Class.forName("com.mysql.jdbc.Driver");
	    	String url = "jdbc:mysql://localhost:3306/manchat";
	    	conn = DriverManager.getConnection(url,name,pwd);
	    	RunningLog.record("���ݿ����ӳɹ�������");
	    	return true;
	    	
	    }catch(Exception e){
            JOptionPane.showMessageDialog(null, "�޷����ӵ����ݿ⣬��������������Ϣ�Ƿ���ȷ��");
	    	RunningLog.record("*****�������ݿ����Ӵ��󣡣���");
	    	ServerConfig con = new ServerConfig();
	    	con.showUI();
	    	return false;
	    }
	}
	/**
	 * ���������û�������Ϊ���ѹ�ϵ
	 */
	public static boolean addFriendShip(String user1,String user2){
		int id1 = getUser(user1).getId();
		int id2 = getUser(user2).getId();
		String sql = "insert into relationship (userid,friendid) value(?,?)";
		try{
			PreparedStatement pstate = conn.prepareStatement(sql);
			pstate.setInt(1, id1);
			pstate.setInt(2, id2);
			pstate.executeUpdate();
			pstate.setInt(1, id2);
			pstate.setInt(2, id1);
			pstate.executeUpdate();
			
			return true;
		}catch(Exception e){
			e.printStackTrace();
		}
		return false;
	}
	/**
	 * �������ֵõ��û�����
	 * @param userName
	 * @return
	 */
	public static UserInfo getUser(String userName){
		String sql = "select * from userinfo where username='"+userName+"'";
		try{
			Statement state = conn.createStatement();
			ResultSet set = state.executeQuery(sql);
			if(set.next()){
				UserInfo user = new UserInfo();
				user.setId(set.getInt("id"));
				user.setUserName(set.getString("username"));
				user.setUserPwd(set.getString("userpwd"));
				user.setUserSex(set.getString("usersex"));
				user.setUserAge(set.getInt("userage"));
				user.setUserImage(set.getString("userimage"));
				user.setUserDownlineMsg(set.getString("userdownlinemsg"));
				user.setUserState(set.getString("userstate"));
				
				return user;
			}
			
		}catch(Exception e){
			e.printStackTrace();
			RunningLog.record("*****���󣺸��û�������!!!!!!");
		}
		
		return null;
	}
	/**
	 * �������������ݿ���ȡ��5��userinfo
	 * @param index
	 * @return
	 */
	public static ArrayList<UserInfo> getOnlineUser(int index,String userName){
		ArrayList<UserInfo> list = new ArrayList<UserInfo>();
		String sql = "select * from userinfo where userstate='b' and username<>'"+userName+"' limit "+index+",5";
		try{
			Statement state = conn.createStatement();
			ResultSet set = state.executeQuery(sql);
			while(set.next()){
				UserInfo user = new UserInfo();
				user.setUserName(set.getString("username"));
				user.setUserImage(set.getString("userimage"));
				user.setUserSex(set.getString("usersex"));
				user.setUserAge(set.getInt("userage"));
				list.add(user);
			}
			return list;
		}catch(Exception e){
			e.printStackTrace();
		}
		return null;
	}
	/**
	 * �����û������ٺ��ѹ�ϵ
	 * @param userName1
	 * @param userName2
	 * @return
	 */
	public static boolean destroyFriendship(String userName1,String userName2){
		int id1 = 0;
		int id2 = 0;
		String sql = "select id from userinfo where username=?";
		try{
			PreparedStatement pstate = conn.prepareStatement(sql);
			pstate.setString(1, userName1);
			ResultSet set = pstate.executeQuery();
			if(set.next()){
				id1 = set.getInt("id");
			}
			pstate.setString(1, userName2);
			set = pstate.executeQuery();
			if(set.next()){
				id2 = set.getInt("id");
			}
			
			sql = "delete from relationship where userid=? and friendid=?";
			pstate = conn.prepareStatement(sql);
			pstate.setInt(1, id1);
			pstate.setInt(2, id2);
			pstate.executeUpdate();
			pstate.setInt(1, id2);
			pstate.setInt(2, id1);
			pstate.executeUpdate();
			
			return true;
			
		}catch(Exception e){
			e.printStackTrace();
		}
		return false;
	}
	/**
	 * �������ֵõ����û��ĺ����б�
	 * @param user
	 * @return
	 */
	public static ArrayList<UserInfo> getFriendList(UserInfo user){
		ArrayList<UserInfo> list = new ArrayList<UserInfo>();
		String sql = "select username from userinfo where id in (select friendid from relationship where userid="+user.getId()+")";
		try{
			Statement state = conn.createStatement();
			ResultSet set = state.executeQuery(sql);
			while(set.next()){
				String userName = set.getString("username");
				UserInfo friend = getUser(userName);
				list.add(friend);
			}
			return list;
		}catch(Exception e){
			e.printStackTrace();
		}
		return null;
	}
	/**
	 * �����������ô��û�״̬Ϊ����
	 * @param userName
	 */
	public static void setUserStateOnline(String userName){
		String sql = "update userinfo set userstate='b' where username='"+userName+"'";//b��ʾ�û���״̬Ϊ����
		try{
			Statement state = conn.createStatement();
			state.executeUpdate(sql);
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	/**
	 * �����������ô��û�״̬Ϊ����
	 * @param userName
	 */
	public static void setUserStateDownline(String userName){
		String sql = "update userinfo set userstate='a' where username='"+userName+"'";
		try{
			Statement state = conn.createStatement();
			state.executeUpdate(sql);
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	/**
	 * ���һ���¶���
	 */
	public static boolean addNewUser(UserInfo user){
		String sql = "insert into userinfo(username,userpwd,usersex,userage,userimage,userstate) value(?,?,?,?,?,?)";
		try{
			PreparedStatement pstate = conn.prepareStatement(sql);
			pstate.setString(1, user.getUserName());
			pstate.setString(2, user.getUserPwd());
			pstate.setString(3, user.getUserSex());
			pstate.setInt(4, user.getUserAge());
			pstate.setString(5, user.getUserImage());
			pstate.setString(6,user.getUserState());
			pstate.executeUpdate();
			return true;
		}catch(Exception e){
			e.printStackTrace();
			return false;
		}
	}
	
	public static ArrayList<String> getOnlineFriends(String userName){
		ArrayList<String> list = new ArrayList<String>();
		String sql = "select username from userinfo where userstate='b' and id in (" +
				"select friendid from relationship where userid=(" +
				"select id from userinfo where username='"+userName+"'))";
		try{
			Statement state = conn.createStatement();
			ResultSet set = state.executeQuery(sql);
			while(set.next()){
				list.add(set.getString("username"));
			}
			return list;
		}catch(Exception e){
			e.printStackTrace();
		}
		return null;
	}
}
