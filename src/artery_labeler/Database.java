package artery_labeler;

import java.io.File;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;

import org.apache.derby.jdbc.EmbeddedDriver;

public class Database {
	
	public static String path;
	private static Connection conn;
    
	private String createABP = "CREATE TABLE ABP (" +
             "TIME INTEGER, " +
             "P DOUBLE" +
             ")";
	
	private String createABP_ID = "CREATE TABLE ABP_ID (" +
			"ID BIGINT NOT NULL PRIMARY KEY GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1)," +
            "P DOUBLE" +
            ")";
	
	private static PreparedStatement csvExp;
	private static PreparedStatement csvImpT;
	private static PreparedStatement insRes;
	private static PreparedStatement emptyABP;
	
	public Database (String connURL){
		try {
			//SimpleDateFormat tf = new SimpleDateFormat ("HH:mm:ss MMM"); 
		    conn = DriverManager.getConnection(connURL);
		    Statement s = conn.createStatement();
		    System.out.println (" . . . . creating ABP table in JavaDB");
		    s.execute(createABP);
		    createStatements();
		    s.execute(createABP_ID);
		}catch (Throwable e)  {   
			System.out.println("Database creation  failed !");
			e.printStackTrace();
		}
	}
	
	public void drop(String dropURL){
		Boolean gotSQLExc = false;
		try {
			File f = new File("pressure.csv");
			if(f.exists() && !f.isDirectory()){f.delete();}
			csvExp.execute();
			DriverManager.getConnection(dropURL);
		   } catch (SQLException se)  {
			   System.out.println(se.getSQLState());
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
		csvExp.setString(2,"ABP_ID");
		csvExp.setString(3,"pressure.csv");
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
		
	}
	
	void insertResults(Timestamp time, double p) throws SQLException{
		insRes.setTimestamp(1, time);
		insRes.setDouble(2, p);
		insRes.execute();
	}
	 
	static void importCSV() throws SQLException{
		 csvImpT.execute();
	 }
	
	static void addToABP_ID() throws SQLException{
		String sql = "INSERT INTO ABP_ID(P) VALUES(?)";  
		PreparedStatement pstmt = conn.prepareStatement(sql);
		Statement statement = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_UPDATABLE);
		ResultSet rs = statement.executeQuery("SELECT * FROM ABP");
		
		while ( rs.next() )  
		{  
			Double p = rs.getDouble(2);
			pstmt.setDouble(1, p);          
			pstmt.executeUpdate();
		}
		emptyABP.execute();
	 }
}
