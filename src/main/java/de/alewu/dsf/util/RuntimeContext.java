package de.alewu.dsf.util;

import de.alewu.dsf.scanning.DungeonLayout;
import de.alewu.dsf.web.RemoteDataUpdateResult;

public class RuntimeContext {

    private static RuntimeContext instance;
    private DungeonLayout currentDungeonLayout;
    private boolean debugEnabled;
    private DebugMarkerData markerData;
    private boolean debugDisplayingMarkers;
    private RemoteDataUpdateResult remoteDataUpdateResult;

    private RuntimeContext() {
        this.debugEnabled = true;
        this.debugDisplayingMarkers = false;
    }

    public static RuntimeContext getInstance() {
        if (instance == null) {
            instance = new RuntimeContext();
        }
        return instance;
    }

    public boolean isDebugDisabled() {
        return !debugEnabled;
    }

    public void setDebugEnabled(boolean debugEnabled) {
        this.debugEnabled = debugEnabled;
    }

    public DungeonLayout getCurrentDungeonLayout() {
        return currentDungeonLayout;
    }

    public void setCurrentDungeonLayout(DungeonLayout currentDungeonLayout) {
        this.currentDungeonLayout = currentDungeonLayout;
    }

    public void setMarkerData(DebugMarkerData markerData) {
        this.markerData = markerData;
    }

    public DebugMarkerData getMarkerData() {
        return markerData;
    }

    public void setDebugDisplayingMarkers(boolean debugDisplayingMarkers) {
        this.debugDisplayingMarkers = debugDisplayingMarkers;
    }

    public boolean isDebugDisplayingMarkers() {
        return debugDisplayingMarkers;
    }

    public RemoteDataUpdateResult getRemoteDataUpdateResult() {
        return remoteDataUpdateResult;
    }

    public void setRemoteDataUpdateResult(RemoteDataUpdateResult remoteDataUpdateResult) {
        this.remoteDataUpdateResult = remoteDataUpdateResult;
    }
}
