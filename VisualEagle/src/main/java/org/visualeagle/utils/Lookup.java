package org.visualeagle.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Dmitry
 */
public class Lookup {

    private static Lookup defaultInstance;
    private final Map<Class, Object> map = new HashMap<>();
    private final Map<Class, List<LookupEvent>> changeEvents = new HashMap<>();
    private final Map<String, Object> strMap = new HashMap<>();
    private final Map<String, List<LookupEvent>> strChangeEvents = new HashMap<>();

    public static Lookup get() {
        if (defaultInstance == null) {
            defaultInstance = new Lookup();
        }

        return defaultInstance;
    }

    @SuppressWarnings(value = "unchecked")
    public <T> T get(Class<T> clazz) {
        return (T) map.get(clazz);
    }

    @SuppressWarnings(value = "unchecked")
    public Object get(String key) {
        return strMap.get(key);
    }

    public void fire(Object obj) {
        put(obj);
    }

    public <T> void fire(Class<T> clazz, Object obj) {
        put(clazz, obj);
    }

    public void fire(String key, Object obj) {
        put(key, obj);
    }

    /**
     * put object in lookup by obj class as key
     */
    public Lookup put(Object obj) {
        if (obj == null) {
            throw new RuntimeException("Lookup put method cannot accept null argument. If you need assign null value, please use put(Class, Object) method");
        }
        put(obj.getClass(), obj);
        return this;
    }

    /**
     * put object in lookup by string value as key
     */
    @SuppressWarnings(value = "unchecked")
    public Lookup put(String key, Object obj) {
        Object oldValue = strMap.get(key);
        List<LookupEvent> list = strChangeEvents.get(key);
        if (list != null) {
            for (int i = 0; i < list.size(); i++) {
                LookupEvent ref = list.get(i);
                LookupEvent lookupEvent = ref;
                if (lookupEvent != null) {
                    lookupEvent.change(oldValue, obj);
                } else {
                    list.remove(i);
                    i--;
                }
            }
        }

        strMap.put(key, obj);
        return this;
    }

    /**
     * put object in lookup by clazz as key
     */
    @SuppressWarnings(value = "unchecked")
    public <T> Lookup put(Class<T> clazz, Object obj) {
        T oldValue = (T) map.get(clazz);
        List<LookupEvent> list = changeEvents.get(clazz);
        if (list != null) {
            for (int i = 0; i < list.size(); i++) {
                LookupEvent ref = list.get(i);
                LookupEvent lookupEvent = ref;
                if (lookupEvent != null) {
                    lookupEvent.change(oldValue, obj);
                } else {
                    list.remove(i);
                    i--;
                }
            }
        }

        map.put(clazz, obj);
        return this;
    }

    public <T> Lookup addChangeEvent(Class<T> clazz, LookupEvent<T> event) {
        List<LookupEvent> list = changeEvents.get(clazz);
        if (list == null) {
            list = new ArrayList<>();
            changeEvents.put(clazz, list);
        }

        //WeakReference<LookupEvent> ref = new WeakReference<LookupEvent>(event);
        list.add(event);
        return this;
    }

    public <T> Lookup addChangeEvent(Class<T> clazz, final LookupEventChangedValue<T> event) {
        List<LookupEvent> list = changeEvents.get(clazz);
        if (list == null) {
            list = new ArrayList<>();
            changeEvents.put(clazz, list);
        }

        //WeakReference<LookupEvent> ref = new WeakReference<LookupEvent>(event);
        list.add((LookupEvent<T>) (T oldValue, T newValue) -> {
            event.change(newValue);;
        });
        return this;
    }

    public <T> Lookup addChangeEvent(String key, LookupEvent<T> event) {
        List<LookupEvent> list = strChangeEvents.get(key);
        if (list == null) {
            list = new ArrayList<>();
            strChangeEvents.put(key, list);
        }

        list.add(event);
        return this;
    }

    public <T> Lookup addChangeEvent(String key, final LookupEventChangedValue<T> event) {
        List<LookupEvent> list = strChangeEvents.get(key);
        if (list == null) {
            list = new ArrayList<>();
            strChangeEvents.put(key, list);
        }

        list.add((LookupEvent<T>) (T oldValue, T newValue) -> {
            event.change(newValue);
        });

        return this;
    }

    /**
     * public void change(T oldValue, T newValue);
     */
    public interface LookupEvent<T> {

        public void change(T oldValue, T newValue);
    };

    /**
     * The same as LookupEvent, but hides oldValue. If you need oldValue, use
     * LookupEvent directly<br/>
     * public abstract void change(T newValue);
     */
    public interface LookupEventChangedValue<T> {

        public abstract void change(T newValue);
    };
}
