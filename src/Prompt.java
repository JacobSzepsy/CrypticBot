import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;



//TODO save values to a local file
public class Prompt extends JFrame{

	static JFrame gui = new JFrame();
	//static String token;
	public static void main(String[] args){
		JPanel pane = new JPanel();
		JButton button = new JButton();
		JTextField id = new JTextField();
		JTextField oauth = new JTextField();
		JTextPane test = new JTextPane();
		GridLayout layout = new GridLayout(3,2);
		JLabel lbl1 = new JLabel();
		JLabel lbl2 = new JLabel();
		JLabel lbl3 = new JLabel();
		//JLabel lbl4 = new JLabel();
		layout.setHgap(5);
		layout.setVgap(5);
		test.setEditable(false);
		test.setBackground(null);
		test.setBorder(null);
		test.setContentType("text/html");
		test.setText("https://goo.gl/DibO8T");
		gui.setSize(300, 125);
		gui.setResizable(false);
		gui.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		gui.setTitle("CircuitBot");
		pane.setLayout(layout);
		id.setText("ID");
		lbl1.setText("SpreadSheet ID: ");
		lbl2.setText("Bot Oauth Token: ");
		lbl3.setText("SpreadSheet Template: ");
		//lbl4.setText("https://goo.gl/DibO8T");
		oauth.setText("oauth:token");
		button.setText("Connect");
		pane.add(lbl1);
		pane.add(id);
		pane.add(lbl2);
		pane.add(oauth);
		pane.add(lbl3);
		pane.add(test);
		gui.add(pane, BorderLayout.NORTH);
		gui.add(button, BorderLayout.SOUTH);
		button.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				//System.out.println(id.getText() + " " + oauth.getText());
				//token = oauth.getText();
				//new Bot();
				new BotMain(oauth.getText(), id.getText());
				gui.dispose();
			}
		});
		gui.setVisible(true);
		
		
	}
}
