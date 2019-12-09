package client;

import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.JTextField;

/**
 * 
 * @author SeongYun on 2019. 12. 07....
 *
 */

// TextField 에 Hint 기능 구현
public class JHintText extends JTextField implements FocusListener{
	private final String hint;
	private boolean showingHint;
	
	public JHintText(final String hint){
		super(hint,30);
		this.hint = hint;
		this.showingHint = true;
		super.addFocusListener(this);
	}
	
	@Override
	public void focusGained(FocusEvent userInput) {
		if(this.getText().isEmpty()) {
			super.setText("");
			this.showingHint = false;
		}
	}

	@Override
	public void focusLost(FocusEvent userInput) {
		if(this.getText().isEmpty()) {
			super.setText(this.hint);
			this.showingHint = true;
		}
	}

	public String getText() {
		return showingHint ? "" : super.getText();
	}
}
