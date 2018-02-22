package com.guardswift.core.parse;

import com.google.common.collect.Maps;
import com.guardswift.persistence.parse.documentation.report.Report;
import com.parse.ParseCloud;
import com.parse.ParseFile;

import java.util.HashMap;
import java.util.Map;

import bolts.Task;


public class CloudFunctions {

    private static final String SEND_REPORT = "sendReport";
    private static final String FILE_DELETE = "fileDelete";

    public static Task<Void> deleteFile(ParseFile file) {
        Map<String, String> params = Maps.newHashMap();
        params.put("fileUrl", file.getUrl());

        return ParseCloud.callFunctionInBackground(FILE_DELETE, params);
    }

    public static Task<Void> sendReport(Report report) {
        final HashMap<String, String> params = Maps.newHashMap();
        params.put("reportId", report.getObjectId());

        return ParseCloud.callFunctionInBackground(SEND_REPORT, params);
    }
}
