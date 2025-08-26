package org.example.Maintenix.DAO;
import com.mongodb.client.*;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import io.github.cdimascio.dotenv.Dotenv;
import org.bson.*;
import org.bson.conversions.Bson;
import org.example.Maintenix.DBConnection;

import java.util.ArrayList;
import java.util.List;

public class admindao {
    private final MongoCollection<Document> collection;

    public admindao(){
        Dotenv dotenv = Dotenv.load();
        String dbname = dotenv.get("DB_NAME");
        String collectionname = dotenv.get("ADMIN_COLLECTION_NAME");
        this.collection = DBConnection.createConnection(dbname,collectionname);
    }

    public Document loginAdmin(String email, String password) {
        Bson filter = Filters.and(
                Filters.eq("email", email),
                Filters.eq("password", password)
        );

        return collection.find(filter).first();
    }
}
