package ver1;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.*;

import org.w3c.dom.*;
import org.xml.sax.*;
import javax.xml.parsers.*;

public class ClientStatus extends JFrame {
   private MessengerClient client;
   private JLabel statusLabel;
   private JList usersList;
   private JButton disconnectButton;
   private String user;
   private Vector onlineUsers;
   
   public ClientStatus( String name, MessengerClient mc )
   {
      // create GUI
      super( name + "'s Messenger Status" );

      client = mc;
      user = name;

      Container c = getContentPane();

      statusLabel = new JLabel( "Available users:" );
      c.add( statusLabel, BorderLayout.NORTH );

      // determine how many users are online
      NodeList userElts =
         client.getUsers().getDocumentElement().
            getElementsByTagName( "user" );
      int numberOfUsers = userElts.getLength();

      // initialize Vector onlineUsers
      onlineUsers = new Vector( numberOfUsers );

      for ( int i = 0; i < numberOfUsers; i++) {
         String currentUser = 
            userElts.item( i ).getFirstChild().getNodeValue();
         onlineUsers.addElement( currentUser );
      }

      usersList = new JList( onlineUsers );
      usersList.setSelectionMode( 
         ListSelectionModel.SINGLE_SELECTION );

      MouseListener usersListener = new MouseAdapter() {
         public void mouseClicked( MouseEvent e ) {
            int selectedIndex = usersList.getSelectedIndex();

            if ( e.getClickCount() == 2 && selectedIndex >= 0 )
               initiateMessage( selectedIndex );
         }
      };
      usersList.addMouseListener( usersListener );
      c.add( new JScrollPane( usersList ), BorderLayout.CENTER );

      disconnectButton = new JButton( "Disconnect" );
      disconnectButton.addActionListener(
         new ActionListener() {
            public void actionPerformed( ActionEvent e ) {
               disconnectUser();
            }
         }
      );
      c.add( disconnectButton, BorderLayout.SOUTH );
      addWindowListener(
         new WindowAdapter() {
            public void windowClosing( WindowEvent e ) {
               disconnectUser();
            }
         }
      );

      setSize( 250, 300 );
      show();
   }

   public String getUser() 
   {
      return user;
   }

   public void initiateMessage( int index )
   {
      String target = ( String ) onlineUsers.elementAt( index );

      // only open a new Conversation 
      // if there is not an already open one
      if ( client.findConversationIndex( target ) == -1 )
         new Conversation( target, this, client );
   }

   public void add( String userToAdd )
   {
      // add user to Vector onlineUsers
      onlineUsers.addElement( userToAdd );

      // update JList usersList
      usersList.setListData( onlineUsers );
   }

   public void remove( String userToRemove )
   {
      // remove user from Vector onlineUsers
      onlineUsers.removeElementAt( 
         findOnlineUsersIndex( userToRemove ) );

      // update JList usersList
      usersList.setListData( onlineUsers );
   }

   public int findOnlineUsersIndex( String onlineUserName )
   {
     for ( int i = 0; i < onlineUsers.size(); i++ ) {
        String currentUserName = 
           ( String ) onlineUsers.elementAt( i );

        if ( currentUserName.equals( onlineUserName ) ) 
           return i;
     }

     return -1;
   }

   public void disconnectUser()
   {
      DocumentBuilderFactory factory =
         DocumentBuilderFactory.newInstance();
      Document disconnectUser;
      try {

         // get DocumentBuilder
         DocumentBuilder builder = 
            factory.newDocumentBuilder();

         // create root node       
         disconnectUser = builder.newDocument();

         disconnectUser.appendChild( 
            disconnectUser.createElement( "disconnect" ) );

         client.send( disconnectUser );
         client.stopListening();
      } 
      catch ( ParserConfigurationException pce ) {
         pce.printStackTrace();
      }

   }
}
