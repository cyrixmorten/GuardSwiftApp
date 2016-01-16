package com.guardswift.persistence.parse;

import com.parse.ParseGeoPoint;

/**
 * Created by cyrix on 11/21/15.
 */
public interface Positioned {
    ParseGeoPoint getPosition();
}
