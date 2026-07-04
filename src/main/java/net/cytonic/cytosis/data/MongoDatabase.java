package net.cytonic.cytosis.data;

import java.util.List;

import com.google.gson.JsonParser;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.ReplaceOptions;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.model.WriteModel;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import net.minestom.server.codec.Codec;
import net.minestom.server.codec.Transcoder;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.jetbrains.annotations.Blocking;
import org.jetbrains.annotations.Nullable;

import net.cytonic.cytosis.Bootstrappable;
import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.bootstrap.annotations.CytosisComponent;
import net.cytonic.cytosis.config.CytosisConfig;
import net.cytonic.cytosis.config.CytosisConfig.MongoConfig;
import net.cytonic.cytosis.environments.Environment;
import net.cytonic.cytosis.logging.Logger;

@CytosisComponent
public class MongoDatabase implements Bootstrappable {

    private final String url;
    private final String database;
    private MongoClient client;

    /**
     * Initializes the connection to redis using the loaded settings and the Jedis client
     */
    public MongoDatabase() {
        MongoConfig config = Cytosis.get(CytosisConfig.class).mongo();
        url = config.url();
        String prefix = Cytosis.get(Environment.class).getPrefix();
        database = prefix + config.database();
    }

    @Override
    public void init() {
        client = MongoClients.create(url);
        Logger.info("MongoDB Client successfully connected!");
    }

    @Blocking
    @Nullable
    public <T> T getDocument(String documentId, String collection, String database, Codec<T> codec) {
        String json = getDocumentJson(documentId, collection, database);
        if (json == null) return null;
        return codec.decode(Transcoder.JSON, JsonParser.parseString(json)).orElseThrow();
    }

    @Blocking
    public <T> void setDocument(String documentId, String collection, String database, T obj, Codec<T> codec) {
        String json = codec.encode(Transcoder.JSON, obj).orElseThrow().toString();
        setDocumentJson(documentId, collection, database, json);
    }

    @Blocking
    @Nullable
    public <T> T getDocument(String documentId, String collection, Codec<T> codec) {
        return getDocument(documentId, collection, this.database, codec);
    }

    @Blocking
    public <T> void setDocument(String documentId, String collection, T obj, Codec<T> codec) {
        setDocument(documentId, collection, this.database, obj, codec);
    }

    @Blocking
    @Nullable
    public String getDocumentJson(String documentId, String collection, String database) {
        com.mongodb.client.MongoDatabase db = client.getDatabase(database);
        MongoCollection<Document> col = db.getCollection(collection);
        Document doc = col.find(Filters.eq("_id", documentId)).first();
        if (doc == null) return null;
        return doc.toJson();
    }

    @Blocking
    public void setDocumentJson(String documentId, String collection, String database, String json) {
        com.mongodb.client.MongoDatabase db = client.getDatabase(database);
        MongoCollection<Document> col = db.getCollection(collection);

        Document doc = Document.parse(json);
        doc.put("_id", documentId);

        ReplaceOptions options = new ReplaceOptions().upsert(true);
        col.replaceOne(Filters.eq("_id", documentId), doc, options);
    }

    @Blocking
    public boolean deleteDocument(String documentId, String collection, String database) {
        com.mongodb.client.MongoDatabase db = client.getDatabase(database);
        MongoCollection<Document> col = db.getCollection(collection);

        DeleteResult result = col.deleteOne(Filters.eq("_id", documentId));
        return result.getDeletedCount() > 0;
    }

    @Blocking
    public boolean documentExists(String documentId, String collection, String database) {
        com.mongodb.client.MongoDatabase db = client.getDatabase(database);
        MongoCollection<Document> col = db.getCollection(collection);

        return col.find(Filters.eq("_id", documentId)).first() != null;
    }

    /**
     * Example usage: {@code updateDocument("<document ID>", "users", Updates.set("coins", 100))}
     */
    @Blocking
    public UpdateResult updateDocument(String documentId, String collection, String database, Bson update) {
        com.mongodb.client.MongoDatabase db = client.getDatabase(database);
        MongoCollection<Document> col = db.getCollection(collection);

        return col.updateOne(Filters.eq("_id", documentId), update, new UpdateOptions().upsert(true));
    }

    @Blocking
    public List<Document> find(String collection, String database, Bson filter, int limit, int skip) {
        com.mongodb.client.MongoDatabase db = client.getDatabase(database);
        MongoCollection<Document> col = db.getCollection(collection);

        return col.find(filter)
            .skip(skip)
            .limit(limit)
            .into(new java.util.ArrayList<>());
    }

    @Blocking
    public void bulkWrite(String collection, List<WriteModel<Document>> operations) {
        com.mongodb.client.MongoDatabase db = client.getDatabase(database);
        MongoCollection<Document> col = db.getCollection(collection);

        col.bulkWrite(operations);
    }

    @Blocking
    public List<Document> find(String collection, Bson filter) {
        return find(collection, this.database, filter, 0, 0);
    }

    /**
     * Example usage: {@code updateDocument("<document ID>", "users", Updates.set("coins", 100))}
     */
    @Blocking
    public UpdateResult updateDocument(String documentId, String collection, Bson update) {
        return updateDocument(documentId, collection, this.database, update);
    }

    @Blocking
    public boolean documentExists(String documentId, String collection) {
        return documentExists(documentId, collection, this.database);
    }

    @Blocking
    public boolean deleteDocument(String documentId, String collection) {
        return deleteDocument(documentId, collection, this.database);
    }

    @Nullable
    @Blocking
    public String getDocumentJson(String documentId, String collection) {
        return getDocumentJson(documentId, collection, this.database);
    }

    @Blocking
    public void setDocumentJson(String documentId, String collection, String json) {
        setDocumentJson(documentId, collection, this.database, json);
    }
}
