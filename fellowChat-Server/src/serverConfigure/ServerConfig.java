
/*
 * ���ķ��������õĺ�̨*/
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

	private JFrame frame;//����
	private JLabel ipLabel;//���ݿ�����ǩ
	private JTextField ipText;//���ݿ���
	private JLabel portLabel;//�����ǩ
	private JTextField portText;//�������
	private JButton button;
	
	public void showUI(){
		frame = new JFrame("���ѷ���������");
		frame.setLayout(new FlowLayout());
		
		String[] data = Tools.getConfig();
		
		ipLabel = new JLabel("���ݿ��˺ţ�");
		frame.add(ipLabel);
		ipText = new JTextField(10);
		//�������ݿ�����ǩ��ͬʱҲ���Զ�ȡ���޸ģ�
		if(data!=null&&data[0]!=null&&data[0].length()>0){
			ipText.setText(data[0]);
		}
		frame.add(ipText);
		portLabel = new JLabel("���ݿ����룺");
		frame.add(portLabel);
		portText = new JTextField(10);
		//�������ݿ�����
		if(data!=null&&data[1]!=null&&data[1].length()>0){
			portText.setText(data[1]);
		}
		frame.add(portText);
		button = new JButton("ȷ��");
		button.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				String ip = ipText.getText();
				String port = portText.getText();
				if(ip!=null&&port!=null&&ip.length()>0&&port.length()>0){
					ExecuteCfg execute = new ExecuteCfg(ip,port);
					execute.start();
				}else{
					JOptionPane.showMessageDialog(null, "�����������󣡣���");
				}
			}
		});
		frame.add(button);
		
		frame.pack();
		frame.setVisible(true);
		frame.setResizable(false);//�����С���ܵ���
		frame.setLocationRelativeTo(null);//Ĭ����Ļ�����м�
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
		//��дrun����
		public void run(){
			try{
				FileOutputStream fout = new FileOutputStream(file);
				BufferedOutputStream bout = new BufferedOutputStream(fout);
				String str = ip+" "+port;
				bout.write(str.getBytes());
				bout.flush();
				bout.close();
				
				JOptionPane.showMessageDialog(null, "���óɹ�������");
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
