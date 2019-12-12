package ver1;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.io.*;
import java.net.*;
import java.util.*;

import org.w3c.dom.*;
import org.xml.sax.*;


import javax.xml.parsers.*;
//import com.sun.xml.tree.XmlDocument;
import javax.xml.transform.*;
import javax.xml.transform.dom.*;
import javax.xml.transform.stream.*;

public class MessengerClient extends JFrame {
	private JPanel titlePanel = new JPanel();
	private JPanel idInputPanel = new JPanel();
	private JPanel passwordInputPanel = new JPanel();
	private JPanel loginPanel = new JPanel();

	private JLabel programTitle = new JLabel("XML Messanger Program", JLabel.CENTER);
	private JLabel idLabel = new JLabel("ID : ");
	private JTextField idTextfield = new JHintText("Enter your ID");
//	 private JLabel passwordLabel = new JLabel("Password : ");
//	 private JTextField passwordTextfield = new JHintText("Enter your Password");
	private JButton loginButton = new JButton("Login");

	private static final int BORDER = 1; // 패널 내 공백 크기
	private Socket clientSocket;
	private OutputStream output;
	private InputStream input;
	private boolean keepListening;
	private ClientStatus clientStatus;
	private Document users;
	private Vector conversations;
	private DocumentBuilderFactory factory;
	private DocumentBuilder builder;

	public MessengerClient() {
		// create GUI
		 super("XML Messanger Program");	// 윈도우 타이틀 설정
		 this.setLayout(new GridLayout(0,1,1,1));

		try {
			// obtain the default parser
			factory = DocumentBuilderFactory.newInstance();

			// get DocumentBuilder
			builder = factory.newDocumentBuilder();
		} catch (ParserConfigurationException pce) {
			pce.printStackTrace();
		}

		initTitle();
		initLoginInput();
		initLoginButton();

		setBounds(700, 400, 500, 400);
		setVisible(true);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}

	public void initTitle() {
		titlePanel.setLayout(new FlowLayout());
		programTitle.setFont(new Font("consolas", Font.BOLD, 30));
		titlePanel.add(programTitle);
		titlePanel.setBorder(BorderFactory.createEmptyBorder(BORDER, BORDER, BORDER, BORDER)); // 상하좌우 BORDER 만큼 공백 생성

		this.add(titlePanel);
	}

	public void initLoginInput() {
		idInputPanel.setLayout(new FlowLayout());
		idLabel.setFont(new Font("consolas", Font.BOLD, 20));
		idInputPanel.add(idLabel);
		idInputPanel.add(idTextfield);
		idInputPanel.setBorder(BorderFactory.createEmptyBorder(BORDER, BORDER, BORDER, BORDER)); // 상하좌우 BORDER 만큼 공백 생성

//		 passwordInputPanel.setLayout(new FlowLayout());
//		 passwordLabel.setFont(new Font("consolas", Font.BOLD, 20));
//		 passwordInputPanel.add(passwordLabel);
//		 passwordInputPanel.add(passwordTextfield);
//		 passwordInputPanel.setBorder(BorderFactory.createEmptyBorder(BORDER,BORDER,BORDER,BORDER));	// 상하좌우 BORDER 만큼 공백 생성

		this.add(idInputPanel);
//		 this.add(passwordInputPanel);
	}

	public void initLoginButton() {
		loginPanel.setLayout(new FlowLayout());
		loginPanel.add(loginButton);
		loginButton.setFont(new Font("consolas", Font.BOLD, 40));
		loginPanel.setBorder(BorderFactory.createEmptyBorder(BORDER, BORDER, BORDER, BORDER));

		loginButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				loginUser();
			}
		});

		this.add(loginPanel);
	}

	public void runMessengerClient() {
		try {
			clientSocket = new Socket(InetAddress.getByName("127.0.0.1"), 5000);

			// get input and output streams
			output = clientSocket.getOutputStream();
			input = clientSocket.getInputStream();

			loginButton.setEnabled(true);
			keepListening = true;

			int bufferSize = 0;

			while (keepListening) {

				bufferSize = input.available();

				if (bufferSize > 0) {
					byte buf[] = new byte[bufferSize];

					input.read(buf);

					InputSource source = new InputSource(new ByteArrayInputStream(buf));
					Document message;

					try {

						// obtain document object from XML document
						message = builder.parse(source);

						if (message != null)
							messageReceived(message);

					} catch (SAXException se) {
						se.printStackTrace();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}

			input.close();
			output.close();
			clientSocket.close();
			System.exit(0);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	public void loginUser() {
		// create Document with user login
		Document submitName = builder.newDocument();
		Element root = submitName.createElement("user");

		submitName.appendChild(root);
		root.appendChild(submitName.createTextNode(idTextfield.getText()));

		send(submitName);
	}

	public Document getUsers() {
		return users;
	}

	public void stopListening() {
		keepListening = false;
	}

	public void messageReceived(Document message) {
		Element root = message.getDocumentElement();

		if (root.getTagName().equals("nameInUse"))
			// did not enter a unique name
			JOptionPane.showMessageDialog(this, "That name is already in use." + "\nPlease enter a unique name.");
		else if (root.getTagName().equals("users")) {
			// entered a unique name for login
			users = message;
			clientStatus = new ClientStatus(idTextfield.getText(), this);
			conversations = new Vector();
		
		} else if (root.getTagName().equals("update")) {

			// either a new user login or a user logout
			String type = root.getAttribute("type");
			NodeList userElt = root.getElementsByTagName("user");
			String updatedUser = userElt.item(0).getFirstChild().getNodeValue();

			// test for login or logout
			if (type.equals("login"))
				// login
				// add user to onlineUsers Vector
				// and update usersList
				clientStatus.add(updatedUser);
			else {
				// logout
				// remove user from onlineUsers Vector
				// and update usersList
				clientStatus.remove(updatedUser);

				// if there is an open conversation, inform user
				int index = findConversationIndex(updatedUser);

				if (index != -1) {
					Conversation receiver = (Conversation) conversations.elementAt(index);

					receiver.updateGUI(updatedUser + " logged out");
					receiver.disableConversation();
				}
			}
		} else if (root.getTagName().equals("message")) {
			String from = root.getAttribute("from");
			String messageText = root.getFirstChild().getNodeValue();

			// test if conversation already exists
			int index = findConversationIndex(from);

			if (index != -1) {
				// conversation exists
				Conversation receiver = (Conversation) conversations.elementAt(index);
				receiver.updateGUI(from + ":  " + messageText);
			} else {
				// conversation does not exist
				Conversation newConv = new Conversation(from, clientStatus, this);
				newConv.updateGUI(from + ":  " + messageText);
			}
		}
	}

	public int findConversationIndex(String userName) {
		// find index of specified Conversation
		// in Vector conversations
		// if no corresponding Conversation is found, return -1
		for (int i = 0; i < conversations.size(); i++) {
			Conversation current = (Conversation) conversations.elementAt(i);

			if (current.getTarget().equals(userName))
				return i;
		}

		return -1;
	}

	public void addConversation(Conversation newConversation) {
		conversations.add(newConversation);
	}

	public void removeConversation(String userName) {
		conversations.removeElementAt(findConversationIndex(userName));
	}

	public void send(Document message) {
		try {

			// write to output stream
			// ( ( XmlDocument ) message).write( output );
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer serializer = transformerFactory.newTransformer();
			serializer.transform(new DOMSource(message), new StreamResult(output));

		}
		// catch ( IOException e ) {
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String args[]) {
		MessengerClient cm = new MessengerClient();

		cm.runMessengerClient();
	}
}