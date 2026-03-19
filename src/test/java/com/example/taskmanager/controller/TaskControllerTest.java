package com.example.taskmanager.controller;

import com.example.taskmanager.model.AppUser;
import com.example.taskmanager.model.Task;
import com.example.taskmanager.repository.AppUserRepository;
import com.example.taskmanager.service.TaskService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.ui.Model;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TaskControllerTest {

    @Mock
    private TaskService taskService;

    @Mock
    private AppUserRepository appUserRepository;

    @Mock
    private Model model;

    @InjectMocks
    private TaskController taskController;

    private AppUser currentUser;
    private OAuth2User principal;

    @BeforeEach
    public void setup() {
        currentUser = AppUser.builder().id("sub-123").username("testuser").build();
        principal = new DefaultOAuth2User(
                Collections.emptyList(),
                Map.of("sub", "sub-123", "email", "test@test.com"),
                "email"
        );
    }

    @Test
    public void testIndex_AuthenticatedUser() {
        when(appUserRepository.findById("sub-123")).thenReturn(Optional.of(currentUser));
        when(taskService.getOwnedTasks(currentUser)).thenReturn(Collections.emptyList());
        when(taskService.getSharedTasks(currentUser)).thenReturn(Collections.emptyList());

        String viewName = taskController.index(principal, model);

        assertEquals("index", viewName);
        verify(model).addAttribute("ownedTasks", Collections.emptyList());
        verify(model).addAttribute("sharedTasks", Collections.emptyList());
        verify(model).addAttribute("currentUser", currentUser);
        verify(model).addAttribute("taskService", taskService);
    }

    @Test
    public void testCreateTask() {
        Task task = Task.builder().title("New Task").build();
        when(appUserRepository.findById("sub-123")).thenReturn(Optional.of(currentUser));

        String viewName = taskController.createTask(task, "tag1, tag2", principal);

        assertEquals("redirect:/", viewName);
        verify(taskService).createTask(eq(task), eq(currentUser), eq("tag1, tag2"));
    }
}
