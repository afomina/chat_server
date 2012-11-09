package net;

import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;

public class ServerFrame extends JFrame {

	private static final long serialVersionUID = 1L;
	JEditorPane out;
	static final int X1 = 100;
	static final int X2 = 460;
	static final int Y1 = 100;
	static final int Y2 = 260;

	public ServerFrame() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(X1, Y1, X2, Y2);
		setMinimumSize(getSize());
		setTitle("Server");

		out = new JEditorPane();
		out.setEditable(false);

		JScrollPane scroll = new JScrollPane(out);
		scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		add(scroll);

		setVisible(true);
	}

	public void println(String msg) {
		out.setText(out.getText() + msg + "\n");
	}
}
