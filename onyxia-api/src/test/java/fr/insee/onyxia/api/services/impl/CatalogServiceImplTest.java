package fr.insee.onyxia.api.services.impl;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import fr.insee.onyxia.api.configuration.CatalogWrapper;
import fr.insee.onyxia.api.configuration.Catalogs;
import fr.insee.onyxia.model.User;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class CatalogServiceImplTest {

    @Test
    void shouldReturnAllCatalogsWhenNoRestrictions() {
        CatalogServiceImpl impl =
                new CatalogServiceImpl(new Catalogs(List.of(catalogWithRestriction("1"))));
        List<CatalogWrapper> catalogs =
                impl.getCatalogs(null, getUserWithAttributes(Map.of())).getCatalogs();
        assertThat(catalogs.size(), is(1));
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

        CatalogServiceImpl service =
                new CatalogServiceImpl(
                        new Catalogs(
                                List.of(
                                        catalogWithRestriction("0", restrictions),
                                        catalogWithRestriction("1"))));
        List<CatalogWrapper> catalogs =
                service.getCatalogs(null, getUserWithAttributes(Map.of(key, userAttributeValue)))
                        .getCatalogs();

        assertThat(catalogs.size(), is(1));
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

        CatalogServiceImpl service =
                new CatalogServiceImpl(
                        new Catalogs(
                                List.of(
                                        catalogWithRestriction("0", restrictions),
                                        catalogWithRestriction("1"))));
        List<CatalogWrapper> catalogs =
                service.getCatalogs(null, getUserWithAttributes(Map.of(key, userAttributeValue)))
                        .getCatalogs();

        assertThat(catalogs.size(), is(2));
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

        CatalogServiceImpl service =
                new CatalogServiceImpl(
                        new Catalogs(
                                List.of(
                                        catalogWithRestriction("0", restrictions),
                                        catalogWithRestriction("1"))));
        List<CatalogWrapper> catalogs =
                service.getCatalogs(null, getUserWithAttributes(Map.of(key, userAttributeValue)))
                        .getCatalogs();

        assertThat(catalogs.size(), is(2));
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

        CatalogServiceImpl service =
                new CatalogServiceImpl(
                        new Catalogs(
                                List.of(
                                        catalogWithRestriction("0", restrictions),
                                        catalogWithRestriction("1"))));
        List<CatalogWrapper> catalogs =
                service.getCatalogs(null, getUserWithAttributes(Map.of(key, userAttributeValue)))
                        .getCatalogs();

        assertThat(catalogs.size(), is(2));
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

        CatalogServiceImpl service =
                new CatalogServiceImpl(
                        new Catalogs(
                                List.of(
                                        catalogWithRestriction("0", restrictions),
                                        catalogWithRestriction("1"))));
        List<CatalogWrapper> catalogs =
                service.getCatalogs(null, getUserWithAttributes(Map.of(key, userAttributeValue)))
                        .getCatalogs();

        assertThat(catalogs.size(), is(2));
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

        CatalogServiceImpl service =
                new CatalogServiceImpl(
                        new Catalogs(
                                List.of(
                                        catalogWithRestriction("0", restrictions),
                                        catalogWithRestriction("1"))));
        List<CatalogWrapper> catalogs =
                service.getCatalogs(null, getUserWithAttributes(Map.of(key, userAttributeValue)))
                        .getCatalogs();

        assertThat(catalogs.size(), is(2));
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

        CatalogServiceImpl service =
                new CatalogServiceImpl(
                        new Catalogs(
                                List.of(
                                        catalogWithRestriction("0", restrictions),
                                        catalogWithRestriction("1"))));
        List<CatalogWrapper> catalogs =
                service.getCatalogs(null, getUserWithAttributes(Map.of(key, userAttributeValue)))
                        .getCatalogs();

        assertThat(catalogs.size(), is(1));
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
