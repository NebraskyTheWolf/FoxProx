package com.foxprox.network.proxy;

import com.foxprox.network.proxy.core.api.ServerPing;
import com.foxprox.network.proxy.networking.protocol.ProtocolConstants;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import java.lang.reflect.Type;
import java.util.UUID;

public class PlayerInfoSerializer implements JsonSerializer<ServerPing.PlayerInfo>, JsonDeserializer<ServerPing.PlayerInfo>
{
    // FlameCord start - 1.7.x support
    private final int protocol;

    public PlayerInfoSerializer()
    {
        this.protocol = ProtocolConstants.MINECRAFT_1_7_6;
    }

    public PlayerInfoSerializer(int protocol)
    {
        this.protocol = protocol;
    }
    // FlameCord end - 1.7.x support

    @Override
    public ServerPing.PlayerInfo deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
    {
        JsonObject js = json.getAsJsonObject();
        ServerPing.PlayerInfo info = new ServerPing.PlayerInfo( js.get( "name" ).getAsString(), (UUID) null );
        String id = js.get( "id" ).getAsString();
        if ( ProtocolConstants.isBeforeOrEq( protocol, ProtocolConstants.MINECRAFT_1_7_2 ) || !id.contains( "-" ) ) // FlameCord - 1.7.x support
        {
            info.setId( id );
        } else
        {
            info.setUniqueId( UUID.fromString( id ) );
        }
        return info;
    }

    @Override
    public JsonElement serialize(ServerPing.PlayerInfo src, Type typeOfSrc, JsonSerializationContext context)
    {
        JsonObject out = new JsonObject();
        out.addProperty( "name", src.getName() );
        // FlameCord start - 1.7.x support
        if ( ProtocolConstants.isBeforeOrEq( protocol, ProtocolConstants.MINECRAFT_1_7_2 ) )
        {
            out.addProperty( "id", src.getId() );
        } else
        {
            out.addProperty( "id", src.getUniqueId().toString() );
        }
        // FlameCord end - 1.7.x support
        return out;
    }
}
