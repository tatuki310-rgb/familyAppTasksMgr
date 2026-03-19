package com.example.taskmanager.service;

import com.example.taskmanager.model.AppUser;
import com.example.taskmanager.model.Tag;
import com.example.taskmanager.model.Task;
import com.example.taskmanager.repository.AppUserRepository;
import com.example.taskmanager.repository.TagRepository;
import com.example.taskmanager.repository.TaskRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private TagRepository tagRepository;

    @Mock
    private AppUserRepository appUserRepository;

    @InjectMocks
    private TaskService taskService;

    @Test
    public void testCreateTask() {
        AppUser owner = AppUser.builder().id(1L).username("testuser").build();
        Task taskToCreate = Task.builder().title("Test Task").description("Test Description").build();

        when(tagRepository.findByName("urgent")).thenReturn(Optional.of(Tag.builder().id(1L).name("urgent").build()));
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Task createdTask = taskService.createTask(taskToCreate, owner, "urgent, work");

        assertNotNull(createdTask);
        assertEquals(owner, createdTask.getOwner());
        assertEquals(2, createdTask.getTags().size());
        verify(taskRepository, times(1)).save(taskToCreate);
    }

    @Test
    public void testShareTask_Success() {
        Task task = Task.builder().id(1L).title("Test").build();
        AppUser userToShareWith = AppUser.builder().id(2L).username("friend").build();

        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        when(appUserRepository.findByUsername("friend")).thenReturn(Optional.of(userToShareWith));

        taskService.shareTask(1L, "friend");

        assertTrue(task.getSharedUsers().contains(userToShareWith));
        verify(taskRepository, times(1)).save(task);
    }

    @Test
    public void testShareTask_UserNotFound() {
        Task task = Task.builder().id(1L).title("Test").build();

        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        when(appUserRepository.findByUsername("unknown")).thenReturn(Optional.empty());
        when(appUserRepository.findByEmail("unknown")).thenReturn(Optional.empty());

        Exception exception = assertThrows(RuntimeException.class, () -> {
            taskService.shareTask(1L, "unknown");
        });

        assertEquals("User not found: unknown", exception.getMessage());
        verify(taskRepository, never()).save(task);
    }
}
