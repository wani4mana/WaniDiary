package net.wanisys.wanidiary;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.print.PrinterException;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Document;
import javax.swing.text.Highlighter;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoManager;


public class WaniDiary extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	protected JTextArea WD_textarea;
	protected JFileChooser WD_filechooser;
	protected JToolBar WDToolBar;
	protected Container cp;
	protected JComboBox WD_fonts;
	protected JComboBox WD_Sizes;
	protected Font myfont = null;
	protected String myfontname;
	protected int myfontsize;
	protected JToggleButton boldbtn;
	protected JToggleButton italicbtn;
	protected boolean isBold = false;
	protected boolean isItalic = false;
	
	public static final String ASTERISK_TITLEBAR = "unsaved";
	protected String title;

	protected UndoManager WD_undoManager = new UndoManager();
	protected JMenuItem miUndo;
	protected JMenuItem miRedo;
	protected JButton undobtn;
	protected JButton redobtn;
	protected File WDFile = null; 
	protected Document WDDoc;
	protected JLabel statusLabel;
	
	protected JMenu mFile;
	protected JMenu mHistory;
	private int MAXHISTORY = 3;
    private Vector<String> fh = new Vector<String>();
    private JMenuItem noFile = new JMenuItem("no");

    private String keyword = null;
    private static final Highlighter.HighlightPainter WD_hlPainter = new DefaultHighlighter.DefaultHighlightPainter(Color.YELLOW);
    private boolean keywordHL = false;
    private StatusTipManager stManager;
    
    protected JMenuBar WD_menuBar;
    protected JPanel WD_statusBar;
    
    protected GraphicsEnvironment ge;
    
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		new WaniDiary();
		
	}
	
	public WaniDiary() {
		super("Untitled - Wani Diary");
		this.setSize(700, 500);
		//this.setDefaultCloseOperation(EXIT_ON_CLOSE);
		this.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		this.setIconImage(Toolkit.getDefaultToolkit().createImage("images/icon.jpg"));
		
		this.title = this.getTitle();
		this.addPropertyChangeListener(new PropertyChangeListener() {
			
			@Override
			public void propertyChange(PropertyChangeEvent arg0) {
			     if(ASTERISK_TITLEBAR.equals(arg0.getPropertyName())) {
			         Boolean unsaved = (Boolean)arg0.getNewValue();
			         WaniDiary.this.setTitle((unsaved?"* ":"")+WaniDiary.this.title);
			       }
			}
			
		});

		this.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				WD_Exit();
			}
//			@Override
//			public void windowClosed(WindowEvent e) {
//				System.exit(0);
//			}
			
		});
		
		ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		
		cp = getContentPane();

		WD_textarea = new JTextArea();
		WDDoc = WD_textarea.getDocument();
		
		this.makeDocListener();
		JScrollPane jsp = new JScrollPane(WD_textarea);
		
		
		cp.add(jsp, BorderLayout.CENTER);

		WD_statusBar = this.createWDStatusBar();
		stManager = new StatusTipManager(statusLabel);

		cp.add(WD_statusBar, BorderLayout.SOUTH);
		
		WD_menuBar = this.createWDMenuBar();
		setJMenuBar(WD_menuBar);

		WD_filechooser = new JFileChooser(); 
		WD_filechooser. setCurrentDirectory(new File("."));
		WD_filechooser.addChoosableFileFilter(new FileFilter() {
			
			@Override
			public String getDescription() {
				return "Diary File(*.txt)";
			}
			
			@Override
			public boolean accept(File f) {
				if(f.isDirectory()) return true;
				return f.getName().toLowerCase().endsWith(".txt");
			}
		});

		WDUtil.autoAlign(this);
		this.setVisible(true);
		
	}
	
	
	protected void makeDocListener(){

		WDDoc.addDocumentListener(new DocumentListener() {
			
			@Override
			public void removeUpdate(DocumentEvent e) {
				fireUnsavedFlagChangeEvent(true);				
			}
			
			@Override
			public void insertUpdate(DocumentEvent e) {
				fireUnsavedFlagChangeEvent(true);					
			}
			
			@Override
			public void changedUpdate(DocumentEvent e) {
			}
		});
		
		WDDoc.addUndoableEditListener(new UndoableEditListener() {						
			@Override
			public void undoableEditHappened(UndoableEditEvent arg0) {
				WD_undoManager.addEdit(arg0.getEdit());
				updateUndoState();
			}
		});	
	}
	
	protected JPanel createWDStatusBar() {
		
		statusLabel = new JLabel();
		JPanel statusPanel = new JPanel();
		statusPanel.setLayout(new BorderLayout(10,0));
		statusPanel.setBackground(Color.lightGray);
		JProgressBar statusPbar = new JProgressBar(0,100);
		statusPbar.setPreferredSize(new Dimension(80,20));
		statusPbar.setValue(0);
		statusPanel.add(statusPbar, "West");
		statusPanel.add(statusLabel, "Center");

		JPanel keystatus = new JPanel(new GridLayout(1, 3));
		final JLabel numLock = new JLabel("", JLabel.CENTER);
		keystatus.add(numLock);
		final JLabel capsLock = new JLabel("", JLabel.CENTER);
		keystatus.add(capsLock);
		final JLabel scrollLock = new JLabel("", JLabel.CENTER);
		keystatus.add(scrollLock);

		statusPanel.add(keystatus, "East");

		setKeyStatus(numLock, KeyEvent.VK_NUM_LOCK, "Num");
		setKeyStatus(capsLock, KeyEvent.VK_CAPS_LOCK, "Caps");
		setKeyStatus(scrollLock, KeyEvent.VK_SCROLL_LOCK, "Scroll");

		KeyListener listener = new KeyAdapter() {
			public void keyPressed(KeyEvent keyEvent) {
				if (keyEvent.getKeyCode() == KeyEvent.VK_NUM_LOCK) {
					setKeyStatus(numLock, KeyEvent.VK_NUM_LOCK, "Num");
				} else if (keyEvent.getKeyCode() == KeyEvent.VK_CAPS_LOCK) {
					setKeyStatus(capsLock, KeyEvent.VK_CAPS_LOCK, "Caps");
				} else if (keyEvent.getKeyCode() == KeyEvent.VK_SCROLL_LOCK) {
					setKeyStatus(scrollLock, KeyEvent.VK_SCROLL_LOCK, "Scroll");
				}
			}
		};
		WD_textarea.addKeyListener(listener);
        
		return statusPanel;
	}
	
	
	protected JMenuBar createWDMenuBar() {
		
		JMenuBar WDMenuBar = new JMenuBar();
		mFile = new JMenu("File");
		
		//////////////////////////////////
		JMenuItem miNew = new JMenuItem("New");
		miNew.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, ActionEvent.CTRL_MASK));
		ImageIcon newicon = new ImageIcon("images/file_new.gif");
		miNew.setIcon(newicon);
		ActionListener newAL = new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				if(WaniDiary.this.title.equals(WaniDiary.this.getTitle())||WD_textarea.getText().equals("")) {
					WD_New();
				}
				else {
					int ret = confirmSave();
					if (ret == 0){ 
					if(WD_Save(false))WD_New();
					}
					else if(ret == 1){
					WD_New();	
					}
					else {
					//cancel	
					}
				}
			}
		};
		
		miNew.addActionListener(newAL);
		stManager.setStatusTip(miNew, "You can make a new diary!");

		mFile.add(miNew);
		
		//////////////////////////////////////
		JMenuItem miOpen = new JMenuItem("Open...");
		miOpen.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, ActionEvent.CTRL_MASK));
		ImageIcon openicon = new ImageIcon("images/file_open.gif");

		miOpen.setIcon(openicon);
		ActionListener openAL = new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				if(WaniDiary.this.title.equals(WaniDiary.this.getTitle())||WD_textarea.getText().equals("")) {
					WD_Open();
				}
				else {
					int ret = confirmSave();
					if (ret == 0){ 
					if(WD_Save(false)) WD_Open();
					}
					else if(ret == 1){
					WD_Open();	
					}
					else {
					//cancel	
					}
				}
					
			}
		};
		
		miOpen.addActionListener(openAL);
		stManager.setStatusTip(miOpen, "You can open diary file!");
		
		mFile.add(miOpen);

		//////////////////////////////////////////////////
		JMenuItem miSave = new JMenuItem("Save");
		miSave.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK));
		ImageIcon saveicon = new ImageIcon("images/file_save.gif");
		miSave.setIcon(saveicon);
		ActionListener saveAL = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				
				WD_Save(false);
			}
		};
		
		miSave.addActionListener(saveAL);
		stManager.setStatusTip(miSave, "You can save your diary!");
		
		
		mFile.add(miSave);
		
		//////////////////////////////////////////////////
		JMenuItem miSaveas = new JMenuItem("Save As...");
		ImageIcon saveasicon = new ImageIcon("images/file_saveas.gif");
		miSaveas.setIcon(saveasicon);
		ActionListener saveasAL = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				
				WD_Save(true);
			}
		};
		
		miSaveas.addActionListener(saveasAL);
		stManager.setStatusTip(miSaveas, "You can save your diary as ...!");
		
		
		mFile.add(miSaveas);
		
		mFile.addSeparator();
		//////////////////////////////////////////////////
		JMenuItem miPrint = new JMenuItem("Print");
		miPrint.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P, ActionEvent.CTRL_MASK));
		ImageIcon printicon = new ImageIcon("images/file_print.gif");
		miPrint.setIcon(printicon);
		ActionListener printAL = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				WD_Print();
			}
		};
		
		miPrint.addActionListener(printAL);
		stManager.setStatusTip(miPrint, "You can print your diary!");
		
		
		mFile.add(miPrint);

		mFile.addSeparator();
		mFile.add(initHistory());
		mFile.addSeparator();
		////////////////////////////////////////////////
		JMenuItem miExit = new JMenuItem("Exit");
		//miExit.setIcon(new ImageIcon("images/file_exit.gif"));
		miExit.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
			WD_Exit();	
			}
		});
		stManager.setStatusTip(miExit, "You can exit Wani Diary!");
		
		mFile.add(miExit);
		
		WDMenuBar.add(mFile);
		/////////////////////////
		//Edit Menu
		JMenu mEdit = new JMenu("Edit");
		JMenuItem miCopy = new JMenuItem("Copy");
		miCopy.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, ActionEvent.CTRL_MASK));
		ImageIcon copyicon = new ImageIcon("images/edit_copy.gif");		
		miCopy.setIcon(copyicon);
		ActionListener copyAL = new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				WD_textarea.copy();
			}
		};		
		miCopy.addActionListener(copyAL);
		stManager.setStatusTip(miCopy, "You can copy what you want!");
		
		JMenuItem miCut = new JMenuItem("Cut");
		miCut.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, ActionEvent.CTRL_MASK));
		ImageIcon cuticon = new ImageIcon("images/edit_cut.gif");
		miCut.setIcon(cuticon);
		ActionListener cutAL = new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				WD_textarea.cut();
			}
		};
		miCut.addActionListener(cutAL);
		stManager.setStatusTip(miCut, "You can cut what you want!");
		
		JMenuItem miPaste = new JMenuItem("Paste");
		miPaste.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V, ActionEvent.CTRL_MASK));
		ImageIcon pasteicon = new ImageIcon("images/edit_paste.gif");
		miPaste.setIcon(pasteicon);
		ActionListener pasteAL = new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				WD_textarea.paste();
			}
		};
		miPaste.addActionListener(pasteAL);
		stManager.setStatusTip(miPaste, "You can paste what you copied!");
		
		mEdit.add(miCopy);
		mEdit.add(miCut);
		mEdit.add(miPaste);

		
		//
		miUndo = new JMenuItem("Undo");
		miUndo.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, ActionEvent.CTRL_MASK ));
		ImageIcon undoicon = new ImageIcon("images/edit_undo.gif");
		miUndo.setIcon(undoicon);
		miUndo.setEnabled(false);
		ActionListener undoAL = new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				try {
					WD_undoManager.undo();
				} catch (CannotUndoException e) {
					e.printStackTrace();
				}
				updateUndoState();
			}
		};
		miUndo.addActionListener(undoAL);
		stManager.setStatusTip(miUndo, "You can undo what's done!");
		
		miRedo = new JMenuItem("Redo");
		miRedo.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Y, ActionEvent.CTRL_MASK ));
		ImageIcon redoicon = new ImageIcon("images/edit_redo.gif");
		miRedo.setIcon(redoicon);
		miRedo.setEnabled(false);
		ActionListener redoAL = new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				try {
					WD_undoManager.redo();
				} catch (CannotUndoException e) {
					e.printStackTrace();
				}
				updateUndoState();
			}
		};
		miRedo.addActionListener(redoAL);
		stManager.setStatusTip(miRedo, "You can redo what's undone! ");
		
		mEdit.add(miUndo);
		mEdit.add(miRedo);
		mEdit.addSeparator();

		JMenuItem miFind = new JMenuItem("Find");
		miFind.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F, ActionEvent.CTRL_MASK ));
		ImageIcon findicon = new ImageIcon("images/edit_find.gif");
		miFind.setIcon(findicon);
		ActionListener findAL = new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {

				try{
					keyword = JOptionPane.showInputDialog("Type the word to find");
					if ( keyword.equals(null) || keyword.equals("")) {
						JOptionPane.showMessageDialog(WaniDiary.this,"You must type the word to find!","Aborted",JOptionPane.WARNING_MESSAGE);
						return;
					}
					while(WD_textarea.getText().indexOf(keyword) == -1){
						JOptionPane.showMessageDialog(WaniDiary.this,"Word not found!","No match",JOptionPane.WARNING_MESSAGE);
						keyword = JOptionPane.showInputDialog("Type the word to find");
					}
					WD_textarea.select(WD_textarea.getText().indexOf(keyword), WD_textarea.getText().indexOf(keyword) + keyword.length());
		            setHighlight(keyword);
				}
				catch(Exception ex){
					JOptionPane.showMessageDialog(WaniDiary.this,"Search canceled","Aborted",JOptionPane.WARNING_MESSAGE);
				}
				

			}
		};
		miFind.addActionListener(findAL);
		stManager.setStatusTip(miFind, "You can search your diary with any keyword!");
		
		mEdit.add(miFind);

//		JMenuItem miNext = new JMenuItem("Find Next");
//		miNext.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F3, ActionEvent.CTRL_MASK));
//		ActionListener nextAL = new ActionListener() {
//			
//			@Override
//			public void actionPerformed(ActionEvent e) {
//				
//				WD_textarea.select(WD_textarea.getText().indexOf(keyword,(int)WD_textarea.getText().indexOf(keyword)+1), WD_textarea.getText().indexOf(keyword,(int)WD_textarea.getText().indexOf(keyword)+1));
//				
//			}
//		};
//		miNext.addActionListener(nextAL);
//		mEdit.add(miNext);

		WDMenuBar.add(mEdit);
		//////////////////////////
		JMenu mFormat = new JMenu("Format");
		final JCheckBoxMenuItem miLinewrap = new JCheckBoxMenuItem("Line Wrap");
		miLinewrap.setSelected(false);
		miLinewrap.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				if(miLinewrap.isSelected()) {
					WD_textarea.setLineWrap(true);
					WD_textarea.setWrapStyleWord(true);
				}
				else {
					WD_textarea.setLineWrap(false);
					WD_textarea.setWrapStyleWord(false);
				}
			}
		});
		stManager.setStatusTip(miLinewrap, "You can make Linewrap-mode On/Off!");
		
		mFormat.add(miLinewrap);
		WDMenuBar.add(mFormat);
		
		//////////////////////////
		JMenu mView = new JMenu("View");
		final JCheckBoxMenuItem miVMenubar = new JCheckBoxMenuItem("Menu Bar");
		miVMenubar.setSelected(true);
		miVMenubar.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				if(miVMenubar.isSelected()) {
					WD_menuBar.setVisible(true);
				}
				else {
					WD_menuBar.setVisible(false);
				}
			}
		});
		stManager.setStatusTip(miVMenubar, "You can make Menubar visible/invisible!");
		
		final JCheckBoxMenuItem miVToolbar = new JCheckBoxMenuItem("Tool Bar");
		miVToolbar.setSelected(true);
		miVToolbar.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				if(miVToolbar.isSelected()) {
					WDToolBar.setVisible(true);
				}
				else {
					WDToolBar.setVisible(false);
				}
			}
		});
		stManager.setStatusTip(miVToolbar, "You can make Toolbar visible/invisible!");
		
		final JCheckBoxMenuItem miVStatusbar = new JCheckBoxMenuItem("Status Bar");
		miVStatusbar.setSelected(true);
		miVStatusbar.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				if(miVStatusbar.isSelected()) {
					WD_statusBar.setVisible(true);
				}
				else {
					WD_statusBar.setVisible(false);
				}
				
			}
		});
		stManager.setStatusTip(miVStatusbar, "You can make Status-bar visible/invisible!");
		
		JMenuItem miFullScreen = new JMenuItem("Full Screen");
		miFullScreen.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F11, 0));
		ActionListener fullscreenAL = new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				toggleFullScreen();
			}
		};
		miFullScreen.addActionListener(fullscreenAL);
		stManager.setStatusTip(miFullScreen, "You can write your diary on Full-screen mode!");
		
		mView.add(miVMenubar);		
		mView.add(miVToolbar);
		mView.add(miVStatusbar);
		mView.addSeparator();
		mView.add(miFullScreen);
		
		WDMenuBar.add(mView);
		//////////////////////////
		JMenu mHelp = new JMenu("Help");
		JMenuItem miHelp = new JMenuItem("Help Contents");
		miHelp.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				
			}
		});
		stManager.setStatusTip(miHelp, "Wani Diary Help Doc!!");
		
		JMenuItem miAbout = new JMenuItem("About");
		miAbout.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				new AboutDialog(WaniDiary.this);
			}
		});
		stManager.setStatusTip(miAbout, "About this program!!");
		
		mHelp.add(miHelp);		
		mHelp.addSeparator();
		mHelp.add(miAbout);
		
		WDMenuBar.add(mHelp);
		
		//////////////////////////
		WDToolBar = new JToolBar();
		JButton newbtn = new JButton(newicon);
		newbtn.addActionListener(newAL);
		newbtn.setToolTipText("New diary");
		newbtn.setFocusable(false);
		JButton openbtn = new JButton(openicon);
		openbtn.addActionListener(openAL);
		openbtn.setToolTipText("Open diary file");
		openbtn.setFocusable(false);
		JButton savebtn = new JButton(saveicon);
		savebtn.addActionListener(saveAL);
		savebtn.setToolTipText("Save diary file");
		savebtn.setFocusable(false);
		JButton saveasbtn = new JButton(saveasicon);
		saveasbtn.addActionListener(saveasAL);
		saveasbtn.setToolTipText("Save diary file As...");
		saveasbtn.setFocusable(false);
		JButton printbtn = new JButton(printicon);
		printbtn.addActionListener(printAL);
		printbtn.setToolTipText("Print diary");
		printbtn.setFocusable(false);
		
		WDToolBar.add(newbtn);
		WDToolBar.add(openbtn);
		WDToolBar.add(savebtn);
		WDToolBar.add(saveasbtn);
		WDToolBar.add(printbtn);
		////////////////////////////
		WDToolBar.addSeparator();
		JButton copybtn = new JButton(copyicon);
		copybtn.addActionListener(copyAL);
		copybtn.setToolTipText("Copy");
		copybtn.setFocusable(false);
		JButton cutbtn = new JButton(cuticon);
		cutbtn.addActionListener(cutAL);
		cutbtn.setToolTipText("Cut");
		cutbtn.setFocusable(false);
		JButton pastebtn = new JButton(pasteicon);
		pastebtn.addActionListener(pasteAL);
		pastebtn.setToolTipText("Paste");
		pastebtn.setFocusable(false);
		redobtn = new JButton(redoicon);
		redobtn.addActionListener(redoAL);
		redobtn.setToolTipText("Redo");
		redobtn.setFocusable(false);
		redobtn.setEnabled(false);
		undobtn = new JButton(undoicon);
		undobtn.addActionListener(undoAL);
		undobtn.setToolTipText("Undo");
		undobtn.setFocusable(false);
		undobtn.setEnabled(false);
		WDToolBar.add(copybtn);
		WDToolBar.add(cutbtn);
		WDToolBar.add(pastebtn);
		WDToolBar.add(undobtn);
		WDToolBar.add(redobtn);

		////////////
		WDToolBar.addSeparator();
		JButton findbtn = new JButton(findicon);
		findbtn.addActionListener(findAL);
		findbtn.setToolTipText("Find...");
		findbtn.setFocusable(false);
		WDToolBar.add(findbtn);

		////////////
		WDToolBar.addSeparator();
		//GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		String fontnames[] = ge.getAvailableFontFamilyNames();
		
		if( myfont == null) {
			myfont = new Font(fontnames[0], Font.PLAIN, 12);
			myfontname = fontnames[0];
			myfontsize = 12;
			WD_textarea.setFont(myfont);			
		}

		WD_fonts = new JComboBox(fontnames);
		WD_fonts.setMaximumSize(WD_fonts.getPreferredSize());
		WD_fonts.setSelectedItem(myfontname);
		WD_fonts.setToolTipText("Available fonts");
		WD_fonts.setFocusable(false);
		WD_fonts.setEditable(true);
		ActionListener fontAL = new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				int index = WD_fonts.getSelectedIndex();
				if(index < 0) return;
				myfontname = WD_fonts.getSelectedItem().toString();
				updateTextArea(WD_fonts.getSelectedItem().toString());
			}
		};
		WD_fonts.addActionListener(fontAL);
		WDToolBar.add(WD_fonts);
		WDToolBar.addSeparator();
		//
		String fontsizes[] = {"8", "9", "10", "11", "12", "14", "16", "18", "20", "22", "24", "26", "28", "36", "48", "72"};

		WD_Sizes = new JComboBox(fontsizes);
		WD_Sizes.setMaximumSize(WD_Sizes.getPreferredSize());
		WD_Sizes.setSelectedItem(Integer.toString(myfontsize));
		WD_Sizes.setToolTipText("Select Font Size!");
		WD_Sizes.setFocusable(false);
		WD_Sizes.setEditable(true);
		ActionListener sizeAL = new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				int index = WD_Sizes.getSelectedIndex();
				if(index < 0) return;
				myfontsize = Integer.parseInt(WD_Sizes.getSelectedItem().toString());
				updateTextArea(myfontname);
			}
		};
		WD_Sizes.addActionListener(sizeAL);
		WDToolBar.add(WD_Sizes);
		///////////////
		WDToolBar.addSeparator();
		ImageIcon boldicon = new ImageIcon("images/font_bold1.gif");
		boldbtn = new JToggleButton(boldicon);
		boldbtn.setToolTipText("Bold font");
		boldbtn.setFocusable(false);
		ActionListener boldAL = new ActionListener(){
			
			@Override
			public void actionPerformed(ActionEvent e) {
				if (isBold){
					isBold = false;
				}
				else{
					isBold = true;
				}
				updateTextArea("");
			}
		};
		boldbtn.addActionListener(boldAL);
		
		
		ImageIcon italicicon = new ImageIcon("images/font_italic1.gif");
		italicbtn = new JToggleButton(italicicon);
		italicbtn.setToolTipText("Italic font");
		italicbtn.setFocusable(false);
		ActionListener italicAL = new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				if (isItalic){
					isItalic = false;
				}
				else {
					isItalic = true;
				}
				updateTextArea("");
			}
		};
		
		italicbtn.addActionListener(italicAL);
		
		WDToolBar.add(boldbtn);
		WDToolBar.add(italicbtn);
		///////////////
		cp.add(WDToolBar, BorderLayout.NORTH);
		
		/////////////////////////
		final JPopupMenu WDPopupMenu = new JPopupMenu();
		
		final JMenuItem pmiCut = new JMenuItem("Cut");
		pmiCut.addActionListener(cutAL);
		final JMenuItem pmiCopy = new JMenuItem("Copy");
		pmiCopy.addActionListener(copyAL);
		JMenuItem pmiPaste = new JMenuItem("Paste");
		pmiPaste.addActionListener(pasteAL);
		final JMenuItem pmiDelete = new JMenuItem("Delete");
		ActionListener deleteAL = new ActionListener() {			
			@Override
			public void actionPerformed(ActionEvent e) {
				WD_textarea.replaceSelection(null);
			}
		};
		pmiDelete.addActionListener(deleteAL);
		JMenuItem pmiSelectAll = new JMenuItem("SelectAll");
		ActionListener selectallAL = new ActionListener() {			
			@Override
			public void actionPerformed(ActionEvent e) {
				WD_textarea.selectAll();
			}
		};
		pmiSelectAll.addActionListener(selectallAL);

		JMenuItem pmiClear = new JMenuItem("Clear");
		ActionListener clearAL = new ActionListener() {			
			@Override
			public void actionPerformed(ActionEvent e) {
				WD_textarea.setText("");
			}
		};
		
		pmiClear.addActionListener(clearAL);

		final JMenuItem pmiDeleteHL = new JMenuItem("Delete Highlights");
		ActionListener deletehlAL = new ActionListener() {			
			@Override
			public void actionPerformed(ActionEvent e) {
				removeHighlights();
			}
		};
		
		pmiDeleteHL.addActionListener(deletehlAL);

		
		WDPopupMenu.add(pmiCut);
		WDPopupMenu.add(pmiCopy);
		WDPopupMenu.add(pmiPaste);
		WDPopupMenu.add(pmiDelete);
		WDPopupMenu.addSeparator();
		WDPopupMenu.add(pmiSelectAll);
		WDPopupMenu.add(pmiClear);
		WDPopupMenu.add(pmiDeleteHL);
		
		WD_textarea.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				checkForTriggerEvent(e);
			}
			@Override
			public void mouseReleased(MouseEvent e) {
				checkForTriggerEvent(e);
			}
			
			private void checkForTriggerEvent(MouseEvent e) {
				if( e.isPopupTrigger()) {
					boolean flag;
			    	flag = (WD_textarea.getSelectedText()!=null)? true: false;
			    	pmiCut.setEnabled(flag);
			    	pmiCopy.setEnabled(flag);
			    	pmiDelete.setEnabled(flag);
			    	
			    	pmiDeleteHL.setEnabled(keywordHL);
					WDPopupMenu.show(e.getComponent(), e.getX(), e.getY());
				}
			}
		});
		////////////////////////////
		final JPopupMenu WDPopupMenu2 = new JPopupMenu();
		
		final JCheckBoxMenuItem pmiSMenubar = new JCheckBoxMenuItem("Menu", true);
		pmiSMenubar.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if(pmiSMenubar.isSelected()) {
					WD_menuBar.setVisible(true);
					miVMenubar.setSelected(true);
				}
				else {
					WD_menuBar.setVisible(false);
					miVMenubar.setSelected(false);
				}
				
			}
		});
		final JCheckBoxMenuItem pmiSToolbar = new JCheckBoxMenuItem("Tool Bar", true);
		pmiSToolbar.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if(pmiSToolbar.isSelected()) {
					WDToolBar.setVisible(true);
					miVToolbar.setSelected(true);
				}
				else {
					WDToolBar.setVisible(false);
					miVToolbar.setSelected(false);
				}
				
			}
		});
		final JCheckBoxMenuItem pmiSStatusbar = new JCheckBoxMenuItem("Status Bar", true);
		pmiSStatusbar.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if(pmiSStatusbar.isSelected()) {
					WD_statusBar.setVisible(true);
					miVStatusbar.setSelected(true);
				}
				else {
					WD_statusBar.setVisible(false);
					miVStatusbar.setSelected(false);
				}
				
			}
		});
		
		WDPopupMenu2.add(pmiSMenubar);
		WDPopupMenu2.add(pmiSToolbar);
		WDPopupMenu2.add(pmiSStatusbar);
		
		this.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				checkForTriggerEvent(e);
			}
			@Override
			public void mouseReleased(MouseEvent e) {
				checkForTriggerEvent(e);
			}
			
			private void checkForTriggerEvent(MouseEvent e) {
				if( e.isPopupTrigger()) {
					if( miVMenubar.isSelected()){
						pmiSMenubar.setSelected(true);
					}
					else {
						pmiSMenubar.setSelected(false);						
					}
					if( miVToolbar.isSelected()){
						pmiSToolbar.setSelected(true);
					}
					else {
						pmiSToolbar.setSelected(false);						
					}
					if( miVStatusbar.isSelected()){
						pmiSStatusbar.setSelected(true);
					}
					else {
						pmiSStatusbar.setSelected(false);						
					}
					WDPopupMenu2.show(e.getComponent(), e.getX(), e.getY());
				}
			}
		});
		////////////////////////////
		
		
		return WDMenuBar;
		
	}

	protected void setHighlight(String pattern) {
		removeHighlights();
		try{
			Highlighter hilite = WD_textarea.getHighlighter();
			String text = WDDoc.getText(0, WDDoc.getLength());
			int pos = 0;
			while((pos = text.indexOf(pattern, pos)) >= 0) {
				hilite.addHighlight(pos, pos+pattern.length(), WD_hlPainter);
				pos += pattern.length();
			}
			keywordHL = true;
		}catch(BadLocationException e) {
			e.printStackTrace();
		}
	}
	protected void removeHighlights() {
		Highlighter hilite = WD_textarea.getHighlighter();
		Highlighter.Highlight[] hilites = hilite.getHighlights();
		for(int i=0;i<hilites.length;i++) {
			if(hilites[i].getPainter() instanceof DefaultHighlighter.DefaultHighlightPainter) {
				hilite.removeHighlight(hilites[i]);
			}
		}
		keywordHL = false;
	}

	
	protected void updateTextArea(String fontname) {
		int style = Font.PLAIN;
		
		if(isBold) style |= Font.BOLD;
		if(isItalic) style |= Font.ITALIC;
		if(fontname != "") { 
			myfont = new Font(fontname, Font.PLAIN, myfontsize);
		}
		Font fn = myfont.deriveFont(style);
		WD_textarea.setFont(fn);
		WD_textarea.repaint();		
		
	}

	protected void updateUndoState() {

		miUndo.setText(WD_undoManager.getUndoPresentationName());
		miRedo.setText(WD_undoManager.getRedoPresentationName());
		miUndo.setEnabled(WD_undoManager.canUndo());
		miRedo.setEnabled(WD_undoManager.canRedo());
		undobtn.setEnabled(WD_undoManager.canUndo());
		undobtn.setToolTipText(WD_undoManager.getUndoPresentationName());
		redobtn.setEnabled(WD_undoManager.canRedo());
		redobtn.setToolTipText(WD_undoManager.getRedoPresentationName());

	}
	
	private void WD_Exit() {
		if(this.title.equals(this.getTitle())) {
			this.dispose();
			return;
		}
		Toolkit.getDefaultToolkit().beep();
		Object[] options = { "Save", "Discard", "Cancel" };
		int retValue = JOptionPane.showOptionDialog(this,
				"<html>Save: Exit & Save Changes<br>"+
				"Discard: Exit & Discard Changes<br>"+
				"Cancel: Continue</html>",
				"Exit Options",
				JOptionPane.YES_NO_CANCEL_OPTION,
				JOptionPane.WARNING_MESSAGE, null, options, options[0]);
		if(retValue==JOptionPane.YES_OPTION) {
			boolean ret = WD_Save(false);
			if(ret) { //saved and exit
			this.dispose();
			}else{ //error and cancel exit
			  return;
			}
			this.dispose();
		}else if(retValue==JOptionPane.NO_OPTION) {
			this.dispose();
		}else if(retValue==JOptionPane.CANCEL_OPTION) {
			//cancel
		}
	}
	private void fireUnsavedFlagChangeEvent(boolean unsaved) {
		if(unsaved) {
			firePropertyChange(ASTERISK_TITLEBAR, Boolean.FALSE, Boolean.TRUE);
		}else{
			firePropertyChange(ASTERISK_TITLEBAR, Boolean.TRUE, Boolean.FALSE);
		}
	}

	private void WD_New() {
		WD_textarea.setText("");
		fireUnsavedFlagChangeEvent(false);
		WD_undoManager.die();
		updateUndoState();
		this.setTitle("Untitled - Wani Diary");
		this.title = this.getTitle();
		WDFile = null;
		
	}
	
	private boolean WD_Open(){
		
 		int i = WD_filechooser.showOpenDialog(this);
		if ( i == JFileChooser.APPROVE_OPTION){

			File fo_file = WD_filechooser.getSelectedFile();
				try {
					InputStreamReader fr = new InputStreamReader(new FileInputStream(fo_file), "UTF-8");
					BufferedReader br = new BufferedReader(fr);
//					char[] buff = new char[100000];
//					int nch;
//					while((nch = br.read(buff, 0, buff.length)) != -1)
//					WD_textarea.append(new String(buff, 0, nch));
					WD_textarea.setText(null);
					String str = "";
					while( (str=br.readLine())!= null ){
						WD_textarea.append(str+"\n");
					}
					fr.close();
					br.close();			
					WDFile = fo_file;
					this.setTitle(WDFile.getName()+" - Wani Diary");
					this.title = this.getTitle();
					fireUnsavedFlagChangeEvent(false);
					WD_undoManager.die();
					updateUndoState();
		            String fileName = WDFile.getAbsolutePath();
		            updateHistory(fileName);
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}	
	
			return true;
		}
		if ( i == JFileChooser.CANCEL_OPTION) {
			return false;
		}
		return false;

	}
	
	private boolean WD_Open(File historyfile){
		
			File fo_file = historyfile;
				try {
					InputStreamReader fr = new InputStreamReader(new FileInputStream(fo_file), "UTF-8");
					BufferedReader br = new BufferedReader(fr);
//					char[] buff = new char[100000];
//					int nch;
//					while((nch = br.read(buff, 0, buff.length)) != -1)
//					WD_textarea.append(new String(buff, 0, nch));
					WD_textarea.setText(null);
					String str = "";
					while( (str=br.readLine())!= null ){
						WD_textarea.append(str+"\n");
					}
					fr.close();
					br.close();			
					WDFile = fo_file;
					this.setTitle(WDFile.getName()+" - Wani Diary");
					this.title = this.getTitle();
					fireUnsavedFlagChangeEvent(false);
					WD_undoManager.die();
					updateUndoState();
					return true;
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}	
				return false;
	}
	
	
	private boolean WD_Save(boolean AsMode){
		
		if (AsMode == true ||WDFile == null) {
			int i = WD_filechooser.showSaveDialog(this);
			if ( i == JFileChooser.APPROVE_OPTION){
				File fs_file = WD_filechooser.getSelectedFile();
				try {
					//FileWriter out = new FileWriter(fs_file);
					BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fs_file), "UTF8"));
					WD_textarea.write(bw);
					bw.close();
					//WD_textarea.write(out);
					//out.close();
					this.WDFile = fs_file;
					this.setTitle(this.WDFile.getName()+" - Wani Diary");
					this.title = this.getTitle();
					fireUnsavedFlagChangeEvent(false);
		            String fileName = WDFile.getAbsolutePath();
		            updateHistory(fileName);
				} catch (IOException e) {
					e.printStackTrace();
				}
				return true;
			}		
			if ( i == JFileChooser.CANCEL_OPTION) {
				return false;
			}
		} //if
		else {
			try {
				//FileWriter out = new FileWriter(WDFile);
				//WD_textarea.write(out);
				//out.close();
				BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(WDFile), "UTF8"));
				WD_textarea.write(bw);
				bw.close();
				this.setTitle(this.WDFile.getName()+" - Wani Diary");
				this.title = this.getTitle();
				fireUnsavedFlagChangeEvent(false);
			} catch (IOException e) {
				e.printStackTrace();
			}
			return true;
		}
		return false;
	}
	
	protected void WD_Print() {
		
		try {
			WD_textarea.print();
		} catch (PrinterException e) {
			e.printStackTrace();
		}
	}
	
	protected int confirmSave() {
		String msg = "Diary has been modified! Do you want to save it?";
		int ret = JOptionPane.showConfirmDialog(this, msg, "Wani Diary", JOptionPane.YES_NO_CANCEL_OPTION);
		switch(ret) {
		case JOptionPane.YES_OPTION : return 0;
		case JOptionPane.NO_OPTION : return 1;
		case JOptionPane.CANCEL_OPTION : return 2; 
		default : return 2;
		}
	}
	
	///////////////////
	private JMenu initHistory() {

		if(mHistory==null) {
			mHistory = new JMenu("Latest Files...");
		}else{
			mHistory.removeAll();
		}
		if(fh.size()<=0) {
			noFile.setEnabled(false);
			mHistory.add(noFile);
		}else{
			//fm.remove(noFile);
			for(int i=0;i<fh.size();i++) {
				String name = fh.elementAt(i);
				String num  = Integer.toString(i+1);
				//JMenuItem mi = new JMenuItem(new HistoryAction(new File(name).getAbsolutePath()));
				JMenuItem mi = new JMenuItem(new HistoryAction(new File(name)));
				mi.setText(num + ": "+ name);
				byte[] bt = num.getBytes();
				mi.setMnemonic((int) bt[0]);
				mHistory.add(mi);
			}
		}
		return mHistory;
	}

	private void updateHistory(String str) {
		mHistory.removeAll();
		fh.removeElement(str);
		fh.insertElementAt(str, 0);
		if(fh.size()>MAXHISTORY) fh.remove(fh.size()-1);
		for(int i=0;i<fh.size();i++) {
			String name = fh.elementAt(i);
			String num  = Integer.toString(i+1);
			//JMenuItem mi = new JMenuItem(new HistoryAction(name));
			JMenuItem mi = new JMenuItem(new HistoryAction(new File(name)));
			mi.setText(num + ": "+ name);
			byte[] bt = num.getBytes();
			mi.setMnemonic((int) bt[0]);
			mHistory.add(mi, i);
		}
	}

	class HistoryAction extends AbstractAction{
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		final private File file;
		public HistoryAction(File file_) {
			super();
			file = file_;
		}


		public void actionPerformed(ActionEvent evt) {
			if(WaniDiary.this.title.equals(WaniDiary.this.getTitle())||WD_textarea.getText().equals("")) {
				WD_Open(file);
			}
			else {
				if(WD_Save(false)) WD_Open(file);
			}
			//repaint();
			updateHistory(file.getAbsolutePath());

		}
	}

	
	private void toggleFullScreen(){

		GraphicsDevice graphicsDevice = ge.getDefaultScreenDevice();
		if(graphicsDevice.getFullScreenWindow()==null) {
			this.dispose(); //destroy the native resources
			this.setUndecorated(true);			
			this.setVisible(true); //rebuilding the native resources
			graphicsDevice.setFullScreenWindow(this);
		}else{
			graphicsDevice.setFullScreenWindow(null);
			this.dispose();
			this.setUndecorated(false);
			this.setVisible(true);
			this.repaint();
		}
		this.requestFocusInWindow(); //for Ubuntu
	
	}

///////////////
	private void setKeyStatus(JLabel label, int key, String prefix) {

		String status = (Toolkit.getDefaultToolkit().getLockingKeyState(key) ? "On" : "Off");
		label.setText(prefix+":"+status+" ");

	}
	   
}

