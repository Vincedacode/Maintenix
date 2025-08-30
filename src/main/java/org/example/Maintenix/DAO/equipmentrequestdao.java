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

public class equipmentrequestdao {

    private MongoClient mongoClient;
    private MongoDatabase database;
    private MongoCollection<Document> equipmentCollection;
    private MongoCollection<Document> staffCollection;
    private staffdao staffDAO; // Add reference to staff DAO

    // Constructor
    public equipmentrequestdao() {
        try {
            Dotenv dotenv = Dotenv.load();
            String dbname = dotenv.get("DB_NAME");
            String staffCollectionName = dotenv.get("STAFF_COLLECTION_NAME");
            String equipmentCollectionName = dotenv.get("REQUESTS_COLLECTION_NAME");

            this.staffCollection = DBConnection.createConnection(dbname, staffCollectionName);
            this.equipmentCollection = DBConnection.createConnection(dbname, equipmentCollectionName);
            this.staffDAO = new staffdao(); // Initialize staff DAO

        } catch (Exception e) {
            System.err.println("Error connecting to MongoDB: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Get equipment requests by username - KEY METHOD FOR PERSONALIZATION
     */
    public List<Document> getEquipmentRequestsByUsername(String username) {
        List<Document> requests = new ArrayList<>();

        try {
            // Get staff ObjectId from username
            ObjectId staffId = staffDAO.getStaffIdByUsername(username);
            if (staffId != null) {
                Document query = new Document("staff_name", staffId);
                requests = equipmentCollection.find(query).into(new ArrayList<>());
                System.out.println("Found " + requests.size() + " equipment requests for user: " + username);
            } else {
                System.err.println("Staff not found for username: " + username);
            }

        } catch (Exception e) {
            System.err.println("Error retrieving equipment requests by username: " + e.getMessage());
            e.printStackTrace();
        }

        return requests;
    }



    /**
     * Creates a new equipment request
     */
    public boolean createEquipmentRequest(String staffName, String type, String itemName, String description) {
        try {
            // Get staff ObjectId by name
            ObjectId staffId = getStaffIdByName(staffName);
            if (staffId == null) {
                System.err.println("Staff member not found: " + staffName);
                return false;
            }

            Document equipmentDoc = new Document()
                    .append("staff_name", staffId)
                    .append("type", type)
                    .append("item_name", itemName)
                    .append("description", description)
                    .append("status","pending")
                    .append("priority","low")
                    .append("created_at", new Date());

            equipmentCollection.insertOne(equipmentDoc);
            System.out.println("Equipment request created successfully");
            return true;

        } catch (Exception e) {
            System.err.println("Error creating equipment request: " + e.getMessage());
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
                String fullName = staff.getString("Fullname"); // Match your actual database field
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
            Document query = new Document("Fullname", fullName); // Match the actual field name in your database
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
     * Get all equipment requests for a specific staff member
     */
    public List<Document> getEquipmentRequestsByStaff(String staffName) {
        List<Document> requests = new ArrayList<>();

        try {
            ObjectId staffId = getStaffIdByName(staffName);
            if (staffId != null) {
                Document query = new Document("staff_name", staffId);
                requests = equipmentCollection.find(query).into(new ArrayList<>());
            }

        } catch (Exception e) {
            System.err.println("Error retrieving equipment requests: " + e.getMessage());
            e.printStackTrace();
        }

        return requests;
    }

    /**
     * Get all equipment requests
     */
    public List<Document> getAllEquipmentRequests() {
        List<Document> requests = new ArrayList<>();

        try {
            requests = equipmentCollection.find().into(new ArrayList<>());

        } catch (Exception e) {
            System.err.println("Error retrieving all equipment requests: " + e.getMessage());
            e.printStackTrace();
        }

        return requests;
    }

    // ... [Keep all your existing methods] ...

    /**
     * Get equipment requests by type
     */
    public List<Document> getEquipmentRequestsByType(String type) {
        List<Document> requests = new ArrayList<>();

        try {
            Document query = new Document("type", type);
            requests = equipmentCollection.find(query).into(new ArrayList<>());

        } catch (Exception e) {
            System.err.println("Error retrieving equipment requests by type: " + e.getMessage());
            e.printStackTrace();
        }

        return requests;
    }

    /**
     * Delete equipment request
     */
    public boolean deleteEquipmentRequest(ObjectId requestId) {
        try {
            Document query = new Document("_id", requestId);
            long deletedCount = equipmentCollection.deleteOne(query).getDeletedCount();

            if (deletedCount > 0) {
                System.out.println("Equipment request deleted successfully");
                return true;
            } else {
                System.out.println("No equipment request found with the given ID");
                return false;
            }

        } catch (Exception e) {
            System.err.println("Error deleting equipment request: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Update equipment request
     */
    public boolean updateEquipmentRequest(ObjectId requestId, String type, String itemName, String description) {
        try {
            Document query = new Document("_id", requestId);
            Document update = new Document("$set",
                    new Document("type", type)
                            .append("item_name", itemName)
                            .append("description", description)
            );

            long modifiedCount = equipmentCollection.updateOne(query, update).getModifiedCount();

            if (modifiedCount > 0) {
                System.out.println("Equipment request updated successfully");
                return true;
            } else {
                System.out.println("No equipment request found with the given ID");
                return false;
            }

        } catch (Exception e) {
            System.err.println("Error updating equipment request: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Get staff name by ObjectId (for display purposes)
     */
    public String getStaffNameById(ObjectId staffId) {
        try {
            Document query = new Document("_id", staffId);
            Document staff = staffCollection.find(query).first();

            if (staff != null) {
                return staff.getString("Fullname"); // Match your actual database field
            }

        } catch (Exception e) {
            System.err.println("Error finding staff by ID: " + e.getMessage());
            e.printStackTrace();
        }

        return "Unknown Staff";
    }

    /**
     * Check if equipment request exists
     */
    public boolean equipmentRequestExists(ObjectId requestId) {
        try {
            Document query = new Document("_id", requestId);
            Document request = equipmentCollection.find(query).first();
            return request != null;

        } catch (Exception e) {
            System.err.println("Error checking equipment request existence: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Get equipment request by ID
     */
    public Document getEquipmentRequestById(ObjectId requestId) {
        try {
            Document query = new Document("_id", requestId);
            return equipmentCollection.find(query).first();

        } catch (Exception e) {
            System.err.println("Error retrieving equipment request by ID: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Get equipment requests by staff ID (ObjectId)
     */
    public List<Document> getEquipmentRequestsByStaffId(ObjectId staffId) {
        List<Document> requests = new ArrayList<>();

        try {
            Document query = new Document("staff_name", staffId);
            requests = equipmentCollection.find(query).into(new ArrayList<>());

        } catch (Exception e) {
            System.err.println("Error retrieving equipment requests by staff ID: " + e.getMessage());
            e.printStackTrace();
        }

        return requests;
    }

    /**
     * Get count of equipment requests by type
     */
    public long getEquipmentRequestCountByType(String type) {
        try {
            Document query = new Document("type", type);
            return equipmentCollection.countDocuments(query);

        } catch (Exception e) {
            System.err.println("Error counting equipment requests by type: " + e.getMessage());
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * Get recent equipment requests (last N requests)
     */
    public List<Document> getRecentEquipmentRequests(int limit) {
        List<Document> requests = new ArrayList<>();

        try {
            requests = equipmentCollection.find()
                    .sort(new Document("created_at", -1))
                    .limit(limit)
                    .into(new ArrayList<>());

        } catch (Exception e) {
            System.err.println("Error retrieving recent equipment requests: " + e.getMessage());
            e.printStackTrace();
        }

        return requests;
    }

    /**
     * Search equipment requests by item name
     */
    public List<Document> searchEquipmentRequestsByItemName(String itemName) {
        List<Document> requests = new ArrayList<>();

        try {
            // Case-insensitive search using regex
            Document query = new Document("item_name",
                    new Document("$regex", itemName)
                            .append("$options", "i"));
            requests = equipmentCollection.find(query).into(new ArrayList<>());

        } catch (Exception e) {
            System.err.println("Error searching equipment requests by item name: " + e.getMessage());
            e.printStackTrace();
        }

        return requests;
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