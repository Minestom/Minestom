package net.minestom.demo.extension;

import net.minestom.server.extensions.Extension;

public class TestExtension extends Extension {
    @Override
    public void initialize() {
        System.out.println("Initialize test extension");

        try {
            Class.forName("com.mysql.cj.jdbc.Driver", true, getOrigin().getClassLoader());
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void terminate() {
        System.out.println("Terminate test extension");
    }
}
