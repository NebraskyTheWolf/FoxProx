package com.foxprox.network.proxy.networking.compress;

import com.foxprox.network.proxy.jni.zlib.BungeeZlib;
import com.foxprox.network.proxy.jni.zlib.JavaZlib;
import com.foxprox.network.proxy.jni.zlib.NativeZlib;
import com.foxprox.network.proxy.jni.NativeCode;

public class CompressFactory
{

    public static final NativeCode<BungeeZlib> zlib = new NativeCode<>( "native-compress", JavaZlib::new, NativeZlib::new );
}
