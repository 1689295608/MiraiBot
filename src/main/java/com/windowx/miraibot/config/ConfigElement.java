package com.windowx.miraibot.config;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import java.math.BigDecimal;
import java.math.BigInteger;

public class ConfigElement extends JsonElement {
    private final JsonElement element;

    public ConfigElement() {
        this.element = new JsonObject();
    }

    @Override
    public JsonElement deepCopy() {
        return element.deepCopy();
    }

    public ConfigElement(JsonElement element) {
        this.element = element;
    }

    public ConfigElement(JsonPrimitive primitive) {
        this.element = primitive;
    }

    public ConfigElement(String str) {
        this.element = new JsonPrimitive(str);
    }
    public ConfigElement(Number num) {
        this.element = new JsonPrimitive(num);
    }
    public ConfigElement(Boolean bool) {
        this.element = new JsonPrimitive(bool);
    }
    public ConfigElement(Character character) {
        this.element = new JsonPrimitive(character);
    }

    public JsonElement get() {
        return element;
    }

    public String getAsString() {
        return element.getAsString();
    }
    public JsonObject getAsJsonObject() {
        return element.getAsJsonObject();
    }
    public JsonArray getAsJsonArray() {
        return element.getAsJsonArray();
    }
    public int getAsInt() {
        return element.getAsInt();
    }
    public BigDecimal getAsBigDecimal() {
        return element.getAsBigDecimal();
    }
    public BigInteger getAsBigInteger() {
        return element.getAsBigInteger();
    }

    public boolean getAsBoolean() {
        return element.getAsBoolean();
    }

    public Number getAsNumber() {
        return element.getAsNumber();
    }

    public double getAsDouble() {
        return element.getAsDouble();
    }

    public float getAsFloat() {
        return element.getAsFloat();
    }

    public long getAsLong() {
        return element.getAsLong();
    }

    public byte getAsByte() {
        return element.getAsByte();
    }

    public short getAsShort() {
        return element.getAsShort();
    }

    @Override
    public String toString() {
        return element.getAsString();
    }
}
