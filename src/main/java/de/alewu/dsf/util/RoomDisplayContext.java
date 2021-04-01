package de.alewu.dsf.util;

import de.alewu.dsf.scanning.DungeonLayout;
import de.alewu.dsf.scanning.DungeonRoom;
import de.alewu.dsf.scanning.secrets.RoomSecret;
import java.util.List;

public class RoomDisplayContext {

    private static RoomDisplayContext instance;
    private DungeonRoom room;
    private int displayedSecretIndex;
    private DisplayOption displayOption;

    public RoomDisplayContext(DungeonRoom room) {
        this.room = room;
        this.displayedSecretIndex = -1;
        this.displayOption = DisplayOption.DISPLAY_ALL;
    }

    public static RoomDisplayContext getInstance() {
        DungeonLayout layout = RuntimeContext.getInstance().getCurrentDungeonLayout();
        if (layout != null) {
            DungeonRoom current = layout.getCurrentRoom();
            if (instance == null) {
                instance = new RoomDisplayContext(current);
            } else {
                if (instance.hasRoomChanged(current)) {
                    instance.setRoom(current);
                    instance.resetOnChange();
                }
            }
        }
        return instance;
    }

    public void setDisplayOption(DisplayOption displayOption) {
        this.displayOption = displayOption;
    }

    public void nextDisplayOption() {
        switch (displayOption) {
            case DISPLAY_ALL:
                this.displayOption = DisplayOption.DISPLAY_SINGLE;
                break;
            case DISPLAY_SINGLE:
                this.displayOption = DisplayOption.DISPLAY_NONE;
                break;
            case DISPLAY_NONE:
                this.displayOption = DisplayOption.DISPLAY_MARKERS;
                break;
            case DISPLAY_MARKERS:
                this.displayOption = DisplayOption.DISPLAY_ALL;
                break;
        }
        resetOnChange();
    }

    public void previousDisplayOption() {
        switch (displayOption) {
            case DISPLAY_MARKERS:
                this.displayOption = DisplayOption.DISPLAY_NONE;
                break;
            case DISPLAY_ALL:
                this.displayOption = DisplayOption.DISPLAY_MARKERS;
                break;
            case DISPLAY_SINGLE:
                this.displayOption = DisplayOption.DISPLAY_ALL;
                break;
            case DISPLAY_NONE:
                this.displayOption = DisplayOption.DISPLAY_SINGLE;
                break;
        }
        resetOnChange();
    }

    public void nextSecretIndex() {
        if (room.getRootSecrets().isEmpty()) {
            displayedSecretIndex = -1;
            return;
        }
        displayedSecretIndex++;
        if (displayedSecretIndex >= room.getRootSecrets().size()) {
            displayedSecretIndex = 0;
        }
    }

    public void previousSecretIndex() {
        if (room.getRootSecrets().isEmpty()) {
            displayedSecretIndex = -1;
            return;
        }
        displayedSecretIndex--;
        if (displayedSecretIndex < 0) {
            displayedSecretIndex = room.getRootSecrets().size() - 1;
        }
    }

    public DisplayOption getDisplayOption() {
        return displayOption;
    }

    public int getDisplayedSecretIndex() {
        if (!room.getRootSecrets().isEmpty() && displayedSecretIndex == -1) {
            displayedSecretIndex = 0;
        }
        return displayedSecretIndex;
    }

    public void setRoom(DungeonRoom room) {
        this.room = room;
        resetOnChange();
    }

    public void resetOnChange() {
        if (displayOption == DisplayOption.DISPLAY_SINGLE) {
            List<RoomSecret> rootSecrets = room.getRootSecrets();
            if (rootSecrets.isEmpty()) {
                displayedSecretIndex = -1;
            } else {
                displayedSecretIndex = 0;
            }
        }
    }

    public void purge() {
        instance = null;
    }

    public boolean hasRoomChanged(DungeonRoom room) {
        if (this.room == null || room == null) {
            return false;
        }
        return !this.room.getCenterBlockPos().equals(room.getCenterBlockPos());
    }

    public enum DisplayOption {

        DISPLAY_ALL,
        DISPLAY_SINGLE,
        DISPLAY_NONE,
        DISPLAY_MARKERS;

    }
}
