package edu.najah.cap;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;

import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public class Editor extends JFrame implements ActionListener, DocumentListener {

	public static  void main(String[] args) {
		new Editor();
	}

	public JEditorPane TP;//Text Panel
	private final JMenuBar menu;//Menu
	public boolean changed = false;
	private File file;
	private static final String QUIT = "Quit";
	private static final String OPEN = "Open";
	private static final String SAVE = "Save";
	private static final String SAVE_AS = "Save as...";
	private static final String NEW = "New";
	private static final String COPY = "Copy";
	private static final String CUT = "Cut";
	private static final String PASTE = "Paste";
	private static final String SELECT_ALL = "Select All";
	private static final String FIND = "Find";

	public Editor() {
		//Editor the name of our application
		super("Editor");
		TP = new JEditorPane();
		// center means middle of container.
		add(new JScrollPane(TP), "Center");
		TP.getDocument().addDocumentListener(this);

		menu = new JMenuBar();
		setJMenuBar(menu);
		BuildMenu();
		//The size of window
		setSize(500, 500);
		setVisible(true);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}

	private void BuildMenu() {
		buildFileMenu();
		buildEditMenu();
	}

	private void buildFileMenu() {
		JMenu file = new JMenu("File");
		file.setMnemonic('F');
		menu.add(file);

		JMenuItem n = new JMenuItem(NEW);
		setFileMenuItems(file, n, 'N', KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_DOWN_MASK));

		JMenuItem open = new JMenuItem(OPEN);
		setFileMenuItems(file, open, 'O', KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_DOWN_MASK));

		JMenuItem save = new JMenuItem(SAVE);
		setFileMenuItems(file, save, 'S', KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK));

		JMenuItem saveAs = new JMenuItem(SAVE_AS);
		setFileMenuItems(file, saveAs, 'S', KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_MASK | InputEvent.SHIFT_MASK));

		JMenuItem quit = new JMenuItem(QUIT);
		setFileMenuItems(file, quit, 'Q', KeyStroke.getKeyStroke(KeyEvent.VK_Q, InputEvent.CTRL_DOWN_MASK));
	}

	private void buildEditMenu() {
		JMenu edit = new JMenu("Edit");
		menu.add(edit);
		edit.setMnemonic('E');
		// cut
		JMenuItem cut = new JMenuItem(CUT);
		setFileMenuItems(edit, cut, 'T', KeyStroke.getKeyStroke(KeyEvent.VK_X, InputEvent.CTRL_DOWN_MASK));

		// copy
		JMenuItem copy = new JMenuItem(COPY);
		setFileMenuItems(edit, copy, 'C', KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.CTRL_DOWN_MASK));

		// paste
		JMenuItem paste = new JMenuItem(PASTE);
		setFileMenuItems(edit, paste, 'P', KeyStroke.getKeyStroke(KeyEvent.VK_V, InputEvent.CTRL_DOWN_MASK));

		//move 
		/*
		JMenuItem move = new JMenuItem("Move");
		setFileMenuItems(edit, move, 'M',KeyStroke.getKeyStroke(KeyEvent.VK_V, InputEvent.CTRL_DOWN_MASK));
		*/
		// find
		JMenuItem find = new JMenuItem(FIND);
		setFileMenuItems(edit, find, 'F', KeyStroke.getKeyStroke(KeyEvent.VK_F, InputEvent.CTRL_DOWN_MASK));

		// select all
		JMenuItem selectAll = new JMenuItem(SELECT_ALL);
		setFileMenuItems(edit, selectAll, 'A', KeyStroke.getKeyStroke(KeyEvent.VK_A, InputEvent.CTRL_DOWN_MASK));
	}

	private void setFileMenuItems(JMenu file, JMenuItem menuItem, char mnemonic, KeyStroke keyStroke){
		menuItem.setMnemonic(mnemonic);
		menuItem.setAccelerator(keyStroke);
		menuItem.addActionListener(this);
		file.add(menuItem);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		String action = e.getActionCommand();
		switch(action){
			case QUIT:
				System.exit(0);
				break;
			case OPEN:
				loadFile();
				break;
			case SAVE:
				saveFile();
				break;
			case NEW:
				newFile();
				break;
			case SAVE_AS:
				saveAs(SAVE_AS);
				break;
			case SELECT_ALL:
				TP.selectAll();
				break;
			case COPY:
				TP.copy();
				break;
			case CUT:
				TP.cut();
				break;
			case PASTE:
				TP.paste();
				break;
			case FIND:
				new FindDialog(this, true).showDialog();
				break;
		}
	}

	private void newFile(){
		//Save File
		saveFile();
		//New file
		file = null;
		TP.setText("");
		changed = false;
		setTitle("Editor");
	}

	private void saveFile(){
		int ans = 0;
		if (changed) {
			// 0 means yes and no option, 2 Used for warning messages.
			ans = JOptionPane.showConfirmDialog(null, "The file has changed. You want to save it?", "Save file", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
		}
		//1 value from class method if NO is chosen.
		if (ans != 1) {
			if (file == null) {
				saveAs(SAVE);
			} else {
				writeFile();
			}
		}
	}

	private void writeFile(){
		String text = TP.getText();
		try (PrintWriter writer = new PrintWriter(file)){
			if (!file.canWrite())
				throw new Exception("Cannot write file!");
			writer.write(text);
			changed = false;
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private void loadFile() {
		JFileChooser dialog = new JFileChooser(System.getProperty("user.home"));
		dialog.setMultiSelectionEnabled(false);
		try {
			int result = dialog.showOpenDialog(this);

			if (result == 1)//1 value if cancel is chosen.
				return;
			if (result == 0) {// value if approve (yes, ok) is chosen.
				//Save File
				saveFile();
				file = dialog.getSelectedFile();
				//Read file
				StringBuilder rs = new StringBuilder();
				try (FileReader fr = new FileReader(file);
					 BufferedReader reader = new BufferedReader(fr)) {
					String line;
					while ((line = reader.readLine()) != null) {
						rs.append(line).append("\n");
					}
				} catch (IOException e) {
					e.printStackTrace();
					JOptionPane.showMessageDialog(null, "Cannot read file !", "Error !", JOptionPane.ERROR_MESSAGE);//0 means show Error Dialog
				}

				TP.setText(rs.toString());
				changed = false;
				setTitle("Editor - " + file.getName());
			}
		} catch (Exception e) {
			e.printStackTrace();
			//0 means show Error Dialog
			JOptionPane.showMessageDialog(null, e, "Error", JOptionPane.ERROR_MESSAGE);
		}
	}


	private void saveAs(String dialogTitle) {
		JFileChooser dialog = new JFileChooser(System.getProperty("user.home"));
		dialog.setDialogTitle(dialogTitle);
		int result = dialog.showSaveDialog(this);
		if (result != 0)//0 value if approve (yes, ok) is chosen.
			return;
		file = dialog.getSelectedFile();
		try (PrintWriter writer = new PrintWriter(file)){
			writer.write(TP.getText());
			changed = false;
			setTitle("Editor - " + file.getName());
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	private void saveAsText(String dialogTitle) {
		JFileChooser dialog = new JFileChooser(System.getProperty("user.home"));
		dialog.setDialogTitle(dialogTitle);
		int result = dialog.showSaveDialog(this);
		if (result != 0)//0 value if approve (yes, ok) is chosen.
			return;
		file = dialog.getSelectedFile();
		try (PrintWriter writer = new PrintWriter(file)){
			writer.write(TP.getText());
			changed = false;
			setTitle("Save as Text Editor - " + file.getName());
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void insertUpdate(DocumentEvent e) {
		changed = true;
	}

	@Override
	public void removeUpdate(DocumentEvent e) {
		changed = true;
	}

	@Override
	public void changedUpdate(DocumentEvent e) {
		changed = true;
	}

}