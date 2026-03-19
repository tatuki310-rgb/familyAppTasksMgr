package com.example.taskmanager.service;

import com.example.taskmanager.model.AppUser;
import com.example.taskmanager.model.Tag;
import com.example.taskmanager.model.Task;
import com.example.taskmanager.repository.AppUserRepository;
import com.example.taskmanager.repository.TagRepository;
import com.example.taskmanager.repository.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@Transactional
public class TaskService {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private TagRepository tagRepository;

    @Autowired
    private AppUserRepository appUserRepository;

    public List<Task> getOwnedTasks(AppUser user) {
        return taskRepository.findByOwner(user);
    }

    public List<Task> getSharedTasks(AppUser user) {
        return taskRepository.findBySharedUsersContaining(user);
    }

    public Task createTask(Task task, AppUser owner, String tagsString) {
        task.setOwner(owner);
        task.setTags(processTags(tagsString));
        return taskRepository.save(task);
    }

    public Optional<Task> getTaskById(Long id) {
        return taskRepository.findById(id);
    }

    public Task updateTask(Long id, Task taskDetails, String tagsString) {
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

    public void deleteTask(Long id) {
        taskRepository.deleteById(id);
    }

    public void shareTask(Long taskId, String usernameOrEmail) {
        Optional<Task> taskOpt = taskRepository.findById(taskId);
        if (taskOpt.isPresent()) {
            Task task = taskOpt.get();
            Optional<AppUser> userOpt = appUserRepository.findByUsername(usernameOrEmail);
            if (userOpt.isEmpty()) {
                userOpt = appUserRepository.findByEmail(usernameOrEmail);
            }

            if (userOpt.isPresent()) {
                task.getSharedUsers().add(userOpt.get());
                taskRepository.save(task);
            } else {
                throw new RuntimeException("User not found: " + usernameOrEmail);
            }
        }
    }

    private Set<Tag> processTags(String tagsString) {
        Set<Tag> tags = new HashSet<>();
        if (tagsString != null && !tagsString.trim().isEmpty()) {
            String[] tagNames = tagsString.split(",");
            for (String name : tagNames) {
                String cleanName = name.trim().toLowerCase();
                if (!cleanName.isEmpty()) {
                    Tag tag = tagRepository.findByName(cleanName)
                            .orElseGet(() -> tagRepository.save(Tag.builder().name(cleanName).build()));
                    tags.add(tag);
                }
            }
        }
        return tags;
    }
}
