package com.denzil.project_management.task.service;

import com.denzil.project_management.member.entity.Member;
import com.denzil.project_management.member.repository.MemberRepository;
import com.denzil.project_management.project.entity.Project;
import com.denzil.project_management.project.repository.ProjectRepository;
import com.denzil.project_management.shared.exception.ResourceNotFoundException;
import com.denzil.project_management.task.dto.BulkUpdateRequest;
import com.denzil.project_management.task.dto.CreateTaskRequest;
import com.denzil.project_management.task.dto.TaskDto;
import com.denzil.project_management.task.dto.TaskPositionDto;
import com.denzil.project_management.task.entity.Task;
import com.denzil.project_management.task.entity.TaskStatus;
import com.denzil.project_management.task.repository.TaskRepository;
import com.denzil.project_management.workspace.entity.Workspace;
import com.denzil.project_management.workspace.repository.WorkspaceRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final MemberRepository memberRepository;
    private final ProjectRepository projectRepository;
    private final WorkspaceRepository workspaceRepository;

    public TaskService(TaskRepository taskRepository, MemberRepository memberRepository,
            ProjectRepository projectRepository, WorkspaceRepository workspaceRepository) {
        this.taskRepository = taskRepository;
        this.memberRepository = memberRepository;
        this.projectRepository = projectRepository;
        this.workspaceRepository = workspaceRepository;
    }

    public TaskDto createTask(CreateTaskRequest request, String userId) {
        Member createdBy = memberRepository.findByUserIdAndWorkspaceId(UUID.fromString(userId), request.workspaceId())
                .orElseThrow(() -> new ResourceNotFoundException("Member not found"));

        Workspace workspace = workspaceRepository.findById(request.workspaceId())
                .orElseThrow(() -> new ResourceNotFoundException("Workspace not found"));

        Project project = projectRepository.findById(request.projectId())
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));

        Member assignee = null;

        if (request.assigneeId() != null) {
            assignee = memberRepository.findByUserIdAndWorkspaceId(request.assigneeId(), request.workspaceId())
                    .orElseThrow(() -> new ResourceNotFoundException("Assignee not found"));
        }

        Task task = new Task();
        task.setName(request.name());
        task.setStatus(request.status() != null ? request.status() : TaskStatus.TODO);
        task.setDueDate(request.dueDate());
        task.setWorkspace(workspace);
        task.setProject(project);
        task.setAssignee(assignee);
        task.setCreatedBy(createdBy);

        Task savedTask = taskRepository.save(task);

        return new TaskDto(savedTask.getId(), savedTask.getName(), savedTask.getStatus(), savedTask.getDescription(),
                savedTask.getDueDate(), savedTask.getPosition(), workspace.getId(), project.getId(),
                request.assigneeId(), createdBy.getId());
    }

    public List<TaskDto> getTasks(UUID workspaceId, UUID projectId, UUID assigneeId, UUID createdById,
            TaskStatus status, String userId) {
        boolean isMember = memberRepository.existsByUserIdAndWorkspaceId(UUID.fromString(userId), workspaceId);

        if (!isMember) {
            throw new ResourceNotFoundException("Member not found");
        }

        List<Task> tasks = taskRepository.findFilteredTasks(workspaceId, projectId, assigneeId, createdById, status);

        return tasks.stream()
                .map(t -> new TaskDto(t.getId(), t.getName(), t.getStatus(), t.getDescription(), t.getDueDate(),
                        t.getPosition(), t.getWorkspace().getId(), t.getProject().getId(),
                        t.getAssignee() != null ? t.getAssignee().getId() : null, t.getCreatedBy().getId()))
                .toList();
    }

    public void bulkUpdateTasks(BulkUpdateRequest request, String userId) {
        // 1. Authorize: Check if user is a member of the workspace
        boolean isMember = memberRepository.existsByUserIdAndWorkspaceId(UUID.fromString(userId),
                request.workspaceId());

        if (!isMember) {
            throw new ResourceNotFoundException("Workspace not found or access denied");
        }

        // 2. Prepare a list to hold the tasks we modify
        List<Task> tasksToUpdate = new ArrayList<>();

        // 3. Loop through the request and update each task
        for (TaskPositionDto updateInfo : request.tasks()) {
            Task task = taskRepository.findById(updateInfo.taskId())
                    .orElseThrow(() -> new ResourceNotFoundException("Task does not belong to this workspace"));

            // Security check: Make sure this task actually belongs to the workspace they
            // provided
            if (!task.getWorkspace().getId().equals(request.workspaceId())) {
                throw new RuntimeException("Task does not belong to this workspace");
            }

            // Update the fields
            task.setStatus(updateInfo.status());
            task.setPosition(updateInfo.position());

            tasksToUpdate.add(task);
        }

        // 4. Save all of them to the database in one big batch!
        taskRepository.saveAll(tasksToUpdate);
    }
}
