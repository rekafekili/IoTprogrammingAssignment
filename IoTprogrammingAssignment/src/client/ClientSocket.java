package client;

import java.io.*;
import java.net.*;

public class ClientSocket extends Socket {
	private BufferedReader inStream;		// �Է� ��Ʈ��
	private PrintWriter outStream;			// ��� ��Ʈ��
	private Socket clientSocket;			// ����
	
	// IP �ּҿ� Port ��ȣ�� ���� ����
	ClientSocket(InetAddress serverIP, int serverPort) throws IOException{
		this.clientSocket = new Socket(serverIP, serverPort);
		System.out.println("Server IP : " + serverIP);
		System.out.println("Server Port : " + serverPort);
		
		createStream();
	}
	
	// ����� ��Ʈ�� ����
	public void createStream() throws IOException{
		inStream = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
		outStream = new PrintWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
		System.out.println("Server Stream Created!!");
	}
	
	// ������ �۽� �޼ҵ�
	public void sendMessage(String msg) {
		outStream.println(msg);
	}
	
	// ������ ���� �޼ҵ�
	public String receiveMessage() throws IOException {
		return inStream.readLine();
	}
	
	// ���� ���� �޼ҵ�
	public void quitSocket() {
		try {
			clientSocket.close();
		}
		catch(IOException e) {
			e.printStackTrace();
		}
	}
}	