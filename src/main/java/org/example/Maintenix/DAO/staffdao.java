package org.example.Maintenix.DAO;
import com.mongodb.client.*;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import io.github.cdimascio.dotenv.Dotenv;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.bson.*;
import org.bson.conversions.Bson;
import org.example.Maintenix.DBConnection;

import java.util.ArrayList;
import java.util.List;
public class staffdao {
    private final MongoCollection<Document> collection;

    public staffdao(){
        Dotenv dotenv = Dotenv.load();
        String dbname = dotenv.get("DB_NAME");
        String collectionname = dotenv.get("STAFF_COLLECTION_NAME");
        this.collection = DBConnection.createConnection(dbname,collectionname);
    }

    public void registerStaff(String username, String department, String fullname, String email, String password){
        try {
            Document staffDocument = new Document()
                    .append("Username",username)
                    .append("Fullname",fullname)
                    .append("Department",department)
                    .append("Email",email)
                    .append("Password",password);
            collection.insertOne(staffDocument);
            System.out.println("Staff saved successfully!");
        }
        catch (Exception e){
            System.out.println(e.getMessage());
        }
    }

    public boolean checkStaffEmail(String email) {
        try {
            Bson emailFilter = Filters.eq("Email", email);
            Document result = collection.find(emailFilter).first();

            return result != null;
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return false;
        }
    }

    public Document loginStaff(String email, String password) {
        try {
            Bson filter = Filters.and(
                    Filters.eq("Email", email),
                    Filters.eq("Password", password)
            );

            return collection.find(filter).first();
        }catch (Exception e){
            System.out.println(e.getMessage());
            return null;
        }
    }

    // Add this method to your existing staffdao.java class

    /**
     * Get all staff names for dropdown in maintenance request form
     */
    public ObservableList<String> getAllStaffNames() {
        ObservableList<String> staffNames = FXCollections.observableArrayList();

        try {
            // Assuming you have a MongoCollection<Document> called staffCollection
            List<Document> staffList = collection.find().into(new ArrayList<>());

            for (Document staff : staffList) {
                String fullName = staff.getString("Fullname");
                if (fullName != null && !fullName.trim().isEmpty()) {
                    staffNames.add(fullName);
                }
            }

        } catch (Exception e) {
            System.err.println("Error retrieving staff names: " + e.getMessage());
            e.printStackTrace();
        }

        return staffNames;
    }

// You'll also need these imports in your staffdao class if not already present:
// import javafx.collections.FXCollections;
// import javafx.collections.ObservableList;
// import java.util.ArrayList;
// import java.util.List;
}
