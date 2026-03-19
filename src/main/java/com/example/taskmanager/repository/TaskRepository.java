package com.example.taskmanager.repository;

import com.example.taskmanager.model.Task;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.Key;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Repository
public class TaskRepository {

    private final DynamoDbTable<Task> taskTable;

    public TaskRepository(DynamoDbEnhancedClient enhancedClient) {
        this.taskTable = enhancedClient.table("Tasks", TableSchema.fromBean(Task.class));
    }

    public Task save(Task task) {
        taskTable.putItem(task);
        return task;
    }

    public Optional<Task> findById(String id) {
        Task task = taskTable.getItem(Key.builder().partitionValue(id).build());
        return Optional.ofNullable(task);
    }

    public void deleteById(String id) {
        taskTable.deleteItem(Key.builder().partitionValue(id).build());
    }

    public List<Task> findByOwnerId(String ownerId) {
        // Scanning is inefficient for large datasets, a GSI would be better in prod.
        return StreamSupport.stream(taskTable.scan().items().spliterator(), false)
                .filter(t -> ownerId.equals(t.getOwnerId()))
                .collect(Collectors.toList());
    }

    public List<Task> findBySharedUserContaining(String userId) {
        return StreamSupport.stream(taskTable.scan().items().spliterator(), false)
                .filter(t -> t.getSharedUsersIds() != null && t.getSharedUsersIds().contains(userId))
                .collect(Collectors.toList());
    }
}
