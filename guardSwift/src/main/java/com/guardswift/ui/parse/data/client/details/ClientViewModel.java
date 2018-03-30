package com.guardswift.ui.parse.data.client.details;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;

import com.guardswift.core.exceptions.HandleException;
import com.guardswift.persistence.parse.data.client.Client;
import com.guardswift.persistence.parse.query.ClientQueryBuilder;
import com.parse.GetCallback;
import com.parse.ParseException;

public class ClientViewModel extends ViewModel {

    private MutableLiveData<Client> client;
    private String objectId;

    public LiveData<Client> getClient(String objectId) {
        if (client == null || !this.objectId.equals(objectId)) {
            this.objectId = objectId;

            client = new MutableLiveData<>();

            new ClientQueryBuilder(false).matchingObjectId(objectId).build().getFirstInBackground(new GetCallback<Client>() {
                @Override
                public void done(Client object, ParseException e) {
                    if (e != null) {
                        new HandleException("ClientViewModel", "Failed to load", e);
                        return;
                    }

                    client.setValue(object);
                }
            });
        }
        return client;
    }

}
