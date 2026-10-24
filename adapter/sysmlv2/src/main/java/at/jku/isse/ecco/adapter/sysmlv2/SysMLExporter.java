package at.jku.isse.ecco.adapter.sysmlv2;

import at.jku.isse.ecco.adapter.ArtifactExporter;
import at.jku.isse.ecco.service.listener.ExportListener;
import at.jku.isse.ecco.tree.Node;

import java.nio.file.Path;
import java.util.Set;

public class SysMLExporter implements ArtifactExporter<Set<Node>, Path> {
    @Override
    public String getPluginId() {
        return "";
    }

    @Override
    public Path[] export(Path base, Set<Node> input) {
        return new Path[0];
    }

    @Override
    public Path[] export(Set<Node> input) {
        return new Path[0];
    }

    @Override
    public void addListener(ExportListener listener) {

    }

    @Override
    public void removeListener(ExportListener listener) {

    }
}
