package com.phreezdry.persistence;

import com.phreezdry.entity.Document;
import com.phreezdry.entity.EntityType;
import com.phreezdry.entity.User;

/**
 * @author petrovic May 28, 2010 7:42:46 PM
 */
public interface PersistenceManager {

    /**
     * Persist the string document.  Return an encapsulation.
     *
     * @param document
     * @return
     */
    public Document persist(String document, String creator) throws PersistenceException;

    /**
     * Retrieve the document by documentId
     *
     * @param documentId
     */
    public Document retrieve(String documentId) throws PersistenceException;

    /**
     * Retrieve a user by username.
     *
     * @param userName
     * @return
     */
    public User getUser(String userName);

    /**
     * Add a user.
     *
     * @param userName
     * @param password
     * @return
     */
    public User putUser(String userName, String password) throws PersistenceException;

    /**
     * Remove all entities of a type.
     *
     * @param t
     */
    public void removeAll(EntityType t) throws PersistenceException;

    /**
     * Get the configured user domain
     *
     * @return
     */
    public String getUserDomain();

    /**
     * Get the configured document domain
     *
     * @return
     */
    public String getDocumentDomain();

    /**
     * Create domains.
     *
     * @throws PersistenceException
     */
    public void createDomains() throws PersistenceException;

    /**
     * Get count of EntityType
     *
     * @param t
     * @return
     */
    public String count(EntityType t) throws PersistenceException;

    /**
     * Delete document from cache or backing store.
     *
     * @param documentId
     */
    public void delete(String documentId);

    /**
     * Provide a textual cache report
     * @return
     */
    public String cacheReport();

    /**
     * init
     */
    public void init();

    /**
     * shutdown
     */
    public void shutdown();
}
