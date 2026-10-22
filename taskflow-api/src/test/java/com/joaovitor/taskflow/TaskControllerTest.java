package com.joaovitor.taskflow;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class TaskControllerTest {

    private static final String BASE = "/api/tasks";

    @Autowired
    private MockMvc mockMvc;

    private long createTask(String json) throws Exception {
        String response = mockMvc.perform(post(BASE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return ((Number) JsonPath.read(response, "$.id")).longValue();
    }

    @Test
    @DisplayName("POST cria tarefa com status TODO e prioridade MEDIUM por padrão")
    void createTask_usesDefaults() throws Exception {
        mockMvc.perform(post(BASE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Estudar Spring Boot\"}"))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.title").value("Estudar Spring Boot"))
                .andExpect(jsonPath("$.status").value("TODO"))
                .andExpect(jsonPath("$.priority").value("MEDIUM"));
    }

    @Test
    @DisplayName("POST com título em branco retorna 400 e detalha o campo inválido")
    void createTask_blankTitle_returnsBadRequest() throws Exception {
        mockMvc.perform(post(BASE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"   \"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.title").exists());
    }

    @Test
    @DisplayName("POST com status inexistente retorna 400")
    void createTask_invalidStatus_returnsBadRequest() throws Exception {
        mockMvc.perform(post(BASE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Teste\",\"status\":\"INEXISTENTE\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET de id inexistente retorna 404")
    void getTask_notFound_returns404() throws Exception {
        mockMvc.perform(get(BASE + "/999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Recurso não encontrado"));
    }

    @Test
    @DisplayName("PUT atualiza os campos da tarefa")
    void updateTask_changesFields() throws Exception {
        long id = createTask("{\"title\":\"Original\"}");

        mockMvc.perform(put(BASE + "/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Atualizada\",\"description\":\"Nova descrição\","
                                + "\"priority\":\"HIGH\",\"dueDate\":\"2026-12-31\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Atualizada"))
                .andExpect(jsonPath("$.description").value("Nova descrição"))
                .andExpect(jsonPath("$.priority").value("HIGH"))
                .andExpect(jsonPath("$.dueDate").value("2026-12-31"));
    }

    @Test
    @DisplayName("PATCH /status altera somente o status")
    void updateStatus_changesOnlyStatus() throws Exception {
        long id = createTask("{\"title\":\"Tarefa\",\"priority\":\"LOW\"}");

        mockMvc.perform(patch(BASE + "/" + id + "/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"DONE\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("DONE"))
                .andExpect(jsonPath("$.priority").value("LOW"))
                .andExpect(jsonPath("$.title").value("Tarefa"));
    }

    @Test
    @DisplayName("DELETE remove a tarefa e o GET seguinte retorna 404")
    void deleteTask_removesIt() throws Exception {
        long id = createTask("{\"title\":\"Remover\"}");

        mockMvc.perform(delete(BASE + "/" + id))
                .andExpect(status().isNoContent());

        mockMvc.perform(get(BASE + "/" + id))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET com ?status= filtra a listagem")
    void listTasks_filtersByStatus() throws Exception {
        createTask("{\"title\":\"A fazer\",\"status\":\"TODO\"}");
        createTask("{\"title\":\"Concluída\",\"status\":\"DONE\"}");

        mockMvc.perform(get(BASE).param("status", "DONE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].title").value("Concluída"));

        mockMvc.perform(get(BASE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(2));
    }
}
