package com.kosbuild;

import com.kosbuild.jsonparser.FieldValuePair;
import com.kosbuild.jsonparser.JsonArray;
import com.kosbuild.jsonparser.JsonElement;
import com.kosbuild.jsonparser.JsonObject;

/**
 * @author Dmitry
 */
public class BuildFileMerger {

    public JsonObject mergeParsedBuildFiles(JsonObject parentJsonObject, JsonObject childJsonObject) {
        if (parentJsonObject.contains("override")) {
            throw new IllegalArgumentException("Root of hierarchy cannot have 'override' section. Only child build files can override properties from parent build files.");
        }

        merge(parentJsonObject, childJsonObject);
        if (childJsonObject.contains("override")) {
            JsonObject override = childJsonObject.removeElementByName("override").getAsObject();
            throw new IllegalStateException("[Override] is not implemented now");
        }

        return parentJsonObject;
    }

    private void merge(JsonObject parentJsonObject, JsonObject childJsonObject) {
        for (FieldValuePair fvp : childJsonObject.getElements()) {
            String name = fvp.getName();
            JsonElement element = fvp.getValue();

            if (name.startsWith("~")) {
                //remove element
                if (parentJsonObject.removeElementByName(name) == null) {
                    throw new IllegalArgumentException("You try to remove element [" + name + "], but it does not exists in parent build file");
                }
            } else if (name.startsWith("+")) {
                name = name.substring(1);
                //add elements to array
                if (!element.isArray()) {
                    throw new IllegalArgumentException("You try to do +" + name + ". But you can do '+' only with JsonArray type");
                }

                JsonElement parentElement = parentJsonObject.getElementByName(name);
                if (parentElement == null) {
                    System.err.println("You try to add elements to [" + name + "], but it does not exists in parent build file");
                } else if (!parentElement.isArray()) {
                    throw new IllegalArgumentException("You try to do +" + name + ". But this element in parent build script has type " + parentElement.getClass().getSimpleName() + ", but should be JsonArray");
                } else {
                    JsonArray parentElementArray = parentElement.getAsArray();
                    JsonArray childElementArray = element.getAsArray();
                    for (int i = 0; i < childElementArray.size(); i++) {
                        parentElementArray.getElements().add(childElementArray.get(i));
                    }
                }
            } else if (element.isPrimitive() || element.isArray()) {
                if (parentJsonObject.contains(name)) {
                    parentJsonObject.removeElementByName(name);
                }
                parentJsonObject.addElement(name, element);
            } else if (element.isObject()) {                
                if(!parentJsonObject.contains(name)){
                    parentJsonObject.addElement(name, new JsonObject());
                }

                merge(parentJsonObject.getElementByName(name).getAsObject(), element.getAsObject());
            }
        }
    }
}
