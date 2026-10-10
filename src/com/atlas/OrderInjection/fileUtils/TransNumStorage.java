package com.atlas.OrderInjection.fileUtils;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public class TransNumStorage {

    private final Path transnumFile;

    public TransNumStorage() {
    	this.transnumFile=Paths.get("atlasTransNum.dat");
    }
    
    public TransNumStorage(String fileName) {
        this.transnumFile = Paths.get(fileName);
    }

    public synchronized long get() throws IOException {
        if (!Files.exists(transnumFile)) {
            return 0;
        }

        String text = new String(
                Files.readAllBytes(transnumFile),
                StandardCharsets.UTF_8
        ).trim();

        if (text.isEmpty()) {
            return 0;
        }

        return Long.parseLong(text);
    }

    public synchronized void set(long value) throws IOException {
        Path tempFile = Paths.get(transnumFile.toString() + ".tmp");

        Files.write(
                tempFile,
                Long.toString(value).getBytes(StandardCharsets.UTF_8)
        );

        try {
            Files.move(
                    tempFile,
                    transnumFile,
                    StandardCopyOption.REPLACE_EXISTING,
                    StandardCopyOption.ATOMIC_MOVE
            );
        } catch (java.nio.file.AtomicMoveNotSupportedException e) {
            Files.move(
                    tempFile,
                    transnumFile,
                    StandardCopyOption.REPLACE_EXISTING
            );
        }
    }

    public synchronized long incrementAndGet() throws IOException {
        long value = get() + 1;
        set(value);
        return value;
    }
}