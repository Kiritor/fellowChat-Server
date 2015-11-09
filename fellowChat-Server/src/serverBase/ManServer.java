package serverBase;

import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

import javax.swing.JOptionPane;

import serverConfigure.ServerConfig;
import util.runninglog.RunningLog;
import util.tools.DataBaseControl;
import util.tools.TemporaryStorage;
import util.tools.Tools;


public class ManServer {
    public void startServer(int port){
    	try{
    		//�����ж������ļ����Ƿ�����ȷ����Ϣ����������������Ϣ
    		String [] data = Tools.getConfig();
    		if(data==null){
    			JOptionPane.showMessageDialog(null, "����û�ж����ݿ����ӽ������ã��������ã�");
    			ServerConfig con = new ServerConfig();
    			con.showUI();
    			return;
    		}
    		if(!DataBaseControl.buildConn(data[0],data[1])) return;
    		RunningLog.record("�����������ɹ�������");//���ݿ����ӽ����ɹ�
    		
            //�¿�һ��socket
    		ServerSocket ss = new ServerSocket(port);
    		
    		ServerUI serverUI = (ServerUI)TemporaryStorage.temporaryStorage.get("serverUI");
    		if(serverUI!=null){
    			serverUI.switchState();
    			serverUI.showIp(InetAddress.getLocalHost().getHostAddress().toString());
        		serverUI.showPort(""+ss.getLocalPort());
    		}
    		//��ѭ��������һֱ���ܿͻ��˵���Ϣ���󣨲�����Ϣ���й��˴Ӷ�������Ӧ�Ĳ�����
    		while(true){
    			Socket server = ss.accept();
    			ReceiveControl receive = new ReceiveControl(server);
    			receive.start();
    		}
    		
    		
    	}catch(Exception e){
    		e.printStackTrace();
    		JOptionPane.showMessageDialog(null, "�˿��ѱ�ռ�ã�����");
    		RunningLog.record("*****���󣺷���������ʧ�ܣ�����");
    	}
    }
//    public static void main(String args[]){
//    	ManServer m = new ManServer();
//    	m.startServer(8800);
//    }
}
