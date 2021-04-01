package de.alewu.dsf.scanning.secrets.conditions;

import com.google.gson.JsonObject;
import de.alewu.dsf.exceptions.DungeonScannerException;
import de.alewu.dsf.scanning.DungeonLayout;
import de.alewu.dsf.scanning.DungeonRoom;
import de.alewu.dsf.util.RuntimeContext;
import java.util.Objects;
import net.minecraft.world.World;

public abstract class SecretConditionEvaluator {

    final DungeonRoom room;
    final JsonObject conditionData;

    public SecretConditionEvaluator(DungeonRoom room, JsonObject conditionData) {
        this.room = Objects.requireNonNull(room);
        this.conditionData = Objects.requireNonNull(conditionData);
    }

    public abstract boolean isValid();

    final World world() {
        DungeonLayout layout = RuntimeContext.getInstance().getCurrentDungeonLayout();
        if (layout == null) {
            throw new DungeonScannerException("No dungeon data available");
        }
        return layout.getApplicableWorld();
    }

}
