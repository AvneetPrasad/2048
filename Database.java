package gameLogic;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Avneet and Kayle
 */
public final class Database {
    
    public static final String Table = "SAVE_FILE";
    public static final String URL ="jdbc:derby:2078SaveFileDB; create=true"; //url of the DB host
    public static final String USERNAME ="2078"; // your DB username
    public static final String PASSWORD ="2078"; // your DB password
    
    private Connection conn = null;
     Statement statement = null;
    
    public Database()throws SQLException
    {
    try{
        // ---- try to connect to the server and create a table called Save_File---//
        conn = DriverManager.getConnection(URL,USERNAME,PASSWORD);
        System.out.println("Connected...");
        statement = conn.createStatement();
        checkExistedTable("Save_File");
        //----- Table contains the ID , score, tile ... values 
        statement.addBatch("CREATE TABLE " +Table +"(ID INT, SCORE INT, TILE INT)");
        statement.executeBatch();
    }  catch(SQLException ex){
        Logger.getLogger(Database.class.getName()).log(Level.SEVERE,null,ex);
    }
    
    }
    
    
    /**
     * Connects database and get the previous data
     * @return 4 x 4 table
     * @throws SQLException
     * @throws IOException 
     */
    public int[][] connect() throws SQLException, IOException{
        if(statement == null){
            return null;
        }
        int[][] data = new int[4][4]; 
        try(BufferedReader fileIn = Files.newBufferedReader(Paths.get(System.getProperty("user.dir"),"save","2048.txt")))
        {
            for(int i = 1; i<=4;i++)
            {
                // insert the the values from the txt file to the drawn table 
                statement.executeUpdate("INSERT INTO "+Table+" VALUED(\'"+fileIn.readLine()+"\')");
            }
        }
        try(ResultSet rs = statement.executeQuery("Select * from " + Table)){
            int row =0;
            while(rs.next()){
                String[] s = rs.getString(1).split(" ");
                for(int j=0;j<4;j++){
                    data[row][j]=Integer.parseInt(s[j]);
                }
                row++;
            }
        }
        return data;
    }
    
    public void close(){
        try{
            if(conn != null){
                conn.close();
            }
            
        } catch(SQLException ex){
            Logger.getLogger(Database.class.getName()).log(Level.SEVERE,null,ex);
        }
    }
    /**
     * Check if the table is exisiting or not or else create 
     * @param name 
     */
    public void checkExistedTable(String name){
        try{
            DatabaseMetaData dbmd = this.conn.getMetaData();
            String[] types = {"TABLE"};
            ResultSet rs = dbmd.getTables(null,null,null,types);
            Statement statement = this.conn.createStatement();
            while(rs.next()){
                String table_name = rs.getString("TABLE_NAME");
                System.out.println(table_name);
                if(table_name.equalsIgnoreCase(name)){
                    statement.executeUpdate("Drop table "+name);
                    System.out.println("Table "+name+" has been deleted.");
                    break;
                }
            }
            rs.close();
            statement.close();
        }catch(SQLException ex){
            Logger.getLogger(Database.class.getName()).log(Level.SEVERE,null,ex);
    }
    }
    /**
     * Main class creates the database
     * @param args
     * @throws SQLException
     * @throws IOException 
     */
    public static void main(String args[])throws SQLException, IOException{
        Database db = new Database();
        db.connect();
       
        
    }
    
    
}
