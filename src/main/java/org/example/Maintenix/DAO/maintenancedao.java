package org.example.Maintenix.DAO;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.ArrayList;
import java.util.List;

public class maintenancedao {

    private MongoClient mongoClient;
    private MongoDatabase database;
    private MongoCollection<Document> maintenanceCollection;
    private MongoCollection<Document> staffCollection;

    // Constructor
    public maintenancedao() {
        try {
            // Replace with your MongoDB connection string
            mongoClient = MongoClients.create("mongodb://localhost:27017");
            database = mongoClient.getDatabase("maintenix"); // Replace with your database name
            maintenanceCollection = database.getCollection("maintenance_requests");
            staffCollection = database.getCollection("Staffs"); // Assuming your staff collection name
        } catch (Exception e) {
            System.err.println("Error connecting to MongoDB: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Creates a new maintenance request with image
     */
    public boolean createMaintenanceRequest(String staffName, String issue, String location,
                                            String status, String priority, String filename,
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
                    .append("status", status)
                    .append("priority", priority)
                    .append("created_at", new Date());

            // Add image data if provided
            if (filename != null && contentType != null && imageBase64 != null) {
                Document imageDoc = new Document()
                        .append("filename", filename)
                        .append("content_type", contentType)
                        .append("data", imageBase64);
                maintenanceDoc.append("image", imageDoc);
            }

            maintenanceCollection.insertOne(maintenanceDoc);
            System.out.println("Maintenance request created successfully");
            return true;

        } catch (Exception e) {
            System.err.println("Error creating maintenance request: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Creates a new maintenance request without image
     */
    public boolean createMaintenanceRequest(String staffName, String issue, String location,
                                            String status, String priority) {
        return createMaintenanceRequest(staffName, issue, location, status, priority, null, null, null);
    }

    /**
     * Get all staff names for dropdown
     */
    public ObservableList<String> getAllStaffNames() {
        ObservableList<String> staffNames = FXCollections.observableArrayList();

        try {
            List<Document> staffList = staffCollection.find().into(new ArrayList<>());

            for (Document staff : staffList) {
                String fullName = staff.getString("full_name");
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
            Document query = new Document("full_name", fullName);
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
                requests = maintenanceCollection.find(query).into(new ArrayList<>());
            }

        } catch (Exception e) {
            System.err.println("Error retrieving maintenance requests: " + e.getMessage());
            e.printStackTrace();
        }

        return requests;
    }

    /**
     * Get all maintenance requests
     */
    public List<Document> getAllMaintenanceRequests() {
        List<Document> requests = new ArrayList<>();

        try {
            requests = maintenanceCollection.find().into(new ArrayList<>());

        } catch (Exception e) {
            System.err.println("Error retrieving all maintenance requests: " + e.getMessage());
            e.printStackTrace();
        }

        return requests;
    }

    /**
     * Update maintenance request status
     */
    public boolean updateMaintenanceRequestStatus(ObjectId requestId, String newStatus) {
        try {
            Document query = new Document("_id", requestId);
            Document update = new Document("$set", new Document("status", newStatus));

            long modifiedCount = maintenanceCollection.updateOne(query, update).getModifiedCount();

            if (modifiedCount > 0) {
                System.out.println("Maintenance request status updated successfully");
                return true;
            } else {
                System.out.println("No maintenance request found with the given ID");
                return false;
            }

        } catch (Exception e) {
            System.err.println("Error updating maintenance request status: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Update maintenance request priority
     */
    public boolean updateMaintenanceRequestPriority(ObjectId requestId, String newPriority) {
        try {
            Document query = new Document("_id", requestId);
            Document update = new Document("$set", new Document("priority", newPriority));

            long modifiedCount = maintenanceCollection.updateOne(query, update).getModifiedCount();

            if (modifiedCount > 0) {
                System.out.println("Maintenance request priority updated successfully");
                return true;
            } else {
                System.out.println("No maintenance request found with the given ID");
                return false;
            }

        } catch (Exception e) {
            System.err.println("Error updating maintenance request priority: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Delete maintenance request
     */
    public boolean deleteMaintenanceRequest(ObjectId requestId) {
        try {
            Document query = new Document("_id", requestId);
            long deletedCount = maintenanceCollection.deleteOne(query).getDeletedCount();

            if (deletedCount > 0) {
                System.out.println("Maintenance request deleted successfully");
                return true;
            } else {
                System.out.println("No maintenance request found with the given ID");
                return false;
            }

        } catch (Exception e) {
            System.err.println("Error deleting maintenance request: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Get maintenance requests by status
     */
    public List<Document> getMaintenanceRequestsByStatus(String status) {
        List<Document> requests = new ArrayList<>();

        try {
            Document query = new Document("status", status);
            requests = maintenanceCollection.find(query).into(new ArrayList<>());

        } catch (Exception e) {
            System.err.println("Error retrieving maintenance requests by status: " + e.getMessage());
            e.printStackTrace();
        }

        return requests;
    }

    /**
     * Get maintenance requests by priority
     */
    public List<Document> getMaintenanceRequestsByPriority(String priority) {
        List<Document> requests = new ArrayList<>();

        try {
            Document query = new Document("priority", priority);
            requests = maintenanceCollection.find(query).into(new ArrayList<>());

        } catch (Exception e) {
            System.err.println("Error retrieving maintenance requests by priority: " + e.getMessage());
            e.printStackTrace();
        }

        return requests;
    }

    /**
     * Get staff name by ObjectId (for display purposes)
     */
    public String getStaffNameById(ObjectId staffId) {
        try {
            Document query = new Document("_id", staffId);
            Document staff = staffCollection.find(query).first();

            if (staff != null) {
                return staff.getString("full_name");
            }

        } catch (Exception e) {
            System.err.println("Error finding staff by ID: " + e.getMessage());
            e.printStackTrace();
        }

        return "Unknown Staff";
    }

    /**
     * Check if maintenance request exists
     */
    public boolean maintenanceRequestExists(ObjectId requestId) {
        try {
            Document query = new Document("_id", requestId);
            Document request = maintenanceCollection.find(query).first();
            return request != null;

        } catch (Exception e) {
            System.err.println("Error checking maintenance request existence: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Get maintenance request by ID
     */
    public Document getMaintenanceRequestById(ObjectId requestId) {
        try {
            Document query = new Document("_id", requestId);
            return maintenanceCollection.find(query).first();

        } catch (Exception e) {
            System.err.println("Error retrieving maintenance request by ID: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Close MongoDB connection
     */
    public void closeConnection() {
        if (mongoClient != null) {
            mongoClient.close();
        }
    }
}