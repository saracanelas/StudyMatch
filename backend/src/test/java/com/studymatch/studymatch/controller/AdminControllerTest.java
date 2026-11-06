package com.studymatch.studymatch.controller;

import com.studymatch.studymatch.dto.AdminRequest;
import com.studymatch.studymatch.dto.AdminResponse;
import com.studymatch.studymatch.model.Admin;
import com.studymatch.studymatch.repository.AdminRepository;
import com.studymatch.studymatch.service.AdminService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class AdminControllerTest {

    private AdminService adminService;
    private MockMvc mockMvc;

    private static final String BODY =
            "{\"nameAdmin\":\"Maria\",\"emailAdmin\":\"maria@studymatch.pt\",\"passwordAdmin\":\"secret\"}";

    @BeforeEach
    void setUp() {
        adminService = Mockito.mock(AdminService.class);
        mockMvc = MockMvcBuilders.standaloneSetup(new AdminController(adminService)).build();
    }

    @Test
    @DisplayName("POST /api/admin returns 201 with the created admin")
    void createAdminReturns201() throws Exception {
        AdminResponse created = new AdminResponse(1L, "Maria", "maria@studymatch.pt");
        when(adminService.createAdmin(any(AdminRequest.class))).thenReturn(created);

        mockMvc.perform(post("/api/admin").contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idAdmin").value(1))
                .andExpect(jsonPath("$.emailAdmin").value("maria@studymatch.pt"));
    }

    @Test
    @DisplayName("POST /api/admin with duplicate email returns 409")
    void createAdminConflictReturns409() throws Exception {
        when(adminService.createAdmin(any(AdminRequest.class)))
                .thenThrow(new ResponseStatusException(HttpStatus.CONFLICT, "exists"));

        mockMvc.perform(post("/api/admin").contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("POST /api/admin with malformed JSON returns 400")
    void malformedJsonReturns400() throws Exception {
        mockMvc.perform(post("/api/admin").contentType(MediaType.APPLICATION_JSON).content("{not json"))
                .andExpect(status().isBadRequest());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "{\"emailAdmin\":\"maria@studymatch.pt\",\"passwordAdmin\":\"secret\"}",
            "{\"nameAdmin\":\" \",\"emailAdmin\":\"maria@studymatch.pt\",\"passwordAdmin\":\"secret\"}",
            "{\"nameAdmin\":\"Maria\",\"passwordAdmin\":\"secret\"}",
            "{\"nameAdmin\":\"Maria\",\"emailAdmin\":\"not-an-email\",\"passwordAdmin\":\"secret\"}",
            "{\"nameAdmin\":\"Maria\",\"emailAdmin\":\"maria@studymatch.pt\"}",
            "{\"nameAdmin\":\"Maria\",\"emailAdmin\":\"maria@studymatch.pt\",\"passwordAdmin\":\" \"}"
    })
    @DisplayName("POST /api/admin with an invalid body returns 400 without calling the service")
    void invalidBodyReturns400(String json) throws Exception {
        mockMvc.perform(post("/api/admin").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(adminService);
    }

    @Test
    @DisplayName("GET /api/admin returns the list of admins")
    void getAllAdminsReturnsList() throws Exception {
        when(adminService.getAllAdmins()).thenReturn(List.of(
                new AdminResponse(1L, "A", "a@studymatch.pt"),
                new AdminResponse(2L, "B", "b@studymatch.pt")));

        mockMvc.perform(get("/api/admin"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[1].emailAdmin").value("b@studymatch.pt"));
    }

    @Test
    @DisplayName("POST and GET /api/admin never expose the password, even when the stored admin has one")
    void responsesDoNotContainPassword() throws Exception {
        // Uses the real service, so the stored entity (which holds the hash) goes through the real mapping
        String storedHash = "stored-bcrypt-hash";
        Admin stored = new Admin(1L, "Maria", "maria@studymatch.pt", storedHash);
        AdminRepository repository = Mockito.mock(AdminRepository.class);
        when(repository.save(any(Admin.class))).thenReturn(stored);
        when(repository.findAll()).thenReturn(List.of(stored));
        MockMvc realServiceMvc = MockMvcBuilders
                .standaloneSetup(new AdminController(new AdminService(repository))).build();

        realServiceMvc.perform(post("/api/admin").contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.emailAdmin").value("maria@studymatch.pt"))
                .andExpect(jsonPath("$.passwordAdmin").doesNotExist())
                .andExpect(content().string(not(containsString(storedHash))))
                .andExpect(content().string(not(containsString("secret"))));

        realServiceMvc.perform(get("/api/admin"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].emailAdmin").value("maria@studymatch.pt"))
                .andExpect(jsonPath("$[0].passwordAdmin").doesNotExist())
                .andExpect(content().string(not(containsString(storedHash))));
    }

    @Test
    @DisplayName("POST /api/admin ignores an idAdmin sent by the client, so it cannot overwrite an existing admin")
    void clientIdIsIgnored() throws Exception {
        AdminRepository repository = Mockito.mock(AdminRepository.class);
        when(repository.save(any(Admin.class))).thenReturn(new Admin(7L, "Maria", "maria@studymatch.pt", "hash"));
        MockMvc realServiceMvc = MockMvcBuilders
                .standaloneSetup(new AdminController(new AdminService(repository))).build();
        String bodyWithId = "{\"idAdmin\":1,\"nameAdmin\":\"Maria\","
                + "\"emailAdmin\":\"maria@studymatch.pt\",\"passwordAdmin\":\"secret\"}";

        realServiceMvc.perform(post("/api/admin").contentType(MediaType.APPLICATION_JSON).content(bodyWithId))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idAdmin").value(7));

        ArgumentCaptor<Admin> captor = ArgumentCaptor.forClass(Admin.class);
        verify(repository).save(captor.capture());
        assertNull(captor.getValue().getIdAdmin());
    }
}
