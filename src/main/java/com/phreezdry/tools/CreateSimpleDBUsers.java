package com.phreezdry.tools;
/**
 * @author petrovic May 28, 2010 4:52:55 PM
 */

import com.phreezdry.entity.User;
import com.phreezdry.persistence.PersistenceManager;
import com.phreezdry.util.Constants;
import com.phreezdry.util.Platform;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class CreateSimpleDBUsers {
    public static void main(String[] args) throws Exception {
        CreateSimpleDBUsers tool = new CreateSimpleDBUsers();
        tool.run(args);
    }

    private void run(String[] args) throws Exception {
        String email = args[0];
        String password = args[1];

        // <tool> user password [--test]
        if (args.length == 3 && args[2].equals("--test")) {
            Platform.setTestEnvironment(true);
        }

        ApplicationContext appContext = new ClassPathXmlApplicationContext(Constants.SPRING_CONFIG);
        PersistenceManager pm = (PersistenceManager) appContext.getBean(Constants.PERSISTENCE_MGR);
        User u = pm.putUser(email, password);
        System.out.println(u);
    }

}
