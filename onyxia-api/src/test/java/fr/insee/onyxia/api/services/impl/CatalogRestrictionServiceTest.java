package fr.insee.onyxia.api.services.impl;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import fr.insee.onyxia.api.configuration.CatalogWrapper;
import fr.insee.onyxia.model.User;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CatalogRestrictionServiceTest {

    private CatalogRestrictionService service;

    @BeforeEach
    void setUp() {
        service = new CatalogRestrictionService();
    }

    @Test
    void shouldReturnAllCatalogsWhenNoRestrictions() {
        CatalogWrapper catalogWrapper = catalogWithRestriction("1");
        CatalogRestrictionService service = new CatalogRestrictionService();
        boolean result =
                service.isCatalogVisibleToUser(getUserWithAttributes(Map.of()), catalogWrapper);
        assertTrue(result);
    }

    @Test
    void shouldReturnAllCatalogsWhenNoRestrictionsAndNoUser() {
        CatalogWrapper catalogWrapper = catalogWithRestriction("1");
        CatalogRestrictionService service = new CatalogRestrictionService();
        boolean result = service.isCatalogVisibleToUser(null, catalogWrapper);
        assertTrue(result);
    }

    @Test
    void shouldReturnNoCatalogsWhenRestrictionsDontMatch() {
        String key = "KEY";
        String match = "match-this";
        String userAttributeValue = "something-else";

        var restrictions = new CatalogWrapper.CatalogRestrictions();
        var restrictionAttribute = new CatalogWrapper.CatalogRestrictions.UserAttribute();
        restrictionAttribute.setKey(key);
        restrictionAttribute.setMatches(match);
        restrictions.setUserAttribute(restrictionAttribute);

        CatalogWrapper catalog = catalogWithRestriction("0", restrictions);

        boolean result =
                service.isCatalogVisibleToUser(
                        getUserWithAttributes(Map.of(key, userAttributeValue)), catalog);

        assertFalse(result);
    }

    @Test
    void shouldReturnNoCatalogsWhenRestrictionsAndNoUser() {
        String key = "KEY";
        String match = "match-this";

        var restrictions = new CatalogWrapper.CatalogRestrictions();
        var restrictionAttribute = new CatalogWrapper.CatalogRestrictions.UserAttribute();
        restrictionAttribute.setKey(key);
        restrictionAttribute.setMatches(match);
        restrictions.setUserAttribute(restrictionAttribute);

        CatalogWrapper catalog = catalogWithRestriction("0", restrictions);

        boolean result = service.isCatalogVisibleToUser(null, catalog);

        assertFalse(result);
    }

    @Test
    void shouldReturnNoCatalogsWhenUserDontHaveAttributeKey() {
        String key = "KEY";
        String match = "match-this";
        String userAttributeKey = "SOMETHING";
        String userAttributeValue = match;

        var restrictions = new CatalogWrapper.CatalogRestrictions();
        var restrictionAttribute = new CatalogWrapper.CatalogRestrictions.UserAttribute();
        restrictionAttribute.setKey(key);
        restrictionAttribute.setMatches(match);
        restrictions.setUserAttribute(restrictionAttribute);

        CatalogWrapper catalog = catalogWithRestriction("0", restrictions);

        boolean result =
                service.isCatalogVisibleToUser(
                        getUserWithAttributes(Map.of(userAttributeKey, userAttributeValue)),
                        catalog);

        assertFalse(result);
    }

    @Test
    void shouldReturnCatalogsWhenRestrictionsMatch() {
        String key = "KEY";
        String match = "match-this";
        String userAttributeValue = match;

        var restrictions = new CatalogWrapper.CatalogRestrictions();
        var restrictionAttribute = new CatalogWrapper.CatalogRestrictions.UserAttribute();
        restrictionAttribute.setKey(key);
        restrictionAttribute.setMatches(match);
        restrictions.setUserAttribute(restrictionAttribute);

        CatalogRestrictionService service = new CatalogRestrictionService();

        var catalog = catalogWithRestriction("0", restrictions);
        boolean result =
                service.isCatalogVisibleToUser(
                        getUserWithAttributes(Map.of(key, userAttributeValue)), catalog);

        assertTrue(result);
    }

    @Test
    void shouldSupportRestrictionsByString() {
        String key = "KEY";
        String match = "match-this";
        String userAttributeValue = match;

        var restrictions = new CatalogWrapper.CatalogRestrictions();
        var restrictionAttribute = new CatalogWrapper.CatalogRestrictions.UserAttribute();
        restrictionAttribute.setKey(key);
        restrictionAttribute.setMatches(match);
        restrictions.setUserAttribute(restrictionAttribute);

        CatalogRestrictionService service = new CatalogRestrictionService();
        var catalog = catalogWithRestriction("0", restrictions);
        boolean result =
                service.isCatalogVisibleToUser(
                        getUserWithAttributes(Map.of(key, userAttributeValue)), catalog);

        assertTrue(result);
    }

    @Test
    void shouldSupportRestrictionsByRegex() {
        String key = "KEY";
        String match = "[a-z ]*";
        String userAttributeValue = "hello world";

        var restrictions = new CatalogWrapper.CatalogRestrictions();
        var restrictionAttribute = new CatalogWrapper.CatalogRestrictions.UserAttribute();
        restrictionAttribute.setKey(key);
        restrictionAttribute.setMatches(match);
        restrictions.setUserAttribute(restrictionAttribute);

        CatalogRestrictionService service = new CatalogRestrictionService();
        var catalog = catalogWithRestriction("0", restrictions);
        boolean result =
                service.isCatalogVisibleToUser(
                        getUserWithAttributes(Map.of(key, userAttributeValue)), catalog);

        assertTrue(result);
    }

    @Test
    void shouldSupportRestrictionsByRegex2() {
        String key = "KEY";
        String match = "^hello.*";
        String userAttributeValue = "hello world";

        var restrictions = new CatalogWrapper.CatalogRestrictions();
        var restrictionAttribute = new CatalogWrapper.CatalogRestrictions.UserAttribute();
        restrictionAttribute.setKey(key);
        restrictionAttribute.setMatches(match);
        restrictions.setUserAttribute(restrictionAttribute);

        CatalogRestrictionService service = new CatalogRestrictionService();
        var catalog = catalogWithRestriction("0", restrictions);
        boolean result =
                service.isCatalogVisibleToUser(
                        getUserWithAttributes(Map.of(key, userAttributeValue)), catalog);

        assertTrue(result);
    }

    @Test
    void shouldNotMatchPartialRegex() {
        String key = "KEY";
        String match = "^hello";
        String userAttributeValue = "hello world";

        var restrictions = new CatalogWrapper.CatalogRestrictions();
        var restrictionAttribute = new CatalogWrapper.CatalogRestrictions.UserAttribute();
        restrictionAttribute.setKey(key);
        restrictionAttribute.setMatches(match);
        restrictions.setUserAttribute(restrictionAttribute);

        CatalogRestrictionService service = new CatalogRestrictionService();
        var catalog = catalogWithRestriction("0", restrictions);
        boolean result =
                service.isCatalogVisibleToUser(
                        getUserWithAttributes(Map.of(key, userAttributeValue)), catalog);

        assertFalse(result);
    }

    @Test
    void shouldSupportRestrictionsByList() {
        String key = "KEY";
        String match = "match-this";
        List<String> userAttributeValue = List.of("something", match);

        var restrictions = new CatalogWrapper.CatalogRestrictions();
        var restrictionAttribute = new CatalogWrapper.CatalogRestrictions.UserAttribute();
        restrictionAttribute.setKey(key);
        restrictionAttribute.setMatches(match);
        restrictions.setUserAttribute(restrictionAttribute);

        CatalogRestrictionService service = new CatalogRestrictionService();
        var catalog = catalogWithRestriction("0", restrictions);
        boolean result =
                service.isCatalogVisibleToUser(
                        getUserWithAttributes(Map.of(key, userAttributeValue)), catalog);

        assertTrue(result);
    }

    @Test
    void shouldSupportRestrictionsByBoolean() {
        String key = "KEY";
        String match = "true";
        Boolean userAttributeValue = true;

        var restrictions = new CatalogWrapper.CatalogRestrictions();
        var restrictionAttribute = new CatalogWrapper.CatalogRestrictions.UserAttribute();
        restrictionAttribute.setKey(key);
        restrictionAttribute.setMatches(match);
        restrictions.setUserAttribute(restrictionAttribute);

        CatalogRestrictionService service = new CatalogRestrictionService();
        var catalog = catalogWithRestriction("0", restrictions);
        boolean result =
                service.isCatalogVisibleToUser(
                        getUserWithAttributes(Map.of(key, userAttributeValue)), catalog);

        assertTrue(result);
    }

    @Test
    void shouldNotSupportRestrictionsByNumber() {
        String key = "KEY";
        String match = "match-this";
        int userAttributeValue = 123;

        var restrictions = new CatalogWrapper.CatalogRestrictions();
        var restrictionAttribute = new CatalogWrapper.CatalogRestrictions.UserAttribute();
        restrictionAttribute.setKey(key);
        restrictionAttribute.setMatches(match);
        restrictions.setUserAttribute(restrictionAttribute);

        CatalogRestrictionService service = new CatalogRestrictionService();
        var catalog = catalogWithRestriction("0", restrictions);
        boolean result =
                service.isCatalogVisibleToUser(
                        getUserWithAttributes(Map.of(key, userAttributeValue)), catalog);

        assertFalse(result);
    }

    private CatalogWrapper catalogWithRestriction(
            String id, CatalogWrapper.CatalogRestrictions... restrictions) {
        var catalogWrapper = new CatalogWrapper();
        catalogWrapper.setId(id);
        catalogWrapper.setRestrictions(List.of(restrictions));
        return catalogWrapper;
    }

    private User getUserWithAttributes(Map<String, Object> attributes) {
        User user = new User();
        user.setAttributes(attributes);
        return user;
    }
}
