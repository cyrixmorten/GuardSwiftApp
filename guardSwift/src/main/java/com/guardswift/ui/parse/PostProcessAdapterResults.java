package com.guardswift.ui.parse;

import com.parse.ParseObject;

import java.util.List;

/**
 * Created by cyrixmorten on 22/10/2016.
 */
public interface PostProcessAdapterResults<T extends ParseObject> {
    List<T> postProcess(List<T> queriedItems);
}
