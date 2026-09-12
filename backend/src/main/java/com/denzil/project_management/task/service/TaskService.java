package com.denzil.project_management.task.service;

import com.denzil.project_management.member.entity.Member;
import com.denzil.project_management.member.repository.MemberRepository;
import com.denzil.project_management.project.entity.Project;
import com.denzil.project_management.project.repository.ProjectRepository;
import com.denzil.project_management.shared.exception.BadRequestException;
import com.denzil.project_management.shared.exception.ResourceNotFoundException;
import com.denzil.project_management.task.dto.*;
import com.denzil.project_management.task.entity.Task;
import com.denzil.project_management.task.entity.TaskStatus;
import com.denzil.project_management.task.repository.TaskRepository;
import com.denzil.project_management.workspace.entity.Workspace;
import com.denzil.project_management.workspace.repository.WorkspaceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
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

        @Transactional
        public TaskDto createTask(CreateTaskRequest request, String userId) {
                Member createdBy = memberRepository
                                .findByUserIdAndWorkspaceId(UUID.fromString(userId), request.workspaceId())
                                .orElseThrow(() -> new ResourceNotFoundException("Member not found"));

                Workspace workspace = workspaceRepository.findById(request.workspaceId())
                                .orElseThrow(() -> new ResourceNotFoundException("Workspace not found"));

                Project project = projectRepository.findById(request.projectId())
                                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));

                // Ensure the project actually belongs to the requested workspace,
                // preventing cross-workspace data leakage.
                if (!project.getWorkspace().getId().equals(request.workspaceId())) {
                        throw new BadRequestException("Project does not belong to the specified workspace");
                }

                Member assignee = null;

                if (request.assigneeId() != null) {
                        assignee = memberRepository.findById(request.assigneeId())
                                        .orElseThrow(() -> new ResourceNotFoundException("Assignee not found"));

                        if (!assignee.getWorkspace().getId().equals(request.workspaceId())) {
                                throw new ResourceNotFoundException("Assignee does not belong to this workspace");
                        }
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

                return new TaskDto(
                                savedTask.getId(),
                                savedTask.getName(),
                                savedTask.getStatus(),
                                savedTask.getDescription(),
                                savedTask.getDueDate(),
                                savedTask.getPosition(),
                                workspace.getId(),
                                new TaskDto.ProjectSummaryDto(project.getId(), project.getName(),
                                                project.getImageUrl()),
                                assignee != null ? new TaskDto.AssigneeSummaryDto(assignee.getId(),
                                                assignee.getUser().getName())
                                                : null,
                                new TaskDto.CreatedBySummaryDto(createdBy.getId(), createdBy.getUser().getName()));
        }

        @Transactional(readOnly = true)
        public List<TaskDto> getTasks(UUID workspaceId, UUID projectId, UUID assigneeId, UUID createdById,
                        TaskStatus status, String search, LocalDate dueDate, String userId) {
                boolean isMember = memberRepository.existsByUserIdAndWorkspaceId(UUID.fromString(userId), workspaceId);

                if (!isMember) {
                        throw new ResourceNotFoundException("Member not found or Access denied");
                }

                List<Task> tasks = taskRepository.findFilteredTasks(workspaceId, projectId, assigneeId, createdById,
                                status, search, dueDate);

                return tasks.stream()
                                .map(t -> {
                                        Project project = t.getProject();
                                        Member assignee = t.getAssignee();
                                        Member createdBy = t.getCreatedBy();

                                        return new TaskDto(t.getId(), t.getName(), t.getStatus(), t.getDescription(),
                                                        t.getDueDate(),
                                                        t.getPosition(), t.getWorkspace().getId(),
                                                        new TaskDto.ProjectSummaryDto(project.getId(),
                                                                        project.getName(), project.getImageUrl()),
                                                        assignee != null
                                                                        ? new TaskDto.AssigneeSummaryDto(
                                                                                        assignee.getId(),
                                                                                        assignee.getUser().getName())
                                                                        : null,
                                                        new TaskDto.CreatedBySummaryDto(createdBy.getId(),
                                                                        createdBy.getUser().getName()));
                                })
                                .toList();
        }

        @Transactional(readOnly = true)
        public TaskDto getTask(UUID taskId, String userId) {
                Task task = taskRepository.findById(taskId)
                                .orElseThrow(() -> new ResourceNotFoundException("Task not found"));

                UUID workspaceId = task.getWorkspace().getId();

                boolean isMember = memberRepository.existsByUserIdAndWorkspaceId(UUID.fromString(userId), workspaceId);

                if (!isMember) {
                        throw new ResourceNotFoundException("Member not found or Access denied");
                }

                Project project = task.getProject();
                Member assignee = task.getAssignee();
                Member createdBy = task.getCreatedBy();

                return new TaskDto(
                                task.getId(),
                                task.getName(),
                                task.getStatus(),
                                task.getDescription(),
                                task.getDueDate(),
                                task.getPosition(),
                                workspaceId,
                                new TaskDto.ProjectSummaryDto(project.getId(), project.getName(),
                                                project.getImageUrl()),
                                assignee != null
                                                ? new TaskDto.AssigneeSummaryDto(assignee.getId(),
                                                                assignee.getUser().getName())
                                                : null,
                                new TaskDto.CreatedBySummaryDto(createdBy.getId(), createdBy.getUser().getName()));
        }

        @Transactional
        public TaskDto updateTask(UUID taskId, UpdateTaskRequest request, String userId) {
                Task task = taskRepository.findById(taskId)
                                .orElseThrow(() -> new ResourceNotFoundException("Task not found"));

                UUID workspaceId = task.getWorkspace().getId();

                boolean isMember = memberRepository.existsByUserIdAndWorkspaceId(UUID.fromString(userId), workspaceId);

                if (!isMember) {
                        throw new ResourceNotFoundException("Member not found or Access denied");
                }

                // projectId is @NotNull in UpdateTaskRequest, so this will never produce null
                Project project = projectRepository.findById(request.projectId())
                                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));

                // Ensure the new project belongs to the task's workspace,
                // preventing cross-workspace task migration.
                if (!project.getWorkspace().getId().equals(workspaceId)) {
                        throw new BadRequestException("Project does not belong to the task's workspace");
                }

                // assigneeId is optional — null means "unassigned"
                Member assignee = request.assigneeId() != null
                                ? memberRepository.findById(request.assigneeId())
                                                .orElseThrow(() -> new ResourceNotFoundException("Assignee not found"))
                                : null;

                task.setName(request.name());
                task.setStatus(request.status());
                task.setProject(project);
                task.setAssignee(assignee);
                if (request.description() != null) {
                        task.setDescription(request.description());
                }
                task.setDueDate(request.dueDate());

                Task updatedTask = taskRepository.save(task);

                // Read project and createdBy from the saved entity to ensure the response
                // always reflects what is persisted, not a potentially stale local variable
                Project savedProject = updatedTask.getProject();
                Member savedCreatedBy = updatedTask.getCreatedBy();

                return new TaskDto(
                                updatedTask.getId(),
                                updatedTask.getName(),
                                updatedTask.getStatus(),
                                updatedTask.getDescription(),
                                updatedTask.getDueDate(),
                                updatedTask.getPosition(),
                                workspaceId,
                                new TaskDto.ProjectSummaryDto(savedProject.getId(), savedProject.getName(),
                                                savedProject.getImageUrl()),
                                assignee != null
                                                ? new TaskDto.AssigneeSummaryDto(assignee.getId(),
                                                                assignee.getUser().getName())
                                                : null,
                                new TaskDto.CreatedBySummaryDto(savedCreatedBy.getId(),
                                                savedCreatedBy.getUser().getName()));
        }

        @Transactional
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
                                        .orElseThrow(() -> new ResourceNotFoundException(
                                                        "Task does not belong to this workspace"));

                        // Security check: Make sure this task actually belongs to the workspace they
                        // provided
                        if (!task.getWorkspace().getId().equals(request.workspaceId())) {
                                throw new BadRequestException("Task does not belong to this workspace");
                        }

                        // Update the fields
                        task.setStatus(updateInfo.status());
                        task.setPosition(updateInfo.position());

                        tasksToUpdate.add(task);
                }

                // 4. Save all of them to the database in one big batch!
                taskRepository.saveAll(tasksToUpdate);
        }

        @Transactional
        public void deleteTask(UUID taskId, String userId) {
                Task task = taskRepository.findById(taskId)
                                .orElseThrow(() -> new ResourceNotFoundException("Task not found"));

                UUID workspaceId = task.getWorkspace().getId();

                boolean isMember = memberRepository.existsByUserIdAndWorkspaceId(UUID.fromString(userId), workspaceId);

                if (!isMember) {
                        throw new ResourceNotFoundException("Workspace not found or access denied");
                }

                taskRepository.deleteById(taskId);
        }
}
