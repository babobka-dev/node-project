package ru.babobka.nodeutils.classloder;

/**
 * Created by dolgopolov.a on 12.12.15.
 */

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

/**
 * JarResources: JarResources maps all resources included in a Zip or Jar file.
 * Additionaly, it provides a method to extract one as a blob.
 */
final class JarResource {

    // jar resource mapping tables
    private final Map<String, Integer> htSizes = new HashMap<>();
    private final Map<String, byte[]> htJarContents = new HashMap<>();

    // a jar file
    private final String jarFileName;

    /**
     * creates a JarResources. It extracts all resources from a Jar into an
     * internal hashtable, keyed by resource names.
     *
     * @param jarFileName a jar or zip file
     * @throws IOException
     */
    public JarResource(String jarFileName) throws IOException {
        this.jarFileName = jarFileName;
        init();
    }

    /**
     * Extracts a jar resource as a blob.
     *
     * @param name a resource name.
     */
    public byte[] getResource(String name) {
        return htJarContents.get(name);
    }

    /**
     * initializes internal hash tables with Jar file resources.
     *
     * @throws IOException
     */
    private void init() throws IOException {
        try (ZipFile zf = new ZipFile(jarFileName);
             FileInputStream fis = new FileInputStream(jarFileName);

             BufferedInputStream bis = new BufferedInputStream(fis);
             ZipInputStream zis = new ZipInputStream(bis)) {
            // extracts just sizes only.
            Enumeration<?> e = zf.entries();
            while (e.hasMoreElements()) {
                ZipEntry ze = (ZipEntry) e.nextElement();

                htSizes.put(ze.getName(), (int) ze.getSize());
            }

            ZipEntry ze;
            while ((ze = zis.getNextEntry()) != null) {
                if (ze.isDirectory()) {
                    continue;
                }

                int size = (int) ze.getSize();
                // -1 means unknown size.
                if (size == -1) {
                    size = htSizes.get(ze.getName());
                }

                byte[] b = new byte[size];
                int rb = 0;
                int chunk;
                while ((size - rb) > 0) {
                    chunk = zis.read(b, rb, size - rb);
                    if (chunk == -1) {
                        break;
                    }
                    rb += chunk;
                }

                // add to internal resource hashtable
                htJarContents.put(ze.getName(), b);

            }
        }
    }

}