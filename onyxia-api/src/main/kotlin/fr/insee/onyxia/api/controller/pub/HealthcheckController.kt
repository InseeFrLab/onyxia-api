package fr.insee.onyxia.api.controller.pub;

import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@Tag(name = "Public")
@RequestMapping("/public")
class HealthcheckController {

    @GetMapping("/healthcheck")
    fun healthcheck() {
    }
}
