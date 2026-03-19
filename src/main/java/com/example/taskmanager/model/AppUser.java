package com.example.taskmanager.model;

import lombok.*;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@DynamoDbBean
public class AppUser {

    private String id; // Cognito Sub serves as the unique ID for simplicity
    private String username;
    private String email;

    @DynamoDbPartitionKey
    public String getId() {
        return id;
    }
}
