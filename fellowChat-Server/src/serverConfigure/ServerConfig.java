
/*
 * 更改服务器配置的后台*/
package serverConfigure;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import com.jtattoo.plaf.mcwin.McWinLookAndFeel;

import util.tools.Tools;

public class ServerConfig {

	private JFrame frame;//窗体
	private JLabel ipLabel;//数据库名标签
	private JTextField ipText;//数据库域
	private JLabel portLabel;//密码标签
	private JTextField portText;//密码口域
	private JButton button;
	
	public void showUI(){
		frame = new JFrame("乡友服务器配置");
		frame.setLayout(new FlowLayout());
		
		String[] data = Tools.getConfig();
		
		ipLabel = new JLabel("数据库账号：");
		frame.add(ipLabel);
		ipText = new JTextField(10);
		//设置数据库名标签（同时也可以读取和修改）
		if(data!=null&&data[0]!=null&&data[0].length()>0){
			ipText.setText(data[0]);
		}
		frame.add(ipText);
		portLabel = new JLabel("数据库密码：");
		frame.add(portLabel);
		portText = new JTextField(10);
		//设置数据库密码
		if(data!=null&&data[1]!=null&&data[1].length()>0){
			portText.setText(data[1]);
		}
		frame.add(portText);
		button = new JButton("确认");
		button.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				String ip = ipText.getText();
				String port = portText.getText();
				if(ip!=null&&port!=null&&ip.length()>0&&port.length()>0){
					ExecuteCfg execute = new ExecuteCfg(ip,port);
					execute.start();
				}else{
					JOptionPane.showMessageDialog(null, "您的输入有误！！！");
				}
			}
		});
		frame.add(button);
		
		frame.pack();
		frame.setVisible(true);
		frame.setResizable(false);//窗体大小不能调整
		frame.setLocationRelativeTo(null);//默认屏幕的正中间
		frame.setDefaultCloseOperation(3);
	}
	
	private class ExecuteCfg extends Thread{
		private String ip;
		private String port;
		private File file = new File("serverConfigure.cfg");
		
		public ExecuteCfg(String ip,String port){
			this.ip = ip;
			this.port = port;
		}
		//重写run方法
		public void run(){
			try{
				FileOutputStream fout = new FileOutputStream(file);
				BufferedOutputStream bout = new BufferedOutputStream(fout);
				String str = ip+" "+port;
				bout.write(str.getBytes());
				bout.flush();
				bout.close();
				
				JOptionPane.showMessageDialog(null, "配置成功！！！");
				frame.dispose();
				
			}catch(Exception e){
				e.printStackTrace();
			}
		}
	}
	
	public static void main(String args[]){
		

		 try {

		 UIManager.setLookAndFeel(new McWinLookAndFeel());

		 } catch (UnsupportedLookAndFeelException e1) {

		 // TODO Auto-generated catch block

		 e1.printStackTrace();

		 }
		ServerConfig con = new ServerConfig();
		con.showUI();
	}
}
