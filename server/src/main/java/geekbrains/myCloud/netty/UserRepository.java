package geekbrains.myCloud.netty;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;

import java.util.Optional;

import static com.mongodb.client.model.Filters.eq;

@Slf4j
public class UserRepository {
    private final String uri;
    private final MongoClient mongoClient;
    private MongoDatabase database;
    private final MongoCollection<Document> collection;


    public UserRepository() {
        this.uri = System.getenv().get("MONGO_CONNECTION_STRING");
        this.mongoClient = MongoClients.create(uri);
        this.database = mongoClient.getDatabase("cloud_storage");
        this.collection = database.getCollection("users");
    }

    protected void addUser(String login, String pwdHash) {
        Document document = new Document();
        document.put("login", login);
        document.put("password", pwdHash);
        collection.insertOne(document);
    }

    protected Optional<User> getUserByLogin(String login) {
        Document result = collection.find(eq("login", login)).first();
        return Optional.ofNullable(result).flatMap(User::fromDocument);
    }
}
