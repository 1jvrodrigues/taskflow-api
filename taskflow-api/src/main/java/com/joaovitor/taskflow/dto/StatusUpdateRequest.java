package com.joaovitor.taskflow.dto;

import com.joaovitor.taskflow.model.TaskStatus;
import jakarta.validation.constraints.NotNull;

public record StatusUpdateRequest(
        @NotNull(message = "O status é obrigatório")
        TaskStatus status
) {
}
