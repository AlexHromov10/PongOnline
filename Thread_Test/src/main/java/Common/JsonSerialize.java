package Common;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class JsonSerialize {
    GsonBuilder builder = new GsonBuilder();
    Gson gson = builder.create();

    public String toJson(Object obj){
        return gson.toJson(obj);
    }

    public <T> T fromJson(String jsonStr, Class<T> tClass){
        return gson.fromJson(jsonStr, tClass);
    }
}
