package com.Gokul.daily_help_exchange.Controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.Gokul.daily_help_exchange.Dto.DashboardResponse;
import com.Gokul.daily_help_exchange.Model.HelpTask;
import com.Gokul.daily_help_exchange.Service.HelpTaskService;

import jakarta.validation.Valid;
import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/api/tasks")
public class HelpTaskController {

    private final HelpTaskService service;

    public HelpTaskController(HelpTaskService service) {
        this.service = service;
    }

    @GetMapping
    public List<HelpTask> getAllTasks() {
        return service.getAllTask();
    }

    @GetMapping("/{id}")
    public HelpTask getTaskById(@PathVariable Long id) {
        return service.getTaskById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public HelpTask createTask(@Valid @RequestBody HelpTask task, HttpSession session) {
        return service.createTask(
                task,
                (Long) session.getAttribute("userId"),
                (String) session.getAttribute("userName")
        );
    }

    @PutMapping("/{id}")
    public HelpTask updateTask(
            @PathVariable Long id,
            @Valid @RequestBody HelpTask task
    ) {
        return service.updateTask(id, task);
    }

    @PatchMapping("/{id}/claim")
    public HelpTask claimTask(
            @PathVariable Long id,
            HttpSession session
    ) {
        return service.claimTask(
                id,
                (Long) session.getAttribute("userId"),
                (String) session.getAttribute("userName")
        );
    }

    @PatchMapping("/{id}/complete")
    public HelpTask completeTask(@PathVariable Long id) {
        return service.completeTask(id);
    }

    @PatchMapping("/{id}/reopen")
    public HelpTask reopenTask(@PathVariable Long id) {
        return service.reopenTask(id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTask(@PathVariable Long id) {
        service.deleteTask(id);
    }

    @GetMapping("/dashboard")
    public DashboardResponse getDashboard() {
        return service.getDashboard();
    }
}
