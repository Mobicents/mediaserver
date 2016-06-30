package org.mobicents.media.server.bootstrap.ioc.provider;

import com.google.inject.Inject;
import com.google.inject.Provider;
import org.mobicents.media.core.configuration.MediaServerConfiguration;

/**
 * Created by achikin on 6/3/16.
 */
public class CachedRemoteStreamProvider implements Provider<org.mobicents.media.server.impl.resource.mediaplayer.audio.CachedRemoteStreamProvider> {

    private static org.mobicents.media.server.impl.resource.mediaplayer.audio.CachedRemoteStreamProvider instance;

    @Inject
    public CachedRemoteStreamProvider(MediaServerConfiguration config) {
        instance = new org.mobicents.media.server.impl.resource.mediaplayer.audio.CachedRemoteStreamProvider(config.getResourcesConfiguration().getPlayerCacheSize());
    }

    @Override
    public org.mobicents.media.server.impl.resource.mediaplayer.audio.CachedRemoteStreamProvider get() {
        return instance;
    }
}
