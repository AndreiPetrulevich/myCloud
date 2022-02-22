package geekbrains.myCloud.netty;

import org.bson.Document;
import org.bson.types.ObjectId;

import java.util.Optional;

public class User {
    private String id;
    private String login;
    private String passwordHash;

    public User(String id, String login, String passwordHash) {
        this.id = id;
        this.login = login;
        this.passwordHash = passwordHash;
    }

    public String getId() {
        return id;
    }

    public String getLogin() {
        return login;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public static Optional<User> fromDocument(Document document) {
        Optional<String> login = Optional.ofNullable(document.getString("login"));
        Optional<String> passwordHash = Optional.ofNullable(document.getString("password"));

        ObjectId id = document.getObjectId("_id");

        if (login.isPresent() && passwordHash.isPresent()) {
            return Optional.of(new User(id.toHexString(), login.get(), passwordHash.get()));
        } else {
            return Optional.empty();
        }
    }
}
