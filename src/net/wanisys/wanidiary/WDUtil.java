package net.wanisys.wanidiary;

import java.awt.Toolkit;
import java.awt.Window;

public class WDUtil {

	public static void autoAlign(Window win) {
		int sw = Toolkit.getDefaultToolkit().getScreenSize().width;
		int sh = Toolkit.getDefaultToolkit().getScreenSize().height;
		int dw = win.getSize().width;
		int dh = win.getSize().height;
		int x = (((sw - dw) / 2) < 0) ? 0 : ((sw - dw) / 2);
		int y = (((sh - dh) / 2) < 0) ? 0 : ((sh - dh) / 2);

		win.setLocation(x, y);
	}
	
}
