package ver1;

import java.awt.*;
import java.util.List;
import java.awt.event.*;
import java.net.*;
import java.io.*;
import javax.swing.*;
import java.util.*;

import org.w3c.dom.*;
import org.xml.sax.*;
import javax.xml.parsers.*;
//import com.sun.xml.tree.XmlDocument;

public class MessengerServer extends JFrame {
   private JLabel logo;
   private JTextArea display;
   private Vector onlineUsers;
   private Image icon;
   private JScrollPane sp;
   private DocumentBuilderFactory factory;
   private DocumentBuilder builder;
   private Document users;
   private String serverIp = "172.19.85.3";
   
   private List<String> banWordList = Arrays.asList(
			"씨발", "시발",
			"병신", "ㅂㅅ",
			"지랄", "ㅈㄹ",
			"니미", "느금마","니애미",
			"좆같다", "좆까", "조까", "ㅈㄲ", "좆",
			"개새끼", "개1새끼"
	);
   
   

   public MessengerServer() throws UnknownHostException
   {
      // create GUI
      super ( "채팅프로그램" );

      try {

         // obtain the default parser
         factory = DocumentBuilderFactory.newInstance();

         // get DocumentBuilder
         builder = factory.newDocumentBuilder();
      } 
      catch ( ParserConfigurationException pce ) {
         pce.printStackTrace();
      }

      Container c = getContentPane();
      
      display=new JTextArea();
      sp=new JScrollPane(display);
      logo=new JLabel(new ImageIcon("./image/serverlogo.jpg"));
		
      getContentPane().setLayout(new BorderLayout());
      getContentPane().add(sp,"Center");
      getContentPane().add(logo,"South");
      icon=getToolkit().getImage("./image/icon.gif");
      display.setEditable(false);

      this.setIconImage(icon);
      this.setVisible(true);
      this.setSize(450,600);
      this.setLocation(100,80);
      this.setDefaultCloseOperation(EXIT_ON_CLOSE);
      
      display.append("채팅프로그램\n");
      display.append("Ip Adress : "+serverIp+"\n"); // 서버의 정보를 표시
      
      onlineUsers = new Vector();
      users = initUsers();
   }

   public void runServer()
   {
      ServerSocket server;
      

      try {
         // create a ServerSocket
         server = new ServerSocket( 5000, 100 );
         display.append("Port Num  : "+server.getLocalPort()+"\n");
         
         display.append("욕설 목록 :\n");
         for(String banWord : banWordList) {
       	  display.append("  - " + banWord + "\n");
         }

         // wait for connections
         while ( true ) {
            Socket clientSocket = server.accept();

            display.append( "\nConnection received from: " +
               clientSocket.getInetAddress().getHostName() );

            UserThread newUser = 
               new UserThread( clientSocket, this );

            newUser.start();
         }
      } 
      catch ( IOException e ) {
         e.printStackTrace();
         System.exit( 1 );
      }
   }

   private Document initUsers()
   {
      // initialize users xml document with root element users
      Document init = builder.newDocument();

      init.appendChild( init.createElement( "users" ) );
      return init;
   }

   public void updateGUI( String s )
   {
      display.append( "\n" + s );
   }

   public Document getUsers()
   {
      return users;
   }

   public void addUser( UserThread newUserThread ) 
   {
      // get new user's name
      String userName = newUserThread.getUsername();
      
      // notify all users of user's login
      updateUsers( userName, "login" );

      // add new user element to Document users
      Element usersRoot = users.getDocumentElement();
      Element newUser = users.createElement( "user" );
     
      newUser.appendChild( 
    		  users.createTextNode( userName ) );
      usersRoot.appendChild( newUser );

      // add to Vector onlineUsers
      onlineUsers.addElement( newUserThread );
   }

   public void sendMessage( Document message )
   {
      // transfer message to specified receiver 
      Element root = message.getDocumentElement();
      String from = root.getAttribute( "from" );
      String to = root.getAttribute( "to" );
      int index = findUserIndex( to );
      
      String msg = root.getTextContent();
      boolean hasBanWord = false;
      // 금지 단어가 포함되어있는지 검사
		for(String banWord : banWordList) {
			if(msg.contains(banWord)) {
				hasBanWord = true;
			}
			
			msg = msg.replace(banWord, fillString("하", banWord.length()));
		}
		
		// 금지 단어가 있다면
		if(hasBanWord) {
			msg = "((욕설이 포함되어있습니다))  " + msg; 
		}
		root.setTextContent(msg);

      updateGUI( "Received message To: " + to + ",  From: " + from );

      // send message to corresponding user
      UserThread receiver = 
         ( UserThread ) onlineUsers.elementAt( index );
      receiver.send( message );
      updateGUI( "Sent message To: " + to +
         ",  From: " + from );
   }

   public void updateUsers( String userName, String type )
   {
      // create xml update document
      Document doc = builder.newDocument();
      Element root = doc.createElement( "update" );
      Element userElt = doc.createElement( "user" );

      doc.appendChild( root );
      root.setAttribute( "type", type );
      root.appendChild( userElt );
      userElt.appendChild( doc.createTextNode( userName ) );

      // send to all users
      for ( int i = 0; i < onlineUsers.size(); i++ ) {
         UserThread receiver = 
            ( UserThread ) onlineUsers.elementAt( i );
         receiver.send( doc );
      }

      updateGUI(userName + "님이 " + type + " 하셨습니다." );
   }

   public int findUserIndex( String userName )
   {
      // find index of specified UserThread in Vector onlineUsers
      // return -1 if no corresponding UserThread is found
      for ( int i = 0; i < onlineUsers.size(); i++ ) {
         UserThread current = 
            ( UserThread ) onlineUsers.elementAt( i );

         if ( current.getUsername().equals( userName ) ) 
            return i;
      }

      return -1;
   }

   public void removeUser( String userName )
   {
      // remove user from Vector onlineUsers
      int index = findUserIndex( userName );

      onlineUsers.removeElementAt( index );

      // remove this user's element from Document users
      NodeList userElements =
         users.getDocumentElement().getElementsByTagName(
         "user" );

      for ( int i = 0; i < userElements.getLength(); i++ ) {
         String str = 
            userElements.item( i ).getFirstChild().getNodeValue();

         if ( str.equals( userName ) )
            users.getDocumentElement().removeChild( 
               userElements.item( i ) );
            
      }
      // update all users of user's logout
      updateUsers( userName, "logout" );
   }
   
   public String fillString(String str, int count) {
		String tmp = "";
		
		for(int i = 0; i < count; i++) {
			tmp += str;
		}
		
		return tmp;
	}

   public static void main( String args[] ) throws UnknownHostException
   {
      MessengerServer ms = new MessengerServer();

      ms.addWindowListener( 
         new WindowAdapter() {
            public void windowClosing( WindowEvent e ) {
               System.exit( 0 );
            }
         } 
      );

      ms.runServer();
   }
}