package geekbrains.myCloud.netty;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;

@Slf4j
public class Repository {
    private final String uri;
    private final MongoClient mongoClient;
    private final MongoDatabase database;

    public Repository() {
        this.uri = System.getenv().get("MONGO_CONNECTION_STRING");
        this.mongoClient = MongoClients.create(uri);
        this.database = mongoClient.getDatabase("cloud_storage");
    }

    protected void addUser(String login, String passwordHash) {
        MongoCollection<Document> collection = database.getCollection("users");
        Document document = new Document();
        document.put("login", login);
        document.put("password", passwordHash);
        collection.insertOne(document);
    }

    protected String verification(String login, String password) {
        String pwd = password;
        return pwd;
    }
}
