package tv.ismar.app.core;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;
import java.util.LinkedHashMap;
import java.util.Map;

import tv.ismar.app.entity.Attribute;


public class AttributeDeserializer implements JsonDeserializer<Attribute> {

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public Attribute deserialize(JsonElement json, Type typeOfT,
			JsonDeserializationContext context) throws JsonParseException {
		if(json!=null){
			Attribute attribute = new Attribute();
			if(json.isJsonObject()){
				attribute.map = new LinkedHashMap();
				JsonObject jsonObj = json.getAsJsonObject();
				for(Map.Entry<String, JsonElement> entry : jsonObj.entrySet()){
					String key = entry.getKey();
					JsonElement element = entry.getValue();
					if(element.isJsonNull()){
//                        String str = element.getAsString();
//                        if(str!=null&&!"".equals(str))
//						   attribute.map.put(key, element.getAsString());
//                        else
                            attribute.map.put(key, null);
					}else if(element.isJsonPrimitive()){
                        attribute.map.put(key, element.getAsString());
                    }
                    else if(element.isJsonArray()){
						JsonArray innerArray = element.getAsJsonArray();
						if(innerArray.get(0).isJsonArray()){
							Attribute.Info[] infos = new Attribute.Info[innerArray.size()];
							for(int i=0; i<innerArray.size(); i++){
								JsonElement innerElement = innerArray.get(i);
								if(innerElement.isJsonArray()){
									String[] values = context.deserialize(innerElement, String[].class);
									infos[i] = new Attribute.Info();
									infos[i].id = Integer.parseInt(values[0]);
									infos[i].name = values[1];
								}
							}
							attribute.map.put(key, infos);
						} else {
							String[] values = context.deserialize(innerArray, String[].class);
							Attribute.Info info = new Attribute.Info();
							info.id = Integer.parseInt(values[0]);
							info.name = values[1];
							attribute.map.put(key, info);
						}
					}
				}
				return attribute;
			} else {
				return null;
			}
		} else {
			return null;
		}
	}

}
