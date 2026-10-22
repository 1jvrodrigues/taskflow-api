package com.joaovitor.taskflow.service;

import com.joaovitor.taskflow.dto.PageResponse;
import com.joaovitor.taskflow.dto.StatusUpdateRequest;
import com.joaovitor.taskflow.dto.TaskRequest;
import com.joaovitor.taskflow.dto.TaskResponse;
import com.joaovitor.taskflow.exception.ResourceNotFoundException;
import com.joaovitor.taskflow.model.Task;
import com.joaovitor.taskflow.model.TaskPriority;
import com.joaovitor.taskflow.model.TaskStatus;
import com.joaovitor.taskflow.repository.TaskRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class TaskService {

    private static final int MAX_PAGE_SIZE = 100;

    private final TaskRepository repository;

    public TaskService(TaskRepository repository) {
        this.repository = repository;
    }

    public PageResponse<TaskResponse> list(TaskStatus status, int page, int size) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), MAX_PAGE_SIZE);
        Pageable pageable = PageRequest.of(safePage, safeSize, Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<Task> result = (status == null)
                ? repository.findAll(pageable)
                : repository.findByStatus(status, pageable);

        return PageResponse.from(result.map(TaskResponse::from));
    }

    public TaskResponse get(Long id) {
        return TaskResponse.from(findOrThrow(id));
    }

    @Transactional
    public TaskResponse create(TaskRequest request) {
        Task task = new Task();
        task.setTitle(request.title().trim());
        task.setDescription(request.description());
        task.setStatus(request.status() != null ? request.status() : TaskStatus.TODO);
        task.setPriority(request.priority() != null ? request.priority() : TaskPriority.MEDIUM);
        task.setDueDate(request.dueDate());
        return TaskResponse.from(repository.save(task));
    }

    @Transactional
    public TaskResponse update(Long id, TaskRequest request) {
        Task task = findOrThrow(id);
        task.setTitle(request.title().trim());
        task.setDescription(request.description());
        task.setDueDate(request.dueDate());
        if (request.status() != null) {
            task.setStatus(request.status());
        }
        if (request.priority() != null) {
            task.setPriority(request.priority());
        }
        return TaskResponse.from(repository.save(task));
    }

    @Transactional
    public TaskResponse updateStatus(Long id, StatusUpdateRequest request) {
        Task task = findOrThrow(id);
        task.setStatus(request.status());
        return TaskResponse.from(repository.save(task));
    }

    @Transactional
    public void delete(Long id) {
        repository.delete(findOrThrow(id));
    }

    private Task findOrThrow(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tarefa com id " + id + " não encontrada"));
    }
}
