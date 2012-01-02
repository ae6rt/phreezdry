package com.phreezdry.tools;
/**
 * @author petrovic May 22, 2010 5:01:11 PM
 */

import com.phreezdry.server.Config;
import org.jets3t.service.S3Service;
import org.jets3t.service.S3ServiceException;
import org.jets3t.service.impl.rest.httpclient.RestS3Service;
import org.jets3t.service.model.S3Bucket;
import org.jets3t.service.model.S3Object;
import org.jets3t.service.security.AWSCredentials;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Logger;

public class AddUser {
    private final Logger logger = Logger.getLogger(AddUser.class.getName());

    public static void main(String[] args) throws S3ServiceException, IOException, NoSuchAlgorithmException {
        AddUser tool = new AddUser();
        tool.run(args);
    }

    private void run(String[] args) throws S3ServiceException, IOException, NoSuchAlgorithmException {
        String email = args[0];
        String password = args[1];

        Config config = new Config();
        S3Service s3Service = new RestS3Service(new AWSCredentials(config.getAwsKey(), config.getAwsSecret()));

        // optionally create the new user in the test environment
        String userBucketName;
        if (args.length == 3 && args[2].equals("test")) {
            userBucketName = String.format("test.%s", config.getUserBucketName());
        } else {
            userBucketName = String.format("%s", config.getUserBucketName());
        }
        S3Bucket userBucket = s3Service.getOrCreateBucket(userBucketName);

        S3Object user = new S3Object(email);
        user.addMetadata("password", password);
        user.addMetadata("status", "ok");
        S3Object object = s3Service.putObject(userBucket, user);
        System.out.println(object);
    }

}
