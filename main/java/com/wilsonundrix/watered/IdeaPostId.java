package com.wilsonundrix.watered;


import com.google.firebase.database.Exclude;

import android.support.annotation.NonNull;

public class IdeaPostId {

    @Exclude
    public String IdeaPostId;

    public <T extends IdeaPostId> T withId(@NonNull final String id) {
        this.IdeaPostId = id;
        return (T) this;
    }

}
