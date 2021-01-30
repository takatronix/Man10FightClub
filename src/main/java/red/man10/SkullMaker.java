package red.man10;

import com.google.common.collect.ForwardingMultimap;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

public class SkullMaker {

    private String owner;
    private String url;

    private int amount = 1;
    private String name;
    private List<String> lore = new ArrayList<>();

    public SkullMaker withAmount(int amount) {
        this.amount = amount;
        return this;
    }

    public SkullMaker withName(String name) {
        this.name = name;
        return this;
    }

    public SkullMaker withLore(String line) {
        lore.add(line);
        return this;
    }

    public SkullMaker withLore(String... lines) {
        lore.addAll(Arrays.asList(lines));
        return this;
    }

    public SkullMaker withLore(List<String> lines) {
        lore.addAll(lines);
        return this;
    }

    public SkullMaker withOwner(String ownerName) {
        this.owner = ownerName;
        return this;
    }

    public SkullMaker withSkinUrl(String url) {
        this.url = url;
        return this;
    }

    public ItemStack build() {
        ItemStack item = new ItemStack(Material.PLAYER_HEAD, amount);
        SkullMeta meta = (SkullMeta) item.getItemMeta();
        if (owner != null) {
            meta.setOwner(owner);
        } else if (url != null) {
            loadProfile(meta, url);
        }
        if (name != null) {
            meta.setDisplayName(name);
        }
        if (!lore.isEmpty()) {
            meta.setLore(lore);
        }
        item.setItemMeta(meta);
        return item;
    }

    private void loadProfile(ItemMeta meta, String url) {

        Class<?> profileClass = Reflection.getClass("com.mojang.authlib.GameProfile");

        Constructor<?> profileConstructor = Reflection.getDeclaredConstructor(profileClass, UUID.class, String.class);

        Object profile = Reflection.newInstance(profileConstructor, UUID.randomUUID(), null);

        byte[] encodedData = Base64.getEncoder().encode(String.format("{textures:{SKIN:{url:\"%s\"}}}", url).getBytes());

        Method getPropertiesMethod = Reflection.getDeclaredMethod(profileClass, "getProperties");

        Object propertyMap = Reflection.invoke(getPropertiesMethod, profile);

        Class<?> propertyClass = Reflection.getClass("com.mojang.authlib.properties.Property");

        Reflection.invoke(
                Reflection.getDeclaredMethod(
                        ForwardingMultimap.class, "put", Object.class, Object.class
                ),
                propertyMap,
                "textures",
                Reflection.newInstance(Reflection.getDeclaredConstructor(propertyClass, String.class, String.class), "textures", new String(encodedData))
        );

        Reflection.setField("profile", meta, profile);
    }

    private static final class Reflection {

        private static Class<?> getClass(String forName) {
            try {
                return Class.forName(forName);
            } catch (ClassNotFoundException e) {
                return null;
            }
        }

        private static <T> Constructor<T> getDeclaredConstructor(Class<T> clazz, Class<?>... params) {
            try {
                return clazz.getDeclaredConstructor(params);
            } catch (NoSuchMethodException e) {
                return null;
            }
        }

        private static <T> T newInstance(Constructor<T> constructor, Object... params) {
            try {
                return constructor.newInstance(params);
            } catch (IllegalAccessException | InstantiationException | InvocationTargetException e) {
                return null;
            }
        }

        private static Method getDeclaredMethod(Class<?> clazz, String name, Class<?>... params) {
            try {
                return clazz.getDeclaredMethod(name, params);
            } catch (NoSuchMethodException e) {
                return null;
            }
        }

        private static Object invoke(Method method, Object object, Object... params) {
            method.setAccessible(true);
            try {
                return method.invoke(object, params);
            } catch (InvocationTargetException | IllegalAccessException e) {
                return null;
            }
        }

        private static void setField(String name, Object instance, Object value) {
            Field field = getDeclaredField(instance.getClass(), name);
            field.setAccessible(true);
            try {
                field.set(instance, value);
            } catch (IllegalAccessException ignored) {}
        }

        private static Field getDeclaredField(Class<?> clazz, String name) {
            try {
                return clazz.getDeclaredField(name);
            } catch (NoSuchFieldException e) {
                return null;
            }
        }

    }

}
