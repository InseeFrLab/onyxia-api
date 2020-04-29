package fr.insee.onyxia.api.controller.pub;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.info.BuildProperties;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name="Public")
@RequestMapping("/public")
public class HealthCheck {

    @Autowired
    BuildProperties buildProperties;

    @GetMapping("/healthcheck")
    public String healthcheck() {
       return buildProperties.getTime().toString();
    }
    
}
