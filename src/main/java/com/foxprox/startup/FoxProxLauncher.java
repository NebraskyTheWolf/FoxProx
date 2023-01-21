package com.foxprox.startup;

import java.security.Security;

import com.foxprox.network.FoxProx;
import com.foxprox.network.proxy.core.api.FoxServer;
import com.foxprox.network.proxy.lib.console.WaterfallConsole;
import joptsimple.OptionParser;
import joptsimple.OptionSet;

public class FoxProxLauncher
{

    public static void main(String[] args) throws Exception
    {
        Security.setProperty( "networkaddress.cache.ttl", "30" );
        Security.setProperty( "networkaddress.cache.negative.ttl", "10" );
        // For JDK9+ we force-enable multi-release jar file support #3087
        if ( System.getProperty( "jdk.util.jar.enableMultiRelease" ) == null )
        {
            System.setProperty( "jdk.util.jar.enableMultiRelease", "force" );
        }

        OptionParser parser = new OptionParser();
        parser.allowsUnrecognizedOptions();

        OptionSet options = parser.parse( args );

        FoxProx bungee = new FoxProx();
        FoxServer.setInstance( bungee );
        // FlameCord - Use bungee name
        bungee.getLogger().info( "Loading " + bungee.getName() + " version " + bungee.getVersion() );
        bungee.start();

        if ( !options.has( "noconsole" ) )
        {
            new WaterfallConsole().start();
        }
    }
}
