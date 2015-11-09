package serverBase;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.RoundRectangle2D;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import org.jvnet.substance.SubstanceLookAndFeel;
import org.jvnet.substance.theme.SubstanceTerracottaTheme;

import com.jtattoo.plaf.mcwin.McWinLookAndFeel;
import com.sun.awt.AWTUtilities;

import serverBase.ManServer;
import util.tools.TemporaryStorage;

public class ServerUI {
	
	private ImageIcon yellow = new ImageIcon("image/stop.png");
	private ImageIcon green = new ImageIcon("image/start.png");

	private JFrame frame;
	
	private JPanel panel1;
	private JPanel panel2;
	private JPanel panel3;
	
	private JLabel state;
	private JLabel stateLight;
	private JLabel serverIp;
	private JLabel serverPort;
	private JButton start;
	private JButton stop;
	
	public void showIp(String ip){
		serverIp = new JLabel("������IP : "+ip);
		serverIp.setHorizontalAlignment(0);
		panel2.add(serverIp);
		panel2.updateUI();
	}
	
	public void showPort(String port){
		serverPort = new JLabel("������Port : "+port);
		serverPort.setHorizontalAlignment(0);
		panel2.add(serverPort);
		panel2.updateUI();
	}
	/*�ı�״̬*/
	public void switchState(){
		stateLight.setIcon(green);
		stateLight.setText("������");
		start.setEnabled(false);
		stop.setEnabled(true);
		panel1.updateUI();
	}
	
	public void showUI(){
		frame = new JFrame("���ѷ�����");
		frame.setSize(300, 200);
		frame.setLayout(new GridLayout(3,1));
		frame.getContentPane().setForeground(Color.red);
		/*ʹ����ʵ��Բ�ǵ�Ч��*/
		AWTUtilities.setWindowShape(frame,  
			           new RoundRectangle2D.Double(0.0D, 0.0D, frame.getWidth(),  
			               frame.getHeight(), 18.0D, 18.0D));  
		panel1 = new JPanel(new FlowLayout());
		state = new JLabel("������״̬: ");
		stateLight = new JLabel("δ����",yellow,0);
		panel1.add(state);
		panel1.add(stateLight);
		frame.add(panel1);
		
		panel2 = new JPanel(new GridLayout(2,1));
		frame.add(panel2);
		
		panel3 = new JPanel(new FlowLayout());
		start =  new JButton("����������");
		start.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				StartServer s = new StartServer();
				s.start(); 
			}
		});
		panel3.add(start);
		stop = new JButton("�˳�����");
		stop.setEnabled(false);
		stop.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				int option = JOptionPane.showOptionDialog(null, "��ȷ��Ҫ�˳���������", "�رշ�����", JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE, null, null, null);
				if(option==0){
					System.exit(0);
				}
			}
		});
		panel3.add(stop);
		frame.add(panel3);
		
		frame.setVisible(true);
		frame.setDefaultCloseOperation(3);
		frame.setLocationRelativeTo(null);
		
		TemporaryStorage.storeObject("serverUI", this);
	}
	
	private class StartServer extends Thread{
		public void run(){
			ManServer server = new ManServer();
			server.startServer(8800);
		}
	}
	
	public static void main(String args[]){

		 try {

		 UIManager.setLookAndFeel(new McWinLookAndFeel());

		 } catch (UnsupportedLookAndFeelException e1) {

		 // TODO Auto-generated catch block

		 e1.printStackTrace();

		 }
		
		ServerUI ui = new ServerUI();
		ui.showUI();
	}
}
