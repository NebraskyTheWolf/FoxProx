package com.foxprox.network.proxy.networking.chat;

import com.foxprox.network.proxy.core.api.chat.TextComponent;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import java.lang.reflect.Type;

public class TextComponentSerializer extends BaseComponentSerializer implements JsonSerializer<TextComponent>, JsonDeserializer<TextComponent>
{

    @Override
    public TextComponent deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
    {
        TextComponent component = new TextComponent();
        JsonObject object = json.getAsJsonObject();
        if ( !object.has( "text" ) )
        {
            throw new JsonParseException( "Could not parse JSON: missing 'text' property" );
        }
        component.setText( object.get( "text" ).getAsString() );
        deserialize( object, component, context );
        return component;
    }

    @Override
    public JsonElement serialize(TextComponent src, Type typeOfSrc, JsonSerializationContext context)
    {
        JsonObject object = new JsonObject();
        serialize( object, src, context );
        object.addProperty( "text", src.getText() );
        return object;
    }
}
