package client;

import java.awt.*;
import javax.swing.*;

/**
 * 
 * @author SeongYun on 2019. 12. 07
 *
 */

// awt, swing ���� Client UI ����
public class ClientUI extends JFrame{
	 private JPanel titlePanel = new JPanel();
	 private JPanel idInputPanel = new JPanel();
	 private JPanel passwordInputPanel = new JPanel();
	 private JPanel loginPanel = new JPanel();
	 
	 private JLabel programTitle = new JLabel("XML Messanger Program", JLabel.CENTER);
	 private JLabel idLabel = new JLabel("ID : ");
	 private JTextField idTextfield = new JHintText("Enter your ID");
	 private JLabel passwordLabel = new JLabel("Password : ");
	 private JTextField passwordTextfield = new JHintText("Enter your Password");
	 private JButton loginButton = new JButton("Login");
	 
	 private static final int BORDER = 1;	// �г� �� ���� ũ��
	 
	 public ClientUI(){
		 super("JFrame Test");	// ������ Ÿ��Ʋ ����
		 this.setLayout(new GridLayout(0,1,1,1));
		 
		 initTitle();
		 initLoginInput();
		 initLoginButton();
		 
		 setBounds(700,400,500,400);
		 setVisible(true);
		 setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	 }
	 
	 public void initTitle() {
		 titlePanel.setLayout(new FlowLayout());
		 programTitle.setFont(new Font("consolas", Font.BOLD, 30));
		 titlePanel.add(programTitle);
		 titlePanel.setBorder(BorderFactory.createEmptyBorder(BORDER,BORDER,BORDER,BORDER));	// �����¿� BORDER ��ŭ ���� ����
		 
		 this.add(titlePanel);
	 }
	 
	 public void initLoginInput() {
		 idInputPanel.setLayout(new FlowLayout());
		 idLabel.setFont(new Font("consolas", Font.BOLD, 20));
		 idInputPanel.add(idLabel);
		 idInputPanel.add(idTextfield);
		 idInputPanel.setBorder(BorderFactory.createEmptyBorder(BORDER,BORDER,BORDER,BORDER));	// �����¿� BORDER ��ŭ ���� ����		 
	 
		 passwordInputPanel.setLayout(new FlowLayout());
		 passwordLabel.setFont(new Font("consolas", Font.BOLD, 20));
		 passwordInputPanel.add(passwordLabel);
		 passwordInputPanel.add(passwordTextfield);
		 passwordInputPanel.setBorder(BorderFactory.createEmptyBorder(BORDER,BORDER,BORDER,BORDER));	// �����¿� BORDER ��ŭ ���� ����
		 
		 this.add(idInputPanel);
		 this.add(passwordInputPanel);
	 }
	 
	 public void initLoginButton() {
		 loginPanel.setLayout(new FlowLayout());
		 loginPanel.add(loginButton);
		 loginButton.setFont(new Font("consolas", Font.BOLD, 40));
		 loginPanel.setBorder(BorderFactory.createEmptyBorder(BORDER,BORDER,BORDER,BORDER));

		 this.add(loginPanel);
	 }
}