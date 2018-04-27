package com.devbatch.ecommerce.utils;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.FieldNamingStrategy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.Map;

import retrofit.mime.MultipartTypedOutput;
import retrofit.mime.TypedByteArray;
import retrofit.mime.TypedOutput;
import retrofit.mime.TypedString;

public class GsonUtils {

    private static final Gson gson = new GsonBuilder()
            .setFieldNamingStrategy(new CustomFieldNamingStrategy()).setPrettyPrinting()
            .registerTypeAdapter(MultipartTypedOutput.class,
                    new MultipartTypedOutputDeserializer())
            .create();

    private static Gson prettyGson = new GsonBuilder().setPrettyPrinting().create();

    public static Gson getGson() {
        return gson;
    }

    public static String toJson(Object src) {
        return getGson().toJson(src);
    }

    //TODO temp method for printing ... remove it
    public static String toPrettyJson(String src) {
        try {
            JsonObject newObj = new JsonParser().parse(src).getAsJsonObject();
            return prettyGson.toJson(newObj);
        } catch (Exception e) {
            return src;
        }
    }

    public static <T> T fromJson(Reader json, Class<T> classOfT) {
        return getGson().fromJson(json, classOfT);
    }

    public static <T> T fromJson(String json, Class<T> classOfT) {
        return getGson().fromJson(json, classOfT);
    }

    public static <T> T fromJson(String json, Type typeOfT) {
        return getGson().fromJson(json, typeOfT);
    }

    public static MultipartTypedOutput jsonObjectToMultipartTypedOutput(Object obj) {
        return gson.fromJson(gson.toJson(obj), MultipartTypedOutput.class);
    }

    private static class MultipartTypedOutputDeserializer
            implements JsonDeserializer<MultipartTypedOutput> {
        @Override
        public MultipartTypedOutput deserialize(JsonElement json, Type typeOfT,
                                                JsonDeserializationContext context) throws JsonParseException {
            MultipartTypedOutput multipart = new MultipartTypedOutput();
            JsonObject object = (JsonObject) json;
            for (Map.Entry<String, JsonElement> entry : object.entrySet()) {
                TypedOutput output;
                JsonElement element = entry.getValue();
                if (element.isJsonPrimitive()) {
                    output = new TypedString(element.getAsString());
                } else if (element.isJsonNull()) {
                    output = new TypedString("");
                } else {
                    output = new TypedJson(element.toString());
                }
                multipart.addPart(entry.getKey(), output);
            }
            return multipart;
        }

        public static class TypedJson extends TypedByteArray {
            public TypedJson(String string) {
                super("application/json; charset=UTF-8", convertToBytes(string));
            }

            private static byte[] convertToBytes(String string) {
                try {
                    return string.getBytes("UTF-8");
                } catch (UnsupportedEncodingException e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public String toString() {
                try {
                    return "TypedString[" + new String(getBytes(), "UTF-8") + "]";
                } catch (UnsupportedEncodingException e) {
                    throw new AssertionError("Must be able to decode UTF-8");
                }
            }
        }
    }

    private static class CustomFieldNamingStrategy implements FieldNamingStrategy
    {
        @Override
        public String translateName(Field f) {
            return lowerCaseFirstLetter(f.getName());
        }
        private String lowerCaseFirstLetter(String name) {
            StringBuilder fieldNameBuilder = new StringBuilder();
            int index = 0;
            char firstCharacter = name.charAt(index);

            while (index < name.length() - 1) {
                if (Character.isLetter(firstCharacter)) {
                    break;
                }

                fieldNameBuilder.append(firstCharacter);
                firstCharacter = name.charAt(++index);
            }

            if (index == name.length()) {
                return fieldNameBuilder.toString();
            }

            if (!Character.isUpperCase(firstCharacter)) {
                String modifiedTarget = modifyString(Character.toUpperCase(firstCharacter), name, ++index);
                return fieldNameBuilder.append(modifiedTarget).toString();
            } else {
                return name;
            }
        }

        private String modifyString(char firstCharacter, String srcString, int indexOfSubstring) {
            return (indexOfSubstring < srcString.length())
                    ? firstCharacter + srcString.substring(indexOfSubstring)
                    : String.valueOf(firstCharacter);
        }
    }

    private static class ItemTypeAdapterFactory implements TypeAdapterFactory {

        public <T> TypeAdapter<T> create(Gson gson, final TypeToken<T> type) {

            final TypeAdapter<T> delegate = gson.getDelegateAdapter(this, type);
            final TypeAdapter<JsonElement> elementAdapter = gson.getAdapter(JsonElement.class);

            return new TypeAdapter<T>() {

                public void write(JsonWriter out, T value) throws IOException {
                    delegate.write(out, value);
                }

                public T read(JsonReader in) throws IOException {

                    JsonElement jsonElement = elementAdapter.read(in);
                    if (jsonElement.isJsonObject()) {
                        JsonObject jsonObject = jsonElement.getAsJsonObject();
                        if (jsonObject.has("data") && jsonObject.get("data").isJsonObject())
                        {
                            jsonElement = jsonObject.get("data");
                        }
                    }

                    return delegate.fromJsonTree(jsonElement);
                }
            }.nullSafe();
        }
    }

}