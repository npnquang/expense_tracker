package james.expense_tracker.utils;

import java.util.function.Consumer;

public class Utils {
    public static <T> void updateField(T newValue, Consumer<T> setter) {
        if (newValue == null) {
                return;
        }
        setter.accept(newValue);
    }
}
