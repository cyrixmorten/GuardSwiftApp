package com.guardswift.persistence.cache.data;

import android.content.Context;

import com.guardswift.dagger.InjectingApplication;
import com.guardswift.persistence.cache.ParseCache;
import com.guardswift.persistence.parse.data.client.Client;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Created by cyrix on 10/21/15.
 */
@Singleton
public class ClientCache extends ParseCache<Client> {

    private static final String SELECTED = "selected";

    @Inject
    ClientCache(@InjectingApplication.InjectingApplicationModule.ForApplication Context context) {
        super(Client.class, context);
    }

    public void setSelected(Client client) {
        put(SELECTED, client);
    }

    public Client getSelected() {
        return get(SELECTED);
    }

}
