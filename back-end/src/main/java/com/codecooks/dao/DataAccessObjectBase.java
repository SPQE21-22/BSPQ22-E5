package com.codecooks.dao;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.jdo.Transaction;

/**
 * Base class that implements most common db access operations.
 */
public abstract class DataAccessObjectBase {

    protected static PersistenceManagerFactory pmf = JDOHelper.getPersistenceManagerFactory("RecipeMaster");

    public void saveObject(Object object) {

        PersistenceManager pm = pmf.getPersistenceManager();
        Transaction tx = pm.currentTransaction();

        try {
            tx.begin();
            pm.makePersistent(object);
            tx.commit();
        }

        catch (Exception e) {

            // TODO Log some message
        }

        finally {

            if (tx != null && tx.isActive()) {
                tx.rollback();
            }

            pm.close();
        }
    }

    public void deleteObject(Object object) {

        PersistenceManager pm = pmf.getPersistenceManager();
        Transaction tx = pm.currentTransaction();

        try {

            tx.begin();
            pm.deletePersistent(object);
            tx.commit();
        }

        catch (Exception e) {

            // TODO Log some message
        }

        finally {

            if (tx != null && tx.isActive()) {
                tx.rollback();
            }
            pm.close();
        }
    }

}
