
/*信息接收的控制类
 * 对各种信息的分类控制*/

package serverBase;

import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;
import java.util.StringTokenizer;

import util.pojo.UserInfo;
import util.runninglog.RunningLog;
import util.tools.DataBaseControl;
import util.tools.Message;
import util.tools.Online;


public class ReceiveControl extends Thread {
	private boolean flag = true;
	private Socket server;
	
	private boolean firstTime = true;//设置是否是第一次的连接
	
	private String name;//登录名
	private String pwd;//密码

	public ReceiveControl(Socket server) {
		this.server = server;
	}

	public void run() {
		// 开始进行消息接收与处理操作
		try {
			while (flag) {
				//得到取得的消息
				String message = Message.readString(server.getInputStream());
				// 解析消息类型，根据类型作相应处理
				String type = Message.getXMLValue("type", message);

				try{
					if (type.equals("login")) {
						/**
						 * 对登录消息的操作
						 */
						name = Message.getXMLValue("userName", message);//解析用户名
						pwd = Message.getXMLValue("userPwd", message);//解析密码

						UserInfo user = DataBaseControl.getUser(name);

						if (user != null && user.getUserPwd().equals(pwd)&&!Online.onlineMap.containsKey(name)) {
							// 登录成功,并且在已经登陆的map中的用户中不包含该用户，则表明没有重复登陆
							String resp = "<type>loginresp</type><resp>yes</resp><loginner>"
									+ name + "</loginner><loginPwd>"+pwd+"</loginPwd>";
							Message.sendMsg(resp, server.getOutputStream());//输出给客户端信息

							// 加入到在线MAP,设置此用户状态为在线
							Online.add(name, server);
							DataBaseControl.setUserStateOnline(name);

							// 发送好友列表
							ArrayList<UserInfo> friendList = DataBaseControl
									.getFriendList(user);
							ObjectOutputStream out = new ObjectOutputStream(server
									.getOutputStream());
							Message.sendObject(friendList, out);//输出给客户端

							// 发送存储的离线消息给用户（离线消息存储在接受者的记录中）
							String downlineMsg = user.getUserDownlineMsg();
							if (downlineMsg != null) {
								while (downlineMsg.length() != 0) {
									String msgToSend = Message.getXMLValue("msg",
											downlineMsg);//解析离线消息
									Message.sendMsg(msgToSend, server
											.getOutputStream());//发送离线消息
									int index = downlineMsg.indexOf("</msg>");
									downlineMsg = downlineMsg.substring(index + 6);
									//将离线消息设置为原样的格式
								}
								//清空离线消息
								Message.deleteDownlineMsg(name);
							}

							// 发送上线通知给在线好友（这里别发给离线的好友）
							for (int i = 0; i < friendList.size(); i++) {
								if (friendList.get(i).getUserState().equals("b")) {
									String destination = friendList.get(i)
											.getUserName();
									String onlineMsg = "<type>online</type><sender>"
											+ name + "</sender>";
									Message.sendMsg(onlineMsg, Online.onlineMap
											.get(destination).getOutputStream());
								}
							}
							firstTime = false;//第一次登陆的处理结束
						} else {
							if(Online.onlineMap.containsKey(name)){
								//已有此用户在线
								String resp = "<type>loginresp</type><resp>online</resp>";
								Message.sendMsg(resp, server.getOutputStream());
							}else{
								// 登录失败
								String resp = "<type>loginresp</type><resp>no</resp>";
								Message.sendMsg(resp, server.getOutputStream());
							}
						}
					}
					
					if(type.equals("reg")){
						/**
						 * 对注册信息的处理
						 */
						//解析出注册信息
						String userName = Message.getXMLValue("userName", message);
						UserInfo user = DataBaseControl.getUser(userName);
						if(user==null){
							String userPwd = Message.getXMLValue("userPwd", message);
							String userSex = Message.getXMLValue("userSex", message);
							String userAge = Message.getXMLValue("userAge", message);
							String userImage = Message.getXMLValue("userImage", message);
							//存入数据库
							user = new UserInfo();
							user.setUserName(userName);
							user.setUserPwd(userPwd);
							user.setUserSex(userSex);
							user.setUserAge(Integer.parseInt(userAge));
							user.setUserImage(userImage);
							user.setUserState("a");
							
							if(DataBaseControl.addNewUser(user)){
								//注册成功，发送应答消息
								String regResp = "<type>regResp</type><resp>yes</resp>";
								Message.sendMsg(regResp, server.getOutputStream());
							}else{
								//注册失败，发送应答消息
								String regResp = "<type>regResp</type><resp>no</resp><reason>writeError</reason>";
								Message.sendMsg(regResp, server.getOutputStream());
							}
						}else{
							//已有此用户名
							String regResp = "<type>regResp</type><resp>no</resp><reason>exist</reason>";
							Message.sendMsg(regResp, server.getOutputStream());
						}
					}

					if (type.equals("chat")) {
						/**
						 * 对聊天信息的处理
						 */
						// 先解析出目的用户
						String destination = Message.getXMLValue("destination",
								message);
						// 判断目的用户是否在线并进行相应处理
						if (Online.onlineMap.containsKey(destination)) {
							// 如果在线则直接转发消息
							Message.sendMsg(message.substring(5,
									message.length() - 6), Online.onlineMap.get(
									destination).getOutputStream());
						} else {
							// 如果不在线则将消息存入目的用户的downlineMsg内
							Message.saveDownlineMsg(message, destination);
						}
					}

					if (type.equals("viewRequest")) {
						/**
						 * 对视频请求的处理
						 */
						// 解析出目的用户和发送者
						String sender = Message.getXMLValue("sender", message);
						String destination = Message.getXMLValue("destination",
								message);
						// 判断用户是否在线
						if (Online.onlineMap.containsKey(destination)) {
							// 在线
							// 得到请求者的IP，添加后转发
							String senderIP = Online.onlineMap.get(sender)
									.getInetAddress().toString().substring(1);
							message = message.substring(5, message.length() - 6)
									+ "<ip>" + senderIP + "</ip>";
							Message.sendMsg(message, Online.onlineMap.get(
									destination).getOutputStream());

						} else {
							// 不在线的话就通知发送者
							String viewResp = "<type>viewResponse</type><sender>"
									+ destination + "</sender><destination>"
									+ sender
									+ "</destination><resp>notOnline</resp>";
							Message.sendMsg(viewResp, Online.onlineMap.get(sender)
									.getOutputStream());
						}
					}

					if (type.equals("viewResponse")) {
						/**
						 * 对视频应答的处理
						 */
						// 解析出发送者和目的地
						String sender = Message.getXMLValue("sender", message);
						String destination = Message.getXMLValue("destination",
								message);
						String viewResp = Message.getXMLValue("resp", message);
						if (viewResp.equals("no")) {
							// 直接转发
							Message.sendMsg(message.substring(5,
									message.length() - 6), Online.onlineMap.get(
									destination).getOutputStream());
						}
						if (viewResp.equals("yes")) {
							// 解析出发送者的IP
							String senderIP = Online.onlineMap.get(sender)
									.getInetAddress().toString().substring(1);
							message = message.substring(5, message.length() - 6)
									+ "<ip>" + senderIP + "</ip>";
							Message.sendMsg(message, Online.onlineMap.get(
									destination).getOutputStream());
						}
					}

					if (type.equals("viewBreak")) {
						/**
						 * 对视频断开通知的处理
						 */
						// 解析出目的用户
						String destination = Message.getXMLValue("destination",
								message);
						// 直接转发
						Message
								.sendMsg(
										message.substring(5, message.length() - 6),
										Online.onlineMap.get(destination)
												.getOutputStream());
					}

					if (type.equals("fileRequest")) {
						/**
						 * 对文件传输请求的处理
						 */
						// 解析出目的用户
						String destination = Message.getXMLValue("destination",
								message);
						if(Online.onlineMap.containsKey(destination)){
							// 在线，直接转发
							Message
									.sendMsg(
											message.substring(5, message.length() - 6),
											Online.onlineMap.get(destination)
													.getOutputStream());
						}else{
							//不在线
							String fileResponse = "<type>fileResponse</type><sender>"+destination+"</sender><resp>notOnline</resp>";
							Message.sendMsg(fileResponse, server.getOutputStream());
						}
					}
					if (type.equals("fileResponse")) {
						/**
						 * 对文件传输应答的处理
						 */
						String sender = Message.getXMLValue("sender", message);
						String destination = Message.getXMLValue("destination",
								message);
						String fileResp = Message.getXMLValue("resp", message);
						if (fileResp.equals("yes")) {
							// 得到sender的ip
							String senderIP = Online.onlineMap.get(sender)
									.getInetAddress().toString().substring(1);
							String str = message.substring(5, message.length() - 6)
									+ "<ip>" + senderIP + "</ip>";
							Message.sendMsg(str, Online.onlineMap.get(destination)
									.getOutputStream());
						} else {
							// 直接转发
							Message.sendMsg(message.substring(5,
									message.length() - 6), Online.onlineMap.get(
									destination).getOutputStream());
						}
					}

					if (type.equals("remoteRequest")) {
						/**
						 * 对远程监控请求的处理
						 */
						String sender = Message.getXMLValue("sender", message);
						String destination = Message.getXMLValue("destination",
								message);
						// 判断目的用户是否在线
						if (Online.onlineMap.containsKey(destination)) {
							// 在线,直接转发
							Message.sendMsg(message.substring(5,
									message.length() - 6), Online.onlineMap.get(
									destination).getOutputStream());
						} else {
							// 不在线
							String remoteResp = "<type>remoteResponse</type><sender>"
									+ destination
									+ "</sender><destination>"
									+ sender
									+ "</destination><resp>notOnline</resp>";
							Message.sendMsg(remoteResp, Online.onlineMap
									.get(sender).getOutputStream());
						}
					}

					if (type.equals("remoteResponse")) {
						/**
						 * 对远程监控应答的处理
						 */
						String sender = Message.getXMLValue("sender", message);
						String destination = Message.getXMLValue("destination",
								message);
						String remoteResp = Message.getXMLValue("resp", message);
						if (remoteResp.equals("yes")) {
							// 得到sender的IP
							String senderIP = Online.onlineMap.get(sender)
									.getInetAddress().toString().substring(1);
							message = message.substring(5, message.length() - 6)
									+ "<ip>" + senderIP + "</ip>";
							Message.sendMsg(message, Online.onlineMap.get(
									destination).getOutputStream());
						} else {
							// 直接转发
							Message.sendMsg(message.substring(5,
									message.length() - 6), Online.onlineMap.get(
									destination).getOutputStream());
						}
					}

					if (type.equals("remoteBreak")) {
						/**
						 * 对远程监控断开通知的处理
						 */
						// 直接转发
						String destination = Message.getXMLValue("destination",
								message);
						Message
								.sendMsg(
										message.substring(5, message.length() - 6),
										Online.onlineMap.get(destination)
												.getOutputStream());
					}
					
					if(type.equals("directSearch")){
						/**
						 * 对精确查找请求的处理
						 */
						//解析出需查找的用户名
						String userName = Message.getXMLValue("userName", message);
						//查找用户
						UserInfo user = DataBaseControl.getUser(userName);
						//回复
						String result = null;
						if(user!=null){
							result = "<type>directSearchResult</type><result>yes</result><userName>"+user.getUserName()+"</userName><userSex>"+user.getUserSex()+"</userSex><userAge>"+user.getUserAge()+"</userAge><userImage>"+user.getUserImage()+"</userImage>";
						}else{
							result = "<type>directSearchResult</type><result>no</result>";
						}
						Message.sendMsg(result, server.getOutputStream());
					}
					
				
					if(type.equals("randomSearch")){
						/**
						 * 对随机查找请求的处理
						 */
						System.out.println("水机差早");
						String index = Message.getXMLValue("index", message);
						String sender = Message.getXMLValue("sender", message);
						//从所有在线用户中取5个
						ArrayList<UserInfo> list = DataBaseControl.getOnlineUser(Integer.parseInt(index),sender);
						String result = "";
						if(list.size()>0){
							for(int i=0;i<list.size();i++){
								result += list.get(i).getUserImage()+","+list.get(i).getUserName()+","+list.get(i).getUserSex()+","+list.get(i).getUserAge()+"|";
							}
							result = "<type>randomSearchResult</type><result>yes</result><info>"+result+"</info>";
						}else{
							result = "<type>randomSearchResult</type><result>no</result>";
						}
						Message.sendMsg(result, server.getOutputStream());
					}
					
					if(type.equals("addRequest")){
						/**
						 * 对添加好友信息的处理
						 */
						String destination = Message.getXMLValue("destination", message);
						//判断对方是否在线
						if(Online.onlineMap.containsKey(destination)){
							//在线，直接转发
							Message.sendMsg(message.substring(5,message.length()-6), Online.onlineMap.get(destination).getOutputStream());
						}else{
							//不在线，存入消息盒子
							Message.saveDownlineMsg(message, destination);
						}
					}
					
					if(type.equals("addResponse")){
						/**
						 * 对添加好友应答信息的处理
						 */
						String sender = Message.getXMLValue("sender", message);
						String destination = Message.getXMLValue("destination", message);
						String addResp = Message.getXMLValue("resp", message);
						if(addResp.equals("yes")){
							//同意添加
							if(DataBaseControl.addFriendShip(sender, destination)){
								//写入数据库后先判断对方在不在线
								if(Online.onlineMap.containsKey(destination)){
									//在线
									UserInfo user = DataBaseControl.getUser(sender);
									message = message.substring(5,message.length()-6)+"<info>"+user.getUserName()+","+user.getUserImage()+","+user.getUserSex()+","+user.getUserState()+"</info>";
									Message.sendMsg(message, Online.onlineMap.get(destination).getOutputStream());
								}else{
									//不在线，存入消息盒子
									message = "<msg>"+message.substring(5,message.length()-6)+"<info></info></msg>";
									Message.saveDownlineMsg(message, destination);
								}
								UserInfo user = DataBaseControl.getUser(destination);
								String resp = "<type>addResponse</type><sender>"+destination+"</sender><destination>"+sender+"</destination><resp>yes</resp><info>"+user.getUserName()+","+user.getUserImage()+","+user.getUserSex()+","+user.getUserState()+"</info>";
								Message.sendMsg(resp, server.getOutputStream());
							}
						}else{
							//不同意
							if(Online.onlineMap.containsKey(destination)){
								//在线
								Message.sendMsg(message.substring(5,message.length()-6), Online.onlineMap.get(destination).getOutputStream());
							}else{
								//不在线
								Message.saveDownlineMsg(message, destination);
							}
						}
					}
					
					if(type.equals("delete")){
						/**
						 * 对删除好友信息的处理
						 */
						String sender = Message.getXMLValue("sender", message);
						String destination = Message.getXMLValue("destination", message);
						if(DataBaseControl.destroyFriendship(sender, destination)){
							//判断对方是否在线
							if(Online.onlineMap.containsKey(destination)){
								//转发消息
								Message.sendMsg(message.substring(5,message.length()-6), Online.onlineMap.get(destination).getOutputStream());
							}
						}
					}

					if (type.equals("leave")) {
						/**
						 * 对用户离线通知的处理
						 */
						String sender = Message.getXMLValue("sender", message);
						String destinations = Message.getXMLValue("destinations",
								message);
						Online.remove(sender);
						DataBaseControl.setUserStateDownline(sender);
						// 逐个转发通知
						StringTokenizer token = new StringTokenizer(destinations,
								",");
						while (token.hasMoreTokens()) {
							String destination = token.nextToken();
							String leaveMsg = "<type>leave</type><sender>" + sender
									+ "</sender>";
							Message.sendMsg(leaveMsg, Online.onlineMap.get(
									destination).getOutputStream());
						}
						break;
					}
				}catch(Exception e){
					e.printStackTrace();
				}
			}
		} catch (Exception e) {
			//用户与服务器断开连接
			if(firstTime){
				//登录前
				//不做任何操作
			}else{
				//登录后
				Online.remove(name);
				DataBaseControl.setUserStateDownline(name);
				ArrayList<String> list = DataBaseControl.getOnlineFriends(name);
				if(list.size()>0){
					try{
						for(String userName : list){
							String leaveMsg = "<type>leave</type><sender>" + name
							+ "</sender>";
							if(Online.onlineMap.get(userName)!=null){
								Message.sendMsg(leaveMsg, Online.onlineMap.get(
										userName).getOutputStream());
							}
						}
					}catch(Exception ef){
						ef.printStackTrace();
					}
				}
			}
		}
	}
}
