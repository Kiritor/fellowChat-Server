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
    		//首先判断配置文件中是否有正确的信息，否者重新配置信息
    		String [] data = Tools.getConfig();
    		if(data==null){
    			JOptionPane.showMessageDialog(null, "您还没有对数据库连接进行配置，请先配置！");
    			ServerConfig con = new ServerConfig();
    			con.showUI();
    			return;
    		}
    		if(!DataBaseControl.buildConn(data[0],data[1])) return;
    		RunningLog.record("服务器启动成功！！！");//数据库连接建立成功
    		
            //新开一个socket
    		ServerSocket ss = new ServerSocket(port);
    		
    		ServerUI serverUI = (ServerUI)TemporaryStorage.temporaryStorage.get("serverUI");
    		if(serverUI!=null){
    			serverUI.switchState();
    			serverUI.showIp(InetAddress.getLocalHost().getHostAddress().toString());
        		serverUI.showPort(""+ss.getLocalPort());
    		}
    		//死循环，用于一直接受客户端的信息请求（并对信息进行过滤从而进行相应的操作）
    		while(true){
    			Socket server = ss.accept();
    			ReceiveControl receive = new ReceiveControl(server);
    			receive.start();
    		}
    		
    		
    	}catch(Exception e){
    		e.printStackTrace();
    		JOptionPane.showMessageDialog(null, "端口已被占用！！！");
    		RunningLog.record("*****错误：服务器启动失败！！！");
    	}
    }
//    public static void main(String args[]){
//    	ManServer m = new ManServer();
//    	m.startServer(8800);
//    }
}
