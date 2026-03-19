package com.example.taskmanager.model;

import lombok.*;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.*;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@DynamoDbBean
public class Task {

    private String id;
    private String title;
    private String description;

    // Storing owner's ID (Cognito Sub)
    private String ownerId;

    // Set of User IDs (Cognito Subs) this task is shared with
    @Builder.Default
    private Set<String> sharedUsersIds = new HashSet<>();

    // Set of simple tag strings
    @Builder.Default
    private Set<String> tags = new HashSet<>();

    @DynamoDbPartitionKey
    public String getId() {
        return id;
    }
}
