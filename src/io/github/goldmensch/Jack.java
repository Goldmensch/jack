package io.github.goldmensch;

import io.github.goldmensch.config.Config;
import io.github.goldmensch.sources.SourceSet;
import io.github.goldmensch.tasks.BuildTask;
import io.github.goldmensch.tasks.DependenciesTask;
import io.github.goldmensch.tasks.RunTask;
import io.github.goldmensch.tasks.Task;
import org.eclipse.aether.collection.DependencyCollectionException;
import org.eclipse.aether.repository.NoLocalRepositoryManagerException;
import org.eclipse.aether.resolution.ArtifactDescriptorException;
import org.eclipse.aether.resolution.ArtifactResolutionException;
import org.eclipse.aether.resolution.DependencyResolutionException;

import java.io.IOException;

public class Jack {

    private final Config config;
    private final SourceSet sourceSet;
    private final String[] args;
    private final Paths paths;

    public static void main(String[] args) throws IOException, ArtifactResolutionException, DependencyCollectionException, DependencyResolutionException, NoLocalRepositoryManagerException, InterruptedException, ArtifactDescriptorException {
        Paths paths = Paths.create();

        Config config = Config.read(paths.config());
        SourceSet sourceSet = SourceSet.read(paths.source());
        new Jack(args, paths, config, sourceSet).run();
    }

   public Jack(String[] args, Paths paths, Config config, SourceSet sourceSet) {
        this.config = config;
        this.sourceSet = sourceSet;
        this.args = args;
        this.paths = paths;
   }

   private void run() throws ArtifactResolutionException, DependencyCollectionException, DependencyResolutionException, IOException, NoLocalRepositoryManagerException, InterruptedException, ArtifactDescriptorException {
       if (args.length < 1) {
           System.out.println("You have to be provide an argument");
           return;
       }

       Task task = switch (args[0]) {
           case "build" -> new BuildTask(this);
           case "run" -> new RunTask(this);
           case "dependencies" -> new DependenciesTask(this);
           default -> throw new IllegalStateException("Unexpected task: " + args[0]);
       };

       task.process();
   }

    public Config config() {
        return config;
    }

    public SourceSet sourceSet() {
        return sourceSet;
    }

    public Paths paths() {
        return paths;
    }

    public String[] args() {
        return args;
    }
}
