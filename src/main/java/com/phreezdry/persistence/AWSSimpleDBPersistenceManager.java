package com.phreezdry.persistence;
/**
 * @author petrovic May 28, 2010 7:44:51 PM
 */

import com.phreezdry.entity.Document;
import com.phreezdry.entity.EntityType;
import com.phreezdry.entity.User;
import com.phreezdry.server.Caches;
import com.phreezdry.util.Constants;
import com.phreezdry.util.Platform;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import org.carpediem.simpledb.SimpleDB;
import org.carpediem.util.Base64;
import org.carpediem.util.Digests;
import org.carpediem.util.Gzip;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AWSSimpleDBPersistenceManager implements PersistenceManager {
    private final Logger logger = Logger.getLogger(AWSSimpleDBPersistenceManager.class.getName());
    private final String TEXT = "text";
    private final String PASSWORD = "password";
    private final String COMPRESSED = "compressed";
    private final String TEST_PREFIX = "test.";
    private static final CacheManager cm = CacheManager.create(AWSSimpleDBPersistenceManager.class.getResource(Constants.CACHE_CONFIG));

    private String userDomain;
    private String documentDomain;
    private String key;
    private String secret;
    private int compressThreshold;

    public void init() {
        logger.info("cache disk store path: " + cm.getDiskStorePath());
    }

    @Override
    public void shutdown() {
        cm.shutdown();
    }

    public void setUserDomain(String userDomain) {
        this.userDomain = String.format("%s%s", Platform.isTestEnvironment() ? TEST_PREFIX : "", userDomain);
        logger.info(String.format("PM userDomain: %s", this.userDomain));
    }

    public void setDocumentDomain(String documentDomain) {
        this.documentDomain = String.format("%s%s", Platform.isTestEnvironment() ? TEST_PREFIX : "", documentDomain);
        logger.info(String.format("PM documentDomain: %s", this.documentDomain));
    }

    public void setKey(String key) {
        this.key = key;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    @Override
    public Document persist(String text, String creator) throws PersistenceException {
        String documentId = Digests.byteToHex(Digests.sha1(text, "UTF-8"));

        if (fromCache(documentId) != null) {
            return new Document(text, documentId);
        }

        try {
            SimpleDB sdb = new SimpleDB(key, secret);
            boolean compress = text.length() > compressThreshold;
            Map<String, String> m = new HashMap<String, String>();
            m.put(Constants.LOGIN_USERNAME, creator);
            m.put(TEXT, compress ? new Base64().encode(Gzip.gzip(text, "UTF-8")) : text);
            m.put(COMPRESSED, Boolean.valueOf(compress).toString());
            sdb.putAttributes(documentDomain, documentId, m);
            cache(documentId, text);
            return new Document(text, documentId);
        } catch (Exception e) {
            throw new PersistenceException(e);
        }
    }

    @Override
    public Document retrieve(String documentId) throws PersistenceException {
        documentId = documentId.toLowerCase();

        String text = fromCache(documentId);
        if (text != null) {
            return new Document(text, documentId);
        }

        try {
            SimpleDB sdb = new SimpleDB(key, secret);
            Map<String, String[]> map = sdb.getAttributes(documentDomain, documentId);
            if (map.isEmpty()) {
                return null;
            }
            text = map.get(TEXT)[0];
            boolean isCompressed = Boolean.valueOf(map.get(COMPRESSED)[0]);
            text = isCompressed ? new String(Gzip.gunzip(new Base64().decode(text), Constants.MAXDOCSIZE), Constants.CHARSET) : text;
            Document document = new Document(text, documentId);
            cache(documentId, text);
            return document;
        } catch (Exception e) {
            logger.log(Level.WARNING, String.format("error retrieving document: %s", documentId), e);
            return null;
        }
    }


    /**
     * Retrieve a user by username.
     *
     * @param userName
     * @return
     */
    @Override
    public User getUser(String userName) {
        Element element = cm.getCache(Caches.USER.key).get(userName);
        if (element != null) {
            return (User) element.getObjectValue();
        }

        SimpleDB sdb = new SimpleDB(key, secret);
        try {
            Map<String, String[]> map = sdb.getAttributes(userDomain, userName);
            if (map.isEmpty()) {
                return null;
            }
            String password = map.get("password")[0];
            User user = new User(userName, password);
            cm.getCache(Caches.USER.key).put(new Element(userName, user));
            return user;
        } catch (Exception e) {
            logger.log(Level.WARNING, "error retrieving user", e);
            return null;
        }
    }

    /**
     * Add a user in a domain
     *
     * @param userName
     * @param password
     * @return
     * @throws PersistenceException
     */
    public User putUser(String userName, String password) throws PersistenceException {
        SimpleDB sdb = new SimpleDB(key, secret);
        Map<String, String> m = new HashMap<String, String>();
        m.put(PASSWORD, password);
        try {
            sdb.putAttributes(userDomain, userName, m);
            User u = new User(userName, password);
            cache(userName, u);
            return u;
        } catch (Exception e) {
            e.printStackTrace();
            throw new PersistenceException(e);
        }
    }

    /**
     * Remove all entities of a type.
     *
     * @param t
     */
    @Override
    public void removeAll(EntityType t) throws PersistenceException {
        SimpleDB sdb = new SimpleDB(key, secret);
        try {
            String domain = t == EntityType.DOCUMENT ? documentDomain : userDomain;
            sdb.deleteDomain(domain);
            sdb.createDomain(domain);
        }
        catch (Exception e) {
            throw new PersistenceException(e);
        }
    }

    /**
     * Get the configured user domain
     *
     * @return
     */
    @Override
    public String getUserDomain() {
        return userDomain;
    }

    /**
     * Get the configured document domain
     *
     * @return
     */
    @Override
    public String getDocumentDomain() {
        return documentDomain;
    }

    @Override
    public void createDomains() throws PersistenceException {
        SimpleDB sdb = new SimpleDB(key, secret);
        try {
            for (String domain : new String[]{userDomain, documentDomain}) {
                sdb.createDomain(domain);
                sdb.createDomain(String.format("%s%s", TEST_PREFIX, domain));
            }
        } catch (Exception e) {
            throw new PersistenceException(e);
        }
    }

    /**
     * Get count of EntityType
     *
     * @param t
     * @return
     */
    @Override
    public String count(EntityType t) throws PersistenceException {
        SimpleDB sdb = new SimpleDB(key, secret);
        try {
            String query = String.format("select count(*) from `%s`", t == EntityType.DOCUMENT ? documentDomain : userDomain);
            List<HashMap<String, String[]>> list = sdb.select(query);
            return list.get(0).get("Count")[0];
        } catch (Exception e) {
            throw new PersistenceException(e);
        }
    }

    /**
     * Delete document from cache or backing store.
     *
     * @param documentId
     */
    @Override
    public void delete(String documentId) {
        Element element = elementFromCache(documentId.toLowerCase());
        if (element != null) {
            cm.getCache(Caches.DOCUMENT.key).remove(element.getKey());
        }
    }

    /**
     * Provide a textual cache report
     *
     * @return
     */
    @Override
    public String cacheReport() {
        StringBuilder sb = new StringBuilder();
        for (Caches t : Caches.values()) {
            Cache cache = cm.getCache(t.key);
            sb.append(cache.getStatistics().toString()).append('\n');
        }
        return sb.toString();
    }

    private String fromCache(String documentId) {
        Element el = cm.getCache(Caches.DOCUMENT.key).get(documentId);
        return el != null ? (String) el.getObjectValue() : null;
    }

    private void cache(String documentId, String text) {
        cm.getCache(Caches.DOCUMENT.key).put(new Element(documentId, text));
    }

    private void cache(String userName, User user) {
        cm.getCache(Caches.USER.key).put(new Element(userName, user));
    }

    private Element elementFromCache(String documentId) {
        return cm.getCache(Caches.DOCUMENT.key).get(documentId);
    }

    public void setCompressThreshold(String compressThreshold) {
        this.compressThreshold = Integer.valueOf(compressThreshold);
    }

}


