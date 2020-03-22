package fr.insee.onyxia.api.controller.pub;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@Tag(name="Public")
@RequestMapping("/public")
public class HealthCheck {

    @GetMapping("/healthcheck")
    public String healthcheck() {
       return null;
    }
    
}
