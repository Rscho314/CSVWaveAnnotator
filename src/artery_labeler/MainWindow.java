package artery_labeler;

import java.sql.SQLException;

import org.eclipse.draw2d.LightweightSystem;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;

import artery_labeler.Database;
import artery_labeler.Graph;

import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;

public class MainWindow {

	protected Shell shell;
	public static Display display;
	private static String dbName="flowDb";
	private String connURL = "jdbc:derby:memory:" + dbName + ";create=true";
	private static String dropURL = "jdbc:derby:memory:" + dbName + ";drop=true";
	public static Database db;

	/**
	 * Launch the application.
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			MainWindow window = new MainWindow();
			window.open();
			db.drop(dropURL);
			System.exit(0);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Open the window.
	 */
	public void open() {
		
		createContents();
		shell.open();
		shell.layout();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
	}

	/**
	 * Create contents of the window.
	 */
	protected void createContents() {
		db = new Database(connURL);
		display = Display.getDefault();
		
		shell = new Shell();
		shell.setSize(1164, 757);
		shell.setText("Artery Data Labeler");
		shell.setLayout(new FillLayout(SWT.HORIZONTAL));
		
		Canvas graphCanvas = new Canvas(shell, SWT.NONE);
		final LightweightSystem arealws = new LightweightSystem(graphCanvas);
		
		Menu menu = new Menu(shell, SWT.BAR);
		shell.setMenuBar(menu);
		
		MenuItem mntmFile_1 = new MenuItem(menu, SWT.CASCADE);
		mntmFile_1.setText("File");
		
		Menu menu_1 = new Menu(mntmFile_1);
		mntmFile_1.setMenu(menu_1);
		
		MenuItem mntmOpen = new MenuItem(menu_1, SWT.NONE);
		mntmOpen.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				FileDialog dialog = new FileDialog(shell);
                String path = dialog.open();
                if (path != null)
                	try {
                		Database.path = path;
						Database.createStatements();
						Database.importCSV();
						Database.addToABP_ID();
					} catch (SQLException e1) {
						System.out.println(path);
						e1.printStackTrace();
					}
			}
		});
		mntmOpen.setText("Insert into database");
		Graph areaFigure = new Graph();
		arealws.setContents(areaFigure);

	}
}
