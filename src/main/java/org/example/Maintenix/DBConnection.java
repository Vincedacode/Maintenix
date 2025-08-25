package org.example.Maintenix;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import io.github.cdimascio.dotenv.Dotenv;
import org.bson.Document;
public class DBConnection {
    public static MongoCollection<Document> createConnection(String dbname, String collectionname){
        Dotenv dotenv = Dotenv.load();
        String uri = dotenv.get("MONGO_URI");
        try {
            MongoClient mongoClient = MongoClients.create(uri);
            MongoDatabase database = mongoClient.getDatabase(dbname);
            System.out.println("Database found!");
            return database.getCollection(collectionname);
        }catch (Exception e){
            System.out.println(e.getMessage());
            return null;
        }
    }

}
