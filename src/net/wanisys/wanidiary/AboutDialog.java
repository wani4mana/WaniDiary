package net.wanisys.wanidiary;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class AboutDialog extends JDialog {

	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	class URILabel extends JLabel {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		private final String href;
		public URILabel(String href) {
			super("<html><a href='"+href+"'>"+href+"</a>", JLabel.CENTER);
			this.href = href;
			setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
			addMouseListener(new MouseAdapter() {
				@Override
				public void mousePressed(MouseEvent e) {
					if(!Desktop.isDesktopSupported()) return;
						try {
							Desktop.getDesktop().browse(new URI(URILabel.this.href));
						} catch (IOException e1) {
							e1.printStackTrace();
						} catch (URISyntaxException e1) {
							e1.printStackTrace();
						}
				}
			});
			
		}
	}


	public AboutDialog(WaniDiary mainwin) {
		super(mainwin, "About this program", true);
		this.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		
		Container adcp = getContentPane();
		adcp.setLayout(new BorderLayout());
		JLabel msg = new JLabel("<html>This program is programmed by wanikoo! Ver 1.5<br><center>Have a good time!!</center>", JLabel.CENTER);
		JButton ok = new JButton("OK");
		URILabel wanisys = new URILabel("http://www.wanisys.net");
		
		adcp.add(msg, "North");
		adcp.add(wanisys, "Center");
		JPanel p = new JPanel();
		p.add(ok);
		adcp.add(p, "South");
		
		ok.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				AboutDialog.this.setVisible(false);
				AboutDialog.this.dispose();
			}
		});
		
		
		this.pack();
		WDUtil.autoAlign(this);
		this.setVisible(true);
		
	}
	
}
