package com.joaovitor.taskflow.dto;

import com.joaovitor.taskflow.model.TaskPriority;
import com.joaovitor.taskflow.model.TaskStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record TaskRequest(
        @NotBlank(message = "O título é obrigatório")
        @Size(max = 120, message = "O título deve ter no máximo 120 caracteres")
        String title,

        @Size(max = 1000, message = "A descrição deve ter no máximo 1000 caracteres")
        String description,

        TaskStatus status,

        TaskPriority priority,

        LocalDate dueDate
) {
}
