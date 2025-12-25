package vn.agriconnect.API.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.EnableMongoAuditing;

/**
 * MongoDB Configuration
 * - Enables auditing for @CreatedDate, @LastModifiedDate
 * - Custom converters can be added here
 */
@Configuration
@EnableMongoAuditing
public class MongoConfig {
    // Add custom MongoDB configurations here
}
