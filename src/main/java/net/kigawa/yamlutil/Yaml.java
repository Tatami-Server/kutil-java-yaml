package net.kigawa.yamlutil;


import net.kigawa.util.InterfaceLogger;
import net.kigawa.util.LogSender;
import net.kigawa.util.Logger;
import org.yaml.snakeyaml.constructor.CustomClassLoaderConstructor;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class Yaml extends LogSender {
    private final InterfaceLogger logger;
    private final org.yaml.snakeyaml.Yaml yaml;
    private final File dir;

    public Yaml() {
        this(Paths.get("").toAbsolutePath().toFile());
    }

    public Yaml(File dir) {
        this(dir, null, new Logger(false, false));
    }

    public Yaml(File dir, InterfaceLogger logger) {
        this(dir, null, logger);
    }

    public Yaml(CustomClassLoaderConstructor constructor) {
        this(constructor, new Logger(false, false));
    }

    public Yaml(CustomClassLoaderConstructor constructor, InterfaceLogger logger) {
        this(Paths.get("").toAbsolutePath().toFile(), constructor, logger);
    }

    public Yaml(File dir, CustomClassLoaderConstructor constructor) {
        this(dir, constructor, new Logger(false, false));
    }

    public Yaml(File dir, CustomClassLoaderConstructor constructor, InterfaceLogger logger) {
        super(logger);
        if (constructor == null) {
            yaml = new org.yaml.snakeyaml.Yaml();
        } else {
            yaml = new org.yaml.snakeyaml.Yaml(constructor);
        }
        if (!dir.exists()) dir.mkdirs();
        this.dir = dir;
        this.logger = logger;
    }

    public void save(YamlData data, File file) {
        info("save file " + file.getName());
        try {
            if (!file.exists()) {
                if (file.createNewFile()) throw new FileNotFoundException();
            }
            FileWriter fileWriter = new FileWriter(file);
            String dump = yaml.dump(data);
            debug(dump);
            fileWriter.write(dump);
            fileWriter.close();
        } catch (IOException e) {
            warning(e);
        }
    }

    public void save(YamlData data) {
        File file = new File(dir, data.getName() + ".yml");
        save(data, file);
    }

    public <T> T load(Class<T> type, String name) {
        File file = new File(dir, name + ".yml");
        return load(type, file);
    }

    public <T> T load(Class<T> type, File file) {
        info("load file " + file.getName());
        T data = null;
        //check file exists
        if (file.exists()) {
            try {
                data = type.getDeclaredConstructor().newInstance();
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                e.printStackTrace();
            }
            try {
                FileReader reader = new FileReader(file);
                data = yaml.loadAs(reader, type);
            } catch (FileNotFoundException e) {
                warning(e);
            }
        }
        return data;
    }

    public <T> List<T> loadAll(Class<T> type) {
        return loadAll(type, dir);
    }

    public <T> List<T> loadAll(Class<T> type, File dir) {
        info("load files in " + dir.getName());
        List<T> yamlData = new ArrayList<>();
        try {
            //make dir
            if (!dir.exists()) {
                if (dir.mkdirs()) throw new IOException();
            }
            //get files name
            String[] files = dir.list();
            //load and add data
            assert files != null;
            for (String s : files) {
                File file = new File(dir, s);
                T data = load(type, file);
                yamlData.add(data);
            }
        } catch (IOException e) {
            warning(e);
        }

        return yamlData;
    }

}
