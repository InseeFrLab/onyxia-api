package fr.insee.onyxia.api.services.control;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * IDEnforcerTest
 */
@SpringBootTest
public class IDEnforcerTest {

    @Autowired
    IDEnforcer idEnforcer;

    @ParameterizedTest
    @ValueSource(strings = { "/myidep/*+/%$£¨|[{#random" })
    public void shouldConvertWithoutErrors(String text) {
        text = idEnforcer.sanitize(text);
        Pattern pattern = Pattern.compile("[a-z0-9]*");
        Matcher m = pattern.matcher(text);
        assertTrue(m.matches());
    }

}