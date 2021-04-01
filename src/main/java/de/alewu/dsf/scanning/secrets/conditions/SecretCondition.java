package de.alewu.dsf.scanning.secrets.conditions;

import com.google.gson.JsonObject;
import de.alewu.dsf.exceptions.DungeonScannerException;
import de.alewu.dsf.exceptions.DungeonSecretFinderException;
import de.alewu.dsf.scanning.DungeonRoom;
import de.alewu.dsf.scanning.secrets.SecretConditionType;
import java.util.function.BiFunction;
import org.apache.commons.lang3.EnumUtils;

public class SecretCondition {

    private final SecretConditionEvaluator evaluator;
    private final SecretConditionType type;
    private final JsonObject conditionData;

    public SecretCondition(DungeonRoom room, JsonObject conditionData) {
        this.type = EnumUtils.getEnum(SecretConditionType.class, (conditionData.has("t") ? conditionData.get("t").getAsString() : null));
        if (this.type == null) {
            throw new DungeonSecretFinderException("Type cannot be parsed");
        }
        this.conditionData = conditionData;
        BiFunction<DungeonRoom, JsonObject, SecretConditionEvaluator> builder = SecretConditionEvaluators.EVALUATORS.get(type);
        if (builder != null) {
            this.evaluator = builder.apply(room, conditionData);
        } else {
            throw new DungeonSecretFinderException("No condition evaluator found for type " + type);
        }
    }

    public SecretCondition(DungeonRoom room, SecretConditionType type, JsonObject conditionData) {
        this.type = type;
        this.conditionData = conditionData;
        BiFunction<DungeonRoom, JsonObject, SecretConditionEvaluator> builder = SecretConditionEvaluators.EVALUATORS.get(type);
        if (builder != null) {
            this.evaluator = builder.apply(room, conditionData);
        } else {
            throw new DungeonScannerException("No condition evaluator found for type " + type);
        }
    }

    public boolean evaluate() {
        return evaluator.isValid();
    }

    public SecretConditionType getType() {
        return type;
    }

    public JsonObject getConditionData() {
        return conditionData;
    }
}
