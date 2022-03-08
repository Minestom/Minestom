package net.minestom.demo.extension;

import net.minestom.server.extensions.Extension;

public class TestExtension extends Extension {
    @Override
    public LoadStatus initialize() {
        System.out.println("Initialize test extension");

        try {
            Class<?> driver = Class.forName("com.mysql.cj.jdbc.Driver", true, descriptor().classLoader());
            System.out.println("MySQL driver class: " + driver.getName() + " (in " + driver.getClassLoader().getName() + ")");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return LoadStatus.SUCCESS;
    }

    @Override
    public void terminate() {
        System.out.println("Terminate test extension");
    }
}
