package net.wanisys.wanidiary;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JComponent;
import javax.swing.JLabel;

public class StatusTipManager{


	private JLabel StatusLabel;
	
	public StatusTipManager(JLabel label) {
		StatusLabel = label;
	}
	public void setStatusTip(JComponent comp, String tipText) {

		final String tiptxt = tipText;

		comp.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseExited(MouseEvent arg0) {
				StatusLabel.setText("");
			}

			@Override
			public void mouseEntered(MouseEvent arg0) {
				if (tiptxt != null) StatusLabel.setText(tiptxt);
			}
		});

	}
}