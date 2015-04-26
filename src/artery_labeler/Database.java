package artery_labeler;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.PrintWriter;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;

import org.apache.derby.jdbc.EmbeddedDriver;

public class Database {
	
	public static String path;
	static Connection conn;
    
	// table used for data input
	private String createABP = "CREATE TABLE ABP (" +
             "TIME INTEGER, " +
             "P DOUBLE" +
             ")";
	
	// table used for the graph
	private String createABP_ID = "CREATE TABLE ABP_ID (" +
			"ID BIGINT NOT NULL PRIMARY KEY GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1)," +
            "CLASS INTEGER, " +
			"P DOUBLE" +
            ")";
	
	// table for csv export
	private String createABP_CSV = "CREATE TABLE ABP_CSV (" +
            "CLASS INTEGER, " +
            "P_STR VARCHAR(10)" +
            ")";
	
	private static PreparedStatement csvExp;
	private static PreparedStatement csvImpT;
	private static PreparedStatement setTo0;
	private static PreparedStatement emptyABP;
	
	static DecimalFormat df = new DecimalFormat("#.###");
	
	public Database (String connURL){
		try {
			df.setRoundingMode(RoundingMode.HALF_UP);
			
		    conn = DriverManager.getConnection(connURL);
		    Statement s = conn.createStatement();
		    System.out.println (" . . . . creating ABP tables in JavaDB");
		    s.execute(createABP);
		    s.execute(createABP_ID);
		    s.execute(createABP_CSV);
		    createStatements();
		}catch (Throwable e)  {   
			System.out.println("Database creation  failed !");
			e.printStackTrace();
		}
	}
	
	public void drop(String dropURL){
		Boolean gotSQLExc = false;
		try {
			File f = new File("temp");
			if(f.exists() && !f.isDirectory()){f.delete();}
			exportToCSV();
			//csvExp.execute();
			DriverManager.getConnection(dropURL);
		   } catch (SQLException se)  {
			   //System.out.println(se.getSQLState());
			   if ( se.getSQLState().equals("08006") ) {
		         gotSQLExc = true;
		      }
		   }
		   if (!gotSQLExc) {
		      System.out.println("Database did not shut down normally");
		   }  else  {
		      System.out.println("Database shut down normally");
		   }
	}
	
	static void createStatements() throws SQLException{
		
		csvExp=conn.prepareStatement(
			    "CALL SYSCS_UTIL.SYSCS_EXPORT_TABLE (?,?,?,?,?,?)");
		csvExp.setString(1,null);
		csvExp.setString(2,"ABP_CSV");
		csvExp.setString(3,"temp");
		csvExp.setString(4,";");
		csvExp.setString(5,"%");
		csvExp.setString(6,null);
		
		csvImpT=conn.prepareStatement(
			    "CALL SYSCS_UTIL.SYSCS_IMPORT_TABLE (?,?,?,?,?,?,?)");
		csvImpT.setString(1,null);
		csvImpT.setString(2,"ABP");
		csvImpT.setString(3,path);
		csvImpT.setString(4,",");
		csvImpT.setString(5,"'");
		csvImpT.setString(6,null);
		csvImpT.setLong(7,0);
		
		emptyABP = conn.prepareStatement("DELETE FROM ABP");
		
		setTo0 = conn.prepareStatement("UPDATE ABP_ID SET CLASS = 0");
		
	}
	 
	static void importCSV() throws SQLException{
		 csvImpT.execute();
	 }
	
	static void addToABP_ID() throws SQLException{
		PreparedStatement pstmt = conn.prepareStatement("INSERT INTO ABP_ID(P) VALUES(?)");
		Statement statement = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_UPDATABLE);
		ResultSet rs = statement.executeQuery("SELECT P FROM ABP");
		
		while ( rs.next() )  
		{  
			Double p = rs.getDouble(1);
			pstmt.setDouble(1, p);
			pstmt.executeUpdate();
		}
		emptyABP.execute();
		setTo0.execute();
		
		//Delete rows where P = 0 to adhere to libSVM format
		PreparedStatement psDel = conn.prepareStatement("DELETE FROM ABP_ID WHERE P = 0");
		psDel.executeUpdate();
	 }
	
	static void exportToCSV() throws SQLException{
		try{
			// Find P max for future scaling of the data
			PreparedStatement ps = conn.prepareStatement("SELECT MAX(P) FROM ABP_ID");
			ResultSet res = ps.executeQuery();
			res.next();
			Double pMax = res.getDouble(1);
			
			// Copy over ABP_ID to ABP_CSV to get rid of the ID in the csv export
			PreparedStatement pstmt = conn.prepareStatement("INSERT INTO ABP_CSV(CLASS, P_STR) VALUES(?,?)");
			Statement statement = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_UPDATABLE);
			ResultSet rs = statement.executeQuery("SELECT * FROM ABP_ID");
		
			while ( rs.next() )  
			{  
				Integer c = rs.getInt(2);
				Double p = rs.getDouble(3);
				// scaling the data & converting to #.### format
				p = p/pMax;
				String p_str = "1:"+ df.format(p);
				pstmt.setInt(1, c);
				pstmt.setString(2, p_str);          
				pstmt.executeUpdate();
			}
			csvExp.execute();
			
			// Replace unwanted characters to get libsvm format
			File temp= new File("temp");
			FileReader fr = new FileReader(temp);
			String s;
			BufferedReader br = new BufferedReader(fr);
			PrintWriter pw = new PrintWriter("pressure.csv");
			while ((s = br.readLine()) != null) {
				String r = s.replaceAll(";", " ");
				r = r.replaceAll("%", "");
		        pw.println(r);
			}
			pw.close();
		}catch(Exception e){e.printStackTrace();}
	}
}
