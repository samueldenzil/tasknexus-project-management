package com.denzil.project_management.task.service;

import com.denzil.project_management.member.entity.Member;
import com.denzil.project_management.member.repository.MemberRepository;
import com.denzil.project_management.project.entity.Project;
import com.denzil.project_management.project.repository.ProjectRepository;
import com.denzil.project_management.shared.exception.ResourceNotFoundException;
import com.denzil.project_management.task.dto.CreateTaskRequest;
import com.denzil.project_management.task.dto.TaskDto;
import com.denzil.project_management.task.entity.Task;
import com.denzil.project_management.task.repository.TaskRepository;
import com.denzil.project_management.workspace.entity.Workspace;
import com.denzil.project_management.workspace.repository.WorkspaceRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final MemberRepository memberRepository;
    private final ProjectRepository projectRepository;
    private final WorkspaceRepository workspaceRepository;

    public TaskService(
            TaskRepository taskRepository,
            MemberRepository memberRepository,
            ProjectRepository projectRepository,
            WorkspaceRepository workspaceRepository
    ) {
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
        task.setStatus(request.status());
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
                project.getId(),
                request.assigneeId(),
                createdBy.getId()
        );
    }
}
