package com.foxprox.network.proxy.networking.connection;

import lombok.AllArgsConstructor;
import lombok.Data;
import com.foxprox.network.proxy.networking.protocol.Property;

@Data
@AllArgsConstructor
public class LoginResult
{

    private String id;
    private String name;
    private Property[] properties;
}
