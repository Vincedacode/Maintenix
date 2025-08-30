package org.example.Maintenix.DAO;
import com.mongodb.client.*;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import io.github.cdimascio.dotenv.Dotenv;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.bson.*;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.example.Maintenix.DBConnection;

import java.util.ArrayList;
import java.util.Date;
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
                    .append("Password",password)
                    .append("created_at", new Date());
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

    /**
     * Get staff information by username - KEY METHOD FOR PERSONALIZATION
     */
    public Document getStaffByUsername(String username) {
        try {
            Bson usernameFilter = Filters.eq("Username", username);
            return collection.find(usernameFilter).first();
        } catch (Exception e) {
            System.err.println("Error retrieving staff by username: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Get staff ObjectId by username - CRITICAL for filtering collections
     */
    public ObjectId getStaffIdByUsername(String username) {
        try {
            Document staff = getStaffByUsername(username);
            if (staff != null) {
                return staff.getObjectId("_id");
            }
        } catch (Exception e) {
            System.err.println("Error getting staff ID by username: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    public boolean usernameExists(String username) {
        try {
            Document query = new Document("Username", username); // Adjust field name to match your database
            return collection.find(query).first() != null;
        } catch (Exception e) {
            System.err.println("Error checking username existence: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public String getFullNameByUsername(String username) {
        try {
            Document query = new Document("Username", username); // Adjust field name to match your database
            Document staff = collection.find(query).first();

            if (staff != null) {
                return staff.getString("Fullname"); // Adjust field name to match your database
            }
        } catch (Exception e) {
            System.err.println("Error finding staff by username: " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }



    /**
     * Get staff full name by username
     */
    public String getStaffFullNameByUsername(String username) {
        try {
            Document staff = getStaffByUsername(username);
            return staff != null ? staff.getString("Fullname") : "Unknown Staff";
        } catch (Exception e) {
            System.err.println("Error getting staff full name: " + e.getMessage());
            return "Unknown Staff";
        }
    }

    /**
     * Get all staff names for dropdown in maintenance request form
     */
    public ObservableList<String> getAllStaffNames() {
        ObservableList<String> staffNames = FXCollections.observableArrayList();

        try {
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

    // Add this method to your existing staffdao class

    /**
     * Get staff ObjectId by username - CRITICAL METHOD FOR PERSONALIZATION
     */

}

