package net.kigawa.yamlutil;


import net.kigawa.kutil.kutil.interfaces.LoggerInterface;
import org.yaml.snakeyaml.constructor.CustomClassLoaderConstructor;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class Yaml {
    private final org.yaml.snakeyaml.Yaml yaml;
    private final File dir;
    private final LoggerInterface logger;

    public Yaml(LoggerInterface logger) {
        this(Paths.get("").toAbsolutePath().toFile(), logger);
    }

    public Yaml(File dir, LoggerInterface logger) {
        this(dir, null, logger);
    }

    public Yaml(File dir, CustomClassLoaderConstructor constructor, LoggerInterface logger) {
        this.logger = logger;
        if (constructor == null) {
            yaml = new org.yaml.snakeyaml.Yaml();
        } else {
            yaml = new org.yaml.snakeyaml.Yaml(constructor);
        }
        dir.mkdirs();
        this.dir = dir;
    }

    public Yaml(CustomClassLoaderConstructor constructor, LoggerInterface logger) {
        this(Paths.get("").toAbsolutePath().toFile(), constructor, logger);
    }

    public <T> T load(Class<T> type, String name) {
        File file = new File(dir, name + ".yml");
        return load(type, file);
    }

    public <T> T load(Class<T> type, File file) {
        logger.info("load file " + file.getName());
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
                logger.warning(e);
            }
        }
        return data;
    }

    public <T> List<T> loadAll(Class<T> type) {
        return loadAll(type, dir);
    }

    public <T> List<T> loadAll(Class<T> type, File dir) {
        logger.info("loading files in " + dir.getName());
        List<T> yamlData = new ArrayList<>();

        //make dir
        dir.mkdirs();
        //get files name
        String[] files = dir.list();
        //load and add data
        assert files != null;
        for (String s : files) {
            File file = new File(dir, s);
            T data = load(type, file);
            yamlData.add(data);
        }

        return yamlData;
    }

    public void save(YamlData data) {
        File file = new File(dir, data.getName() + ".yml");
        save(data, file);
    }

    public void save(YamlData data, File file) {
        logger.info("save file " + file.getName());
        try {
            file.createNewFile();
            FileWriter fileWriter = new FileWriter(file);
            String dump = yaml.dump(data);
            logger.fine(dump);
            fileWriter.write(dump);
            fileWriter.close();
        } catch (IOException e) {
            logger.warning(e);
        }
    }

}
