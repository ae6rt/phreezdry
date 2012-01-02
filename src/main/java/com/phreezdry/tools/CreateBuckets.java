package com.phreezdry.tools;
/**
 * @author petrovic May 22, 2010 2:25:33 PM
 */

import com.phreezdry.server.Config;
import org.jets3t.service.S3Service;
import org.jets3t.service.S3ServiceException;
import org.jets3t.service.impl.rest.httpclient.RestS3Service;
import org.jets3t.service.security.AWSCredentials;

/**
 * Create the S3 buckets the service requires
 */
public class CreateBuckets {

    public static void main(String[] args) throws S3ServiceException {
        CreateBuckets tool = new CreateBuckets();
        tool.run(args);
    }

    private void run(String[] args) throws S3ServiceException {
        Config config = new Config();
        S3Service s3Service = new RestS3Service(new AWSCredentials(config.getAwsKey(), config.getAwsSecret()));

        // production buckets
        System.out.println(s3Service.getOrCreateBucket(String.format("%s", config.getUserBucketName())));
        System.out.println(s3Service.getOrCreateBucket(String.format("%s", config.getDocBucketName())));

        // unit test buckets
        System.out.println(s3Service.getOrCreateBucket(String.format("test.%s", config.getUserBucketName())));
        System.out.println(s3Service.getOrCreateBucket(String.format("test.%s", config.getDocBucketName())));
    }

}
