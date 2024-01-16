package info.skyblond.vazan.domain

enum class SettingsKey(
    val key: String,
    val singleLine: Boolean,
    val validator: (String) -> Boolean
) {
    APP_LAST_PRINTER_ADDRESS("app.thermal_printer.last_address", true, { it.isNotBlank() }),
    APP_LAST_PRINTER_PAPER("app.thermal_printer.last_paper", true, { it.toIntOrNull() != null }),
    APP_LAST_PRINTER_REPEAT("app.thermal_printer.last_repeat", true, { it.toIntOrNull() != null }),

    MEMENTO_API_KEY("memento.api_key", true, { it.isNotBlank() }),

    // location
    MEMENTO_LOCATION_LIBRARY_ID("memento.location.library_id", true, { it.isNotBlank() }),
    MEMENTO_LOCATION_FIELD_ID("memento.location.field_id", true, { it.toIntOrNull() != null }),

    // box
    MEMENTO_BOX_LIBRARY_ID("memento.box.library_id", true, { it.isNotBlank() }),
    MEMENTO_BOX_PARENT_LOCATION_FIELD_ID(
        "memento.box.parent_location.field_id", true, { it.toIntOrNull() != null }),
    MEMENTO_BOX_PARENT_BOX_FIELD_ID(
        "memento.box.parent_box.field_id", true, { it.toIntOrNull() != null }),

    // item
    MEMENTO_ITEM_LIBRARY_ID("memento.item.library_id", true, { it.isNotBlank() }),
    MEMENTO_ITEM_PARENT_LOCATION_FIELD_ID(
        "memento.item.parent_location.field_id", true, { it.toIntOrNull() != null }),
    MEMENTO_ITEM_PARENT_BOX_FIELD_ID(
        "memento.item.parent_box.field_id", true, { it.toIntOrNull() != null }),



    // jim host
    JIM_HOST("jim.basic.host", true, { it.isNotBlank() }),
    JIM_API_PASSWORD("jim.basic.password", true, { it.isNotBlank() }),
}