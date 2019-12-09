package client;

import java.io.*;
import java.net.*;

public class ClientSocket extends Socket {
	private BufferedReader inStream;		// 입력 스트림
	private PrintWriter outStream;			// 출력 스트림
	private Socket clientSocket;			// 소켓
	
	// IP 주소와 Port 번호로 소켓 생성
	ClientSocket(InetAddress serverIP, int serverPort) throws IOException{
		this.clientSocket = new Socket(serverIP, serverPort);
		System.out.println("Server IP : " + serverIP);
		System.out.println("Server Port : " + serverPort);
		
		createStream();
	}
	
	// 입출력 스트림 생성
	public void createStream() throws IOException{
		inStream = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
		outStream = new PrintWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
		System.out.println("Server Stream Created!!");
	}
	
	// 데이터 송신 메소드
	public void sendMessage(String msg) {
		outStream.println(msg);
	}
	
	// 데이터 수신 메소드
	public String receiveMessage() throws IOException {
		return inStream.readLine();
	}
	
	// 소켓 종료 메소드
	public void quitSocket() {
		try {
			clientSocket.close();
		}
		catch(IOException e) {
			e.printStackTrace();
		}
	}
}	