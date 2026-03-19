package com.example.taskmanager.service;

import com.example.taskmanager.model.AppUser;
import com.example.taskmanager.model.Task;
import com.example.taskmanager.repository.AppUserRepository;
import com.example.taskmanager.repository.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class TaskService {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private AppUserRepository appUserRepository;

    public List<Task> getOwnedTasks(AppUser user) {
        return taskRepository.findByOwnerId(user.getId());
    }

    public List<Task> getSharedTasks(AppUser user) {
        return taskRepository.findBySharedUserContaining(user.getId());
    }

    public Task createTask(Task task, AppUser owner, String tagsString) {
        task.setId(UUID.randomUUID().toString());
        task.setOwnerId(owner.getId());
        task.setTags(processTags(tagsString));
        if (task.getSharedUsersIds() == null) {
            task.setSharedUsersIds(new HashSet<>());
        }
        return taskRepository.save(task);
    }

    public Optional<Task> getTaskById(String id) {
        return taskRepository.findById(id);
    }

    public Task updateTask(String id, Task taskDetails, String tagsString) {
        Optional<Task> taskOpt = taskRepository.findById(id);
        if (taskOpt.isPresent()) {
            Task task = taskOpt.get();
            task.setTitle(taskDetails.getTitle());
            task.setDescription(taskDetails.getDescription());
            task.setTags(processTags(tagsString));
            return taskRepository.save(task);
        }
        throw new RuntimeException("Task not found");
    }

    public void deleteTask(String id) {
        taskRepository.deleteById(id);
    }

    public void shareTask(String taskId, String usernameOrEmail) {
        Optional<Task> taskOpt = taskRepository.findById(taskId);
        if (taskOpt.isPresent()) {
            Task task = taskOpt.get();
            Optional<AppUser> userOpt = appUserRepository.findByUsername(usernameOrEmail);
            if (userOpt.isEmpty()) {
                userOpt = appUserRepository.findByEmail(usernameOrEmail);
            }

            if (userOpt.isPresent()) {
                if(task.getSharedUsersIds() == null) {
                    task.setSharedUsersIds(new HashSet<>());
                }
                task.getSharedUsersIds().add(userOpt.get().getId());
                taskRepository.save(task);
            } else {
                throw new RuntimeException("User not found: " + usernameOrEmail);
            }
        }
    }

    private Set<String> processTags(String tagsString) {
        Set<String> tags = new HashSet<>();
        if (tagsString != null && !tagsString.trim().isEmpty()) {
            String[] tagNames = tagsString.split(",");
            for (String name : tagNames) {
                String cleanName = name.trim().toLowerCase();
                if (!cleanName.isEmpty()) {
                    tags.add(cleanName);
                }
            }
        }
        return tags;
    }

    // Helper to get actual AppUser objects for Thymeleaf
    public AppUser getUserById(String userId) {
        return appUserRepository.findById(userId).orElse(null);
    }
}
