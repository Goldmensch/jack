package io.github.goldmensch.bom;

import org.cyclonedx.model.Dependency;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.graph.DependencyNode;
import org.eclipse.aether.graph.DependencyVisitor;

public class AddingDependencyVisitor implements DependencyVisitor {

    private final Dependency dependency;
    private final DependencyNode root;

    public AddingDependencyVisitor(Dependency dependency, DependencyNode root) {
        this.dependency = dependency;
        this.root = root;
    }

    @Override
    public boolean visitEnter(DependencyNode node) {
        Artifact artifact = node.getArtifact();
        if (root != node) {
            dependency.addDependency(new Dependency(BomUtil.purlFromArtifact(artifact).canonicalize()));
        }
        return true;
    }

    @Override
    public boolean visitLeave(DependencyNode node) {
        return true;
    }
}
