package com.foxprox.network.util;

import com.foxprox.network.proxy.core.api.FoxServer;
import com.google.common.base.Preconditions;
import net.samagames.persistanceapi.beans.players.SanctionBean;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class SanctionHelper {
    public List<SanctionBean> getSanctionByUUID(UUID player, int type) throws Exception {
        Preconditions.checkNotNull(player, "Player UUID cannot be NULL.");
        Preconditions.checkState(type > 5, "Type cannot exceed (0, 5).");
        return FoxServer.getInstance()
                .getGameServiceManager()
                .getAllSanctions(player, type);
    }
    public SanctionBean createSanction(UUID player, UUID issuer, int type, String reason, long expiration) {
        Preconditions.checkNotNull(player, "Player UUID cannot be NULL.");
        Preconditions.checkNotNull(issuer, "Issuer UUID cannot be NULL.");
        Preconditions.checkState(type > 5, "Type cannot exceed (0, 5).");
        Preconditions.checkState(reason.length() > 199, "The reason cannot exceed 199 characters.");
        Preconditions.checkState(expiration > 180000, "Expiration cannot exceed 180000 seconds.");
        return new SanctionBean(
                player,
                type,
                reason,
                issuer,
                new Timestamp(this.epochCalculator(expiration)),
                true
        );
    }

    private long epochCalculator(long seconds) {
        return (System.currentTimeMillis() / 1000) * seconds;
    }
}
