package com.example.taskmanager.service;

import com.example.taskmanager.model.AppUser;
import com.example.taskmanager.model.Task;
import com.example.taskmanager.repository.AppUserRepository;
import com.example.taskmanager.repository.TaskRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashSet;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private AppUserRepository appUserRepository;

    @InjectMocks
    private TaskService taskService;

    @Test
    public void testCreateTask() {
        AppUser owner = AppUser.builder().id("user-1").username("testuser").build();
        Task taskToCreate = Task.builder().title("Test Task").description("Test Description").build();

        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Task createdTask = taskService.createTask(taskToCreate, owner, "urgent, work");

        assertNotNull(createdTask);
        assertEquals(owner.getId(), createdTask.getOwnerId());
        assertEquals(2, createdTask.getTags().size());
        assertTrue(createdTask.getTags().contains("urgent"));
        assertTrue(createdTask.getTags().contains("work"));
        verify(taskRepository, times(1)).save(taskToCreate);
    }

    @Test
    public void testShareTask_Success() {
        Task task = Task.builder().id("task-1").title("Test").build();
        AppUser userToShareWith = AppUser.builder().id("user-2").username("friend").build();

        when(taskRepository.findById("task-1")).thenReturn(Optional.of(task));
        when(appUserRepository.findByUsername("friend")).thenReturn(Optional.of(userToShareWith));

        taskService.shareTask("task-1", "friend");

        assertTrue(task.getSharedUsersIds().contains(userToShareWith.getId()));
        verify(taskRepository, times(1)).save(task);
    }

    @Test
    public void testShareTask_UserNotFound() {
        Task task = Task.builder().id("task-1").title("Test").build();

        when(taskRepository.findById("task-1")).thenReturn(Optional.of(task));
        when(appUserRepository.findByUsername("unknown")).thenReturn(Optional.empty());
        when(appUserRepository.findByEmail("unknown")).thenReturn(Optional.empty());

        Exception exception = assertThrows(RuntimeException.class, () -> {
            taskService.shareTask("task-1", "unknown");
        });

        assertEquals("User not found: unknown", exception.getMessage());
        verify(taskRepository, never()).save(task);
    }
}
