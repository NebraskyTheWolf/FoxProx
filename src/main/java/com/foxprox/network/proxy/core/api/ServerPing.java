package com.foxprox.network.proxy.core.api;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.foxprox.network.proxy.core.api.chat.BaseComponent;
import com.foxprox.network.proxy.core.api.chat.TextComponent;
import com.foxprox.network.proxy.lib.utils.UUIDUtils;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import com.foxprox.network.proxy.Util;

/**
 * Represents the standard list data returned by opening a server in the
 * Minecraft client server list, or hitting it with a packet 0xFE.
 */
@Data
@ToString(exclude = "favicon")
@NoArgsConstructor
@AllArgsConstructor
public class ServerPing
{

    private Protocol version;

    @Data
    @AllArgsConstructor
    public static class Protocol
    {

        private String name;
        private int protocol;
    }
    private Players players;

    @Data
    @AllArgsConstructor
    public static class Players
    {

        private int max;
        private int online;
        private PlayerInfo[] sample;
    }

    @Data
    @AllArgsConstructor
    public static class PlayerInfo
    {

        private String name;
        private UUID uniqueId;

        private static final UUID md5UUID = Util.getUUID( "af74a02d19cb445bb07f6866a861f783" );

        public PlayerInfo(String name, String id)
        {
            setName( name );
            setId( id );
        }

        public void setId(String id)
        {
            try
            {
                uniqueId = Util.getUUID( id );
            } catch ( Exception e )
            {
                // Fallback on a valid uuid otherwise Minecraft complains
                uniqueId = md5UUID;
            }
        }

        public String getId()
        {
            return UUIDUtils.undash( uniqueId.toString() ); // Waterfall
        }
    }

    private BaseComponent description;
    private Favicon favicon;

    @Data
    public static class ModInfo
    {

        private String type = "FML";
        public List<ModItem> modList = new ArrayList<>();
    }

    @Data
    @AllArgsConstructor
    public static class ModItem
    {

        private String modid;
        private String version;
    }

    // Right now, we don't get the mods from the user, so we just use a stock ModInfo object to
    // create the server ping. Vanilla clients will ignore this.
    public final ModInfo modinfo = new ModInfo();

    @Deprecated
    public ServerPing(Protocol version, Players players, String description, String favicon)
    {
        this( version, players, new TextComponent( TextComponent.fromLegacyText( description ) ), favicon == null ? null : Favicon.create( favicon ) );
    }

    public ServerPing(Protocol version, Players players, String description, Favicon favicon)
    {
        this( version, players, new TextComponent( TextComponent.fromLegacyText( description ) ), favicon );
    }

    @Deprecated
    public String getFavicon()
    {
        return getFaviconObject() == null ? null : getFaviconObject().getEncoded();
    }

    public Favicon getFaviconObject()
    {
        return this.favicon;
    }

    @Deprecated
    public void setFavicon(String favicon)
    {
        setFavicon( favicon == null ? null : Favicon.create( favicon ) );
    }

    public void setFavicon(Favicon favicon)
    {
        this.favicon = favicon;
    }
    public void setDescription(String description)
    {
        this.description = new TextComponent( TextComponent.fromLegacyText( description ) );
    }

    public String getDescription()
    {
        return BaseComponent.toLegacyText( description );
    }

    public void setDescriptionComponent(BaseComponent description)
    {
        this.description = description;
    }

    public BaseComponent getDescriptionComponent()
    {
        return description;
    }
}
