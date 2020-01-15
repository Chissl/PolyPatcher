package club.sk1er.patcher.config;

import club.sk1er.vigilance.data.Property;
import club.sk1er.vigilance.data.PropertyData;
import club.sk1er.vigilance.data.PropertyType;

import java.lang.annotation.Annotation;
import java.util.function.Consumer;

public class ConfigUtil {

    public static PropertyData createAndRegisterConfig(PropertyType type, String category, String subCategory, String name, String description, Object defaultValue, Consumer<Object> onUpdate) {
        PropertyData config = createConfig(type, category, subCategory, name, description, defaultValue, onUpdate);
        register(config);
        return config;
    }

    public static PropertyData createConfig(PropertyType type, String category, String subCategory, String name, String description, Object defaultValue, Consumer<Object> onUpdate) {
        Property property = createProperty(type, category, subCategory, name, description);
        PropertyData data = PropertyData.Companion.withValue(property, defaultValue, PatcherConfig.instance);

        if (onUpdate != null) data.setCallbackConsumer(onUpdate);
        return data;
    }

    public static void register(PropertyData data) {
        PatcherConfig.instance.registerProperty(data);
    }

    public static Property createProperty(PropertyType type, String category, String subCategory, String name, String description) {
        return new Property() {
            /**
             * Returns the annotation type of this annotation.
             *
             * @return the annotation type of this annotation
             */
            @Override
            public Class<? extends Annotation> annotationType() {
                return Property.class;
            }

            @Override
            public PropertyType type() {
                return type;
            }

            @Override
            public String subcategory() {
                return subCategory;
            }

            @Override
            public String[] options() {
                return new String[]{};
            }

            @Override
            public String name() {
                return name;
            }

            @Override
            public int min() {
                return 0;
            }

            @Override
            public int max() {
                return 0;
            }

            @Override
            public String description() {
                return description;
            }

            @Override
            public String category() {
                return category;
            }
        };
    }
}
