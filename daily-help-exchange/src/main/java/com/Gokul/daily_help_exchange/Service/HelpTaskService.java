package com.Gokul.daily_help_exchange.Service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.Gokul.daily_help_exchange.Dto.DashboardResponse;
import com.Gokul.daily_help_exchange.Exception.ResourceNotFoundException;
import com.Gokul.daily_help_exchange.Model.HelpTask;
import com.Gokul.daily_help_exchange.Model.TaskStatus;
import com.Gokul.daily_help_exchange.Repository.HelpTaskRepository;

@Service
@Transactional
public class HelpTaskService {

    private final HelpTaskRepository repository;

    public HelpTaskService(HelpTaskRepository repository) {
        this.repository = repository;
    }

    /*
     * Read all tasks
     */
    @Transactional(readOnly = true)
    public List<HelpTask> getAllTask() {
        return repository.findAllByOrderByDueDateAsc();
    }

    /*
     * Read one task
     */
    @Transactional(readOnly = true)
    public HelpTask getTaskById(Long id) {
        return repository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Task not found with id: " + id
                        )
                );
    }

    /*
     * Create a new task
     *
     * Every newly created task automatically starts
     * with OPEN status.
     */
    public HelpTask createTask(
            HelpTask task,
            Long creatorUserId,
            String creatorName
    ) {
        task.setId(null);

        task.setCreatedBy(creatorName);
        task.setCreatedByUserId(creatorUserId);

        // New tasks always start as OPEN
        task.setStatus(TaskStatus.OPEN);

        // New tasks cannot already be claimed
        task.setClaimedBy(null);
        task.setClaimedByUserId(null);

        return repository.save(task);
    }

    /*
     * Update an existing task
     */
    public HelpTask updateTask(
            Long id,
            HelpTask updatedTask
    ) {
        HelpTask existingTask = repository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Task not found with id: " + id
                        )
                );

        existingTask.setTitle(updatedTask.getTitle());
        existingTask.setDescription(updatedTask.getDescription());
        existingTask.setDueDate(updatedTask.getDueDate());
        existingTask.setPriority(updatedTask.getPriority());
        existingTask.setRewardPoints(updatedTask.getRewardPoints());

        return repository.save(existingTask);
    }

    /*
     * Claim an open task
     */
    public HelpTask claimTask(
            Long id,
            Long claimantUserId,
            String claimantName
    ) {
        HelpTask task = repository.findByIdForUpdate(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Task not found with id: " + id
                        )
                );

        if (task.getStatus() != TaskStatus.OPEN) {
            throw new IllegalStateException(
                    "Task is not open and cannot be claimed"
            );
        }

        task.setStatus(TaskStatus.CLAIMED);
        task.setClaimedBy(claimantName);
        task.setClaimedByUserId(claimantUserId);

        return repository.save(task);
    }

    /*
     * Complete a claimed task
     */
    public HelpTask completeTask(Long id) {

        HelpTask task = repository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Task not found with id: " + id
                        )
                );

        if (task.getStatus() != TaskStatus.CLAIMED) {
            throw new IllegalStateException(
                    "Only claimed tasks can be marked as completed"
            );
        }

        task.setStatus(TaskStatus.COMPLETED);

        return repository.save(task);
    }

    /*
     * Reopen a completed task
     */
    public HelpTask reopenTask(Long id) {

        HelpTask task = repository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Task not found with id: " + id
                        )
                );

        if (task.getStatus() != TaskStatus.COMPLETED) {
            throw new IllegalStateException(
                    "Only completed tasks can be reopened"
            );
        }

        task.setStatus(TaskStatus.OPEN);

        task.setClaimedBy(null);
        task.setClaimedByUserId(null);

        return repository.save(task);
    }

    /*
     * Delete a task
     */
    public void deleteTask(Long id) {

        HelpTask task = repository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Task not found with id: " + id
                        )
                );

        repository.delete(task);
    }

    /*
     * Dashboard information
     */
    @Transactional(readOnly = true)
    public DashboardResponse getDashboard() {

        long total = repository.count();

        long open = repository.countByStatus(
                TaskStatus.OPEN
        );

        long claimed = repository.countByStatus(
                TaskStatus.CLAIMED
        );

        long completed = repository.countByStatus(
                TaskStatus.COMPLETED
        );

        long totalReward = repository.findAll()
                .stream()
                .filter(task ->
                        task.getStatus() == TaskStatus.COMPLETED
                )
                .mapToLong(HelpTask::getRewardPoints)
                .sum();

        return new DashboardResponse(
                total,
                open,
                claimed,
                completed,
                totalReward
        );
    }
}