
/*��Ϣ���յĿ�����
 * �Ը�����Ϣ�ķ������*/

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
	
	private boolean firstTime = true;//�����Ƿ��ǵ�һ�ε�����
	
	private String name;//��¼��
	private String pwd;//����

	public ReceiveControl(Socket server) {
		this.server = server;
	}

	public void run() {
		// ��ʼ������Ϣ�����봦�����
		try {
			while (flag) {
				//�õ�ȡ�õ���Ϣ
				String message = Message.readString(server.getInputStream());
				// ������Ϣ���ͣ�������������Ӧ����
				String type = Message.getXMLValue("type", message);

				try{
					if (type.equals("login")) {
						/**
						 * �Ե�¼��Ϣ�Ĳ���
						 */
						name = Message.getXMLValue("userName", message);//�����û���
						pwd = Message.getXMLValue("userPwd", message);//��������

						UserInfo user = DataBaseControl.getUser(name);

						if (user != null && user.getUserPwd().equals(pwd)&&!Online.onlineMap.containsKey(name)) {
							// ��¼�ɹ�,�������Ѿ���½��map�е��û��в��������û��������û���ظ���½
							String resp = "<type>loginresp</type><resp>yes</resp><loginner>"
									+ name + "</loginner><loginPwd>"+pwd+"</loginPwd>";
							Message.sendMsg(resp, server.getOutputStream());//������ͻ�����Ϣ

							// ���뵽����MAP,���ô��û�״̬Ϊ����
							Online.add(name, server);
							DataBaseControl.setUserStateOnline(name);

							// ���ͺ����б�
							ArrayList<UserInfo> friendList = DataBaseControl
									.getFriendList(user);
							ObjectOutputStream out = new ObjectOutputStream(server
									.getOutputStream());
							Message.sendObject(friendList, out);//������ͻ���

							// ���ʹ洢��������Ϣ���û���������Ϣ�洢�ڽ����ߵļ�¼�У�
							String downlineMsg = user.getUserDownlineMsg();
							if (downlineMsg != null) {
								while (downlineMsg.length() != 0) {
									String msgToSend = Message.getXMLValue("msg",
											downlineMsg);//����������Ϣ
									Message.sendMsg(msgToSend, server
											.getOutputStream());//����������Ϣ
									int index = downlineMsg.indexOf("</msg>");
									downlineMsg = downlineMsg.substring(index + 6);
									//��������Ϣ����Ϊԭ���ĸ�ʽ
								}
								//���������Ϣ
								Message.deleteDownlineMsg(name);
							}

							// ��������֪ͨ�����ߺ��ѣ�����𷢸����ߵĺ��ѣ�
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
							firstTime = false;//��һ�ε�½�Ĵ������
						} else {
							if(Online.onlineMap.containsKey(name)){
								//���д��û�����
								String resp = "<type>loginresp</type><resp>online</resp>";
								Message.sendMsg(resp, server.getOutputStream());
							}else{
								// ��¼ʧ��
								String resp = "<type>loginresp</type><resp>no</resp>";
								Message.sendMsg(resp, server.getOutputStream());
							}
						}
					}
					
					if(type.equals("reg")){
						/**
						 * ��ע����Ϣ�Ĵ���
						 */
						//������ע����Ϣ
						String userName = Message.getXMLValue("userName", message);
						UserInfo user = DataBaseControl.getUser(userName);
						if(user==null){
							String userPwd = Message.getXMLValue("userPwd", message);
							String userSex = Message.getXMLValue("userSex", message);
							String userAge = Message.getXMLValue("userAge", message);
							String userImage = Message.getXMLValue("userImage", message);
							//�������ݿ�
							user = new UserInfo();
							user.setUserName(userName);
							user.setUserPwd(userPwd);
							user.setUserSex(userSex);
							user.setUserAge(Integer.parseInt(userAge));
							user.setUserImage(userImage);
							user.setUserState("a");
							
							if(DataBaseControl.addNewUser(user)){
								//ע��ɹ�������Ӧ����Ϣ
								String regResp = "<type>regResp</type><resp>yes</resp>";
								Message.sendMsg(regResp, server.getOutputStream());
							}else{
								//ע��ʧ�ܣ�����Ӧ����Ϣ
								String regResp = "<type>regResp</type><resp>no</resp><reason>writeError</reason>";
								Message.sendMsg(regResp, server.getOutputStream());
							}
						}else{
							//���д��û���
							String regResp = "<type>regResp</type><resp>no</resp><reason>exist</reason>";
							Message.sendMsg(regResp, server.getOutputStream());
						}
					}

					if (type.equals("chat")) {
						/**
						 * ��������Ϣ�Ĵ���
						 */
						// �Ƚ�����Ŀ���û�
						String destination = Message.getXMLValue("destination",
								message);
						// �ж�Ŀ���û��Ƿ����߲�������Ӧ����
						if (Online.onlineMap.containsKey(destination)) {
							// ���������ֱ��ת����Ϣ
							Message.sendMsg(message.substring(5,
									message.length() - 6), Online.onlineMap.get(
									destination).getOutputStream());
						} else {
							// �������������Ϣ����Ŀ���û���downlineMsg��
							Message.saveDownlineMsg(message, destination);
						}
					}

					if (type.equals("viewRequest")) {
						/**
						 * ����Ƶ����Ĵ���
						 */
						// ������Ŀ���û��ͷ�����
						String sender = Message.getXMLValue("sender", message);
						String destination = Message.getXMLValue("destination",
								message);
						// �ж��û��Ƿ�����
						if (Online.onlineMap.containsKey(destination)) {
							// ����
							// �õ������ߵ�IP����Ӻ�ת��
							String senderIP = Online.onlineMap.get(sender)
									.getInetAddress().toString().substring(1);
							message = message.substring(5, message.length() - 6)
									+ "<ip>" + senderIP + "</ip>";
							Message.sendMsg(message, Online.onlineMap.get(
									destination).getOutputStream());

						} else {
							// �����ߵĻ���֪ͨ������
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
						 * ����ƵӦ��Ĵ���
						 */
						// �����������ߺ�Ŀ�ĵ�
						String sender = Message.getXMLValue("sender", message);
						String destination = Message.getXMLValue("destination",
								message);
						String viewResp = Message.getXMLValue("resp", message);
						if (viewResp.equals("no")) {
							// ֱ��ת��
							Message.sendMsg(message.substring(5,
									message.length() - 6), Online.onlineMap.get(
									destination).getOutputStream());
						}
						if (viewResp.equals("yes")) {
							// �����������ߵ�IP
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
						 * ����Ƶ�Ͽ�֪ͨ�Ĵ���
						 */
						// ������Ŀ���û�
						String destination = Message.getXMLValue("destination",
								message);
						// ֱ��ת��
						Message
								.sendMsg(
										message.substring(5, message.length() - 6),
										Online.onlineMap.get(destination)
												.getOutputStream());
					}

					if (type.equals("fileRequest")) {
						/**
						 * ���ļ���������Ĵ���
						 */
						// ������Ŀ���û�
						String destination = Message.getXMLValue("destination",
								message);
						if(Online.onlineMap.containsKey(destination)){
							// ���ߣ�ֱ��ת��
							Message
									.sendMsg(
											message.substring(5, message.length() - 6),
											Online.onlineMap.get(destination)
													.getOutputStream());
						}else{
							//������
							String fileResponse = "<type>fileResponse</type><sender>"+destination+"</sender><resp>notOnline</resp>";
							Message.sendMsg(fileResponse, server.getOutputStream());
						}
					}
					if (type.equals("fileResponse")) {
						/**
						 * ���ļ�����Ӧ��Ĵ���
						 */
						String sender = Message.getXMLValue("sender", message);
						String destination = Message.getXMLValue("destination",
								message);
						String fileResp = Message.getXMLValue("resp", message);
						if (fileResp.equals("yes")) {
							// �õ�sender��ip
							String senderIP = Online.onlineMap.get(sender)
									.getInetAddress().toString().substring(1);
							String str = message.substring(5, message.length() - 6)
									+ "<ip>" + senderIP + "</ip>";
							Message.sendMsg(str, Online.onlineMap.get(destination)
									.getOutputStream());
						} else {
							// ֱ��ת��
							Message.sendMsg(message.substring(5,
									message.length() - 6), Online.onlineMap.get(
									destination).getOutputStream());
						}
					}

					if (type.equals("remoteRequest")) {
						/**
						 * ��Զ�̼������Ĵ���
						 */
						String sender = Message.getXMLValue("sender", message);
						String destination = Message.getXMLValue("destination",
								message);
						// �ж�Ŀ���û��Ƿ�����
						if (Online.onlineMap.containsKey(destination)) {
							// ����,ֱ��ת��
							Message.sendMsg(message.substring(5,
									message.length() - 6), Online.onlineMap.get(
									destination).getOutputStream());
						} else {
							// ������
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
						 * ��Զ�̼��Ӧ��Ĵ���
						 */
						String sender = Message.getXMLValue("sender", message);
						String destination = Message.getXMLValue("destination",
								message);
						String remoteResp = Message.getXMLValue("resp", message);
						if (remoteResp.equals("yes")) {
							// �õ�sender��IP
							String senderIP = Online.onlineMap.get(sender)
									.getInetAddress().toString().substring(1);
							message = message.substring(5, message.length() - 6)
									+ "<ip>" + senderIP + "</ip>";
							Message.sendMsg(message, Online.onlineMap.get(
									destination).getOutputStream());
						} else {
							// ֱ��ת��
							Message.sendMsg(message.substring(5,
									message.length() - 6), Online.onlineMap.get(
									destination).getOutputStream());
						}
					}

					if (type.equals("remoteBreak")) {
						/**
						 * ��Զ�̼�ضϿ�֪ͨ�Ĵ���
						 */
						// ֱ��ת��
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
						 * �Ծ�ȷ��������Ĵ���
						 */
						//����������ҵ��û���
						String userName = Message.getXMLValue("userName", message);
						//�����û�
						UserInfo user = DataBaseControl.getUser(userName);
						//�ظ�
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
						 * �������������Ĵ���
						 */
						System.out.println("ˮ������");
						String index = Message.getXMLValue("index", message);
						String sender = Message.getXMLValue("sender", message);
						//�����������û���ȡ5��
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
						 * ����Ӻ�����Ϣ�Ĵ���
						 */
						String destination = Message.getXMLValue("destination", message);
						//�ж϶Է��Ƿ�����
						if(Online.onlineMap.containsKey(destination)){
							//���ߣ�ֱ��ת��
							Message.sendMsg(message.substring(5,message.length()-6), Online.onlineMap.get(destination).getOutputStream());
						}else{
							//�����ߣ�������Ϣ����
							Message.saveDownlineMsg(message, destination);
						}
					}
					
					if(type.equals("addResponse")){
						/**
						 * ����Ӻ���Ӧ����Ϣ�Ĵ���
						 */
						String sender = Message.getXMLValue("sender", message);
						String destination = Message.getXMLValue("destination", message);
						String addResp = Message.getXMLValue("resp", message);
						if(addResp.equals("yes")){
							//ͬ�����
							if(DataBaseControl.addFriendShip(sender, destination)){
								//д�����ݿ�����ж϶Է��ڲ�����
								if(Online.onlineMap.containsKey(destination)){
									//����
									UserInfo user = DataBaseControl.getUser(sender);
									message = message.substring(5,message.length()-6)+"<info>"+user.getUserName()+","+user.getUserImage()+","+user.getUserSex()+","+user.getUserState()+"</info>";
									Message.sendMsg(message, Online.onlineMap.get(destination).getOutputStream());
								}else{
									//�����ߣ�������Ϣ����
									message = "<msg>"+message.substring(5,message.length()-6)+"<info></info></msg>";
									Message.saveDownlineMsg(message, destination);
								}
								UserInfo user = DataBaseControl.getUser(destination);
								String resp = "<type>addResponse</type><sender>"+destination+"</sender><destination>"+sender+"</destination><resp>yes</resp><info>"+user.getUserName()+","+user.getUserImage()+","+user.getUserSex()+","+user.getUserState()+"</info>";
								Message.sendMsg(resp, server.getOutputStream());
							}
						}else{
							//��ͬ��
							if(Online.onlineMap.containsKey(destination)){
								//����
								Message.sendMsg(message.substring(5,message.length()-6), Online.onlineMap.get(destination).getOutputStream());
							}else{
								//������
								Message.saveDownlineMsg(message, destination);
							}
						}
					}
					
					if(type.equals("delete")){
						/**
						 * ��ɾ��������Ϣ�Ĵ���
						 */
						String sender = Message.getXMLValue("sender", message);
						String destination = Message.getXMLValue("destination", message);
						if(DataBaseControl.destroyFriendship(sender, destination)){
							//�ж϶Է��Ƿ�����
							if(Online.onlineMap.containsKey(destination)){
								//ת����Ϣ
								Message.sendMsg(message.substring(5,message.length()-6), Online.onlineMap.get(destination).getOutputStream());
							}
						}
					}

					if (type.equals("leave")) {
						/**
						 * ���û�����֪ͨ�Ĵ���
						 */
						String sender = Message.getXMLValue("sender", message);
						String destinations = Message.getXMLValue("destinations",
								message);
						Online.remove(sender);
						DataBaseControl.setUserStateDownline(sender);
						// ���ת��֪ͨ
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
			//�û���������Ͽ�����
			if(firstTime){
				//��¼ǰ
				//�����κβ���
			}else{
				//��¼��
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
