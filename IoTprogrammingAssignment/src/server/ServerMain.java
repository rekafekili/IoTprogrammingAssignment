package server;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.List;
import java.awt.*;
import javax.swing.*;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.xml.sax.*;

class ServerMain extends JFrame
{
	private ServerSocket srvSocket; // ��������
	private Socket socket; // Ŭ���̾�Ʈ�� ������ ���� ����
	private Vector client=new Vector(); // Ŭ���̾�Ʈ���� ��� ���� ����
	private JTextArea textArea; // ���� ���¸� ����ϱ� ���� �ʵ�
	private JScrollPane sp; // ��ũ���� ���� ���
	private Image icon; // ������
	private JLabel logo; // �ΰ�


    public ServerMain() // ������
	{
		super("ä�����α׷�");
		textArea=new JTextArea();
		sp=new JScrollPane(textArea);
		logo=new JLabel(new ImageIcon("./image/serverlogo.jpg"));
		
		getContentPane().setLayout(new BorderLayout());
		getContentPane().add(sp,"Center");
		getContentPane().add(logo,"South");
		icon=getToolkit().getImage("./image/icon.gif");
		textArea.setEditable(false);

		this.setIconImage(icon);
		this.setVisible(true);
		this.setSize(450,600);
		this.setLocation(100,80);
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
		
		try{
			srvSocket=new ServerSocket(2848); // ���� ������ ����
			textArea.append("ä�����α׷�\n");
			textArea.append("Ip Adress : "+"Localhost"+"\n"); // ������ ������ ǥ��
			textArea.append("Port Num  : "+srvSocket.getLocalPort()+"\n");
			
			while(true)
			{
				socket=srvSocket.accept(); // ���ο� Ŭ���̾�Ʈ�� ����
				User usr=new User(socket); // ���ο� Ŭ���̾�Ʈ�� ���� User �����带 ����
				usr.start();
				addUser(usr); // ���Ϳ� ���ο� Ŭ���̾�Ʈ�� ����
			}
		}catch(IOException ex){
			System.out.println(ex);
		}
	}

	public void addUser(User usr) // ���Ϳ� Ŭ���̾�Ʈ�� �߰� ��Ű�� �޼ҵ�
	{
		client.addElement(usr);
	}

	public void removeUser(User usr) // ���Ϳ��� Ŭ���̾�Ʈ�� ���� ��Ű�� �޼ҵ�
	{
		client.removeElement(usr);
	}

	public void send(String msg) // ��� Ŭ���̾�Ʈ�鿡�� �޼����� ����
	{
		for(int i=0;i<client.size();i++)
		{
			User usr=((User)client.elementAt(i));
			usr.sendMessage(msg);
		}
	}

	public void sendName() // ��� Ŭ���̾�Ʈ�鿡�� ���� ������ ������ ����
	{
		String[] tmp=new String[client.size()];
		for(int i=0;i<client.size();i++) // ���� �����ڸ� ��Ʈ�� �迭�� ����
		{
			User usr=((User)client.elementAt(i));
			tmp[i]=usr.name;
		}
		send("%"); // %�� ���� -> Ŭ���̾�Ʈ���� ������â �ʱ�ȭ
		for(int i=0;i<client.size();i++) // ����ڵ��� �̸��� ����
		{
			User usr=((User)client.elementAt(i));
			send("*"+usr.name); // �̸��տ� *�� �߰��Ͽ� ���� -> Ŭ���̾�Ʈ���� ������ â�� �߰� ��Ŵ
		}
	}


	public static void main(String[] args)
	{
		new ServerMain();
	}

	class User extends Thread // �� Ŭ���̾�Ʈ���� �޼����� �ް� ������ ���� Ŭ����
	{
		private Socket socket1;
		private BufferedReader br;
		private BufferedWriter bw;
		public String name;
		
		public User(Socket socket) // ������
		{
			this.socket1=socket;
			try{
				br=new BufferedReader(new InputStreamReader(socket.getInputStream())); // �б� ���� ���� ����
				bw=new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())); // ���� ���� ���� ����
				name=br.readLine(); // ���� ó�� Ŭ���̾�Ʈ�� ������ ������ �̸����� ����
			}catch(IOException ex){
				System.out.println(ex);
			}

		}

		
		public void run()
		{
			String str;
			send("&�͢͢͢� "+name+"�� ���� �����ϼ̽��ϴ� �͢͢͢�"); // Ŭ���̾�Ʈ�� ������ �˸�
			sendName(); // ��� ����ڵ鿡�� ���� �����ڸ� ����
			try{
				textArea.append(name+"�� ���� at "+socket.getInetAddress()+"\n"); // ���� ���� �߰�
				sp.getVerticalScrollBar().setValue(sp.getVerticalScrollBar().getMaximum());
				while((str=br.readLine())!="/bye") // Ŭ���̾�Ʈ�� ������ ���� �� ����
				{	
					send("&"+name+" > "+str); // &�� �ٿ��� ä�ó����� ���� -> Ŭ���̾�Ʈ���� ä��â�� �߰�
				}
				close();
			}catch(IOException ex){
				System.out.println(ex);
				close();
			}
		}
		
		private String fillString(String str, int count) {
			String tmp = "";
			
			for(int i = 0; i < count; i++) {
				tmp += str;
			}
			
			return tmp;
		}

		public void sendMessage(String msg) // �� Ŭ���̾�Ʈ���� �޼����� ������ �޼ҵ�
		{
			try{
				bw.write(msg); 
				bw.newLine();
				bw.flush();
			}catch(IOException ex){
				System.out.println(ex);
			}
		}

		public void close() // Ŭ���̾�Ʈ�� ������ �ݴ� �޼ҵ�
		{
			try{ // ���ۿ� ������ ����
				bw.close();
				br.close();
				socket.close();
			}catch(IOException ex){
				System.out.println(ex);
			}
			removeUser(this); // ���Ϳ��� Ŭ���̾�Ʈ�� ����
			send("&�͢͢͢� "+name+"�Բ��� ��ȭ���� �����̽��ϴ� �͢͢͢�"); // ��� Ŭ���̾�Ʈ���� �޼��� ����
			sendName(); // ������ ����Ʈ �ʱ�ȭ
			textArea.append(name+"�� ���� ����\n");
			sp.getVerticalScrollBar().setValue(sp.getVerticalScrollBar().getMaximum());
		}
	}
}
