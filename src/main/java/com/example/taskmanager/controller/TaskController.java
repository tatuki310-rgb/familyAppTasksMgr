package com.example.taskmanager.controller;

import com.example.taskmanager.model.AppUser;
import com.example.taskmanager.model.Tag;
import com.example.taskmanager.model.Task;
import com.example.taskmanager.repository.AppUserRepository;
import com.example.taskmanager.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.stream.Collectors;

@Controller
@RequestMapping("/")
public class TaskController {

    @Autowired
    private TaskService taskService;

    @Autowired
    private AppUserRepository appUserRepository;

    private AppUser getCurrentUser(OAuth2User principal) {
        if (principal == null) return null;
        String sub = principal.getAttribute("sub");
        return appUserRepository.findByCognitoSub(sub).orElseThrow(() -> new RuntimeException("User not found"));
    }

    @GetMapping
    public String index(@AuthenticationPrincipal OAuth2User principal, Model model) {
        AppUser user = getCurrentUser(principal);
        if (user != null) {
            model.addAttribute("ownedTasks", taskService.getOwnedTasks(user));
            model.addAttribute("sharedTasks", taskService.getSharedTasks(user));
            model.addAttribute("currentUser", user);
        }
        return "index";
    }

    @GetMapping("/tasks/new")
    public String newTaskForm(Model model) {
        model.addAttribute("task", new Task());
        model.addAttribute("tagsString", "");
        return "task-form";
    }

    @PostMapping("/tasks")
    public String createTask(@ModelAttribute Task task,
                             @RequestParam(value = "tagsString", required = false) String tagsString,
                             @AuthenticationPrincipal OAuth2User principal) {
        taskService.createTask(task, getCurrentUser(principal), tagsString);
        return "redirect:/";
    }

    @GetMapping("/tasks/{id}/edit")
    public String editTaskForm(@PathVariable Long id, Model model, @AuthenticationPrincipal OAuth2User principal) {
        Task task = taskService.getTaskById(id).orElseThrow(() -> new IllegalArgumentException("Invalid task Id:" + id));
        AppUser currentUser = getCurrentUser(principal);

        // Only owner can edit
        if (!task.getOwner().getId().equals(currentUser.getId())) {
            return "redirect:/";
        }

        String tagsString = task.getTags().stream().map(Tag::getName).collect(Collectors.joining(", "));
        model.addAttribute("task", task);
        model.addAttribute("tagsString", tagsString);
        return "task-form";
    }

    @PostMapping("/tasks/{id}")
    public String updateTask(@PathVariable Long id,
                             @ModelAttribute Task taskDetails,
                             @RequestParam(value = "tagsString", required = false) String tagsString,
                             @AuthenticationPrincipal OAuth2User principal) {
        Task task = taskService.getTaskById(id).orElseThrow();
        AppUser currentUser = getCurrentUser(principal);
        if (task.getOwner().getId().equals(currentUser.getId())) {
            taskService.updateTask(id, taskDetails, tagsString);
        }
        return "redirect:/";
    }

    @GetMapping("/tasks/{id}/delete")
    public String deleteTask(@PathVariable Long id, @AuthenticationPrincipal OAuth2User principal) {
        Task task = taskService.getTaskById(id).orElseThrow();
        AppUser currentUser = getCurrentUser(principal);
        if (task.getOwner().getId().equals(currentUser.getId())) {
            taskService.deleteTask(id);
        }
        return "redirect:/";
    }

    @GetMapping("/tasks/{id}/share")
    public String shareForm(@PathVariable Long id, Model model, @AuthenticationPrincipal OAuth2User principal) {
        Task task = taskService.getTaskById(id).orElseThrow();
        AppUser currentUser = getCurrentUser(principal);
        if (!task.getOwner().getId().equals(currentUser.getId())) {
            return "redirect:/";
        }
        model.addAttribute("task", task);
        return "share-form";
    }

    @PostMapping("/tasks/{id}/share")
    public String shareTask(@PathVariable Long id,
                            @RequestParam("usernameOrEmail") String usernameOrEmail,
                            RedirectAttributes redirectAttributes,
                            @AuthenticationPrincipal OAuth2User principal) {
        Task task = taskService.getTaskById(id).orElseThrow();
        AppUser currentUser = getCurrentUser(principal);

        if (task.getOwner().getId().equals(currentUser.getId())) {
            try {
                taskService.shareTask(id, usernameOrEmail);
                redirectAttributes.addFlashAttribute("message", "Task shared successfully!");
            } catch (Exception e) {
                redirectAttributes.addFlashAttribute("error", e.getMessage());
            }
        }
        return "redirect:/tasks/" + id + "/share";
    }
}
