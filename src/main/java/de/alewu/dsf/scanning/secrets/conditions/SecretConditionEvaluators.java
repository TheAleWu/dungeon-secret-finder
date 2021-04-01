package de.alewu.dsf.scanning.secrets.conditions;

import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonObject;
import de.alewu.dsf.scanning.DungeonRoom;
import de.alewu.dsf.scanning.secrets.SecretConditionType;
import java.util.Map;
import java.util.function.BiFunction;

public class SecretConditionEvaluators {

    public static final Map<SecretConditionType, BiFunction<DungeonRoom, JsonObject, SecretConditionEvaluator>> EVALUATORS =
        ImmutableMap.<SecretConditionType, BiFunction<DungeonRoom, JsonObject, SecretConditionEvaluator>>builder()
            .put(SecretConditionType.BLOCK_EQUALS, BlockEqualsSecretConditionEvaluator::new)
            .build();

}
