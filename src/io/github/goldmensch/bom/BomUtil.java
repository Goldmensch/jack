package io.github.goldmensch.bom;

import com.github.packageurl.MalformedPackageURLException;
import com.github.packageurl.PackageURL;
import io.github.goldmensch.AbortProgramException;
import org.eclipse.aether.artifact.Artifact;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BomUtil {
    private static final Logger log = LoggerFactory.getLogger(BomUtil.class);

    private BomUtil() {}

    public static PackageURL purlFromArtifact(Artifact artifact) {
        try {
            return new PackageURL("maven", artifact.getGroupId(), artifact.getArtifactId(), artifact.getVersion(), null, null);
        } catch (MalformedPackageURLException e) {
            log.error("Could not create purl url for artifact {}", artifact, e);
            throw new AbortProgramException();
        }
    }
}
