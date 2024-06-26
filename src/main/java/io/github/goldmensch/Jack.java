package io.github.goldmensch;

import io.github.goldmensch.config.project.ProjectConfig;
import io.github.goldmensch.sources.SourceSet;
import io.github.goldmensch.tasks.*;
import org.cyclonedx.exception.ParseException;
import org.eclipse.aether.collection.DependencyCollectionException;
import org.eclipse.aether.repository.NoLocalRepositoryManagerException;
import org.eclipse.aether.resolution.ArtifactDescriptorException;
import org.eclipse.aether.resolution.ArtifactResolutionException;
import org.eclipse.aether.resolution.DependencyResolutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class Jack {

    public static final Logger log = LoggerFactory.getLogger(Jack.class);

    private final ProjectConfig config;
    private final SourceSet sourceSet;
    private final String[] args;
    private final Paths paths;

    public static void main(String[] args) {
        try {
            Paths paths = Paths.create();

            ProjectConfig config = ProjectConfig.read(paths.config());
            SourceSet sourceSet = SourceSet.read(paths.source());
            new Jack(args, paths, config, sourceSet).run();
        } catch (AbortProgramException ignored) {
            System.exit(1);
        }
    }

   public Jack(String[] args, Paths paths, ProjectConfig config, SourceSet sourceSet) {
        this.config = config;
        this.sourceSet = sourceSet;
        this.args = args;
        this.paths = paths;
   }

   private void run() {
       if (args.length < 1) {
           log.error("You have to provide an argument!");
           return;
       }

       Task task = switch (args[0]) {
           case "build" -> new BuildTask(this);
           case "run" -> new RunTask(this);
           case "dependencies" -> new DependenciesTask(this);
           case "bom" -> new CreateBOMTask(this);
           default -> throw new IllegalStateException("Unexpected task: " + args[0]);
       };

       log.debug("Found task {}", task);

       task.process();
   }

    public ProjectConfig projectConfig() {
        return config;
    }

    public SourceSet sourceSet() {
        return sourceSet;
    }

    public Paths paths() {
        return paths;
    }

    public List<String> providedArgs() {
        return Arrays.asList(Arrays.copyOfRange(args, 1, args.length));
    }

    public String[] args() {
        return args;
    }
}
