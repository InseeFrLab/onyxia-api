package fr.insee.onyxia.api.services.control.utils;

import org.springframework.stereotype.Service;

/**
 * IDSanitizer
 */
@Service
public class IDSanitizer {

    public String sanitize(String text) {
        text = text.replaceAll("[^a-zA-Z0-9]", "");
        return text.toLowerCase();
    }
}