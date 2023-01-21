package com.foxprox.network.proxy.jni.cipher;

class NativeCipherImpl
{

    native long init(boolean forEncryption, byte[] key);

    native void free(long ctx);

    native void cipher(long ctx, long in, long out, int length);
}
