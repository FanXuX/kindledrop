package io.kindledrop.engine.api;

import io.kindledrop.engine.service.SendToKindleService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mail.MailAuthenticationException;
import org.springframework.mail.MailSendException;
import org.springframework.test.web.servlet.MockMvc;

import java.net.SocketTimeoutException;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = SendController.class)
public class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private SendToKindleService service;

    private static final String BASE_JSON = "{\"url\":\"https://github.com/org/repo/blob/main/file.pdf\",\"kindleEmail\":\"a@b.com\"}";

    @Test
    void illegalArgumentReturns400() throws Exception {
        Mockito.when(service.send(Mockito.any())).thenThrow(new IllegalArgumentException("bad input"));

        mvc.perform(post("/api/send").contentType(MediaType.APPLICATION_JSON).content(BASE_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.ok").value(false))
                .andExpect(jsonPath("$.message").value("bad input"));
    }

    @Test
    void mailAuthReturns502() throws Exception {
        Mockito.when(service.send(Mockito.any())).thenThrow(new MailAuthenticationException("auth failed"));

        mvc.perform(post("/api/send").contentType(MediaType.APPLICATION_JSON).content(BASE_JSON))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.ok").value(false))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("SMTP failed")));
    }

    @Test
    void mailSendReturns502() throws Exception {
        Mockito.when(service.send(Mockito.any())).thenThrow(new MailSendException("send failed"));

        mvc.perform(post("/api/send").contentType(MediaType.APPLICATION_JSON).content(BASE_JSON))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.ok").value(false))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("SMTP failed")));
    }

    @Test
    void socketTimeoutReturns504() throws Exception {
        Mockito.when(service.send(Mockito.any())).thenThrow(new SocketTimeoutException("timeout"));

        mvc.perform(post("/api/send").contentType(MediaType.APPLICATION_JSON).content(BASE_JSON))
                .andExpect(status().isGatewayTimeout())
                .andExpect(jsonPath("$.ok").value(false))
                .andExpect(jsonPath("$.message").value("SMTP timeout"));
    }

    @Test
    void fallbackReturns500() throws Exception {
        Mockito.when(service.send(Mockito.any())).thenThrow(new RuntimeException("boom"));

        mvc.perform(post("/api/send").contentType(MediaType.APPLICATION_JSON).content(BASE_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.ok").value(false))
                .andExpect(jsonPath("$.message").value("Unexpected error"));
    }
}
