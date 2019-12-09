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
	private ServerSocket srvSocket; // 서버소켓
	private Socket socket; // 클라이언트의 접속을 위한 소켓
	private Vector client=new Vector(); // 클라이언트들을 담기 위한 벡터
	private JTextArea textArea; // 서버 상태를 출력하기 위한 필드
	private JScrollPane sp; // 스크롤을 위한 페널
	private Image icon; // 아이콘
	private JLabel logo; // 로고


    public ServerMain() // 생성자
	{
		super("채팅프로그램");
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
			srvSocket=new ServerSocket(2848); // 서버 소켓을 생성
			textArea.append("채팅프로그램\n");
			textArea.append("Ip Adress : "+"Localhost"+"\n"); // 서버의 정보를 표시
			textArea.append("Port Num  : "+srvSocket.getLocalPort()+"\n");
			
			while(true)
			{
				socket=srvSocket.accept(); // 새로운 클라이언트를 받음
				User usr=new User(socket); // 새로운 클라이언트에 대해 User 쓰레드를 만듬
				usr.start();
				addUser(usr); // 벡터에 새로운 클라이언트를 더함
			}
		}catch(IOException ex){
			System.out.println(ex);
		}
	}

	public void addUser(User usr) // 벡터에 클라이언트를 추가 시키는 메소드
	{
		client.addElement(usr);
	}

	public void removeUser(User usr) // 벡터에서 클라이언트를 제거 시키는 메소드
	{
		client.removeElement(usr);
	}

	public void send(String msg) // 모든 클라이언트들에게 메세지를 보냄
	{
		for(int i=0;i<client.size();i++)
		{
			User usr=((User)client.elementAt(i));
			usr.sendMessage(msg);
		}
	}

	public void sendName() // 모든 클라이언트들에게 현재 접속자 정보를 보냄
	{
		String[] tmp=new String[client.size()];
		for(int i=0;i<client.size();i++) // 현재 접속자를 스트링 배열에 저장
		{
			User usr=((User)client.elementAt(i));
			tmp[i]=usr.name;
		}
		send("%"); // %를 보냄 -> 클라이언트에서 접속자창 초기화
		for(int i=0;i<client.size();i++) // 사용자들의 이름을 보냄
		{
			User usr=((User)client.elementAt(i));
			send("*"+usr.name); // 이름앞에 *을 추가하여 보냄 -> 클라이언트에서 접속자 창에 추가 시킴
		}
	}


	public static void main(String[] args)
	{
		new ServerMain();
	}

	class User extends Thread // 한 클라이언트에게 메세지를 받고 보내는 내부 클래스
	{
		private Socket socket1;
		private BufferedReader br;
		private BufferedWriter bw;
		public String name;
		
		public User(Socket socket) // 생성자
		{
			this.socket1=socket;
			try{
				br=new BufferedReader(new InputStreamReader(socket.getInputStream())); // 읽기 위한 버퍼 생성
				bw=new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())); // 쓰기 위한 버퍼 생성
				name=br.readLine(); // 제일 처음 클라이언트가 보내는 정보를 이름으로 저장
			}catch(IOException ex){
				System.out.println(ex);
			}

		}

		
		public void run()
		{
			String str;
			send("&♨♨♨♨ "+name+"님 께서 입장하셨습니다 ♨♨♨♨"); // 클라이언트의 입장을 알림
			sendName(); // 모든 사용자들에게 현재 접속자를 갱신
			try{
				textArea.append(name+"님 접속 at "+socket.getInetAddress()+"\n"); // 서버 정보 추가
				sp.getVerticalScrollBar().setValue(sp.getVerticalScrollBar().getMaximum());
				while((str=br.readLine())!="/bye") // 클라이언트가 연결을 끊을 때 까지
				{	
					send("&"+name+" > "+str); // &를 붙여서 채팅내용을 보냄 -> 클라이언트에서 채팅창에 추가
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

		public void sendMessage(String msg) // 한 클라이언트에게 메세지를 보내는 메소드
		{
			try{
				bw.write(msg); 
				bw.newLine();
				bw.flush();
			}catch(IOException ex){
				System.out.println(ex);
			}
		}

		public void close() // 클라이언트의 연결을 닫는 메소드
		{
			try{ // 버퍼와 소켓을 닫음
				bw.close();
				br.close();
				socket.close();
			}catch(IOException ex){
				System.out.println(ex);
			}
			removeUser(this); // 벡터에서 클라이언트를 제거
			send("&♨♨♨♨ "+name+"님께서 대화방을 나가셨습니다 ♨♨♨♨"); // 모든 클라이언트에게 메세지 보냄
			sendName(); // 접속자 리스트 초기화
			textArea.append(name+"님 접속 종료\n");
			sp.getVerticalScrollBar().setValue(sp.getVerticalScrollBar().getMaximum());
		}
	}
}
