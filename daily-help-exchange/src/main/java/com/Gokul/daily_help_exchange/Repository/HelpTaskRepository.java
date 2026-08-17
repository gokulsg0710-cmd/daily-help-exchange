package com.Gokul.daily_help_exchange.Repository;

import com.Gokul.daily_help_exchange.Model.HelpTask;
import com.Gokul.daily_help_exchange.Model.TaskStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface HelpTaskRepository extends JpaRepository<HelpTask, Long> {

    List<HelpTask> findAllByOrderByDueDateAsc();

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select task from HelpTask task where task.id = :id")
    Optional<HelpTask> findByIdForUpdate(@Param("id") Long id);

    long countByStatus(TaskStatus status);
}
