package com.example.taskmanager.repository;

import com.example.taskmanager.model.AppUser;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.model.ScanEnhancedRequest;

import java.util.Optional;
import java.util.stream.StreamSupport;

@Repository
public class AppUserRepository {

    private final DynamoDbTable<AppUser> userTable;

    public AppUserRepository(DynamoDbEnhancedClient enhancedClient) {
        this.userTable = enhancedClient.table("AppUsers", TableSchema.fromBean(AppUser.class));
    }

    public void save(AppUser user) {
        userTable.putItem(user);
    }

    public Optional<AppUser> findById(String id) {
        AppUser user = userTable.getItem(Key.builder().partitionValue(id).build());
        return Optional.ofNullable(user);
    }

    public Optional<AppUser> findByEmail(String email) {
        return StreamSupport.stream(userTable.scan().items().spliterator(), false)
                .filter(u -> email.equals(u.getEmail()))
                .findFirst();
    }

    public Optional<AppUser> findByUsername(String username) {
        return StreamSupport.stream(userTable.scan().items().spliterator(), false)
                .filter(u -> username.equals(u.getUsername()))
                .findFirst();
    }
}
