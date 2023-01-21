package com.foxprox.network.proxy.lib.conf;

import com.foxprox.network.proxy.conf.Configuration;
import com.foxprox.network.proxy.conf.YamlConfig;
import com.foxprox.network.proxy.networking.protocol.ProtocolConstants;

import java.io.File;

public class WaterfallConfiguration extends Configuration {

    /**
     * Whether we log InitialHandler connections
     * <p>
     * Default is true
     */
    private boolean logInitialHandlerConnections = true;

    /**
     * The supported versions displayed to the client
     * <p>Default is a comma separated list of supported versions. For example 1.8.x, 1.9.x, 1.10.x</p>
     */
    private String gameVersion;

    /**
     * Whether we use Netty's async DNS resolver for the HttpClient.
     * <p>Default is true (use Netty's async DNS resolver)</p>
     */
    private boolean useNettyDnsResolver = true;

    /*
     * Throttling options
     * Helps prevent players from overloading the servers behind us
     */

    /**
     * How often players are allowed to send tab throttle.
     * Value in milliseconds.
     * <p/>
     * Default is one packet per second.
     */
    private int tabThrottle = 1000;
    private boolean disableModernTabLimiter = true;

    private boolean disableTabListRewrite = true;

    /*
     * Plugin Message limiting options
     * Allows for more control over server-client communication
     */

    /**
     * How many channels there can be between server and player,
     * typically used by mods or some plugins.
     */
    private int pluginChannelLimit = 128;

    /**
     * How long the maximum channel name can be,
     * only reason to change it would be broken mods.
     */
    private int pluginChannelNameLimit = 128;

    public String sqlUrl  = "";
    public String sqlUser = "";
    public String sqlPass = "";

    public String redisHost  = "";
    public Integer redisPort = 6379;
    public String redisPass = "";

    public Boolean isProduction = false;

    @Override
    public void load() {
        super.load();
        YamlConfig config = new YamlConfig(new File("foxprox.yml"));

        config.load(false); // Load, but no permissions
        logInitialHandlerConnections = config.getBoolean( "log_initial_handler_connections", logInitialHandlerConnections );
        // FlameCord - Make the version look better
        gameVersion = config.getString("game_version", "").isEmpty() ? ProtocolConstants.SUPPORTED_VERSIONS.get(0) + "-" + ProtocolConstants.SUPPORTED_VERSIONS.get(ProtocolConstants.SUPPORTED_VERSIONS.size() - 1) : config.getString("game_version", "");
        useNettyDnsResolver = config.getBoolean("use_netty_dns_resolver", useNettyDnsResolver);
        // Throttling options
        tabThrottle = config.getInt("throttling.tab_complete", tabThrottle);
        disableModernTabLimiter = config.getBoolean("disable_modern_tab_limiter", disableModernTabLimiter);
        disableTabListRewrite = config.getBoolean("disable_tab_list_rewrite", disableTabListRewrite);
        pluginChannelLimit = config.getInt("registered_plugin_channels_limit", pluginChannelLimit);
        pluginChannelNameLimit = config.getInt("plugin_channel_name_limit", pluginChannelNameLimit);

        sqlUrl = config.getString("mysql_hostname", sqlUrl);
        sqlUser = config.getString("mysql_username", sqlUser);
        sqlPass = config.getString("mysql_password", sqlPass);

        redisHost = config.getString("redis_ip", redisHost);
        redisPort = config.getInt("redis_port", redisPort);
        redisPass = config.getString("redis_password", redisPass);

        isProduction = config.getBoolean("isMerlinRequired", isProduction);
    }

    @Override
    public boolean isLogInitialHandlerConnections() {
        return logInitialHandlerConnections;
    }

    @Override
    public String getGameVersion() {
        return gameVersion;
    }

    @Override
    public boolean isUseNettyDnsResolver() {
        return useNettyDnsResolver;
    }

    @Override
    public int getTabThrottle() {
        return tabThrottle;
    }

    @Override
    public boolean isDisableModernTabLimiter() {
        return disableModernTabLimiter;
    }

    @Override
    public boolean isDisableTabListRewrite() {
        return disableTabListRewrite;
    }

    @Override
    public int getPluginChannelLimit() {
        return pluginChannelLimit;
    }

    @Override
    public int getPluginChannelNameLimit() {
        return pluginChannelNameLimit;
    }

    public String getSqlPass() {
        return sqlPass;
    }

    public String getSqlUrl() {
        return sqlUrl;
    }

    public String getSqlUser() {
        return sqlUser;
    }

    @Override
    public String getRedisHost() {
        return redisHost;
    }

    @Override
    public Integer getRedisPort() {
        return redisPort;
    }

    @Override
    public String getRedisPass() {
        return redisPass;
    }

    public Boolean getProduction() {
        return isProduction;
    }
}
