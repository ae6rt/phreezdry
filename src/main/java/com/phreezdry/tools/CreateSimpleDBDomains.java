package com.phreezdry.tools;
/**
 * @author petrovic May 28, 2010 8:28:21 AM
 */

import com.phreezdry.persistence.PersistenceException;
import com.phreezdry.persistence.PersistenceManager;
import com.phreezdry.util.Constants;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.logging.Logger;

public class CreateSimpleDBDomains {
    private final Logger logger = Logger.getLogger(CreateSimpleDBDomains.class.getName());

    public static void main(String[] args) throws PersistenceException {
        CreateSimpleDBDomains tool = new CreateSimpleDBDomains();
        tool.run(args);
    }

    private void run(String[] args) throws PersistenceException {
        ApplicationContext appContext = new ClassPathXmlApplicationContext(Constants.SPRING_CONFIG);
        PersistenceManager pm = (PersistenceManager) appContext.getBean(Constants.PERSISTENCE_MGR);
        pm.createDomains();
    }

}
