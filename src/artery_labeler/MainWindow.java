package artery_labeler;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

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
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;

public class MainWindow {

	protected Shell shell;
	public static Display display;
	private static String dbName="flowDb";
	private String connURL = "jdbc:derby:memory:" + dbName + ";create=true";
	private static String dropURL = "jdbc:derby:memory:" + dbName + ";drop=true";
	public static Database db;
	static Canvas graphCanvas;
	int startInterval;
	int endInterval = 0;
	Boolean inInterval = false;
	String intervalType;

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
	 * @throws SQLException 
	 */
	public void open() throws SQLException {
		
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
	 * @throws SQLException 
	 */
	protected void createContents() throws SQLException {
		db = new Database(connURL);
		
		Statement st = Database.conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_UPDATABLE);
		ResultSet res = st.executeQuery("SELECT * FROM ABP_ID");
		
		display = Display.getDefault();
		
		shell = new Shell();
		shell.setSize(1164, 757);
		shell.setText("Artery Data Labeler");
		shell.setLayout(new FillLayout(SWT.HORIZONTAL));
		
		graphCanvas = new Canvas(shell, SWT.NONE);
		graphCanvas.addKeyListener(new KeyAdapter()
		{	
			public void keyPressed(KeyEvent e)
			{
				
				if(e.keyCode == 's'){
					intervalType = "systole";
					System.out.println(intervalType);
				}
				
				if(e.keyCode == 'd'){
					intervalType = "diastole";
					System.out.println(intervalType);
				}
				/*
				if(e.keyCode == SWT.CR){
					try {
						if(intervalType == "systole" || intervalType == "diastole"){}
						else{throw new Exception();}
						if(inInterval == true){
							endInterval = res.getRow()-625;
							System.out.println("end: "+endInterval);
							int intervalLength = endInterval - startInterval;
							int count = 0;
							if(intervalType == "systole"){
								//Statement st = Database.conn.createStatement();
								for(int i=0; i<intervalLength; i++){
									int id = startInterval + i;
									int c = st.executeUpdate("UPDATE ABP_ID SET CLASS=1 WHERE ID="+ id);
									count = count + c;
									//System.out.println(res.getRow());
								}
								intervalType = "diastole";
								System.out.println(intervalType);
								//System.out.println(res.getRow());
							}
							else if(intervalType == "diastole"){
								//Statement st = Database.conn.createStatement();
								for(int i=0; i<intervalLength; i++){
									int id = startInterval + i;
									int c = st.executeUpdate("UPDATE ABP_ID SET CLASS=-1 WHERE ID="+ id);
									count = count + c;
								}
								intervalType = "systole";
								System.out.println(intervalType);
								System.out.println(res.getRow());
							}
							inInterval = false;
							System.out.println(count + " rows were set");
							
							
						}else if(inInterval == false){
							startInterval = res.getRow()-625;
							inInterval = true;
							System.out.println("start: "+startInterval);
							System.out.println(res.getRow());
						}
					} catch (Exception e1) {
						// TODO Auto-generated catch block
						System.out.println("You must define an interval type! Press 's' or 'd'.");
						e1.printStackTrace();
					}
				}
				*/
				if(e.keyCode == SWT.CR){
					try{
						int intervalLength;
						int count = 0;
						if(intervalType == "systole"){
							if(inInterval == false){
								startInterval = res.getRow()-625;
								System.out.println("start systole: "+startInterval);
								inInterval = true;
							}
							else{
								endInterval = res.getRow()-625;
								
								intervalLength = endInterval - startInterval;
								
								res.absolute(res.getRow()-625-intervalLength);
								
								for(int i=0; i<intervalLength; i++){
									res.updateObject("CLASS", 1);
									res.updateRow();
									res.next();
									count = i+1;
								}
								res.updateObject("CLASS", 1);
								res.updateRow();
								res.absolute(res.getRow()+625);
								System.out.println(count +" records were turned into systole.");
								inInterval = false;
								intervalType = "diastole";
								Graph.advance(res);
								
							}
						}
					
						else if(intervalType == "diastole"){
							if(inInterval == false){
								startInterval = res.getRow()-625;
								System.out.println("start diastole: "+startInterval);
								inInterval = true;
							}
							else{
								endInterval = res.getRow()-625;
								
								intervalLength = endInterval - startInterval;
								
								res.absolute(res.getRow()-625-intervalLength);
								for(int i=0; i<intervalLength; i++){
									res.updateObject("CLASS", -1);
									res.updateRow();
									res.next();
									count = i+1;
								}
								res.updateObject("CLASS", -1);
								res.updateRow();
								res.absolute(res.getRow()+625);
								System.out.println(count +" records were turned into diastole.");
								inInterval = false;
								intervalType = "systole";
								Graph.advance(res);
								
							}
						}
					
						else{
							System.out.println("You must define an interval type. Press 's' or 'd'.");
						}
					}catch(Exception e1){
						e1.printStackTrace();
					}
				}
				
				if(e.keyCode == SWT.ARROW_RIGHT){
					try {
						Graph.advance(res);
						
					} catch (SQLException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}
				
				if(((e.stateMask & SWT.SHIFT) == SWT.SHIFT) && (e.keyCode == SWT.ARROW_RIGHT)){
					try {
						Graph.advance10(res);
						
					} catch (SQLException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}
			
				if(e.keyCode == SWT.ARROW_LEFT){
					try {
						Graph.retreat(res, 1250);
						
					} catch (SQLException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}
				
				if(((e.stateMask & SWT.SHIFT) == SWT.SHIFT) && (e.keyCode == SWT.ARROW_LEFT)){
					try {
						Graph.retreat10(res, 1250);
						
					} catch (SQLException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}
			}
		});
		  
		final LightweightSystem arealws = new LightweightSystem(graphCanvas);
		Graph areaFigure = new Graph();
		arealws.setContents(areaFigure);
		
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
                if (path != null){
                	try {
                		Database.path = path;
						Database.createStatements();
						Database.importCSV();
						Database.addToABP_ID();
						
						Graph.init(res, 1250);

						
					} catch (SQLException e1) {
						e1.printStackTrace();
					}
                }
			}
                
		});
		mntmOpen.setText("Insert into database");
	}	
}
