package de.alewu.dsf.scanning.secrets.conditions;

import com.google.gson.JsonObject;
import de.alewu.dsf.scanning.DungeonRoom;
import de.alewu.dsf.scanning.shared.RelativeLocation;
import net.minecraft.block.Block;

public class BlockEqualsSecretConditionEvaluator extends SecretConditionEvaluator {

    public BlockEqualsSecretConditionEvaluator(DungeonRoom room, JsonObject conditionData) {
        super(room, conditionData);
    }

    @Override
    public boolean isValid() {
        if (!conditionData.has("r") || !conditionData.has("v")) {
            return false;
        }
        RelativeLocation loc = new RelativeLocation(conditionData.get("r").getAsString()).rotate(room.getRotation());
        String expected = conditionData.get("v").getAsString();
        Block actualBlock = world().getBlockState(room.getCenterBlockPos().add(loc.getX(), loc.getY(), loc.getZ())).getBlock();
        return expected.equals(actualBlock.getRegistryName());
    }
}
