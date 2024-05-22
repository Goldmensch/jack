package io.github.goldmensch;

import io.github.goldmensch.config.Config;
import io.github.goldmensch.sources.SourceSet;
import io.github.goldmensch.tasks.BuildTask;
import io.github.goldmensch.tasks.RunTask;
import io.github.goldmensch.tasks.Task;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;

public class Jack {

    private static final Path ROOT = Path.of("");
    private final Config config;
    private final SourceSet sourceSet;

    public static void main(String[] args) {
        try {
            if (args.length < 1) {
                System.out.println("You have to be provide an argument");
                return;
            }

            Config config = Config.read(ROOT);
            SourceSet sourceSet = SourceSet.read(ROOT);
            var jack = new Jack(config, sourceSet);

            Task<?> task = switch (args[0]) {
                case "build" -> new BuildTask(jack);
                case "run" -> new RunTask(jack, Arrays.copyOfRange(args, 1, args.length));
                default -> throw new IllegalStateException("Unexpected task: " + args[0]);
            };
            task.run();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

   public Jack(Config config, SourceSet sourceSet) {
        this.config = config;
       this.sourceSet = sourceSet;
   }

    public Config config() {
        return config;
    }

    public SourceSet sourceSet() {
        return sourceSet;
    }

    public Path root() {
        return ROOT;
    }
}
