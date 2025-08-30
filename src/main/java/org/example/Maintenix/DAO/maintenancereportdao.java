package org.example.Maintenix.DAO;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import io.github.cdimascio.dotenv.Dotenv;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.example.Maintenix.DBConnection;

import java.util.Date;
import java.util.ArrayList;
import java.util.List;

public class maintenancereportdao {

    private MongoClient mongoClient;
    private MongoDatabase database;
    private MongoCollection<Document> reportsCollection;
    private MongoCollection<Document> staffCollection;
    private staffdao staffDAO; // Add reference to staff DAO

    // Constructor
    public maintenancereportdao() {
        try {
            Dotenv dotenv = Dotenv.load();
            String dbname = dotenv.get("DB_NAME");
            String staffCollectionName = dotenv.get("STAFF_COLLECTION_NAME");
            String reportsCollectionName = dotenv.get("REPORTS_COLLECTION_NAME");
            this.staffCollection = DBConnection.createConnection(dbname,staffCollectionName);
            this.reportsCollection = DBConnection.createConnection(dbname,reportsCollectionName);
            this.staffDAO = new staffdao(); // Initialize staff DAO

        } catch (Exception e) {
            System.err.println("Error connecting to MongoDB: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Get maintenance requests by username - KEY METHOD FOR PERSONALIZATION
     */
    public List<Document> getMaintenanceRequestsByUsername(String username) {
        List<Document> requests = new ArrayList<>();

        try {
            // Get staff ObjectId from username
            ObjectId staffId = staffDAO.getStaffIdByUsername(username);
            if (staffId != null) {
                Document query = new Document("staff_name", staffId);
                requests = reportsCollection.find(query).into(new ArrayList<>());
                System.out.println("Found " + requests.size() + " maintenance requests for user: " + username);
            } else {
                System.err.println("Staff not found for username: " + username);
            }

        } catch (Exception e) {
            System.err.println("Error retrieving maintenance requests by username: " + e.getMessage());
            e.printStackTrace();
        }

        return requests;
    }

    /**
     * Creates a new maintenance request with image
     */
    public boolean createMaintenanceRequest(String staffName, String issue, String location,
                                            String filename,
                                            String contentType, String imageBase64) {
        try {
            // Get staff ObjectId by name
            ObjectId staffId = getStaffIdByName(staffName);
            if (staffId == null) {
                System.err.println("Staff member not found: " + staffName);
                return false;
            }

            Document maintenanceDoc = new Document()
                    .append("staff_name", staffId)
                    .append("issue", issue)
                    .append("location", location)
                    .append("status", "pending")
                    .append("priority", "low")
                    .append("created_at", new Date());

            // Add image data if provided
            if (filename != null && contentType != null && imageBase64 != null) {
                Document imageDoc = new Document()
                        .append("filename", filename)
                        .append("content_type", contentType)
                        .append("data", imageBase64);
                maintenanceDoc.append("image", imageDoc);
            }

            reportsCollection.insertOne(maintenanceDoc);
            System.out.println("Maintenance request created successfully");
            return true;

        } catch (Exception e) {
            System.err.println("Error creating maintenance request: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Get all staff names for dropdown
     */
    public ObservableList<String> getAllStaffNames() {
        ObservableList<String> staffNames = FXCollections.observableArrayList();

        try {
            List<Document> staffList = staffCollection.find().into(new ArrayList<>());

            for (Document staff : staffList) {
                String fullName = staff.getString("Fullname"); // Match your field name
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
    /**
     * Get staff ObjectId by full name
     */
    private ObjectId getStaffIdByName(String fullName) {
        try {
            Document query = new Document("Fullname", fullName); // Match case exactly
            Document staff = staffCollection.find(query).first();

            if (staff != null) {
                return staff.getObjectId("_id");
            }
        } catch (Exception e) {
            System.err.println("Error finding staff by name: " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Get all maintenance requests for a specific staff member
     */
    public List<Document> getMaintenanceRequestsByStaff(String staffName) {
        List<Document> requests = new ArrayList<>();

        try {
            ObjectId staffId = getStaffIdByName(staffName);
            if (staffId != null) {
                Document query = new Document("staff_name", staffId);
                requests = reportsCollection.find(query).into(new ArrayList<>());
            }

        } catch (Exception e) {
            System.err.println("Error retrieving maintenance requests: " + e.getMessage());
            e.printStackTrace();
        }

        return requests;
    }

}

