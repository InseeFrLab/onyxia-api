package fr.insee.onyxia.model.catalog;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import fr.insee.onyxia.model.User;

public class UniversePackage {

	String description;
	boolean framework, selected, disable;
	String currentVersion;
	String name;
	String category;
	String scm;
	String maintainer;
	String website;
	String preInstallNotes;
	String postInstallNotes;
	String status;
	List<String> tags;
	Config properties;
	Map<String,Object> resource = new HashMap<>();
	String jsonMustache;
	
	@JsonIgnore
	public String getJsonMustache() {
		return jsonMustache;
	}
	public void setJsonMustache(String jsonMustache) {
		this.jsonMustache = jsonMustache;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getScm() {
		return scm;
	}
	public void setScm(String scm) {
		this.scm = scm;
	}
	public String getCategory() {
		return category;
	}
	public void setCategory(String category) {
		this.category = category;
	}
	public String getMaintainer() {
		return maintainer;
	}
	public void setMaintainer(String maintainer) {
		this.maintainer = maintainer;
	}
	public String getWebsite() {
		return website;
	}
	public void setWebsite(String website) {
		this.website = website;
	}
	public String getPreInstallNotes() {
		return preInstallNotes;
	}
	public void setPreInstallNotes(String preInstallNotes) {
		this.preInstallNotes = preInstallNotes;
	}
	public String getPostInstallNotes() {
		return postInstallNotes;
	}
	public void setPostInstallNotes(String postInstallNotes) {
		this.postInstallNotes = postInstallNotes;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public boolean isFramework() {
		return framework;
	}
	public void setFramework(boolean framework) {
		this.framework = framework;
	}
	public boolean isSelected() {
		return selected;
	}
	public void setSelected(boolean selected) {
		this.selected = selected;
	}
	public boolean isDisable() {
		return disable;
	}
	public void setDisable(boolean disable) {
		this.disable = disable;
	}
	public String getCurrentVersion() {
		return currentVersion;
	}
	public void setCurrentVersion(String currentVersion) {
		this.currentVersion = currentVersion;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public List<String> getTags() {
		return tags;
	}
	public void setTags(List<String> tags) {
		this.tags = tags;
	}
	public Map<String, Object> getResource() {
		return resource;
	}
	public void setResource(Map<String, Object> resource) {
		this.resource = resource;
	}
	public Config getProperties() {
		return properties;
	}
	public void setProperties(Config properties) {
		this.properties = properties;
	}
	
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class Config {
		
		private Map<String, Category> categories = new HashMap<>();
        
        
        @JsonAnyGetter
        public Map<String, Category> getCategories() {
			return categories;
		}

        @JsonAnySetter
        public void setUnrecognizedFields(String key, Category value) {
            this.categories.put(key, value);
        }

        public boolean enforceUser(Map<String,String> userValues,Object object) {
            Map<String,Object> map;
            if (! (object instanceof Map)){
                return false;
            }
            else {
                map = (Map<String,Object>) object;
            }
            for (Map.Entry<String, Category> entry : categories.entrySet()){
                Object objectCategory = map.get(entry.getKey());
                if (objectCategory ==null){
                    objectCategory = new HashMap<String,Object>();
                    map.put(entry.getKey(),objectCategory);
                }
                
                entry.getValue().enforceUser(userValues,objectCategory);
                
                
                
	        }
            return true;
    	}
		@JsonIgnoreProperties(ignoreUnknown = true)
        public static class Category {
        	
        	Map<String,Property> properties = new HashMap<>();
        	String description;
        	String type;
        	String[] required;

			public String getDescription() {
				return description;
			}
			public void setDescription(String description) {
				this.description = description;
			}
			public String getType() {
				return type;
			}
			public void setType(String type) {
				this.type = type;
			}
			@JsonAnyGetter
        	public Map<String, Property> getProperties() {
//				Map<String,Property> map = new HashMap<>();
//				for (Entry<String,Property> entry : properties.entrySet()) {
//					//if (entry.getValue().getApiDefined() == null) {
//						map.put(entry.getKey(), entry.getValue());
//					}
//				}
				return properties;
			}
			
//			@Transient
//			public Map<String,Property> getAllProperties() {
//				return properties;
//			}
        	public String[] getRequired() {
				return required;
			}
			public void setRequired(String[] required) {
				this.required = required;
			}
			@JsonAnySetter
        	public void setUnrecognizedFields(String key, Property value) {
        		this.properties.put(key, value);
        	}
    		public void setProperties(Map<String, Property> properties) {
				this.properties = properties;
			}
            public boolean enforceUser(Map<String,String> userValues,Object object) {
                Map<String,Object> map;
                if (! (object instanceof Map)){
                    return false;
                }
                else {
                    map = (Map<String,Object>) object;
                }
                for (Map.Entry<String, Property> entry : properties.entrySet()){
                    entry.getValue().enforceUser(userValues,map,entry.getKey());
    	        }
    	        return true;
        	}


			@JsonIgnoreProperties(ignoreUnknown = true)
        	public static class Property {
        		String type;
        		String description;
        		String title;
        		@JsonProperty("default")
        		Object defaut;
        		Media media;
        		String minimum;
        		@JsonProperty("enum")
        		Object enumeration;
        		Map<String,Property> properties;
        		
        		@JsonProperty("api-defined")
            	Boolean apiDefined = null;
            	
            	@JsonProperty("api-default")
            	String apiDefault;
            	
            	@JsonProperty("js-control")
            	String jsControl;
            	
            	@JsonProperty("api-control")
            	String apiControl;
            	
				public Boolean getApiDefined() {
					return apiDefined;
				}
				public void setApiDefined(Boolean apiDefined) {
					this.apiDefined = apiDefined;
				}
				public String getJsControl() {
					return jsControl;
				}
				public void setJsControl(String jsControl) {
					this.jsControl = jsControl;
				}
				public String getApiDefault() {
					return apiDefault;
				}
				public void setApiDefault(String apiDefault) {
					this.apiDefault = apiDefault;
				}
				public String getApiControl() {
					return apiControl;
				}
				public void setApiControl(String apiControl) {
					this.apiControl = apiControl;
				}
				public Map<String, Property> getProperties() {
					return properties;
				}
				
				public void setProperties(Map<String, Property> properties) {
					this.properties = properties;
				}
				public String getType() {
					return type;
				}
				public void setType(String type) {
					this.type = type;
				}
				public String getTitle() {
					return title;
				}
				public void setTitle(String title) {
					this.title = title;
				}
				public String getDescription() {
					return description;
				}
				public void setDescription(String description) {
					this.description = description;
				}
				public Media getMedia() {
					return media;
				}
				public void setMedia(Media media) {
					this.media = media;
				}
				public Object getDefaut() {
					return defaut;
				}
				public void setDefaut(Object defaut) {
					this.defaut = defaut;
				}
				public Object getEnumeration() {
					return enumeration;
				}
				public void setEnumeration(Object enumeration) {
					this.enumeration = enumeration;
				}
				public String getMinimum() {
					return minimum;
				}
				public void setMinimum(String minimum) {
					this.minimum = minimum;
				}
				
				public boolean enforceUser(Map<String,String> userValues,Map<String,Object> map, String key) {
                    switch (type){
                        case "string" :
                            if (apiDefined!= null && apiControl !=null && apiDefined){
                                switch (apiControl){
                                    case "strict":
                                        String force = apiDefault;
                                        for (Map.Entry<String, String> entry : userValues.entrySet()){
                                            if (entry.getValue()!=null){
                                                force = force.replaceAll(entry.getKey(),entry.getValue());
                                            }
                                        }
                                        map.put(key,force);
                                        break;
                                    
                                }
                            }
                            break;
                        case "boolean":
                            break;
                        case "number":
                            break;
                        case "object":
							if (! (map.get(key) instanceof Map)){
							    return false;
							}
							for (Map.Entry<String, Property> entry : properties.entrySet()){
								entry.getValue().enforceUser(userValues,(Map<String,Object>) map.get(key),entry.getKey());
							}

							
                            break;
                        default: 
                            return false;
                        
                    }
                    return true;
            	}
//        		public void initUser(User user) {
//       		    if (properties!=null){
//        		        for (Property property : properties.values()){
//		                    property.initUser(user);
//	    	            }
//        		    }
//        		    else{
//        		        defaut = apiDefault;
 //       		    }
		            
 //       	    }

				@JsonIgnoreProperties(ignoreUnknown = true)
	        	public static class Media {
					String type;
					public String getType() {
						return type;
					}
					public void setType(String type) {
						this.type = type;
					}
				}
        		
        	}
        }
	}

	public static class Images {
		@JsonProperty("icon-large")
		String iconLarge;
		@JsonProperty("icon-small")
		String iconSmall;
		@JsonProperty("icon-medium")
		String iconMedium;
		
		
	}

	
}
