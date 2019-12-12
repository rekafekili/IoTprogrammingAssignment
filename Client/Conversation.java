package ver1;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import org.w3c.dom.*;
import org.xml.sax.*;
import javax.xml.parsers.*;

public class Conversation extends JFrame {
   private ClientStatus clientStatus;
   private MessengerClient client;
   private JTextArea display;
   private JTextField message;
   private JButton enter;
   private JPanel messageArea;
   private GridLayout messageAreaLayout;
   private String target;

   public Conversation( String contact, ClientStatus cs,
                        MessengerClient mc )
   {
      // create GUI and initialize variables
      super( cs.getUser() + "'s conversation with " + contact );
      target = contact;
      clientStatus = cs;
      client = mc;

      Container c = getContentPane();
      Font font = new Font( "SansSerif",
                            java.awt.Font.BOLD, 14 );

      display = new JTextArea();
      display.setLineWrap( true );
      display.setEditable( false );
      display.setFont( font );
      c.add( new JScrollPane( display ), BorderLayout.CENTER );

      messageArea = new JPanel();
      messageArea.setLayout( new GridLayout( 2, 1 ) );

      message = new JTextField( 20 );
      message.setText( "" );
      messageArea.add( message );

      message.addActionListener (
         new ActionListener () {
            public void actionPerformed ( ActionEvent e ) {
               submitMessage();
            }
         }
      );

      enter = new JButton( "Enter" );
      messageArea.add( enter );
      c.add( messageArea, BorderLayout.SOUTH );

      enter.addActionListener (
         new ActionListener () {
            public void actionPerformed ( ActionEvent e ) {
               submitMessage();
            }
         }
      );

      addWindowListener(
         new WindowAdapter() {
            public void windowClosing( WindowEvent e ) {

               // remove conversation from client's 
               // conversations Vector
               client.removeConversation( target );
            }
         }
      );

      setSize( 400, 200 );
      show();

      // add this Conversation object to conversations Vector
      client.addConversation( this );
   }

   public String getTarget() 
   {
      return target;
   }

   public void disableConversation() 
   {
      message.setEnabled( false );
      enter.setEnabled( false );
   }

   public void updateGUI( String dialog )
   {
      display.append( dialog + "\n" );
   }

   public void submitMessage()
   {
      String messageToSend = message.getText();

      // do nothing if the user has not typed a message
      if ( !messageToSend.equals( "" ) ) {

         Document sendMessage;
         DocumentBuilderFactory factory =
            DocumentBuilderFactory.newInstance();

         try {

            // get DocumentBuilder
            DocumentBuilder builder = 
               factory.newDocumentBuilder();

            // create xml message
            sendMessage = builder.newDocument();
            Element root = sendMessage.createElement( "message" );

            root.setAttribute( "to", target );
            root.setAttribute( "from", clientStatus.getUser() );
            root.appendChild( 
               sendMessage.createTextNode( messageToSend ) );
            sendMessage.appendChild( root );

            client.send( sendMessage );

            updateGUI( clientStatus.getUser() +
               ":  " + messageToSend );
            message.setText( "" );
         } 
         catch ( ParserConfigurationException pce ) {
            pce.printStackTrace();
         }
      }
   }
}
