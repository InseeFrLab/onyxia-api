// {
// "$schema": "http://json-schema.org/schema#",

package fr.insee.onyxia.model.helm;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * ValuesShemaJson
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class HelmConfig {
    private String type;
    private String schema;
    private Properties properties;

    /**
     * @return the properties
     */
    public Properties getProperties() {
        return properties;
    }

    /**
     * @param properties the properties to set
     */
    public void setProperties(Properties properties) {
        this.properties = properties;
    }

    /**
     * @return the schema
     */
    public String getSchema() {
        return schema;
    }

    /**
     * @param schema the schema to set
     */
    public void setSchema(String schema) {
        this.schema = schema;
    }

    /**
     * @return the type
     */
    public String getType() {
        return type;
    }

    /**
     * @param type the type to set
     */
    public void setType(String type) {
        this.type = type;
    }

    private class Properties {
        private String type;
        private Properties properties;
        private boolean form;
        private String title;
        private String description;

        /**
         * @return the type
         */
        public String getType() {
            return type;
        }

        /**
         * @param type the type to set
         */
        public void setType(String type) {
            this.type = type;
        }

        /**
         * @return the description
         */
        public String getDescription() {
            return description;
        }

        /**
         * @param description the description to set
         */
        public void setDescription(String description) {
            this.description = description;
        }

        /**
         * @return the properties
         */
        public Properties getProperties() {
            return properties;
        }

        /**
         * @param properties the properties to set
         */
        public void setProperties(Properties properties) {
            this.properties = properties;
        }

        /**
         * @return the title
         */
        public String getTitle() {
            return title;
        }

        /**
         * @param title the title to set
         */
        public void setTitle(String title) {
            this.title = title;
        }

        public boolean getForm() {
            return form;
        }

        /**
         * @param form the form to set
         */
        public void setForm(boolean form) {
            this.form = form;
        }

    }
}