package fr.insee.onyxia.model.catalog;

import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import fr.insee.onyxia.model.catalog.UniversePackage.Config.Category;
import fr.insee.onyxia.model.catalog.UniversePackage.Config.Category.Property;
import fr.insee.onyxia.model.deprecated.HttpUtils;
import okhttp3.Request;
import okhttp3.Response;

@JsonIgnoreProperties(ignoreUnknown = true)
public class UniverseWrapper {

    private Universe universe;
    private String id;
    private String name;
    private String description;
    private String maintainer;
    private String url;
    private UniverseStatus status;

    private String scm;

    private long lastUpdateTime = 0;

    public UniverseWrapper() {

    }

    public UniverseWrapper(
        String id,
        String name,
        String description,
        String maintainer,
        String url,
        String scm,
        UniverseStatus status) {
        super();
        this.id = id;
        this.name = name;
        this.description = description;
        this.maintainer = maintainer;
        this.url = url;
        this.scm = scm;
        this.status = status;
    }

    public UniverseStatus getStatus() {
        return status;
    }

    public void setStatus(UniverseStatus status) {
        this.status = status;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getMaintainer() {
        return maintainer;
    }

    public void setMaintainer(String maintainer) {
        this.maintainer = maintainer;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getScm() {
        return scm;
    }

    public void setScm(String scm) {
        this.scm = scm;
    }

    public synchronized Universe getUniverse() {
        if (universe == null || System.currentTimeMillis() > lastUpdateTime + 5 * 1000) {
            updateUniverse();
        }
        return universe;
    }

    public void updateUniverse() {
        try {
            ObjectMapper jacksonObjectMapper = new ObjectMapper();
            jacksonObjectMapper.configure(com.fasterxml.jackson.core.JsonParser.Feature.AUTO_CLOSE_SOURCE, false);
            jacksonObjectMapper.enable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT);
            jacksonObjectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            Request request = new Request.Builder().url(url).build();
            Response response = HttpUtils.CLIENT.newCall(request).execute();
            ZipInputStream zis = new ZipInputStream(response.body().byteStream());
            // Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress("10.1.0.8", 8080));
            // ZipInputStream zis = new ZipInputStream(new
            // URL("https://universe.mesosphere.com/repo").openConnection(proxy).getInputStream());
            ZipEntry entry;

            Universe universe = null;
            while ((entry = zis.getNextEntry()) != null) {
                if ("universe/repo/meta/index.json".equals(entry.getName())) {
                    universe = jacksonObjectMapper.readValue(zis, Universe.class);
                }
                else if (entry.getName().endsWith("package.json")
                    || entry.getName().endsWith("resource.json") || entry.getName().endsWith("config.json")
                    || entry.getName().endsWith("marathon.json.mustache")) {
                    String pkgName = entry.getName().split("/")[entry.getName().split("/").length - 3];
                    UniversePackage pkg = universe.getPackageByName(pkgName);
                    if (pkg == null) {
                        System.out.println("Warning, package " + pkgName + " inconnu");
                        continue;
                    }

                    if (entry.getName().endsWith("resource.json")) {
                        TypeReference<HashMap<String, Object>> typeRef =
                            new TypeReference<HashMap<String, Object>>() {};
                        pkg.getResource().put("resource", jacksonObjectMapper.readValue(zis, typeRef));
                    }
                    else if (entry.getName().endsWith("marathon.json.mustache")) {
                        final StringBuilder sb = new StringBuilder();
                        final char[] buffer = new char[1024];
                        final InputStreamReader isr = new InputStreamReader(zis);
                        int nbRead;
                        while ((nbRead = isr.read(buffer, 0, buffer.length)) != - 1) {
                            sb.append(new String(buffer, 0, nbRead));
                        }
                        pkg.setJsonMustache(sb.toString());
                    }
                    else if (entry.getName().endsWith("config.json")) {
                        pkg = jacksonObjectMapper.readerForUpdating(pkg).readValue(zis);
                        Property friendlyName = new Property();
                        friendlyName.setType("string");
                        friendlyName.setDescription("Nom d'affichage du service sur Onyxia");
                        friendlyName.setDefaut(pkg.getName());
                        friendlyName.setTitle("Un titre plus sympathique?");
                        Category onyxia = new Category();
                        Map<String, Property> map = new HashMap<String, Property>();
                        map.put("friendly_name", friendlyName);
                        onyxia.setProperties(map);
                        onyxia.setType("object");
                        onyxia.setDescription("Configure l'ensemble des metas-donnees pour Onyxia");
                        pkg.getProperties().getCategories().put("onyxia", onyxia);
                    }
                    else {
                        pkg = jacksonObjectMapper.readerForUpdating(pkg).readValue(zis);
                    }
                }
            }
            zis.close();
            lastUpdateTime = System.currentTimeMillis();
            Map<String, Map<String, List>> map = new HashMap<String, Map<String, List>>();
            for (UniversePackage pkg : universe.getPackages()) {
                for (Map.Entry<String, Category> cat : pkg.getProperties().getCategories().entrySet()) {
                    String keyCat = cat.getKey();
                    for (Map.Entry<String, Property> prop : cat.getValue().getProperties().entrySet()) {
                        String keyProp = prop.getKey();
                        if (prop.getValue().getType().equals("object")) {
                            for (Map.Entry<String, Property> subprop : prop.getValue().getProperties().entrySet()) {
                                String keySubProp = subprop.getKey();
                                if (subprop.getValue().getMedia() != null) {
                                    String tag = subprop.getValue().getMedia().getType();
                                    Map<String, List> liste = map.getOrDefault(tag, new HashMap<String, List>());
                                    List<String> lst = liste.getOrDefault(pkg.getName(), new ArrayList<String>());
                                    lst.add(keyCat + "." + keyProp + "." + keySubProp);
                                    liste.put(pkg.getName(), lst);
                                    map.put(tag, liste);
                                }
                            }
                        }
                        else {
                            if (prop.getValue().getMedia() != null) {
                                String tag = prop.getValue().getMedia().getType();
                                Map<String, List> liste = map.getOrDefault(tag, new HashMap<String, List>());
                                List<String> lst = liste.getOrDefault(pkg.getName(), new ArrayList<String>());
                                lst.add(keyCat + "." + keyProp);
                                liste.put(pkg.getName(), lst);
                                map.put(tag, liste);
                            }
                        }
                    }
                }
            }
            universe.setTypeOfFile(map);
            this.universe = universe;
        }
        catch (Exception e) {
            e.printStackTrace();
            lastUpdateTime = 0;
        }
    }
}
