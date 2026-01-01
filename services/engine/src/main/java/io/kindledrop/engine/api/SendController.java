package io.kindledrop.engine.api;

import io.kindledrop.engine.service.SendToKindleService;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class SendController {

    private final SendToKindleService service;

    public SendController(SendToKindleService service) {
        this.service = service;
    }

    @PostMapping(path = "/send", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public SendResponse send(@Valid @RequestBody SendRequest req) throws Exception {
        return service.send(req);
    }
}
